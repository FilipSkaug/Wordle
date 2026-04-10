package com.example.wordle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wordle.ui.WordleKeyboard
import com.example.wordle.ui.auth.AuthScreen
import com.example.wordle.ui.auth.AuthViewModel
import com.example.wordle.ui.game.GameScreen
import com.example.wordle.ui.game.GameViewModel
import com.example.wordle.ui.theme.WordleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordleTheme {
                val authViewModel = viewModel<AuthViewModel>()
                val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

                if (authUiState.isAuthenticated) {
                    val gameViewModel = viewModel<GameViewModel>()
                    val gameUiState by gameViewModel.uiState.collectAsStateWithLifecycle()

                    AuthenticatedApp(
                        gameUiState = gameUiState,
                        onLogout = authViewModel::logout
                    )
                } else {
                    AuthScreen(
                        uiState = authUiState,
                        onEmailChanged = authViewModel::onEmailChanged,
                        onPasswordChanged = authViewModel::onPasswordChanged,
                        onUsernameChanged = authViewModel::onUsernameChanged,
                        onSubmit = authViewModel::submit,
                        onSwitchToLogin = authViewModel::showLogin,
                        onSwitchToSignup = authViewModel::showSignup
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedApp(
    gameUiState: com.example.wordle.ui.game.GameUiState,
    onLogout: () -> Unit
) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onLogout) {
                        Text("Log out")
                    }
                }

                GameScreen(
                    uiState = gameUiState,
                    modifier = Modifier.weight(1f)
                )

                WordleKeyboard(
                    onKeyPress = { pressedKey ->
                        println("Key pressed: $pressedKey")
                    }
                )
            }
        }
    }
}
