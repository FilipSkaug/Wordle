package com.example.wordle.ui.game

enum class GameMode {
    DAILY,
    CUSTOM
}

data class GameConfig(
    val mode: GameMode = GameMode.DAILY,
    val maxGuesses: Int = MAX_GUESSES
)
