package com.example.wordle.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wordle.ui.theme.*

@Composable
fun HowToPlayDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WordleSurface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "How To Play",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = WordleTitle
                    )
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "X",
                            color = WordleTitle,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.semantics {
                                contentDescription = "Close How To Play Dialog"
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Guess the Wordle in 6 tries.",
                    style = MaterialTheme.typography.titleMedium,
                    color = WordleTextPrimary
                )

                BulletPoint("Each guess must be a valid 5-letter word.")
                BulletPoint("The color of the tiles will change to show how close your guess was to the word.")

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text(
                    text = "Examples",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = WordleTitle
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExampleRow(
                    word = "WEARY",
                    highlightIndex = 0,
                    color = TileCorrect,
                    description = "W is in the word and in the correct spot."
                )

                ExampleRow(
                    word = "PILLS",
                    highlightIndex = 1,
                    color = TilePresent,
                    description = "I is in the word but in the wrong spot."
                )

                ExampleRow(
                    word = "VAGUE",
                    highlightIndex = 3,
                    color = TileAbsent,
                    description = "U is not in the word in any spot."
                )
            }
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("• ", color = WordleTextSecondary)
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = WordleTextSecondary)
    }
}

@Composable
private fun ExampleRow(
    word: String,
    highlightIndex: Int,
    color: Color,
    description: String
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            word.forEachIndexed { index, char ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (index == highlightIndex) color else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (index == highlightIndex) color else TileEmptyBorder
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (index == highlightIndex) Color.White else WordleTitle
                    )
                }
            }
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
            color = WordleTextSecondary
        )
    }
}
