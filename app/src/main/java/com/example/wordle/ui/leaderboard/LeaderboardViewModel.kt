package com.example.wordle.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordle.data.leaderboard.LeaderboardEntry
import com.example.wordle.data.leaderboard.LeaderboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val entries: List<LeaderboardEntry> = emptyList(),
    val errorMessage: String? = null
)

class LeaderboardViewModel(
    private val repository: LeaderboardRepository = LeaderboardRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState(isLoading = true))
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = runCatching { repository.fetchTopByMaxStreak() }
            _uiState.value = result.fold(
                onSuccess = { LeaderboardUiState(entries = it, isLoading = false) },
                onFailure = {
                    LeaderboardUiState(
                        isLoading = false,
                        errorMessage = it.message ?: "Could not load leaderboard"
                    )
                }
            )
        }
    }
}
