package com.example.wordle.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordle.ui.theme.WordleBackground
import com.example.wordle.ui.theme.WordleTextSecondary
import com.example.wordle.ui.theme.WordleTheme
import com.example.wordle.ui.theme.WordleTitle

@Composable
fun GameScreen(
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WordleBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WORDLE",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = WordleTitle
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = uiState.statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = WordleTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            GuessGrid(rows = uiState.rows)

            Spacer(modifier = Modifier.weight(1f))

        }
    }
}

@Composable
private fun GuessGrid(
    rows: List<GuessRowUiState>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                row.tiles.forEach { tile ->
                    LetterTile(tile = tile)
                }
            }
        }
    }
}

@Composable
private fun LetterTile(
    tile: TileUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .background(tile.state.getBackgroundColor(), RoundedCornerShape(12.dp))
            .border(2.dp, tile.state.getBorderColor(), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.letter?.toString() ?: "",
            color = tile.state.getTextColor(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GameScreenPreview() {
    WordleTheme {
        GameScreen(
            uiState = GameUiState(
                rows = listOf(
                    GuessRowUiState(
                        listOf(
                            TileUiState('S', TileVisualState.ABSENT),
                            TileUiState('T', TileVisualState.ABSENT),
                            TileUiState('A', TileVisualState.PRESENT),
                            TileUiState('R', TileVisualState.ABSENT),
                            TileUiState('E', TileVisualState.CORRECT)
                        )
                    ),
                    GuessRowUiState(
                        listOf(
                            TileUiState('P', TileVisualState.TYPING),
                            TileUiState('L', TileVisualState.TYPING),
                            TileUiState('A', TileVisualState.TYPING),
                            TileUiState(),
                            TileUiState()
                        )
                    ),
                    GuessRowUiState(List(WORD_LENGTH) { TileUiState() }),
                    GuessRowUiState(List(WORD_LENGTH) { TileUiState() }),
                    GuessRowUiState(List(WORD_LENGTH) { TileUiState() }),
                    GuessRowUiState(List(WORD_LENGTH) { TileUiState() })
                ),
                statusText = "Round 1 of 6"
            )
        )
    }
}
