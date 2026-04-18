package com.example.wordle.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wordle.data.WordProvider
import com.example.wordle.data.daily.DailyPlayRepository
import com.example.wordle.data.stats.StatsRepository

class GameViewModelFactory(
    private val statsRepository: StatsRepository,
    private val wordProvider: WordProvider,
    private val dailyPlayRepository: DailyPlayRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(
                statsRepository = statsRepository,
                wordProvider = wordProvider,
                dailyPlayRepository = dailyPlayRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

