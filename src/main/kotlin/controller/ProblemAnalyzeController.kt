package com.example.controller

import com.example.domain.AnalyzeRequest
import com.example.service.ProblemAnalyzeService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class ProblemAnalyzeController(
    private val analyzeService: ProblemAnalyzeService
) {

    fun register(routing: Routing) {
        routing.post("/analyze") {
            // 1) 요청 JSON을 Kotlin 객체로 받기
            val request = call.receive<AnalyzeRequest>()

            // 2) 서비스 레이어에 분석 요청
            val response = analyzeService.analyzeProblem(request)

            // 3) 응답 JSON 전송
            call.respond(response)
        }
    }
}