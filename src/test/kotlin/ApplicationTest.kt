package com.example  // 기존이랑 동일하게 유지

import com.example.domain.AnalyzeRequest
import com.example.domain.AnalyzeResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApplicationTest {

    @Test
    fun testAnalyzeEndpoint() = testApplication {
        application {
            module()
        }

        // 테스트 클라이언트에도 JSON 설정을 해줘야 합니다.
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
        }

        // 예시용 문제 텍스트 (숫자 야구 느낌)
        val response = client.post("/analyze") {
            contentType(ContentType.Application.Json)
            setBody(
                AnalyzeRequest(
                    text = "당신은 숫자 야구를 플레이하는 프로그램을 작성해야 합니다. 숫자 야구란 1 ~ 9 사이의 서로 다른 숫자 4개로 이루어진 비밀번호를 맞히는 게임입니다.",
                    maxN = 1000
                )
            )
        }

        // 1) HTTP 상태 코드가 200 OK 인지 확인
        assertEquals(HttpStatusCode.OK, response.status)

        // 2) 응답을 객체로 역직렬화
        val responseBody = response.body<AnalyzeResponse>()

        // 3) 추천 목록에 '완전 탐색'이 있는지 확인
        val recommendedNames = responseBody.recommendedAlgorithms.map { it.name }
        assertTrue(
            recommendedNames.any { it.contains("완전 탐색(브루트포스)") },
            "숫자 야구 문제를 넣었을 때 '완전 탐색(브루트포스)'가 추천 목록에 포함되어야 합니다."
        )
    }
}