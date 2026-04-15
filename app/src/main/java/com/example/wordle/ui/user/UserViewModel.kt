package com.example.wordle.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordle.data.auth.AuthRepository
import com.example.wordle.data.auth.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserUiState(
    val username: String = "",
    val email: String = "",
    val message: String? = null // To show success/error messages
)

class UserViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        val user = authRepository.getCurrentUser()
        if (user != null) {
            _uiState.value = UserUiState(
                username = user.username ?: "Unknown",
                email = user.email
            )
        }
    }

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            val result = authRepository.changePassword(newPassword)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(message = "Password updated successfully!")
            } else {
                _uiState.value = _uiState.value.copy(message = "Error updating password. Try logging in again.")
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = authRepository.deleteAccount()
            if (result.isSuccess) {
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(message = "Error deleting account.")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}