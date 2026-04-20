package com.example.wordle.ui.game

import androidx.compose.animation.core.*
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordle.ui.WordleKeyboard
import com.example.wordle.ui.menu.HowToPlayDialog
import com.example.wordle.ui.stats.StatsContent
import com.example.wordle.ui.stats.StatsDialog
import com.example.wordle.ui.theme.WordleTheme

@Composable
fun GameScreen(
    uiState: GameUiState,
    onKeyPress: (String) -> Unit,
    onOpenStats: () -> Unit,
    onCloseStats: () -> Unit,
    onStartCustomDefault: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHowToPlay by rememberSaveable { mutableStateOf(false) }

    if (showHowToPlay) {
        HowToPlayDialog(onDismiss = { showHowToPlay = false })
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Row {
                    IconButton(onClick = { showHowToPlay = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "How to play",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onOpenStats) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Statistics",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (uiState.gameOutcome == null) {
                Box(modifier = Modifier.padding(bottom = 16.dp)) {
                    WordleKeyboard(
                        keyStates = uiState.keyStates,
                        onKeyPress = onKeyPress,
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "WORDLE",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            uiState.topBannerMessage?.let { banner ->
                Text(
                    text = banner,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (uiState.gameOutcome == null) {
                Text(
                    text = uiState.statusText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                val showResult = uiState.gameOutcome != null && uiState.isResultScreenVisible
                if (showResult) {
                    GameResultContent(
                        outcome = uiState.gameOutcome,
                        targetWord = uiState.revealedTargetWord,
                        stats = uiState.stats,
                        onStartCustomDefault = onStartCustomDefault,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GuessGrid(rows = uiState.rows)
                    }
                }
            }
        }
    }

    if (uiState.isStatsDialogVisible) {
        StatsDialog(
            stats = uiState.stats,
            onDismiss = onCloseStats
        )
    }
}

@Composable
private fun GameResultContent(
    outcome: GameOutcome?,
    targetWord: String?,
    stats: com.example.wordle.data.stats.UserStats,
    onStartCustomDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val title = when (outcome) {
            GameOutcome.WON -> "You won"
            GameOutcome.LOST -> "You lost"
            null -> ""
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (!targetWord.isNullOrBlank()) {
            Text(
                text = targetWord.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        StatsContent(stats = stats)

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onStartCustomDefault) {
            Text("Play Random Mode")
        }
    }
}

@Composable
private fun GuessGrid(
    rows: List<GuessRowUiState>,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    val horizontalPadding = 16.dp
    val spacing = 6.dp
    
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        rows.forEach { row ->
            val tileCount = row.tiles.size
            val dynamicTileSize = (screenWidth - horizontalPadding - (spacing * (tileCount - 1))) / tileCount

            val finalTileSize = dynamicTileSize.coerceAtMost(64.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (row.isShaking) Modifier.shake() else Modifier),
                horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
            ) {
                row.tiles.forEach { tile ->
                    LetterTile(tile = tile, size = finalTileSize)
                }
            }
        }
    }
}

@Composable
private fun Modifier.shake(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val offsetX = infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeOffset"
    ).value
    return this.graphicsLayer(translationX = offsetX)
}

@Composable
private fun LetterTile(
    tile: TileUiState,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val backgroundColor = when (tile.state) {
        TileVisualState.CORRECT -> WordleTheme.colors.correct
        TileVisualState.PRESENT -> WordleTheme.colors.present
        TileVisualState.ABSENT -> WordleTheme.colors.absent
        TileVisualState.EMPTY, TileVisualState.TYPING -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when (tile.state) {
        TileVisualState.EMPTY -> MaterialTheme.colorScheme.outlineVariant
        TileVisualState.TYPING -> MaterialTheme.colorScheme.outline
        else -> backgroundColor
    }

    val textColor = when (tile.state) {
        TileVisualState.EMPTY, TileVisualState.TYPING -> MaterialTheme.colorScheme.onSurface
        else -> Color.White
    }

    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.letter?.toString() ?: "",
            color = textColor,
            style = if (size < 40.dp) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
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
                    GuessRowUiState(List(DEFAULT_WORD_LENGTH) { TileUiState() }),
                    GuessRowUiState(List(DEFAULT_WORD_LENGTH) { TileUiState() }),
                    //GuessRowUiState(List(DEFAULT_WORD_LENGTH) { TileUiState() }),
                    //GuessRowUiState(List(DEFAULT_WORD_LENGTH) { TileUiState() }),
                    //GuessRowUiState(List(DEFAULT_WORD_LENGTH) { TileUiState() }),
                ),
                statusText = "Round 1 of 6"
            ),
            onKeyPress = {},
            onOpenStats = {},
            onCloseStats = {},
            onStartCustomDefault = {},
            onBack = {}
        )
    }
}
