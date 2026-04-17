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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
    onStartGame: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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

        Text(
            text = "Choose the number of guesses",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp, bottom = 32.dp),
            color = WordleTitle
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { if (selectedGuesses > 4) onGuessesChanged(selectedGuesses - 1) },
                modifier = Modifier.width(64.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("-")
            }

            Text(
                text = selectedGuesses.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = WordleTitle,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Button(
                onClick = { if (selectedGuesses < 10) onGuessesChanged(selectedGuesses + 1) },
                modifier = Modifier.width(64.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartGame,
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
            onStartGame = {},
            onBack = {}
        )
    }
}
