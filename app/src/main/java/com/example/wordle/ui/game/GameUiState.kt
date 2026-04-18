package com.example.wordle.ui.game

import androidx.compose.ui.graphics.Color
import com.example.wordle.data.stats.UserStats
import com.example.wordle.ui.KeyState
import com.example.wordle.ui.theme.TileAbsent
import com.example.wordle.ui.theme.TileCorrect
import com.example.wordle.ui.theme.TileEmptyBackground
import com.example.wordle.ui.theme.TileEmptyBorder
import com.example.wordle.ui.theme.TilePresent
import com.example.wordle.ui.theme.TileTypingBorder
import com.example.wordle.ui.theme.WordleTextPrimary

const val WORD_LENGTH = 5
const val MAX_GUESSES = 6

enum class GameOutcome {
    WON,
    LOST
}

data class GameUiState(
    val rows: List<GuessRowUiState> = List(MAX_GUESSES) {
        GuessRowUiState(List(WORD_LENGTH) { TileUiState() })
    },
    val statusText: String = "Loading word...",
    val stats: UserStats = UserStats(),
    val isStatsDialogVisible: Boolean = false,
    val isTargetWordLoaded: Boolean = false,
    val keyStates: Map<Char, KeyState> = emptyMap(),
    val gameOutcome: GameOutcome? = null,
    val revealedTargetWord: String? = null,
    val maxGuesses: Int = MAX_GUESSES,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val topBannerMessage: String? = null,
    /**
     * Controls whether the end-of-game screen (You won/You lost + stats) should be shown.
     * The game can be finished (gameOutcome != null) while this is false, e.g. when returning
     * to today's already-played board from the menu.
     */
    val isResultScreenVisible: Boolean = false
)

data class GuessRowUiState(
    val tiles: List<TileUiState>,
    val isShaking: Boolean = false // Added to indicate if the row should shake
)

data class TileUiState(
    val letter: Char? = null,
    val state: TileVisualState = TileVisualState.EMPTY
)

enum class TileVisualState {
    EMPTY,
    TYPING,
    CORRECT,
    PRESENT,
    ABSENT;

    fun getBackgroundColor(): Color = when (this) {
        EMPTY, TYPING -> TileEmptyBackground
        CORRECT -> TileCorrect
        PRESENT -> TilePresent
        ABSENT -> TileAbsent
    }

    fun getBorderColor(): Color = when (this) {
        EMPTY -> TileEmptyBorder
        TYPING -> TileTypingBorder
        CORRECT -> TileCorrect
        PRESENT -> TilePresent
        ABSENT -> TileAbsent
    }

    fun getTextColor(): Color = when (this) {
        EMPTY, TYPING -> WordleTextPrimary
        else -> Color.White
    }
}
