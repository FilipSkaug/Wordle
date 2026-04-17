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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wordle.data.WordProvider
import com.example.wordle.data.stats.SharedPreferencesStatsRepository
import com.example.wordle.ui.WordleKeyboard
import com.example.wordle.ui.auth.AuthScreen
import com.example.wordle.ui.auth.AuthViewModel
import com.example.wordle.ui.game.GameConfig
import com.example.wordle.ui.game.GameMode
import com.example.wordle.ui.game.GameScreen
import com.example.wordle.ui.game.GameViewModel
import com.example.wordle.ui.game.GameViewModelFactory
import com.example.wordle.ui.menu.CustomGameSetupScreen
import com.example.wordle.ui.menu.MenuScreen
import com.example.wordle.ui.settings.SettingsScreen
import com.example.wordle.ui.theme.WordleTheme
import com.example.wordle.ui.user.UserScreen

enum class Screen {
    Menu, Game, Auth, Settings, User, CustomSetup
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
                var isStatsDialogVisible by rememberSaveable { mutableStateOf(false) }
                var selectedCustomGuesses by rememberSaveable { mutableStateOf(MAX_CUSTOM_GUESSES_DEFAULT) }
                var currentGameMode by rememberSaveable { mutableStateOf(GameMode.DAILY.name) }
                var currentMaxGuesses by rememberSaveable { mutableStateOf(DEFAULT_DAILY_GUESSES) }

                val currentGameConfig = GameConfig(
                    mode = GameMode.valueOf(currentGameMode),
                    maxGuesses = currentMaxGuesses
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        Screen.Menu -> MenuScreen(
                            isAuthenticated = authUiState.isAuthenticated,
                            onProfileClick = { currentScreen = Screen.User },
                            onPlayDaily = {
                                currentGameMode = GameMode.DAILY.name
                                currentMaxGuesses = DEFAULT_DAILY_GUESSES
                                currentScreen = Screen.Game
                            },
                            onPlayCustom = { currentScreen = Screen.CustomSetup },
                            onLoginClick = { currentScreen = Screen.Auth },
                            onSettingsClick = { currentScreen = Screen.Settings },
                            onStatsClick = { isStatsDialogVisible = true }
                        )

                        Screen.CustomSetup -> {
                            BackHandler { currentScreen = Screen.Menu }
                            CustomGameSetupScreen(
                                selectedGuesses = selectedCustomGuesses,
                                onGuessesChanged = { selectedCustomGuesses = it },
                                onStartGame = {
                                    currentGameMode = GameMode.CUSTOM.name
                                    currentMaxGuesses = selectedCustomGuesses
                                    currentScreen = Screen.Game
                                },
                                onBack = { currentScreen = Screen.Menu }
                            )
                        }

                        Screen.Game -> {
                            val statsRepository = remember { SharedPreferencesStatsRepository(applicationContext) }
                            val wordProvider = remember { WordProvider() }
                            val gameViewModel = viewModel<GameViewModel>(
                                factory = GameViewModelFactory(
                                    statsRepository = statsRepository,
                                    wordProvider = wordProvider
                                )
                            )
                            val gameUiState by gameViewModel.uiState.collectAsStateWithLifecycle()

                            LaunchedEffect(currentGameConfig.mode, currentGameConfig.maxGuesses) {
                                gameViewModel.startGame(currentGameConfig)
                            }

                            BackHandler { currentScreen = Screen.Menu }

                            AuthenticatedApp(
                                gameUiState = gameUiState,
                                onKeyPress = gameViewModel::onKeyPress,
                                onOpenStats = gameViewModel::onOpenStats,
                                onCloseStats = gameViewModel::onCloseStats,
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
                                onSubmit = authViewModel::submit,
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
                                    authViewModel.logout()
                                    currentScreen = Screen.Menu
                                }
                            )
                        }
                    }
                }

                if (isStatsDialogVisible) {
                    val statsRepository = remember { SharedPreferencesStatsRepository(applicationContext) }
                    com.example.wordle.ui.stats.StatsDialog(
                        stats = statsRepository.load(),
                        onDismiss = { isStatsDialogVisible = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedApp(
    gameUiState: com.example.wordle.ui.game.GameUiState,
    onKeyPress: (String) -> Unit,
    onOpenStats: () -> Unit,
    onCloseStats: () -> Unit,
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
                Row {
                    TextButton(onClick = onOpenStats) {
                        Text("Statistics")
                    }
                    TextButton(onClick = onLogout) {
                        Text("Log out")
                    }
                }
            }

            GameScreen(
                uiState = gameUiState,
                onCloseStats = onCloseStats,
                modifier = Modifier.weight(1f)
            )

            WordleKeyboard(
                keyStates = gameUiState.keyStates,
                onKeyPress = onKeyPress
            )
        }
    }
}

private const val DEFAULT_DAILY_GUESSES = 6
private const val MAX_CUSTOM_GUESSES_DEFAULT = 6
