package com.example.wordle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wordle.ui.WordleKeyboard
import com.example.wordle.ui.game.GameScreen
import com.example.wordle.ui.game.GameViewModel
import com.example.wordle.ui.theme.WordleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordleTheme {
                val gameViewModel: GameViewModel = viewModel()
                val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Game board area
                            GameScreen(
                                uiState = uiState,
                                modifier = Modifier.weight(1f)
                            )

                            // Keyboard at the bottom
                            WordleKeyboard(
                                onKeyPress = { pressedKey ->
                                    gameViewModel.onKeyPress(pressedKey)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
