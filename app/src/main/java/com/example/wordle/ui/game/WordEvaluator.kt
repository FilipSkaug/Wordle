package com.example.wordle.ui.game

/**
 * Represents the result of word evaluation with type-safe error handling.
 */
sealed class EvaluationResult {
    /**
     * Successful evaluation with tile visual states for each position.
     */
    data class Success(val states: List<TileVisualState>) : EvaluationResult()

    /**
     * The guessed word does not have the required length.
     */
    data class InvalidLength(val providedLength: Int, val requiredLength: Int) : EvaluationResult()

    /**
     * The target word does not have the required length.
     */
    data class InvalidTargetLength(val providedLength: Int, val requiredLength: Int) : EvaluationResult()
}

/**
 * Evaluates a guessed word against the target word and returns tile visual states for each position.
 * 
 * Logic:
 * - CORRECT: Letter is in the correct position
 * - PRESENT: Letter exists in the target word but in the wrong position
 * - ABSENT: Letter does not exist in the target word
 * 
 * @return EvaluationResult containing success states or error information
 */
object WordEvaluator {

    @JvmStatic
    fun evaluate(guess: String, target: String): EvaluationResult {
        if (guess.length != WORD_LENGTH) {
            return EvaluationResult.InvalidLength(guess.length, WORD_LENGTH)
        }

        if (target.length != WORD_LENGTH) {
            return EvaluationResult.InvalidTargetLength(target.length, WORD_LENGTH)
        }

        val guessUpper = guess.uppercase()
        val targetUpper = target.uppercase()
        val result = List(WORD_LENGTH) { TileVisualState.ABSENT }.toMutableList()
        val targetLetterCount = mutableMapOf<Char, Int>()

        // First pass: mark correct letters and count target letters
        for (i in 0 until WORD_LENGTH) {
            val guessChar = guessUpper[i]
            val targetChar = targetUpper[i]

            if (guessChar == targetChar) {
                result[i] = TileVisualState.CORRECT
            } else {
                targetLetterCount[targetChar] = targetLetterCount.getOrDefault(targetChar, 0) + 1
            }
        }

        // Second pass: mark present letters (used in wrong position)
        for (i in 0 until WORD_LENGTH) {
            if (result[i] != TileVisualState.CORRECT) {
                val guessChar = guessUpper[i]
                val remainingCount = targetLetterCount[guessChar] ?: 0

                if (remainingCount > 0) {
                    result[i] = TileVisualState.PRESENT
                    targetLetterCount[guessChar] = remainingCount - 1
                }
            }
        }

        return EvaluationResult.Success(result)
    }

    /**
     * Checks if the guess completely matches the target word.
     */
    @JvmStatic
    fun isCorrectWord(guess: String, target: String): Boolean {
        return guess.length == WORD_LENGTH && 
               target.length == WORD_LENGTH && 
               guess.uppercase() == target.uppercase()
    }
}