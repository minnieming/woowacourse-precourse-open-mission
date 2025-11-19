package com.example.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AiAlgorithmPredictServiceTest {

    @Test
    fun `API 키가 없으면 GPT를 호출하지 않고 빈 리스트를 돌려준다`() {
        // given
        val service = AiAlgorithmPredictService(apiKey = "")  // 🔹 강제로 키 비워서 fallback 모드

        // when
        val result = runBlocking {
            service.predictAlgorithms("그래프 최단 거리 문제입니다.")
        }

        // then
        assertNotNull(result)
        assertTrue(result.isEmpty(), "API 키가 없으면 알고리즘 태그 리스트가 비어 있어야 합니다.")
    }
}