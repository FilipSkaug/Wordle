package com.example.wordle.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wordle.data.WordProvider
import com.example.wordle.data.daily.DailyPlayRepository
import com.example.wordle.data.stats.StatsRepository
import com.example.wordle.data.stats.UserStatsRemoteSync

class GameViewModelFactory(
    private val statsRepository: StatsRepository,
    private val wordProvider: WordProvider,
    private val dailyPlayRepository: DailyPlayRepository,
    private val userStatsRemoteSync: UserStatsRemoteSync
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(
                statsRepository = statsRepository,
                wordProvider = wordProvider,
                dailyPlayRepository = dailyPlayRepository,
                userStatsRemoteSync = userStatsRemoteSync
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

