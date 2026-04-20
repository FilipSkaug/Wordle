package com.example.wordle.data.leaderboard

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val maxStreak: Int,
    val gamesPlayed: Int,
    val wins: Int
)
