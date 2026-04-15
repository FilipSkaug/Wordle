package com.example.wordle.ui.game

import com.example.wordle.ui.KeyState
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardStateReducerTest {

    @Test
    fun `reduceKeyboardKeyStates upgrades but never downgrades`() {
        val start = mapOf('A' to KeyState.ABSENT, 'B' to KeyState.PRESENT, 'C' to KeyState.CORRECT)

        val guess = "ABC"
        val states = listOf(TileVisualState.PRESENT, TileVisualState.ABSENT, TileVisualState.ABSENT)

        val end = reduceKeyboardKeyStates(start, guess, states)

        assertEquals(KeyState.PRESENT, end['A'])
        assertEquals(KeyState.PRESENT, end['B'])
        assertEquals(KeyState.CORRECT, end['C'])
    }
}
