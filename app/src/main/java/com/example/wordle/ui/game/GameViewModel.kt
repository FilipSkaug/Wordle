package com.example.wordle.ui.game

import androidx.lifecycle.ViewModel
import com.example.wordle.data.stats.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val WORD_LENGTH = 5

    // Track the user's current typing position
    private var currentRowIndex = 0
    private var currentColIndex = 0

    // Initialize with a completely blank boarrd
    private val _uiState = MutableStateFlow(
        GameUiState(
            rows = List(6) { GuessRowUiState(List(WORD_LENGTH) { TileUiState() }) },
            statusText = "Round 1 of 6",
            stats = statsRepository.load()
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Logic to handle keyboard clicks
    fun onKeyPress(key: String) {
        val currentState = _uiState.value

        // Stop accepting input if the game is over (all 6 rows filled)
        if (currentRowIndex >= 6) return

        when (key) {
            "ENTER" -> {
                // Requirement: Add "word finish" when you press enter and make sure there are 5 letters
                if (currentColIndex == WORD_LENGTH) {
                    // Move to the next row, reset column
                    currentRowIndex++
                    currentColIndex = 0

                    // Update the status text
                    _uiState.value = currentState.copy(
                        statusText = if (currentRowIndex < 6) "Round ${currentRowIndex + 1} of 6" else "Game Over"
                    )
                } else {
                    println("Not enough letters! Need $WORD_LENGTH.")
                }
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