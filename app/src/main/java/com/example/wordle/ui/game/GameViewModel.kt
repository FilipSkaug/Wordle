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

    fun onLeaveGameScreen() {
        // When returning later the same day, show the board instead of the result screen.
        if (_uiState.value.isResultScreenVisible) {
            _uiState.value = _uiState.value.copy(isResultScreenVisible = false)
        }
    }

    // Logic to handle keyboard clicks
    fun onKeyPress(key: String) {
        val currentState = _uiState.value

        // Disable input until we have the daily target word.
        if (!currentState.isTargetWordLoaded) return

        // Stop accepting input if the game has ended.
        if (currentState.gameOutcome != null) return

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

        if (currentState.gameOutcome != null) return

        if (currentColIndex != WORD_LENGTH) {
            _uiState.value = currentState.copy(statusText = "You must type $WORD_LENGTH letters before ENTER.")
            return
        }

        val guess = buildGuess(currentState, currentRowIndex)
        val evaluation = WordEvaluator.evaluate(guess, target)

        when (evaluation) {
            is EvaluationResult.Success -> {
                val nextRows = evaluatedRows(
                    currentState = currentState,
                    rowIndex = currentRowIndex,
                    states = evaluation.states
                )

                val nextKeyStates = reduceKeyboardKeyStates(
                    previous = currentState.keyStates,
                    guess = guess,
                    states = evaluation.states
                )

                val isWin = WordEvaluator.isCorrectWord(guess, target)
                val isLastAttempt = currentRowIndex == MAX_GUESSES - 1

                if (isWin) {
                    val attempts = currentRowIndex + 1
                    val updatedStats = updateStatsForGameEnd(
                        current = statsRepository.load(),
                        outcome = GameOutcome.WON,
                        attempts = attempts
                    )
                    statsRepository.save(updatedStats)

                    _uiState.value = currentState.copy(
                        rows = nextRows,
                        keyStates = nextKeyStates,
                        stats = updatedStats,
                        gameOutcome = GameOutcome.WON,
                        revealedTargetWord = target,
                        isResultScreenVisible = true,
                        statusText = "You won"
                    )
                    return
                }

                if (isLastAttempt) {
                    val updatedStats = updateStatsForGameEnd(
                        current = statsRepository.load(),
                        outcome = GameOutcome.LOST,
                        attempts = null
                    )
                    statsRepository.save(updatedStats)

                    _uiState.value = currentState.copy(
                        rows = nextRows,
                        keyStates = nextKeyStates,
                        stats = updatedStats,
                        gameOutcome = GameOutcome.LOST,
                        revealedTargetWord = target,
                        isResultScreenVisible = true,
                        statusText = "You lost"
                    )
                    return
                }

                // Move to the next row, reset column
                currentRowIndex++
                currentColIndex = 0

                _uiState.value = currentState.copy(
                    rows = nextRows,
                    keyStates = nextKeyStates,
                    statusText = "Round ${currentRowIndex + 1} of 6"
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

    private fun evaluatedRows(
        currentState: GameUiState,
        rowIndex: Int,
        states: List<TileVisualState>
    ): List<GuessRowUiState> {
        val newRows = currentState.rows.toMutableList()
        val row = newRows[rowIndex]
        val newTiles = row.tiles.mapIndexed { idx, tile ->
            tile.copy(state = states.getOrElse(idx) { TileVisualState.ABSENT })
        }
        newRows[rowIndex] = row.copy(tiles = newTiles)
        return newRows
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

    private fun updateStatsForGameEnd(
        current: com.example.wordle.data.stats.UserStats,
        outcome: GameOutcome,
        attempts: Int?
    ): com.example.wordle.data.stats.UserStats {
        val played = current.gamesPlayed + 1

        return when (outcome) {
            GameOutcome.WON -> {
                val won = current.gamesWon + 1
                val nextDist = current.guessDistribution.toMutableList()
                val idx = ((attempts ?: 1) - 1).coerceIn(0, nextDist.lastIndex)
                nextDist[idx] = nextDist[idx] + 1

                val nextStreak = current.currentStreak + 1
                val nextMaxStreak = maxOf(current.maxStreak, nextStreak)

                current.copy(
                    gamesPlayed = played,
                    gamesWon = won,
                    guessDistribution = nextDist,
                    currentStreak = nextStreak,
                    maxStreak = nextMaxStreak
                )
            }

            GameOutcome.LOST -> {
                current.copy(
                    gamesPlayed = played,
                    currentStreak = 0
                )
            }
        }
    }
}