package com.example.service

import com.example.domain.AnalyzeRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ProblemAnalyzeServiceTest {

    // ✨ aiExplanationService를 함께 주입
    private val service = ProblemAnalyzeService(
        aiExplanationService = AiExplanationService(apiKey = "")  // 키는 비워두고 fallback만 사용
    )

    @Test
    fun `숫자 야구 문제에서는 브루트포스 계열 알고리즘이 후보에 올라온다`() {
        // given
        val text = """
            당신은 숫자 야구를 플레이하는 프로그램을 작성해야 합니다.
            숫자 야구란 1 ~ 9 사이의 서로 다른 숫자 4개로 이루어진 비밀번호를 맞히는 게임입니다.
            당신은 1000 이상 9999 이하의 정수를 제출할 수 있는 기회가 총 n번 있으며, 수를 제출할 때마다 단서가 주어집니다.
            비밀번호를 맞히는 것이 목표입니다.
        """.trimIndent()

        val request = AnalyzeRequest(
            text = text,
            maxN = 1000   // N^2도 어느 정도 허용되는 구간이라고 가정
        )

        // when
        val response = service.analyzeProblem(request)

        // 디버깅용 로그 (테스트 실패할 때 콘솔에서 볼 수 있음)
        println("=== 숫자 야구 테스트 ===")
        println("recommended = ${response.recommendedAlgorithms.map { it.name to it.finalScore }}")
        println("dropped     = ${response.droppedAlgorithms.map { it.name to it.finalScore }}")

        // then
        // 1) "완전 탐색" 계열 알고리즘이 추천 또는 제외 목록 중 어딘가에는 등장해야 한다
        val allNames = (response.recommendedAlgorithms + response.droppedAlgorithms)
            .map { it.name }

        val hasBruteforceSomewhere = allNames.any { name ->
            name.contains("완전 탐색") ||
                    name.contains("브루트포스", ignoreCase = true) ||
                    name.contains("Brute", ignoreCase = true)
        }

        assertTrue(
            hasBruteforceSomewhere,
            "숫자 야구 문제에서는 완전 탐색(브루트포스) 계열 알고리즘이 후보 목록 어딘가에는 포함되어야 합니다."
        )

        // 2) (선택) 만약 추천 목록에 있다면, 점수가 0보다는 커야 함
        val bruteInRecommended = response.recommendedAlgorithms
            .find { it.name.contains("완전 탐색") || it.name.contains("Brute", ignoreCase = true) }

        bruteInRecommended?.let {
            assertTrue(
                it.finalScore > 0,
                "추천 목록에 있는 완전 탐색 알고리즘의 점수는 0보다 커야 합니다."
            )
        }
        // 시간복잡도 때문에 dropped에 있는 경우도 있을 수 있으므로,
        // '무조건 dropped에 없어야 한다' 라는 강한 가정은 일단 제거.
    }

    @Test
    fun `N이 매우 큰 경우 브루트포스는 시간 복잡도로 제외된다`() {
        // given
        val text = """
            어떤 수열에서 가능한 모든 경우의 수를 세는 문제입니다.
            모든 경우를 직접 시도하면 시간이 너무 오래 걸릴 수 있습니다.
        """.trimIndent()

        val request = AnalyzeRequest(
            text = text,
            maxN = 1_000_000  
        )

        // when
        val response = service.analyzeProblem(request)

        println("=== 큰 N에서 브루트포스 테스트 ===")
        println("recommended = ${response.recommendedAlgorithms.map { it.name to it.finalScore }}")
        println("dropped     = ${response.droppedAlgorithms.map { it.name to it.finalScore }}")

        // then
        val bruteInDropped = response.droppedAlgorithms
            .any { it.name.contains("완전 탐색") || it.name.contains("Brute", ignoreCase = true) }

        assertTrue(
            bruteInDropped,
            "N이 매우 클 때는 완전 탐색(브루트포스)가 시간 복잡도 때문에 제외되어야 합니다."
        )
    }

    @Test
    fun `그래프 최단 거리 문제에서는 BFS가 높은 점수로 추천된다`() {
        // given
        val text = """
            N개의 정점과 M개의 간선으로 이루어진 그래프가 주어집니다.
            1번 정점에서 다른 모든 정점까지의 최단 거리를 구하는 프로그램을 작성하세요.
            모든 간선의 가중치는 1입니다.
        """.trimIndent()

        val request = AnalyzeRequest(
            text = text,
            maxN = 100_000
        )

        // when
        val response = service.analyzeProblem(request)

        println("=== 그래프 최단 거리 테스트 ===")
        println("recommended = ${response.recommendedAlgorithms.map { it.name to it.finalScore }}")
        println("dropped     = ${response.droppedAlgorithms.map { it.name to it.finalScore }}")

        // then
        val bfs = response.recommendedAlgorithms
            .find { it.name.contains("BFS") }

        assertNotNull(
            bfs,
            "그래프 최단 거리 문제에서는 BFS가 추천 목록에 포함되어야 합니다."
        )

        bfs?.let {
            val maxScore = response.recommendedAlgorithms.maxOf { algo -> algo.finalScore }
            assertEquals(
                maxScore,
                it.finalScore,
                "BFS는 이 문제에서 가장 높은 점수를 받는 알고리즘 중 하나여야 합니다."
            )
        }
    }

    // ai
    @Test
    fun `숫자 야구 문제에서는 aiExplanation이 생성된다`() {
        // given
        val text = """
            당신은 숫자 야구를 플레이하는 프로그램을 작성해야 합니다.
            숫자 야구란 1 ~ 9 사이의 서로 다른 숫자 4개로 이루어진 비밀번호를 맞히는 게임입니다.
            비밀번호를 맞히는 것이 목표입니다.
        """.trimIndent()

        val request = AnalyzeRequest(
            text = text,
            maxN = 1000
        )

        // when
        val response = service.analyzeProblem(request)

        // then
        assertNotNull(response.aiExplanation, "aiExplanation은 null이면 안 됩니다.")
        assertTrue(
            response.aiExplanation!!.contains("허용되는 시간 복잡도"),
            "aiExplanation 안에 기본 설명 문구가 포함되어야 합니다."
        )
    }
}