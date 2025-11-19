package com.example.service

import com.example.domain.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AiExplanationServiceTest {
    /**
     * GPT API 키가 없을 때는 fallback 설명을 생성해야 한다.
     * (이 테스트는 항상 성공해야 하고, 실제 API 호출을 하지 않음)
     */
    @Test
    fun `GPT 키가 없을 때 fallback 설명이 생성된다`() {
        // given
        val response = AnalyzeResponse(
            allowedComplexity = "N_LOG_N",
            allowedExplanation = "테스트용 허용 복잡도 설명",
            recommendedAlgorithms = listOf(
                AlgorithmResult(
                    name = "BFS",
                    finalScore = 10,
                    droppedByTime = false,
                    messages = listOf("그래프 최단 거리 문제에 적합")
                )
            ),
            droppedAlgorithms = listOf(
                AlgorithmResult(
                    name = "브루트포스",
                    finalScore = -5,
                    droppedByTime = true,
                    messages = listOf("입력 크기가 너무 커서 제외")
                )
            ),
            summary = "이 문제는 BFS가 추천됩니다.",
            aiExplanation = null
        )

        // ✨ 환경변수에 OPENAI_API_KEY가 비어 있다고 가정하고 서비스 생성
        val service = AiExplanationService(apiKey = "")

        // when
        val explanation = service.buildExplanation(response)

        // then
        assertNotNull(explanation)
        assertTrue(explanation.contains("BFS"))
        assertTrue(explanation.contains("허용되는 시간 복잡도"))
        assertFalse(explanation.isBlank())
    }
}