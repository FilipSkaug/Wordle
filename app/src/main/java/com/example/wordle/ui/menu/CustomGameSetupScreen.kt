package com.example.wordle.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wordle.ui.theme.WordleBackground
import com.example.wordle.ui.theme.WordleTheme
import com.example.wordle.ui.theme.WordleTitle

@Composable
fun CustomGameSetupScreen(
    selectedGuesses: Int,
    onGuessesChanged: (Int) -> Unit,
    onStartGame: (String, Boolean, Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var customWord by remember { mutableStateOf("") }
    var validateWords by remember { mutableStateOf(true) }
    var hardMode by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WordleBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Custom Wordle",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = WordleTitle
        )

        Spacer(modifier = Modifier.height(24.dp))

        val maxChar = 9
        OutlinedTextField(
            value = customWord,
            onValueChange = { newValue ->
                if (newValue.length <= maxChar && newValue.all { it.isLetter() }) {
                    customWord = newValue.uppercase()
                    errorText = null
                }
            },
            label = { Text("Custom Solution Word") },
            placeholder = { Text("Enter word...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorText != null,
            supportingText = {
                errorText?.let { Text(it) }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Number of guesses: $selectedGuesses",
            style = MaterialTheme.typography.bodyLarge,
            color = WordleTitle
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Button(
                onClick = { if (selectedGuesses > 1) onGuessesChanged(selectedGuesses - 1) },
                modifier = Modifier.width(64.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("-")
            }

            Spacer(modifier = Modifier.width(32.dp))

            Button(
                onClick = { if (selectedGuesses < 20) onGuessesChanged(selectedGuesses + 1) },
                modifier = Modifier.width(64.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hard Mode",
                    style = MaterialTheme.typography.bodyLarge,
                    color = WordleTitle
                )
                Text(
                    text = "Any revealed hints must be used in subsequent guesses",
                    style = MaterialTheme.typography.bodySmall,
                    color = WordleTitle.copy(alpha = 0.6f)
                )
            }
            Switch(
                checked = hardMode,
                onCheckedChange = { hardMode = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Validate guesses",
                    style = MaterialTheme.typography.bodyLarge,
                    color = WordleTitle
                )
                Text(
                    text = "Only works for 5-letter words",
                    style = MaterialTheme.typography.bodySmall,
                    color = WordleTitle.copy(alpha = 0.6f)
                )
            }
            Switch(
                checked = validateWords,
                onCheckedChange = { validateWords = it },
                enabled = customWord.length == 5
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (customWord.isBlank()) {
                    errorText = "Please enter a word"
                } else if (customWord.length < 3) {
                    errorText = "Word too short"
                } else {
                    onStartGame(customWord, validateWords, hardMode)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Start Custom Wordle")
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Back")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomGameSetupScreenPreview() {
    WordleTheme {
        CustomGameSetupScreen(
            selectedGuesses = 6,
            onGuessesChanged = {},
            onStartGame = { _, _, _ -> },
            onBack = {}
        )
    }
}
