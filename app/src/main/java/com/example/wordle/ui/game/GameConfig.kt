package com.example.wordle.ui.game

enum class GameMode {
    DAILY,
    RANDOM,
    CUSTOM
}

data class GameConfig(
    val mode: GameMode = GameMode.DAILY,
    val maxGuesses: Int = 6,
    val targetWord: String? = null,
    val validateWords: Boolean = true,
    val hardMode: Boolean = false
)
