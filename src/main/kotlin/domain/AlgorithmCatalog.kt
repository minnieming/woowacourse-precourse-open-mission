package com.example.algoadvisor.domain

import com.example.domain.AlgorithmInfo
import com.example.domain.TimeLevel

/**
 * 코딩테스트에서 자주 나오는 알고리즘/자료구조 목록
 * (필요하면 더 추가해도 됨)
 */
fun algorithmCatalog(): List<AlgorithmInfo> {
    return listOf(
        AlgorithmInfo(
            id = "bruteforce",
            displayName = "완전 탐색(브루트포스)",
            baseComplexity = TimeLevel.N_SQUARED,
            baseDescription = "가능한 모든 경우를 전부 시도해보는 알고리즘입니다."
        ),
        AlgorithmInfo(
            id = "backtracking",
            displayName = "백트래킹",
            baseComplexity = TimeLevel.EXPONENTIAL,
            baseDescription = "조건에 맞지 않는 경우를 중간에 잘라내면서(가지치기) 탐색하는 기법입니다."
        ),
        AlgorithmInfo(
            id = "bfs",
            displayName = "BFS (너비 우선 탐색)",
            baseComplexity = TimeLevel.N,
            baseDescription = "그래프에서 가까운 곳부터 차례대로 탐색할 때 사용합니다. 보통 최단 거리 문제와 함께 등장합니다."
        ),
        AlgorithmInfo(
            id = "dfs",
            displayName = "DFS (깊이 우선 탐색)",
            baseComplexity = TimeLevel.N,
            baseDescription = "그래프에서 한 방향으로 깊게 탐색할 때 사용합니다. 연결 요소, 사이클 검사 등에 많이 쓰입니다."
        ),
        AlgorithmInfo(
            id = "dijkstra",
            displayName = "다익스트라",
            baseComplexity = TimeLevel.N_LOG_N,
            baseDescription = "가중치가 양수인 그래프에서 최단 거리를 구할 때 사용하는 알고리즘입니다."
        ),
        AlgorithmInfo(
            id = "floyd",
            displayName = "플로이드-워셜",
            baseComplexity = TimeLevel.N_CUBED,
            baseDescription = "모든 정점 쌍 최단 거리를 구할 때 사용하는 알고리즘입니다."
        ),
        AlgorithmInfo(
            id = "mst_kruskal",
            displayName = "최소 스패닝 트리 - Kruskal",
            baseComplexity = TimeLevel.N_LOG_N,
            baseDescription = "간선을 정렬하고, 유니온 파인드로 사이클을 막으면서 최소 스패닝 트리를 만드는 알고리즘입니다."
        ),
        AlgorithmInfo(
            id = "mst_prim",
            displayName = "최소 스패닝 트리 - Prim",
            baseComplexity = TimeLevel.N_LOG_N,
            baseDescription = "하나의 정점에서 시작해, 가장 비용이 적은 간선을 선택해가며 최소 스패닝 트리를 만드는 알고리즘입니다."
        ),
        AlgorithmInfo(
            id = "topological_sort",
            displayName = "위상 정렬",
            baseComplexity = TimeLevel.N,
            baseDescription = "선후 관계(순서)가 있는 작업들을 정렬할 때 사용하는 알고리즘입니다."
        ),
        AlgorithmInfo(
            id = "binary_search",
            displayName = "이분 탐색",
            baseComplexity = TimeLevel.LOG_N,
            baseDescription = "정렬된 배열에서 원하는 값을 빠르게 찾을 때 사용합니다."
        ),
        AlgorithmInfo(
            id = "two_pointers",
            displayName = "투 포인터",
            baseComplexity = TimeLevel.N,
            baseDescription = "배열의 양 끝이나 같은 방향의 두 위치를 이동시키면서 연속된 구간, 합 등을 처리할 때 사용합니다."
        ),
        AlgorithmInfo(
            id = "prefix_sum",
            displayName = "누적 합",
            baseComplexity = TimeLevel.N,
            baseDescription = "미리 부분 합을 계산해두고, 구간 합을 빠르게 구하는 기법입니다."
        ),
        AlgorithmInfo(
            id = "greedy",
            displayName = "그리디(탐욕) 알고리즘",
            baseComplexity = TimeLevel.N_LOG_N,
            baseDescription = "항상 그 순간에 최선이라고 생각되는 선택을 반복해서 전체 최적해를 노리는 방법입니다."
        ),
        AlgorithmInfo(
            id = "dp_1d",
            displayName = "DP (1차원 동적 계획법)",
            baseComplexity = TimeLevel.N,
            baseDescription = "이전 상태 하나만 보고 다음 상태를 계산하는 동적 계획법입니다."
        ),
        AlgorithmInfo(
            id = "dp_2d",
            displayName = "DP (2차원 동적 계획법)",
            baseComplexity = TimeLevel.N_SQUARED,
            baseDescription = "2차원 테이블을 채우면서 부분 문제의 답을 이용하는 동적 계획법입니다."
        ),
        AlgorithmInfo(
            id = "dp_bitmask",
            displayName = "DP + 비트마스크",
            baseComplexity = TimeLevel.EXPONENTIAL,
            baseDescription = "부분 집합 상태를 비트로 표현해서 사용하는 동적 계획법입니다."
        ),
        AlgorithmInfo(
            id = "union_find",
            displayName = "유니온 파인드(분리 집합)",
            baseComplexity = TimeLevel.N,
            baseDescription = "서로소 집합을 관리하면서, 같은 그룹인지(연결되어 있는지) 빠르게 확인하는 자료구조입니다."
        ),
        AlgorithmInfo(
            id = "segment_tree",
            displayName = "세그먼트 트리",
            baseComplexity = TimeLevel.N_LOG_N,
            baseDescription = "구간 합/최솟값/최댓값 등을 빠르게 질의하고 갱신할 수 있는 트리 기반 자료구조입니다."
        ),
        AlgorithmInfo(
            id = "fenwick_tree",
            displayName = "펜윅 트리(비트, Binary Indexed Tree)",
            baseComplexity = TimeLevel.N_LOG_N,
            baseDescription = "세그먼트 트리보다 구현이 간단한 구간 합용 자료구조입니다."
        ),
        AlgorithmInfo(
            id = "stack",
            displayName = "스택",
            baseComplexity = TimeLevel.N,
            baseDescription = "LIFO(마지막에 넣은 걸 먼저 빼는) 구조입니다. 괄호 검사, DFS 구현 등에 사용됩니다."
        ),
        AlgorithmInfo(
            id = "queue",
            displayName = "큐",
            baseComplexity = TimeLevel.N,
            baseDescription = "FIFO(먼저 넣은 걸 먼저 빼는) 구조입니다. BFS 구현 등에 사용됩니다."
        ),
        AlgorithmInfo(
            id = "heap",
            displayName = "우선순위 큐(힙)",
            baseComplexity = TimeLevel.N_LOG_N,
            baseDescription = "항상 가장 작은 값 또는 가장 큰 값을 빠르게 꺼낼 수 있는 자료구조입니다."
        ),
        AlgorithmInfo(
            id = "hash",
            displayName = "해시(Map/Set)",
            baseComplexity = TimeLevel.N,
            baseDescription = "키-값 쌍 저장, 중복 체크, 존재 여부 확인 등에 평균 O(1)로 동작하는 자료구조입니다."
        )
    )
}