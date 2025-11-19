package com.example

import com.example.controller.ProblemAnalyzeController
import com.example.service.ProblemAnalyzeService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import com.example.service.AiExplanationService

// 테스트와 실제 서버 둘 다에서 사용할 공용 설정
fun Application.module() {
    val aiExplanationService = AiExplanationService()
    // 서비스(비즈니스 로직) 생성
    val analyzeService = ProblemAnalyzeService(
        aiExplanationService = aiExplanationService
    )

    // 컨트롤러 생성
    val analyzeController = ProblemAnalyzeController(analyzeService)

    // JSON 직렬화 설정
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    // 라우팅 설정
    routing {
        // 컨트롤러에 라우팅 등록 위임
        analyzeController.register(this)
    }
}

// 실제 서버 실행용 main (로직은 거의 그대로, module()만 호출)
fun main() {
    embeddedServer(Netty, port = 8080) {
        module()   // 위에서 만든 Application.module() 호출
    }.start(wait = true)
}
