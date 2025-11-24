package com.example.service

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 2단계용 서비스:
 * - 문제 텍스트를 GPT에 보내서
 *   ["BFS", "완전 탐색", "이분 탐색"] 이런 알고리즘 태그를 받아온다.
 * - API 키가 없거나 에러 나면 → 빈 리스트 리턴 (= fallback, 룰 엔진만 사용)
 */
open class AiAlgorithmPredictService(
    private val apiKey: String = System.getenv("OPENAI_API_KEY") ?: ""
) {

    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    /**
     * 문제 텍스트를 분석해서 후보 알고리즘 태그 리스트를 돌려준다.
     * 예: ["BFS", "완전 탐색", "이분 탐색"]
     */
    open suspend fun predictAlgorithms(problemText: String): List<String> {
        // 🔹 API 키가 없으면 GPT 호출 자체를 안 한다 → 완전 무료 모드
        if (apiKey.isBlank()) {
            return emptyList()
        }

        // GPT에게 보낼 프롬프트
        val prompt = """
            너는 코딩 테스트 문제를 보고, 필요한 알고리즘 이름을 태그 형식으로 예측해주는 도우미야.

            아래 문제를 보고,
            예를 들어 ["BFS", "DFS", "완전 탐색", "이분 탐색", "DP"] 처럼
            필요한 알고리즘 이름만 리스트로 추출해줘.

            반드시 아래 JSON 형식으로만 답해:
            {
              "algorithms": ["BFS", "완전 탐색"]
            }

            문제 설명:
            $problemText
        """.trimIndent()

        return try {
            // AiExplanationService에서 이미 정의한 ChatRequest, ChatMessage, ChatResponse 재사용
            val response: ChatResponse = client.post("https://api.openai.com/v1/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(
                    ChatRequest(
                        model = "gpt-4o-mini",
                        messages = listOf(
                            ChatMessage(
                                role = "system",
                                content = "너는 문제를 보고 필요한 알고리즘 태그만 JSON으로 추출하는 도우미야."
                            ),
                            ChatMessage(
                                role = "user",
                                content = prompt
                            )
                        )
                    )
                )
            }.body()

            val content = response.choices.firstOrNull()?.message?.content ?: return emptyList()

            // content 안에 JSON이 들어있다고 가정하고 파싱
            val parsed = Json {
                ignoreUnknownKeys = true
            }.decodeFromString(AlgorithmTagResult.serializer(), content)

            parsed.algorithms

        } catch (e: Exception) {
            // 🔹 GPT 호출 실패 → 룰 엔진만 사용하도록 빈 리스트 리턴
            emptyList()
        }
    }
}

/**
 * GPT가 JSON으로 돌려줄 형식:
 * { "algorithms": ["BFS", "완전 탐색"] }
 */
@Serializable
data class AlgorithmTagResult(
    val algorithms: List<String>
)