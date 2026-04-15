package com.example.wordle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wordle.ui.WordleKeyboard
import com.example.wordle.ui.auth.AuthScreen
import com.example.wordle.ui.auth.AuthViewModel
import com.example.wordle.ui.game.GameScreen
import com.example.wordle.ui.game.GameViewModel
import com.example.wordle.ui.menu.MenuScreen
import com.example.wordle.ui.settings.SettingsScreen
import com.example.wordle.ui.theme.WordleTheme
import com.example.wordle.ui.user.UserScreen // <-- NEW IMPORT ADDED HERE

// 1. ADDED "User" TO THE ENUM
enum class Screen {
    Menu, Game, Auth, Settings, User
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordleTheme {
                val authViewModel = viewModel<AuthViewModel>()
                val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

                var currentScreen by rememberSaveable { mutableStateOf(Screen.Menu) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        Screen.Menu -> MenuScreen(
                            isAuthenticated = authUiState.isAuthenticated,
                            onProfileClick = { currentScreen = Screen.User },
                            onPlayDaily = { currentScreen = Screen.Game },
                            onLoginClick = { currentScreen = Screen.Auth },
                            onSettingsClick = { currentScreen = Screen.Settings },
                            onStatsClick = {}
                        )

                        Screen.Game -> {
                            val gameViewModel = viewModel<GameViewModel>()
                            val gameUiState by gameViewModel.uiState.collectAsStateWithLifecycle()

                            BackHandler { currentScreen = Screen.Menu }

                            AuthenticatedApp(
                                gameUiState = gameUiState,
                                onKeyPress = gameViewModel::onKeyPress,
                                onLogout = {
                                    authViewModel.logout()
                                    currentScreen = Screen.Menu
                                },
                                onBack = { currentScreen = Screen.Menu }
                            )
                        }

                        Screen.Auth -> {
                            BackHandler { currentScreen = Screen.Menu }

                            LaunchedEffect(authUiState.isAuthenticated) {
                                if (authUiState.isAuthenticated) {
                                    currentScreen = Screen.Menu
                                }
                            }

                            AuthScreen(
                                uiState = authUiState,
                                onEmailChanged = authViewModel::onEmailChanged,
                                onPasswordChanged = authViewModel::onPasswordChanged,
                                onUsernameChanged = authViewModel::onUsernameChanged,
                                onSubmit = { authViewModel.submit() },
                                onSwitchToLogin = authViewModel::showLogin,
                                onSwitchToSignup = authViewModel::showSignup
                            )
                        }

                        Screen.Settings -> {
                            BackHandler { currentScreen = Screen.Menu }
                            SettingsScreen(onBack = { currentScreen = Screen.Menu })
                        }

                        Screen.User -> {
                            BackHandler { currentScreen = Screen.Menu }
                            UserScreen(
                                onNavigateBack = { currentScreen = Screen.Menu },
                                onLogout = {
                                    authViewModel.logout() // Clear Firebase auth state
                                    currentScreen = Screen.Menu // Send back to menu
                                }
                            )
                    }}
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedApp(
    gameUiState: com.example.wordle.ui.game.GameUiState,
    onKeyPress: (String) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onBack) {
                    Text("Back")
                }
                TextButton(onClick = onLogout) {
                    Text("Log out")
                }
            }

            GameScreen(
                uiState = gameUiState,
                modifier = Modifier.weight(1f)
            )


            WordleKeyboard(
                onKeyPress = onKeyPress
            )
        }
    }
}