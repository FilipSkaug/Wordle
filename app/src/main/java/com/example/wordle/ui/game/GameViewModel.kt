package com.example.wordle.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordle.data.WordProvider
import com.example.wordle.data.daily.DailyPlayRepository
import com.example.wordle.data.stats.StatsRepository
import com.example.wordle.data.stats.UserStatsRemoteSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val statsRepository: StatsRepository,
    private val wordProvider: WordProvider,
    private val dailyPlayRepository: DailyPlayRepository,
    private val userStatsRemoteSync: UserStatsRemoteSync
) : ViewModel() {

    private var targetWord: String? = null
    private var currentConfig: GameConfig = GameConfig()
    private var currentRowIndex = 0
    private var currentColIndex = 0
    private var requestToken = 0

    private val _uiState = MutableStateFlow(
        GameUiState(
            statusText = "Choose a game mode",
            stats = statsRepository.load()
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun startGame(config: GameConfig, topBannerMessage: String? = null) {
        currentConfig = config
        currentRowIndex = 0
        currentColIndex = 0
        targetWord = null
        requestToken++
        val currentRequest = requestToken

        val initialWordLength = config.targetWord?.length ?: DEFAULT_WORD_LENGTH

        _uiState.value = GameUiState(
            rows = List(config.maxGuesses) {
                GuessRowUiState(List(initialWordLength) { TileUiState() })
            },
            statusText = loadingMessage(config.mode),
            stats = statsRepository.load(),
            maxGuesses = config.maxGuesses,
            wordLength = initialWordLength,
            isTargetWordLoaded = false,
            keyStates = emptyMap(),
            gameOutcome = null,
            revealedTargetWord = null,
            isResultScreenVisible = false,
            isLoading = true,
            errorMessage = null,
            topBannerMessage = topBannerMessage
        )

        when (config.mode) {
            GameMode.DAILY -> {
                wordProvider.getDailyWord(
                    onSuccess = { word ->
                        if (currentRequest != requestToken) return@getDailyWord
                        targetWord = word.uppercase()
                        val length = targetWord?.length ?: DEFAULT_WORD_LENGTH
                        _uiState.value = _uiState.value.copy(
                            isTargetWordLoaded = true,
                            isLoading = false,
                            wordLength = length,
                            rows = List(config.maxGuesses) {
                                GuessRowUiState(List(length) { TileUiState() })
                            },
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

            GameMode.RANDOM -> {
                wordProvider.getRandomWord(
                    onSuccess = { word ->
                        if (currentRequest != requestToken) return@getRandomWord
                        targetWord = word.uppercase()
                        val length = targetWord?.length ?: DEFAULT_WORD_LENGTH
                        _uiState.value = _uiState.value.copy(
                            isTargetWordLoaded = true,
                            isLoading = false,
                            wordLength = length,
                            rows = List(config.maxGuesses) {
                                GuessRowUiState(List(length) { TileUiState() })
                            },
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
                            statusText = "Could not start random game",
                            errorMessage = ex.message ?: "Unknown error"
                        )
                    }
                )
            }

            GameMode.CUSTOM -> {
                val word = config.targetWord?.uppercase()
                if (word.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isTargetWordLoaded = false,
                        isLoading = false,
                        statusText = "Custom word is missing",
                        errorMessage = "No target word provided"
                    )
                } else {
                    targetWord = word
                    val length = word.length
                    _uiState.value = _uiState.value.copy(
                        isTargetWordLoaded = true,
                        isLoading = false,
                        wordLength = length,
                        rows = List(config.maxGuesses) {
                            GuessRowUiState(List(length) { TileUiState() })
                        },
                        statusText = roundText(1, config.maxGuesses),
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun startDailyGame() {
        onCloseStats()
        if (dailyPlayRepository.hasPlayedTodayUtc()) {
            val topBannerMessage =
                "Daily word already played!\nSwitching to Random Mode."
            startGame(
                GameConfig(
                    mode = GameMode.RANDOM,
                    maxGuesses = DEFAULT_MAX_GUESSES
                ),
                topBannerMessage = topBannerMessage
            )
        } else {
            startGame(
                GameConfig(
                    mode = GameMode.DAILY,
                    maxGuesses = DEFAULT_MAX_GUESSES
                )
            )
        }
    }

    fun startNewCustomGame(
        maxGuesses: Int = DEFAULT_MAX_GUESSES,
        targetWord: String? = null,
        validateWords: Boolean = true,
        hardMode: Boolean = false
    ) {
        startGame(
            GameConfig(
                mode = GameMode.CUSTOM,
                maxGuesses = maxGuesses,
                targetWord = targetWord,
                validateWords = validateWords,
                hardMode = hardMode
            )
        )
    }

    fun onLeaveGameScreen() {
        // When returning later the same day, show the board instead of the result screen.
        if (_uiState.value.isResultScreenVisible) {
            _uiState.value = _uiState.value.copy(isResultScreenVisible = false)
        }
        if (_uiState.value.topBannerMessage != null) {
            _uiState.value = _uiState.value.copy(topBannerMessage = null)
        }
    }

    fun onOpenStats() {
        _uiState.value = _uiState.value.copy(isStatsDialogVisible = true)
    }

    fun onCloseStats() {
        _uiState.value = _uiState.value.copy(isStatsDialogVisible = false)
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
                if (currentColIndex < currentState.wordLength && key.length == 1) {
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

        if (currentColIndex != currentState.wordLength) {
            _uiState.value = currentState.copy(
                statusText = "You must type ${currentState.wordLength} letters before ENTER.",
                errorMessage = null
            )
            return
        }

        val guess = buildGuess(currentState, currentRowIndex)

        // Check hard mode
        if (currentConfig.hardMode && currentRowIndex > 0) {
            val lastRow = currentState.rows[currentRowIndex - 1]

            // Check green letters (CORRECT)
            lastRow.tiles.forEachIndexed { index, tile ->
                if (tile.state == TileVisualState.CORRECT) {
                    if (guess[index] != tile.letter) {
                        _uiState.value = currentState.copy(
                            statusText = "${index + 1}th letter must be ${tile.letter}",
                            errorMessage = null
                        )
                        return
                    }
                }
            }

            // Check yellow letters (PRESENT)
            val presentLetters = lastRow.tiles
                .filter { it.state == TileVisualState.PRESENT || it.state == TileVisualState.CORRECT }
                .mapNotNull { it.letter }
                .groupingBy { it }.eachCount()

            val guessLetters = guess.groupingBy { it }.eachCount()

            for ((char, count) in presentLetters) {
                if ((guessLetters[char] ?: 0) < count) {
                    _uiState.value = currentState.copy(
                        statusText = "Guess must contain $char",
                        errorMessage = null
                    )
                    return
                }
            }
        }

        // Validate the word using WordProvider only for 5-letter words when enabled
        val shouldValidate = currentConfig.validateWords && currentState.wordLength == 5

        if (shouldValidate) {
            wordProvider.isWordValid(guess, onSuccess = { isValid ->
                if (!isValid) {
                    // Shake the current row and allow re-editing
                    val updatedRows = currentState.rows.toMutableList()
                    updatedRows[currentRowIndex] = updatedRows[currentRowIndex].copy(isShaking = true)
                    _uiState.value = currentState.copy(rows = updatedRows, errorMessage = null)

                    // Stop shaking after a delay
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(500)
                        val resetRows = _uiState.value.rows.toMutableList()
                        resetRows[currentRowIndex] = resetRows[currentRowIndex].copy(isShaking = false)
                        _uiState.value = _uiState.value.copy(rows = resetRows)
                    }
                    return@isWordValid
                }

                evaluateAndApplyGuess(currentState, guess, target)
            }, onFailure = { exception ->
                _uiState.value = currentState.copy(
                    statusText = "Error validating word: ${exception.message}",
                    errorMessage = exception.message
                )
            })
        } else {
            evaluateAndApplyGuess(currentState, guess, target)
        }
    }

    private fun evaluateAndApplyGuess(currentState: GameUiState, guess: String, target: String) {
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

                    // Only update and save stats if playing the Daily mode
                    val finalStats = if (currentConfig.mode == GameMode.DAILY) {
                        val updatedStats = updateStatsForGameEnd(
                            current = statsRepository.load(),
                            outcome = GameOutcome.WON,
                            attempts = attempts
                        )
                        statsRepository.save(updatedStats)
                        syncStatsToRemote(updatedStats)
                        dailyPlayRepository.markPlayedTodayUtc()
                        updatedStats // Use the new stats for the UI
                    } else {
                        statsRepository.load() // Use existing stats for the UI without saving
                    }

                    _uiState.value = currentState.copy(
                        rows = nextRows,
                        keyStates = nextKeyStates,
                        stats = finalStats, // Pass the appropriate stats object
                        gameOutcome = GameOutcome.WON,
                        revealedTargetWord = target,
                        isResultScreenVisible = true,
                        statusText = roundText(currentRowIndex + 1, currentConfig.maxGuesses),
                        errorMessage = null
                    )
                    return
                }

                if (isLastAttempt) {
                    // Only update and save stats if playing the Daily mode
                    val finalStats = if (currentConfig.mode == GameMode.DAILY) {
                        val updatedStats = updateStatsForGameEnd(
                            current = statsRepository.load(),
                            outcome = GameOutcome.LOST,
                            attempts = null
                        )
                        statsRepository.save(updatedStats)
                        syncStatsToRemote(updatedStats)
                        dailyPlayRepository.markPlayedTodayUtc()
                        updatedStats
                    } else {
                        statsRepository.load()
                    }

                    _uiState.value = currentState.copy(
                        rows = nextRows,
                        keyStates = nextKeyStates,
                        stats = finalStats,
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


    private fun updateTile(currentState: GameUiState, r: Int, c: Int, newTile: TileUiState) {
        val newRows = currentState.rows.toMutableList()
        val currentRowState = newRows[r]
        val newTiles = currentRowState.tiles.toMutableList()
        newTiles[c] = newTile
        newRows[r] = currentRowState.copy(tiles = newTiles)
        _uiState.value = currentState.copy(rows = newRows)
    }

    private fun syncStatsToRemote(stats: com.example.wordle.data.stats.UserStats) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { userStatsRemoteSync.syncStatsToFirestore(stats) }
        }
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
            GameMode.RANDOM -> "Loading random word..."
            GameMode.CUSTOM -> "Preparing custom game..."
        }
    }

    private fun roundText(round: Int, maxGuesses: Int): String {
        return "Round $round of $maxGuesses"
    }
}

private const val BACKSPACE_KEY = "⌫"
