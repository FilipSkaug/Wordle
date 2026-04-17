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
    private var currentConfig: GameConfig = GameConfig()
    private var currentRowIndex = 0
    private var currentColIndex = 0
    private var requestToken = 0

    private val _uiState = MutableStateFlow(
        GameUiState(
            rows = List(MAX_GUESSES) { GuessRowUiState(List(WORD_LENGTH) { TileUiState() }) },
            statusText = "Choose a game mode",
            stats = statsRepository.load(),
            maxGuesses = MAX_GUESSES
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun startGame(config: GameConfig) {
        currentConfig = config
        currentRowIndex = 0
        currentColIndex = 0
        targetWord = null
        requestToken++
        val currentRequest = requestToken

        _uiState.value = GameUiState(
            rows = List(config.maxGuesses) {
                GuessRowUiState(List(WORD_LENGTH) { TileUiState() })
            },
            statusText = loadingMessage(config.mode),
            stats = statsRepository.load(),
            maxGuesses = config.maxGuesses,
            isTargetWordLoaded = false,
            keyStates = emptyMap(),
            gameOutcome = null,
            revealedTargetWord = null,
            isResultScreenVisible = false,
            isLoading = true,
            errorMessage = null
        )

        when (config.mode) {
            GameMode.DAILY -> {
                wordProvider.getDailyWord(
                    onSuccess = { word ->
                        if (currentRequest != requestToken) return@getDailyWord
                        targetWord = word.uppercase()
                        _uiState.value = _uiState.value.copy(
                            isTargetWordLoaded = true,
                            isLoading = false,
                            statusText = roundText(1, config.maxGuesses),
                            errorMessage = null
                        )
                    },
                    onFailure = { ex ->
                        if (currentRequest != requestToken) return@getDailyWord
                        targetWord = null
                        _uiState.value = _uiState.value.copy(
                            isTargetWordLoaded = false,
                            isLoading = false,
                            statusText = "Could not start daily game",
                            errorMessage = ex.message ?: "Unknown error"
                        )
                    }
                )
            }

            GameMode.CUSTOM -> {
                wordProvider.getRandomWord(
                    onSuccess = { word ->
                        if (currentRequest != requestToken) return@getRandomWord
                        targetWord = word.uppercase()
                        _uiState.value = _uiState.value.copy(
                            isTargetWordLoaded = true,
                            isLoading = false,
                            statusText = roundText(1, config.maxGuesses),
                            errorMessage = null
                        )
                    },
                    onFailure = { ex ->
                        if (currentRequest != requestToken) return@getRandomWord
                        targetWord = null
                        _uiState.value = _uiState.value.copy(
                            isTargetWordLoaded = false,
                            isLoading = false,
                            statusText = "Could not start custom game",
                            errorMessage = ex.message ?: "Unknown error"
                        )
                    }
                )
            }
        }
    }

    fun onLeaveGameScreen() {
        // When returning later the same day, show the board instead of the result screen.
        if (_uiState.value.isResultScreenVisible) {
            _uiState.value = _uiState.value.copy(isResultScreenVisible = false)
        }
    }

    fun onKeyPress(key: String) {
        val currentState = _uiState.value

        if (!currentState.isTargetWordLoaded) return
        if (currentState.gameOutcome != null) return
        if (currentRowIndex >= currentConfig.maxGuesses) return

        when (key) {
            "ENTER" -> submitGuess()
            BACKSPACE_KEY -> {
                if (currentColIndex > 0) {
                    currentColIndex--
                    updateTile(currentState, currentRowIndex, currentColIndex, TileUiState())
                }
            }
            else -> {
                if (currentColIndex < WORD_LENGTH && key.length == 1) {
                    val newTile = TileUiState(key.first(), TileVisualState.TYPING)
                    updateTile(currentState, currentRowIndex, currentColIndex, newTile)
                    currentColIndex++
                }
            }
        }
    }

    private fun submitGuess() {
        val currentState = _uiState.value
        val target = targetWord

        if (target.isNullOrBlank()) {
            _uiState.value = currentState.copy(
                statusText = "Word is missing. Please try again later.",
                errorMessage = "Target word is missing"
            )
            return
        }

        if (currentState.gameOutcome != null) return

        if (currentColIndex != WORD_LENGTH) {
            _uiState.value = currentState.copy(
                statusText = "You must type $WORD_LENGTH letters before ENTER.",
                errorMessage = null
            )
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
                val isLastAttempt = currentRowIndex == currentConfig.maxGuesses - 1

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
                        statusText = roundText(currentRowIndex + 1, currentConfig.maxGuesses),
                        errorMessage = null
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
                        statusText = "Game Over",
                        errorMessage = null
                    )
                    return
                }

                currentRowIndex++
                currentColIndex = 0

                _uiState.value = currentState.copy(
                    rows = nextRows,
                    keyStates = nextKeyStates,
                    statusText = if (currentRowIndex < currentConfig.maxGuesses) {
                        roundText(currentRowIndex + 1, currentConfig.maxGuesses)
                    } else {
                        "Game Over"
                    },
                    errorMessage = null
                )
            }

            is EvaluationResult.InvalidLength -> {
                _uiState.value = currentState.copy(
                    statusText = "You must type ${evaluation.requiredLength} letters before ENTER.",
                    errorMessage = null
                )
            }

            is EvaluationResult.InvalidTargetLength -> {
                _uiState.value = currentState.copy(
                    statusText = "Selected word is invalid (length ${evaluation.providedLength}).",
                    errorMessage = "Invalid target word"
                )
            }
        }
    }

    private fun buildGuess(state: GameUiState, rowIndex: Int): String {
        return state.rows[rowIndex].tiles.joinToString(separator = "") {
            (it.letter ?: ' ').toString()
        }.trim()
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

    private fun updateTile(currentState: GameUiState, r: Int, c: Int, newTile: TileUiState) {
        val newRows = currentState.rows.toMutableList()
        val currentRowState = newRows[r]
        val newTiles = currentRowState.tiles.toMutableList()
        newTiles[c] = newTile
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

    private fun loadingMessage(mode: GameMode): String {
        return when (mode) {
            GameMode.DAILY -> "Loading daily word..."
            GameMode.CUSTOM -> "Loading random word..."
        }
    }

    private fun roundText(round: Int, maxGuesses: Int): String {
        return "Round $round of $maxGuesses"
    }
}

private const val BACKSPACE_KEY = "⌫"
