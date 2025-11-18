package com.example.domain

import kotlinx.serialization.Serializable

//
// 시간 복잡도 수준 정의

enum class TimeLevel {
    CONSTANT,   // O(1)
    LOG_N,      // O(log N)
    N,          // O(N)
    N_LOG_N,    // O(N log N)
    N_SQUARED,  // O(N^2)
    N_CUBED,    // O(N^3)
    EXPONENTIAL // O(2^N), O(N!)
}

// 비교를 위해서 나눠 놓음
fun timeLevelRank(level: TimeLevel): Int {
    return when (level) {
        TimeLevel.CONSTANT -> 0
        TimeLevel.LOG_N -> 1
        TimeLevel.N -> 2
        TimeLevel.N_LOG_N -> 3
        TimeLevel.N_SQUARED -> 4
        TimeLevel.N_CUBED -> 5
        TimeLevel.EXPONENTIAL -> 6
    }
}


