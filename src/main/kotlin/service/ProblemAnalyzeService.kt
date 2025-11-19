package com.example.service

import com.example.algoadvisor.domain.*
import com.example.domain.AlgorithmResult
import com.example.domain.AlgorithmScore
import com.example.domain.AnalyzeRequest
import com.example.domain.AnalyzeResponse
import com.example.domain.TimeLevel
import com.example.domain.timeLevelRank
import kotlinx.coroutines.runBlocking

// ✨ 여기: GPT 설명 + GPT 알고리즘 태그 서비스를 둘 다 받을 수 있게 생성자에 추가
class ProblemAnalyzeService(
    private val aiExplanationService: AiExplanationService? = null,
    private val aiAlgorithmPredictService: AiAlgorithmPredictService = AiAlgorithmPredictService()
) {

    fun analyzeProblem(request: AnalyzeRequest): AnalyzeResponse {
        // 알고리즘 점수판 초기화
        val catalog = algorithmCatalog() // 결과 설명해주는 클래스
        val scoreMap = mutableMapOf<String, AlgorithmScore>()
        for (info in catalog) { // key - value로 사용하기 위해서 이렇게 만듦
            scoreMap[info.id] = AlgorithmScore(info = info)
        }

        // 키워드 기반 점수 부여
        applyKeywordRules(request.text, scoreMap)

        // N 기반 허용 시간 복잡도 추정
        val (allowedLevel, allowedExplanation) = estimateAllowedTimeLevel(request.maxN)

        // 시간 복잡도 필터 적용
        applyTimeFilter(allowedLevel, scoreMap)

        // ==========================
        // 2단계: GPT 기반 알고리즘 태그 점수 반영
        // ==========================
        val gptTags: List<String> = try {
            runBlocking {
                aiAlgorithmPredictService.predictAlgorithms(request.text)
            }
        } catch (e: Exception) {
            emptyList()
        }

        for (tag in gptTags) {
            val lowerTag = tag.lowercase()

            // displayName에 태그 문자열이 들어가면 매칭된 걸로 간주
            val matchedAlgorithms = scoreMap.values.filter { algoScore ->
                val name = algoScore.info.displayName.lowercase()
                name.contains(lowerTag) || lowerTag.contains(name)
            }

            matchedAlgorithms.forEach { algoScore ->
                algoScore.score += 3  // GPT 추천 보너스 점수
                algoScore.reasons.add("GPT 분석 결과, 이 문제에서 사용할 후보 알고리즘으로 추천되었습니다. (태그: $tag)")
            }
        }

        // 5) 점수 기준으로 정렬 및 결과 가공
        val sorted = scoreMap.values.sortedByDescending { it.score }

        val recommended = mutableListOf<AlgorithmResult>()
        val dropped = mutableListOf<AlgorithmResult>()

        for (algo in sorted) {
            val messages = mutableListOf<String>()
            messages.add("기본 설명: ${algo.info.baseDescription}")
            messages.addAll(algo.reasons)

            val result = AlgorithmResult(
                name = algo.info.displayName,
                finalScore = algo.score,
                droppedByTime = algo.droppedByTime,
                messages = messages
            )

            if (algo.droppedByTime) {
                dropped.add(result)
            } else if (algo.score > 0) {
                // 점수가 0 이하인 건 추천에서 제외
                recommended.add(result)
            }
        }

        // 6) 요약 문장 만들기
        val topNames = recommended.take(3).joinToString(", ") { it.name }
        val droppedNames = dropped.joinToString(", ") { it.name }

        val summaryBuilder = StringBuilder()
        summaryBuilder.appendLine("문제의 입력 크기 기준으로 허용 가능한 시간 복잡도는 대략 '${allowedLevel}' 수준으로 추정했습니다.")
        if (topNames.isNotEmpty()) {
            summaryBuilder.appendLine("키워드와 시간 복잡도를 함께 고려했을 때, 추천 알고리즘은 다음과 같습니다: $topNames")
        } else {
            summaryBuilder.appendLine("점수가 충분히 높은 알고리즘이 없어, 명확히 추천하기 어렵습니다.")
        }
        if (droppedNames.isNotEmpty()) {
            summaryBuilder.appendLine("다음 알고리즘들은 시간 복잡도 때문에 제외되었습니다: $droppedNames")
        }

        val baseResponse = AnalyzeResponse(
            allowedComplexity = allowedLevel.toString(),
            allowedExplanation = allowedExplanation,
            recommendedAlgorithms = recommended,
            droppedAlgorithms = dropped,
            summary = summaryBuilder.toString(),
            aiExplanation = null
        )

        // GPT 설명 서비스가 주입되어 있으면 설명 생성
        val explanation = aiExplanationService?.buildExplanation(baseResponse)

        return if (explanation != null) {
            baseResponse.copy(aiExplanation = explanation)
        } else {
            baseResponse
        }
    }

    // ==========================
    // 1) N 기반 허용 시간 복잡도 추정
    // ==========================

    private fun estimateAllowedTimeLevel(maxN: Long?): Pair<TimeLevel, String> {
        return if (maxN == null) {
            TimeLevel.N_SQUARED to "입력 크기 N 정보를 받지 못해서, 대략 O(N^2) 정도까지는 가능하다고 가정했습니다."
        } else if (maxN <= 2_000) {
            TimeLevel.N_SQUARED to "N ≤ 2,000 이므로 O(N^2) 정도까지는 충분히 가능하다고 볼 수 있습니다."
        } else if (maxN <= 200_000) {
            TimeLevel.N_LOG_N to "N이 약 10만~20만 정도이므로 O(N log N) 정도까지는 가능하지만 O(N^2)는 시간 초과 위험이 큽니다."
        } else {
            TimeLevel.N to "N이 매우 크기 때문에 O(N) 정도의 알고리즘을 사용하는 것이 안전합니다."
        }
    }

    // ==========================
    // 2) 키워드 기반 규칙
    // ==========================

    private fun applyKeywordRules(text: String, scores: MutableMap<String, AlgorithmScore>) {
        val lower = text.lowercase()

        // 숫자 야구 같은 "비밀번호 맞히기" → 브루트포스, 백트래킹
        if (lower.contains("비밀번호") && lower.contains("맞히는")) {
            scores["bruteforce"]?.let { algo ->
                algo.score += 5
                algo.reasons.add("비밀번호를 맞히는 게임이므로 가능한 모든 후보를 시도하는 완전 탐색이 자연스럽습니다.")
            }
            scores["backtracking"]?.let { algo ->
                algo.score += 2
                algo.reasons.add("단서를 이용해 말이 안 되는 후보를 가지치기하는 백트래킹도 고려할 수 있습니다.")
            }
        }

        // 최단 거리 / 미로 → BFS/다익스트라/플로이드
        if (lower.contains("최단 거리") || lower.contains("최단거리") || lower.contains("미로")) {
            scores["bfs"]?.let { algo ->
                algo.score += 5
                algo.reasons.add("최단 거리, 미로 탈출 문제이므로 BFS가 강력한 후보입니다.")
            }
            scores["dijkstra"]?.let { algo ->
                algo.score += 2
                algo.reasons.add("가중치가 있는 그래프라면 다익스트라도 고려할 수 있습니다.")
            }
            scores["floyd"]?.let { algo ->
                algo.score += 1
                algo.reasons.add("정점 수가 작다면 플로이드-워셜로 모든 쌍 최단 거리를 구할 수도 있습니다.")
            }
        }

        // 그래프 / 노드 / 간선 / 트리 → BFS, DFS, MST, Union-Find, 위상정렬
        if (lower.contains("그래프") || lower.contains("노드") || lower.contains("간선") || lower.contains("정점")) {
            scores["bfs"]?.let { algo ->
                algo.score += 2
                algo.reasons.add("그래프 구조가 언급되어 BFS가 유력한 후보입니다.")
            }
            scores["dfs"]?.let { algo ->
                algo.score += 2
                algo.reasons.add("그래프 구조가 언급되어 DFS 또한 고려할 수 있습니다.")
            }
        }

        if (lower.contains("트리") || lower.contains("루트") || lower.contains("부모")) {
            scores["dfs"]?.let { algo ->
                algo.score += 2
                algo.reasons.add("트리 구조에서는 DFS로 깊이 탐색하는 방식이 자주 사용됩니다.")
            }
            scores["bfs"]?.let { algo ->
                algo.score += 1
                algo.reasons.add("트리에서 레벨 순서를 다룰 때는 BFS도 사용할 수 있습니다.")
            }
            scores["union_find"]?.let { algo ->
                algo.score += 1
                algo.reasons.add("트리/그래프에서 연결 여부를 확인하기 위해 유니온 파인드를 사용할 수 있습니다.")
            }
        }

        // 최소 신장 트리 / 네트워크 비용 / 연결 비용 최소 → MST
        if (lower.contains("최소 신장") || lower.contains("스패닝 트리") || (lower.contains("네트워크") && lower.contains("최소 비용"))) {
            scores["mst_kruskal"]?.let { algo ->
                algo.score += 3
                algo.reasons.add("간선을 정렬해서 연결 비용을 최소화하는 Kruskal MST가 대표적인 해결법입니다.")
            }
            scores["mst_prim"]?.let { algo ->
                algo.score += 3
                algo.reasons.add("하나의 정점에서 시작하는 Prim MST도 최소 신장 트리 문제의 전형적인 풀이입니다.")
            }
        }

        // 정렬 키워드
        if (lower.contains("정렬") || lower.contains("sort") || lower.contains("오름차순") || lower.contains("내림차순")) {
            scores["greedy"]?.let { algo ->
                algo.score += 1
                algo.reasons.add("많은 그리디 알고리즘이 정렬 후 처리하는 패턴으로 등장합니다.")
            }
        }

        // 이분 탐색 관련 키워드
        if (lower.contains("이분 탐색") || lower.contains("binary search")) {
            scores["binary_search"]?.let { algo ->
                algo.score += 5
                algo.reasons.add("문제에서 이분 탐색을 직접 언급하고 있습니다.")
            }
        }
        if (lower.contains("정렬된") || lower.contains("정렬 되어") || lower.contains("정렬되어")) {
            scores["binary_search"]?.let { algo ->
                algo.score += 2
                algo.reasons.add("데이터가 정렬되어 있다면 이분 탐색을 활용할 수 있습니다.")
            }
        }

        // 연속된 구간 / 부분 수열 / 구간 합 → 투 포인터, 누적 합
        if (lower.contains("연속된 구간") || lower.contains("구간 합") || lower.contains("부분 수열")) {
            scores["two_pointers"]?.let { algo ->
                algo.score += 4
                algo.reasons.add("연속된 구간이나 부분 수열의 합을 다루고 있어 투 포인터 알고리즘이 자주 사용됩니다.")
            }
            scores["prefix_sum"]?.let { algo ->
                algo.score += 3
                algo.reasons.add("구간 합을 여러 번 계산한다면 누적 합을 이용하면 효율적입니다.")
            }
        }

        // "모든 경우의 수", "경우의 수를 구하라" → 브루트포스 or DP
        if (lower.contains("모든 경우") || lower.contains("경우의 수")) {
            scores["bruteforce"]?.let { algo ->
                algo.score += 2
                algo.reasons.add("모든 경우를 세라는 요구가 있어 완전 탐색을 우선 떠올릴 수 있습니다.")
            }
            scores["dp_1d"]?.let { algo ->
                algo.score += 2
                algo.reasons.add("경우의 수를 효율적으로 세기 위해 동적 계획법(DP)을 사용할 수도 있습니다.")
            }
        }

        // "스택" / 괄호 / 후위 표기 등
        if (lower.contains("스택") || lower.contains("괄호") || lower.contains("stack")) {
            scores["stack"]?.let { algo ->
                algo.score += 4
                algo.reasons.add("괄호 검사, 수식 처리 등에는 스택이 전형적으로 사용됩니다.")
            }
        }

        // "큐" / "BFS" / "레벨 순서"
        if (lower.contains("큐") || lower.contains("대기열") || lower.contains("queue")) {
            scores["queue"]?.let { algo ->
                algo.score += 3
                algo.reasons.add("FIFO 구조가 언급되어 큐 자료구조 사용을 고려할 수 있습니다.")
            }
        }

        // "우선순위 큐" / "가장 작은 값" 반복 추출 / "힙"
        if (lower.contains("우선순위 큐") || (lower.contains("가장 작은 값") && lower.contains("반복")) || lower.contains("heap")) {
            scores["heap"]?.let { algo ->
                algo.score += 4
                algo.reasons.add("가장 작은/큰 값을 반복해서 뽑는 상황에서는 힙(우선순위 큐)를 사용하는 것이 일반적입니다.")
            }
        }

        // "집합", "중복 제거" → 해시
        if (lower.contains("중복") || lower.contains("집합") || lower.contains("hash") || lower.contains("빠르게 찾")) {
            scores["hash"]?.let { algo ->
                algo.score += 4
                algo.reasons.add("중복 제거, 존재 여부 확인에는 해시(Map/Set)가 자주 사용됩니다.")
            }
        }

        // "위상 정렬" / "선수 과목" / "선행 작업"
        if (lower.contains("위상 정렬") || lower.contains("선수 과목") || (lower.contains("선행") && lower.contains("작업"))) {
            scores["topological_sort"]?.let { algo ->
                algo.score += 5
                algo.reasons.add("선후 관계가 있는 작업들의 순서를 구하는 전형적인 위상 정렬 문제입니다.")
            }
        }

        // "동적 계획법", "DP"
        if (lower.contains("동적 계획법") || lower.contains("dp ") || lower.contains("dynamic programming")) {
            scores["dp_1d"]?.let { algo ->
                algo.score += 5
                algo.reasons.add("문제에서 동적 계획법을 직접 언급하고 있습니다.")
            }
        }
    }

    // ==========================
    // 3) 시간 복잡도 기준 필터/보정
    // ==========================

    private fun applyTimeFilter(
        allowedLevel: TimeLevel,
        scores: MutableMap<String, AlgorithmScore>
    ) {
        val allowedRank = timeLevelRank(allowedLevel)

        for ((_, algoScore) in scores) {
            val algoRank = timeLevelRank(algoScore.info.baseComplexity)

            if (algoRank > allowedRank) {
                // 이 알고리즘은 이 문제의 N에서 쓰기에는 너무 느리다
                algoScore.droppedByTime = true
                val msg =
                    "이 문제의 입력 크기에서는 ${algoScore.info.baseComplexity} 수준의 알고리즘은 시간 초과 위험이 커서 제외했습니다."
                algoScore.timeReason = msg
                algoScore.score -= 10
                algoScore.reasons.add(msg)
            } else {
                // 시간 복잡도 측면에서 사용 가능 → 약간 보너스 점수
                algoScore.score += 1
                algoScore.reasons.add("이 문제의 입력 크기에서 시간 복잡도 측면으로도 사용할 수 있는 수준입니다.")
            }
        }
    }
}