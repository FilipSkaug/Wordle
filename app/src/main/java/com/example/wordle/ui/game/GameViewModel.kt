package com.example.wordle.ui.game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        GameUiState(
            rows = listOf(
                GuessRowUiState(
                    listOf(
                        TileUiState('S', TileVisualState.ABSENT),
                        TileUiState('T', TileVisualState.ABSENT),
                        TileUiState('A', TileVisualState.PRESENT),
                        TileUiState('R', TileVisualState.ABSENT),
                        TileUiState('E', TileVisualState.CORRECT)
                    )
                ),
                GuessRowUiState(
                    listOf(
                        TileUiState('P', TileVisualState.TYPING),
                        TileUiState('L', TileVisualState.TYPING),
                        TileUiState('A', TileVisualState.TYPING),
                        TileUiState(),
                        TileUiState()
                    )
                ),
                GuessRowUiState(List(WORD_LENGTH) { TileUiState() }),
                GuessRowUiState(List(WORD_LENGTH) { TileUiState() }),
                GuessRowUiState(List(WORD_LENGTH) { TileUiState() }),
                GuessRowUiState(List(WORD_LENGTH) { TileUiState() })
            ),
            statusText = "Round 1 of 6"
        )
    )

    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
}
