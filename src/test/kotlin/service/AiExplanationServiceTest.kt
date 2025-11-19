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
    fun `API 키가 없을 때 fallback 설명이 생성된다`() {
        // given
        val recommended = listOf(
            AlgorithmResult(
                name = "BFS",
                finalScore = 10,
                droppedByTime = false,
                messages = listOf("그래프 최단 거리 문제에 적합합니다.")
            ),
            AlgorithmResult(
                name = "완전 탐색(브루트포스)",
                finalScore = 7,
                droppedByTime = false,
                messages = listOf("모든 경우를 시도해도 되는 입력 크기입니다.")
            )
        )

        val dropped = listOf(
            AlgorithmResult(
                name = "플로이드-워셜",
                finalScore = -5,
                droppedByTime = true,
                messages = listOf("정점 수가 커서 O(N^3)은 시간 초과 위험이 큽니다.")
            )
        )

        val response = AnalyzeResponse(
            allowedComplexity = "N_LOG_N",
            allowedExplanation = "N이 크기 때문에 O(N log N) 정도까지가 안전하다고 판단했습니다.",
            recommendedAlgorithms = recommended,
            droppedAlgorithms = dropped,
            summary = "이 문제는 BFS와 완전 탐색이 유력하고, 플로이드-워셜은 시간 복잡도 때문에 제외되었습니다.",
            aiExplanation = null
        )

        // ✨ API 키를 빈 문자열로 넘겨서 "반드시 fallback만 쓰도록" 강제
        val service = AiExplanationService(apiKey = "")

        // when
        val explanation = service.buildExplanation(response)

        // then
        assertNotNull(explanation)
        assertTrue(explanation.isNotBlank(), "설명 문자열이 비어 있으면 안 됩니다.")

        // ✨ fallback에서 넣어준 고정 문구가 들어있는지 확인
        assertTrue(
            explanation.contains("[기본 설명 - GPT 없이 생성됨]"),
            "fallback 모드에서 생성된 기본 설명 문구가 포함되어야 합니다."
        )

        // ✨ 우리가 넣은 알고리즘 이름들이 설명 안에 포함됐는지 확인
        assertTrue(explanation.contains("BFS"))
        assertTrue(explanation.contains("완전 탐색"))
        assertTrue(explanation.contains("플로이드-워셜"))
    }
}