package com.example.wordle.ui.game

import org.junit.Assert.*
import org.junit.Test

class WordEvaluatorTest {

    @Test
    fun `evaluate returns Success with all CORRECT for matching words`() {
        val guess = "TESTS"
        val target = "TESTS"
        val result = WordEvaluator.evaluate(guess, target)
        
        assertTrue(result is EvaluationResult.Success)
        val successResult = result as EvaluationResult.Success
        
        assertEquals(WORD_LENGTH, successResult.states.size)
        successResult.states.forEach { state ->
            assertEquals(TileVisualState.CORRECT, state)
        }
    }

    @Test
    fun `evaluate returns Success with all ABSENT for completely wrong word`() {
        val guess = "ABCDE"
        val target = "FGHIJ"
        val result = WordEvaluator.evaluate(guess, target)
        
        assertTrue(result is EvaluationResult.Success)
        val successResult = result as EvaluationResult.Success
        
        assertEquals(WORD_LENGTH, successResult.states.size)
        successResult.states.forEach { state ->
            assertEquals(TileVisualState.ABSENT, state)
        }
    }

    @Test
    fun `evaluate returns Success with mixed PRESENT and ABSENT for letters in wrong position`() {
        val guess = "STARE"
        val target = "TESTS"
        val result = WordEvaluator.evaluate(guess, target)
        
        assertTrue(result is EvaluationResult.Success)
        val states = (result as EvaluationResult.Success).states
        
        // S T A R E vs T E S T S
        // Position 0: S is PRESENT (exists in target, wrong position)
        // Position 1: T is PRESENT (exists in target, wrong position)
        // Position 2: A is ABSENT (not in target)
        // Position 3: R is ABSENT (not in target)
        // Position 4: E is PRESENT (exists in target, wrong position)
        assertEquals(TileVisualState.PRESENT, states[0])
        assertEquals(TileVisualState.PRESENT, states[1])
        assertEquals(TileVisualState.ABSENT, states[2])
        assertEquals(TileVisualState.ABSENT, states[3])
        assertEquals(TileVisualState.PRESENT, states[4])
    }

    @Test
    fun `evaluate handles duplicate letters correctly`() {
        val guess = "SSSSS"
        val target = "TESTS"
        val result = WordEvaluator.evaluate(guess, target)
        
        assertTrue(result is EvaluationResult.Success)
        val states = (result as EvaluationResult.Success).states
        
        // SSSSS vs TESTS
        // Position 0: S vs T - PRESENT (S exists, count 2)
        // Position 1: S vs E - PRESENT (count 1)
        // Position 2: S vs S - CORRECT (exact match)
        // Position 3: S vs T - ABSENT (count was used)
        // Position 4: S vs S - CORRECT (exact match)
        
        assertEquals(TileVisualState.PRESENT, states[0])
        assertEquals(TileVisualState.PRESENT, states[1])
        assertEquals(TileVisualState.CORRECT, states[2])
        assertEquals(TileVisualState.ABSENT, states[3])
        assertEquals(TileVisualState.CORRECT, states[4])
    }

    @Test
    fun `evaluate returns Success with mixed states`() {
        val guess = "TASTE"
        val target = "TESTS"
        val result = WordEvaluator.evaluate(guess, target)
        
        assertTrue(result is EvaluationResult.Success)
        val states = (result as EvaluationResult.Success).states
        
        // TASTE vs TESTS
        // Position 0: T vs T - CORRECT
        // Position 1: A vs E - ABSENT
        // Position 2: S vs S - CORRECT
        // Position 3: T vs T - CORRECT
        // Position 4: E vs S - ABSENT (E is not in target)
        
        assertEquals(TileVisualState.CORRECT, states[0])
        assertEquals(TileVisualState.ABSENT, states[1])
        assertEquals(TileVisualState.CORRECT, states[2])
        assertEquals(TileVisualState.CORRECT, states[3])
        assertEquals(TileVisualState.ABSENT, states[4])
    }

    @Test
    fun `evaluate returns InvalidLength for short guess`() {
        val guess = "TEST"
        val target = "TESTS"
        val result = WordEvaluator.evaluate(guess, target)
        
        assertTrue(result is EvaluationResult.InvalidLength)
        val invalidResult = result as EvaluationResult.InvalidLength
        
        assertEquals(4, invalidResult.providedLength)
        assertEquals(WORD_LENGTH, invalidResult.requiredLength)
    }

    @Test
    fun `evaluate returns InvalidLength for long guess`() {
        val guess = "TESTING"
        val target = "TESTS"
        val result = WordEvaluator.evaluate(guess, target)
        
        assertTrue(result is EvaluationResult.InvalidLength)
        val invalidResult = result as EvaluationResult.InvalidLength
        
        assertEquals(7, invalidResult.providedLength)
        assertEquals(WORD_LENGTH, invalidResult.requiredLength)
    }

    @Test
    fun `evaluate returns InvalidLength for empty guess`() {
        val guess = ""
        val target = "TESTS"
        val result = WordEvaluator.evaluate(guess, target)
        
        assertTrue(result is EvaluationResult.InvalidLength)
        val invalidResult = result as EvaluationResult.InvalidLength
        
        assertEquals(0, invalidResult.providedLength)
        assertEquals(WORD_LENGTH, invalidResult.requiredLength)
    }

    @Test
    fun `evaluate returns InvalidTargetLength for short target`() {
        val guess = "TESTS"
        val target = "TEST"
        val result = WordEvaluator.evaluate(guess, target)
        
        assertTrue(result is EvaluationResult.InvalidTargetLength)
        val invalidResult = result as EvaluationResult.InvalidTargetLength
        
        assertEquals(4, invalidResult.providedLength)
        assertEquals(WORD_LENGTH, invalidResult.requiredLength)
    }

    @Test
    fun `isCorrectWord returns true for matching words`() {
        val result = WordEvaluator.isCorrectWord("TESTS", "tests")
        assertTrue(result)
    }

    @Test
    fun `isCorrectWord returns true for matching words regardless of case`() {
        val result1 = WordEvaluator.isCorrectWord("TESTS", "TESTS")
        val result2 = WordEvaluator.isCorrectWord("tests", "TESTS")
        val result3 = WordEvaluator.isCorrectWord("TeStS", "tests")
        
        assertTrue(result1)
        assertTrue(result2)
        assertTrue(result3)
    }

    @Test
    fun `isCorrectWord returns false for non-matching words`() {
        val result = WordEvaluator.isCorrectWord("TASTE", "TESTS")
        assertFalse(result)
    }

    @Test
    fun `isCorrectWord returns false for incomplete words`() {
        val result1 = WordEvaluator.isCorrectWord("TEST", "TESTS")
        val result2 = WordEvaluator.isCorrectWord("TESTS", "TEST")
        
        assertFalse(result1)
        assertFalse(result2)
    }
}