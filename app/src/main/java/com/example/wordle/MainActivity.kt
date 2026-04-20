package com.example.wordle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wordle.data.WordProvider
import com.example.wordle.data.daily.DailyPlayRepository
import com.example.wordle.data.stats.FirebaseUserStatsRemoteSync
import com.example.wordle.data.stats.SharedPreferencesStatsRepository

import com.example.wordle.ui.auth.AuthScreen
import com.example.wordle.ui.auth.AuthViewModel
import com.example.wordle.ui.game.GameScreen
import com.example.wordle.ui.game.GameViewModel
import com.example.wordle.ui.game.GameViewModelFactory
import com.example.wordle.ui.leaderboard.LeaderboardScreen
import com.example.wordle.ui.leaderboard.LeaderboardViewModel
import com.example.wordle.ui.menu.CustomGameSetupScreen
import com.example.wordle.ui.menu.MenuScreen
import com.example.wordle.ui.settings.SettingsScreen
import com.example.wordle.ui.settings.SettingsViewModel
import com.example.wordle.ui.theme.WordleTheme
import com.example.wordle.ui.user.UserScreen

enum class Screen {
    Menu, Game, Auth, Settings, User, CustomSetup, Leaderboard
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel = viewModel<SettingsViewModel>()

            // Initialize theme from system setting on first app launch
            LaunchedEffect(Unit) {
                settingsViewModel.initializeThemeFromSystem(this@MainActivity)
            }

            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsStateWithLifecycle()
            val isHighContrast by settingsViewModel.isHighContrast.collectAsStateWithLifecycle()

            WordleTheme(
                darkTheme = isDarkTheme,
                highContrast = isHighContrast
            ) {
                val authViewModel = viewModel<AuthViewModel>()
                val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

                var currentScreen by rememberSaveable { mutableStateOf(Screen.Menu) }
                var selectedCustomGuesses by rememberSaveable { mutableIntStateOf(MAX_CUSTOM_GUESSES_DEFAULT) }

                val statsRepository = remember { SharedPreferencesStatsRepository(applicationContext) }
                val wordProvider = remember { WordProvider() }
                val dailyPlayRepository = remember { DailyPlayRepository(applicationContext) }
                val userStatsRemoteSync = remember { FirebaseUserStatsRemoteSync() }
                val gameViewModel = viewModel<GameViewModel>(
                    factory = GameViewModelFactory(
                        statsRepository = statsRepository,
                        wordProvider = wordProvider,
                        dailyPlayRepository = dailyPlayRepository,
                        userStatsRemoteSync = userStatsRemoteSync
                    )
                )
                val gameUiState by gameViewModel.uiState.collectAsStateWithLifecycle()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        Screen.Menu -> MenuScreen(
                            isAuthenticated = authUiState.isAuthenticated,
                            hasPlayedDaily = dailyPlayRepository.hasPlayedTodayUtc(),
                            onProfileClick = { currentScreen = Screen.User },
                            onLeaderboardClick = { currentScreen = Screen.Leaderboard },
                            onPlayDaily = {
                                gameViewModel.startDailyGame()
                                currentScreen = Screen.Game
                            },
                            onPlayCustom = { currentScreen = Screen.CustomSetup },
                            onLoginClick = { currentScreen = Screen.Auth },
                            onSettingsClick = { currentScreen = Screen.Settings },
                            onStatsClick = { gameViewModel.onOpenStats() }
                        )

                        Screen.Game -> {
                            BackHandler {
                                gameViewModel.onCloseStats()
                                gameViewModel.onLeaveGameScreen()
                                currentScreen = Screen.Menu
                            }

                            GameScreen(
                                uiState = gameUiState,
                                onKeyPress = gameViewModel::onKeyPress,
                                onOpenStats = gameViewModel::onOpenStats,
                                onCloseStats = gameViewModel::onCloseStats,
                                onStartCustomDefault = {
                                    gameViewModel.startDailyGame()
                                },
                                onBack = {
                                    gameViewModel.onCloseStats()
                                    gameViewModel.onLeaveGameScreen()
                                    currentScreen = Screen.Menu
                                }
                            )
                        }

                        Screen.CustomSetup -> {
                            BackHandler { currentScreen = Screen.Menu }
                            CustomGameSetupScreen(
                                selectedGuesses = selectedCustomGuesses,
                                onGuessesChanged = { selectedCustomGuesses = it },
                                onStartGame = { word, validate, hard ->
                                    gameViewModel.startNewCustomGame(
                                        maxGuesses = selectedCustomGuesses,
                                        targetWord = word,
                                        validateWords = validate,
                                        hardMode = hard
                                    )
                                    currentScreen = Screen.Game
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
                            SettingsScreen(
                                onBack = { currentScreen = Screen.Menu },
                                isDarkTheme = isDarkTheme,
                                isHighContrast = isHighContrast,
                                onDarkThemeChange = settingsViewModel::toggleDarkTheme,
                                onHighContrastChange = settingsViewModel::toggleHighContrast
                            )
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

                        Screen.Leaderboard -> {
                            BackHandler { currentScreen = Screen.Menu }
                            val leaderboardViewModel = viewModel<LeaderboardViewModel>()
                            val leaderboardUiState by leaderboardViewModel.uiState.collectAsStateWithLifecycle()
                            LeaderboardScreen(
                                uiState = leaderboardUiState,
                                onNavigateBack = { currentScreen = Screen.Menu },
                                onRetry = { leaderboardViewModel.refresh() }
                            )
                        }
                    }
                }

                if (gameUiState.isStatsDialogVisible && currentScreen != Screen.Game) {
                    com.example.wordle.ui.stats.StatsDialog(
                        stats = gameUiState.stats,
                        onDismiss = { gameViewModel.onCloseStats() }
                    )
                }
            }
        }
    }
}

private const val MAX_CUSTOM_GUESSES_DEFAULT = 6
