package com.example.wordle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//Defines the states a key can have to handle your coloring requirement
enum class KeyState {
    DEFAULT, CORRECT, PRESENT, ABSENT
}

// Function for a single key
@Composable
fun KeyboardKey(
    modifier: Modifier = Modifier,
    text: String,
    state: KeyState = KeyState.DEFAULT,
    // Sends key value back to WordleKeyboard
    onClick: (String) -> Unit
) {
    // Colors based on the state
    val backgroundColor = when (state) {
        KeyState.DEFAULT -> Color.LightGray
        KeyState.CORRECT -> Color(0xFF538D4E)
        KeyState.PRESENT -> Color(0xFFB59F3B)
        KeyState.ABSENT -> Color.DarkGray
    }

    val textColor = if (state == KeyState.DEFAULT) Color.Black else Color.White

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable { onClick(text) }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

// Function for the entire keyboard layout
@Composable
fun WordleKeyboard(
    // Map passing the currentt state of any typed letters
    keyStates: Map<Char, KeyState> = emptyMap(),
    // Sends key value back to MainActivity
    onKeyPress: (String) -> Unit
) {
    val row1 = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
    val row2 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
    val row3 = listOf("ENTER", "Z", "X", "C", "V", "B", "N", "M", "⌫")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            row1.forEach { key ->
                KeyboardKey(
                    text = key,
                    state = keyStates[key.first()] ?: KeyState.DEFAULT,
                    modifier = Modifier.weight(1f),
                    onClick = onKeyPress
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.Center) {
            row2.forEach { key ->
                KeyboardKey(
                    text = key,
                    state = keyStates[key.first()] ?: KeyState.DEFAULT,
                    modifier = Modifier.weight(1f),
                    onClick = onKeyPress
                )
            }
        }


        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            row3.forEach { key ->
                val weight = if (key == "ENTER" || key == "⌫") 1.5f else 1f
                val state = when (key) {
                    "ENTER", "⌫" -> KeyState.DEFAULT
                    else -> keyStates[key.first()] ?: KeyState.DEFAULT
                }

                KeyboardKey(
                    text = key,
                    state = state,
                    modifier = Modifier.weight(weight),
                    onClick = {
                        if (key == "⌫") {
                            onKeyPress("BACKSPACE") // Send a specific action for backspace
                        } else {
                            onKeyPress(key)
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WordleKeyboardPreview() {
    val sampleKeyStates = mapOf(
        'W' to KeyState.CORRECT,
        'O' to KeyState.PRESENT,
        'R' to KeyState.ABSENT,
        'D' to KeyState.CORRECT,
        'L' to KeyState.PRESENT,
        'E' to KeyState.ABSENT
    )
    WordleKeyboard(
        keyStates = sampleKeyStates,
        onKeyPress = {}
    )
}
