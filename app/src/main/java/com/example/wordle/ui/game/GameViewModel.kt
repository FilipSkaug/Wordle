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
            statusText = "Choose a game mode",
            stats = statsRepository.load()
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
            isLoading = true,
            errorMessage = null
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

    fun onKeyPress(key: String) {
        val currentState = _uiState.value

        if (!currentState.isTargetWordLoaded) return
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
            val presentLetters = lastRow.tiles.filter { it.state == TileVisualState.PRESENT || it.state == TileVisualState.CORRECT }
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

        // Check if word validation is required
        if (currentConfig.validateWords && currentState.wordLength == 5) {
            if (!wordProvider.isValidWord(guess)) {
                _uiState.value = currentState.copy(
                    statusText = "Not in word list",
                    errorMessage = null
                )
                return
            }
        }

        when (val evaluation = WordEvaluator.evaluate(guess, target)) {
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

                currentRowIndex++
                currentColIndex = 0

                _uiState.value = _uiState.value.copy(
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

    private fun updateTile(currentState: GameUiState, r: Int, c: Int, newTile: TileUiState) {
        val newRows = currentState.rows.toMutableList()
        val currentRowState = newRows[r]
        val newTiles = currentRowState.tiles.toMutableList()
        newTiles[c] = newTile
        newRows[r] = currentRowState.copy(tiles = newTiles)
        _uiState.value = currentState.copy(rows = newRows)
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
