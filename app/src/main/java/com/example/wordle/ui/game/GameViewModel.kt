package com.example.wordle.ui.game

import androidx.lifecycle.ViewModel
import com.example.wordle.data.WordProvider
import com.example.wordle.data.stats.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(
    private val statsRepository: StatsRepository,
    private val wordProvider: WordProvider
) : ViewModel() {

    private var targetWord: String? = null
    private var hasRequestedDailyWord: Boolean = false

    // Track the user's current typing position
    private var currentRowIndex = 0
    private var currentColIndex = 0

    // Initialize with a completely blank boarrd
    private val _uiState = MutableStateFlow(
        GameUiState(
            rows = List(6) { GuessRowUiState(List(WORD_LENGTH) { TileUiState() }) },
            statusText = "Loading today's word…",
            stats = statsRepository.load()
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun onGameStart() {
        // Decouple daily-word loading from ViewModel creation. This avoids loading the word
        // just because the ViewModel was instantiated (e.g. due to navigation/back stack reuse).
        if (hasRequestedDailyWord) return
        hasRequestedDailyWord = true
        loadDailyTargetWord()
    }

    // Logic to handle keyboard clicks
    fun onKeyPress(key: String) {
        val currentState = _uiState.value

        // Disable input until we have the daily target word.
        if (!currentState.isTargetWordLoaded) return

        // Stop accepting input if the game is over (all 6 rows filled)
        if (currentRowIndex >= 6) return

        when (key) {
            "ENTER" -> {
                submitGuess()
            }
            "⌫" -> {
                // Handle Backspace: Move back and clear the tile by setting it to the default empty TileUiState()
                if (currentColIndex > 0) {
                    currentColIndex--
                    updateTile(currentState, currentRowIndex, currentColIndex, TileUiState())
                }
            }
            else -> {
                // Handle Letters: Add the letter if row isn't full, using your teammate's TYPING state
                if (currentColIndex < WORD_LENGTH) {
                    val newTile = TileUiState(key.first(), TileVisualState.TYPING)
                    updateTile(currentState, currentRowIndex, currentColIndex, newTile)
                    currentColIndex++
                }
            }
        }
    }

    private fun loadDailyTargetWord() {
        _uiState.value = _uiState.value.copy(
            isTargetWordLoaded = false,
            statusText = "Loading today's word…"
        )

        wordProvider.getDailyWord(
            onSuccess = { word ->
                targetWord = word
                _uiState.value = _uiState.value.copy(
                    isTargetWordLoaded = true,
                    statusText = "Round 1 of 6"
                )
            },
            onFailure = { ex ->
                targetWord = null
                _uiState.value = _uiState.value.copy(
                    isTargetWordLoaded = false,
                    statusText = "Couldn't load today's word: ${ex.message ?: "unknown error"}"
                )
            }
        )
    }

    private fun submitGuess() {
        val currentState = _uiState.value
        val target = targetWord
        if (target.isNullOrBlank()) {
            _uiState.value = currentState.copy(statusText = "Today's word is missing. Please try again later.")
            return
        }

        if (currentColIndex != WORD_LENGTH) {
            _uiState.value = currentState.copy(statusText = "You must type $WORD_LENGTH letters before ENTER.")
            return
        }

        val guess = buildGuess(currentState, currentRowIndex)
        val evaluation = WordEvaluator.evaluate(guess, target)

        when (evaluation) {
            is EvaluationResult.Success -> {
                applyEvaluationToRow(
                    currentState = currentState,
                    rowIndex = currentRowIndex,
                    states = evaluation.states
                )

                val nextKeyStates = reduceKeyboardKeyStates(
                    previous = currentState.keyStates,
                    guess = guess,
                    states = evaluation.states
                )

                // Move to the next row, reset column
                currentRowIndex++
                currentColIndex = 0

                _uiState.value = _uiState.value.copy(
                    keyStates = nextKeyStates,
                    statusText = if (currentRowIndex < MAX_GUESSES) {
                        "Round ${currentRowIndex + 1} of 6"
                    } else {
                        "Game Over"
                    }
                )
            }

            is EvaluationResult.InvalidLength -> {
                _uiState.value = currentState.copy(
                    statusText = "You must type ${evaluation.requiredLength} letters before ENTER."
                )
            }

            is EvaluationResult.InvalidTargetLength -> {
                _uiState.value = currentState.copy(
                    statusText = "Today's word is invalid (length ${evaluation.providedLength})."
                )
            }
        }
    }

    private fun buildGuess(state: GameUiState, rowIndex: Int): String {
        return state.rows[rowIndex].tiles.joinToString(separator = "") { (it.letter ?: ' ').toString() }.trim()
    }

    private fun applyEvaluationToRow(
        currentState: GameUiState,
        rowIndex: Int,
        states: List<TileVisualState>
    ) {
        val newRows = currentState.rows.toMutableList()
        val row = newRows[rowIndex]
        val newTiles = row.tiles.mapIndexed { idx, tile ->
            tile.copy(state = states.getOrElse(idx) { TileVisualState.ABSENT })
        }
        newRows[rowIndex] = row.copy(tiles = newTiles)
        _uiState.value = currentState.copy(rows = newRows)
    }

    fun onOpenStats() {
        val latestStats = statsRepository.load()
        _uiState.value = _uiState.value.copy(
            stats = latestStats,
            isStatsDialogVisible = true
        )
    }

    fun onCloseStats() {
        _uiState.value = _uiState.value.copy(isStatsDialogVisible = false)
    }

    // Helper function to properly update the deeply nested Compose state
    private fun updateTile(currentState: GameUiState, r: Int, c: Int, newTile: TileUiState) {
        val newRows = currentState.rows.toMutableList()
        val currentRowState = newRows[r]
        val newTiles = currentRowState.tiles.toMutableList()

        // Update the specific tile
        newTiles[c] = newTile

        // Re-package it back up
        newRows[r] = currentRowState.copy(tiles = newTiles)
        _uiState.value = currentState.copy(rows = newRows)
    }
}