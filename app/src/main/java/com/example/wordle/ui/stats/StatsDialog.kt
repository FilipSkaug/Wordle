package com.example.wordle.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.wordle.data.stats.UserStats
import com.example.wordle.ui.theme.WordleTextSecondary
import com.example.wordle.ui.theme.WordleTitle

@Composable
fun StatsDialog(
    stats: UserStats,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = { Text("Statistics") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatNumber(label = "Played", value = stats.gamesPlayed.toString())
                    StatNumber(label = "Won", value = stats.gamesWon.toString())
                    StatNumber(label = "Win rate", value = "${stats.winRatePercent}%")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatNumber(label = "Streak", value = stats.currentStreak.toString())
                    StatNumber(label = "Max", value = stats.maxStreak.toString())
                }

                Text(
                    text = "Guess distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                GuessDistribution(distribution = stats.guessDistribution)
            }
        }
    )
}

@Composable
private fun StatNumber(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = WordleTextSecondary
        )
    }
}

@Composable
private fun GuessDistribution(distribution: List<Int>) {
    val max = (distribution.maxOrNull() ?: 0).coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        distribution.forEachIndexed { idx, value ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${idx + 1}",
                    modifier = Modifier.width(16.dp),
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .background(WordleTextSecondary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                ) {
                    val fraction = value.toFloat() / max.toFloat()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxSize()
                            .background(WordleTitle, RoundedCornerShape(6.dp))
                    )
                    Text(
                        text = value.toString(),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 6.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

