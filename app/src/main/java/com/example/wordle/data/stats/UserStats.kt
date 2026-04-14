package com.example.wordle.data.stats

data class UserStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    /**
     * Index 0 = wins in 1 guess, index 5 = wins in 6 guesses.
     */
    val guessDistribution: List<Int> = List(6) { 0 },
    val currentStreak: Int = 0,
    val maxStreak: Int = 0
) {
    val winRatePercent: Int
        get() = if (gamesPlayed == 0) 0 else ((gamesWon * 100.0) / gamesPlayed).toInt()
}

