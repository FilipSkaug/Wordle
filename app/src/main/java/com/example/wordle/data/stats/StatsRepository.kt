package com.example.wordle.data.stats

interface StatsRepository {
    fun load(): UserStats
    fun save(stats: UserStats)
    fun reset()
}

