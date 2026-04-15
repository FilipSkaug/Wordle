package com.example.wordle.ui.game

import com.example.wordle.ui.KeyState

fun reduceKeyboardKeyStates(
    previous: Map<Char, KeyState>,
    guess: String,
    states: List<TileVisualState>
): Map<Char, KeyState> {
    if (guess.isBlank()) return previous

    val next = previous.toMutableMap()
    val guessUpper = guess.uppercase()

    for (i in guessUpper.indices) {
        val ch = guessUpper[i]
        if (ch !in 'A'..'Z') continue

        val tileState = states.getOrNull(i) ?: TileVisualState.ABSENT
        val incoming = tileState.toKeyState()
        val existing = next[ch] ?: KeyState.DEFAULT
        next[ch] = maxKeyState(existing, incoming)
    }

    return next
}

private fun TileVisualState.toKeyState(): KeyState = when (this) {
    TileVisualState.CORRECT -> KeyState.CORRECT
    TileVisualState.PRESENT -> KeyState.PRESENT
    TileVisualState.ABSENT -> KeyState.ABSENT
    TileVisualState.EMPTY, TileVisualState.TYPING -> KeyState.DEFAULT
}

private fun maxKeyState(a: KeyState, b: KeyState): KeyState {
    return if (keyStatePriority(b) > keyStatePriority(a)) b else a
}

private fun keyStatePriority(state: KeyState): Int = when (state) {
    KeyState.DEFAULT -> 0
    KeyState.ABSENT -> 1
    KeyState.PRESENT -> 2
    KeyState.CORRECT -> 3
}
package com.example.wordle.ui.game

import com.example.wordle.ui.KeyState

fun reduceKeyboardKeyStates(
    previous: Map<Char, KeyState>,
    guess: String,
    states: List<TileVisualState>
): Map<Char, KeyState> {
    if (guess.isBlank()) return previous

    val next = previous.toMutableMap()
    val guessUpper = guess.uppercase()

    for (i in guessUpper.indices) {
        val ch = guessUpper[i]
        if (ch !in 'A'..'Z') continue

        val tileState = states.getOrNull(i) ?: TileVisualState.ABSENT
        val incoming = tileState.toKeyState()
        val existing = next[ch] ?: KeyState.DEFAULT
        next[ch] = maxKeyState(existing, incoming)
    }

    return next
}

private fun TileVisualState.toKeyState(): KeyState = when (this) {
    TileVisualState.CORRECT -> KeyState.CORRECT
    TileVisualState.PRESENT -> KeyState.PRESENT
    TileVisualState.ABSENT -> KeyState.ABSENT
    TileVisualState.EMPTY, TileVisualState.TYPING -> KeyState.DEFAULT
}

private fun maxKeyState(a: KeyState, b: KeyState): KeyState {
    return if (keyStatePriority(b) > keyStatePriority(a)) b else a
}

private fun keyStatePriority(state: KeyState): Int = when (state) {
    KeyState.DEFAULT -> 0
    KeyState.ABSENT -> 1
    KeyState.PRESENT -> 2
    KeyState.CORRECT -> 3
}
