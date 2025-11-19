package com.example.service

import com.example.domain.AnalyzeResponse
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AiExplanationService(

    // ✨ OpenAI API 키는 환경변수에서 읽어오게 함
    //   (터미널에서 export OPENAI_API_KEY="sk-..." 이런 식으로 설정)
    private val apiKey: String = System.getenv("OPENAI_API_KEY") ?: ""
) {

    // ✨ OpenAI에 HTTP 요청을 보내기 위한 Ktor 클라이언트
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
     * 룰 엔진이 만든 AnalyzeResponse를 입력받아서:
     * 1) GPT에게 "이 결과를 코딩테스트 준비생에게 설명해줘"라고 요청하고
     * 2) 응답을 aiExplanation에 넣기 위한 문자열로 돌려준다.
     * 3) API 키가 없거나 에러가 나면, 코드로 만든 fallback 설명을 리턴한다.
     */
    fun buildExplanation(response: AnalyzeResponse): String {

        // 1️⃣ GPT가 안 되면 쓸 기본 설명(fallback) 먼저 만들어 둔다
        val fallback = buildFallbackExplanation(response)

        // 2️⃣ API 키가 없으면 GPT 호출을 하지 않고 바로 fallback 리턴
        if (apiKey.isBlank()) {
            return fallback
        }

        // 3️⃣ GPT에게 보낼 프롬프트 구성
        val prompt = """
            너는 코딩 테스트 준비생에게 알고리즘 추천 결과를 한국어로 쉽게 설명해주는 도우미야.
            아래는 어떤 문제에 대한 알고리즘 추천 결과야.

            [허용 시간 복잡도]
            - ${response.allowedComplexity}
            - ${response.allowedExplanation}

            [추천 알고리즘]
            ${response.recommendedAlgorithms.joinToString("\n") { "- ${it.name} (score=${it.finalScore}) / 이유: ${it.messages.joinToString(" / ")}" }}

            [제외된 알고리즘]
            ${response.droppedAlgorithms.joinToString("\n") { "- ${it.name} / 제외 이유: ${it.messages.joinToString(" / ")}" }}

            위 내용을 바탕으로,

            1. 왜 이런 알고리즘들이 추천되었는지
            2. 어떤 알고리즘은 왜 시간 복잡도 때문에 제외되었는지
            3. 이 문제를 푸는 전략을 한두 줄로 정리

            를 포함해서, 5~8문장 정도의 짧은 한국어 설명을 써줘.
            너무 어려운 수학기호 말고, 코딩테스트 준비생이 이해하기 쉬운 말로 설명해줘.
        """.trimIndent()

        // 4️⃣ 실제 GPT 호출 (네트워크 통신 문제를 대비해서 try-catch)
        return try {
            runBlocking {
                val gptAnswer = callOpenAi(prompt)
                gptAnswer ?: fallback   // null이면 fallback 사용
            }
        } catch (e: Exception) {
            // 에러가 나도 서비스 전체가 죽지 않고, 최소한 기본 설명은 리턴
            fallback
        }
    }

    // ✨ OpenAI Chat Completions API 호출 부분
    private suspend fun callOpenAi(prompt: String): String? {
        val requestBody = ChatRequest(
        model = "gpt-4o-mini",  // 필요 시 gpt-4.1-mini 등으로 변경 가능
        messages = listOf(
            ChatMessage(role = "system", content = "너는 코딩테스트 알고리즘 추천 결과를 한국어로 친절하게 설명해주는 도우미야."),
        ChatMessage(role = "user", content = prompt)
        )
        )

        val response: ChatResponse = client.post("https://api.openai.com/v1/chat/completions") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()

        return response.choices.firstOrNull()?.message?.content?.trim()
    }

    // ============================
    // ✨ GPT가 실패했을 때 사용할 "기본 설명" (0.5단계 버전)
    // ============================
    private fun buildFallbackExplanation(response: AnalyzeResponse): String {
        val sb = StringBuilder()

        sb.appendLine("[기본 설명 - GPT 없이 생성됨]")
        sb.appendLine()
        sb.appendLine("이 문제에 대한 알고리즘 추천 결과입니다.")
        sb.appendLine()
        sb.appendLine("허용되는 시간 복잡도: ${response.allowedComplexity}")
        sb.appendLine(response.allowedExplanation)
        sb.appendLine()

        if (response.recommendedAlgorithms.isNotEmpty()) {
            sb.appendLine("✅ 추천 알고리즘:")
            response.recommendedAlgorithms.forEach { algo ->
                sb.appendLine("- ${algo.name} (score=${algo.finalScore})")
                if (algo.messages.isNotEmpty()) {
                    sb.appendLine("  · 이유: ${algo.messages.joinToString(" / ")}")
                }
            }
        } else {
            sb.appendLine("추천할 만한 알고리즘을 찾지 못했습니다.")
        }

        sb.appendLine()

        if (response.droppedAlgorithms.isNotEmpty()) {
            sb.appendLine("⚠️ 시간 복잡도 등의 이유로 제외된 알고리즘:")
            response.droppedAlgorithms.forEach { algo ->
                sb.appendLine("- ${algo.name}")
                if (algo.messages.isNotEmpty()) {
                    sb.appendLine("  · 제외 이유: ${algo.messages.joinToString(" / ")}")
                }
            }
        }

        sb.appendLine()
        sb.appendLine("요약: ${response.summary}")

        return sb.toString().trim()
    }
}

// =======================
// ✨ OpenAI API Request/Response DTO
// =======================
@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>
)

@Serializable
data class ChatChoice(
    val index: Int,
    val message: ChatMessage
)

@Serializable
data class ChatResponse(
    val choices: List<ChatChoice>
)