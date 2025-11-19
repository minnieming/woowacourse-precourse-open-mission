package com.example.service

import com.example.domain.AnalyzeResponse

class AiExplanationService {

    fun buildExplanation(response: AnalyzeResponse): String {
        val sb = StringBuilder()

        sb.appendLine("이 문제에 대한 알고리즘 추천 결과입니다.")
        sb.appendLine()
        sb.appendLine("허용되는 시간 복잡도: ${response.allowedComplexity}")
        sb.appendLine(response.allowedExplanation)
        sb.appendLine()

        // 추천 알고리즘
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

        // 제외된 알고리즘
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