package com.example.wordle.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordle.data.auth.AuthRepository
import com.example.wordle.data.auth.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(isAuthenticated = authRepository.getCurrentUser() != null)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username, errorMessage = null) }
    }

    fun showLogin() {
        _uiState.update {
            it.copy(
                isLoginMode = true,
                errorMessage = null,
                password = ""
            )
        }
    }

    fun showSignup() {
        _uiState.update {
            it.copy(
                isLoginMode = false,
                errorMessage = null,
                password = ""
            )
        }
    }

    fun submit() {
        if (_uiState.value.isLoading) return
        if (_uiState.value.isLoginMode) {
            login()
        } else {
            signup()
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }

    private fun login() {
        val state = _uiState.value
        val validationError = validateEmail(state.email) ?: validatePasswordForLogin(state.password)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authRepository.login(
                email = state.email.trim(),
                password = state.password
            )

            result.fold(
                onSuccess = { session ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            errorMessage = null,
                            password = ""
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toFriendlyMessage()
                        )
                    }
                }
            )
        }
    }

    private fun signup() {
        val state = _uiState.value
        val validationError = validateUsername(state.username)
            ?: validateEmail(state.email)
            ?: validatePasswordForSignup(state.password)

        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authRepository.signup(
                username = state.username.trim(),
                email = state.email.trim(),
                password = state.password
            )

            result.fold(
                onSuccess = { session ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            errorMessage = null,
                            password = ""
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toFriendlyMessage()
                        )
                    }
                }
            )
        }
    }

    private fun validateUsername(username: String): String? {
        return if (username.isBlank()) "Username is required" else null
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Enter a valid email address"
            else -> null
        }
    }

    private fun validatePasswordForLogin(password: String): String? {
        return if (password.isBlank()) "Password is required" else null
    }

    private fun validatePasswordForSignup(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }
}

private fun Throwable.toFriendlyMessage(): String {
    val message = message.orEmpty()
    return when {
        "password is invalid" in message.lowercase() -> "Incorrect password"
        "no user record" in message.lowercase() -> "No account found for that email"
        "badly formatted" in message.lowercase() -> "Enter a valid email address"
        "email address is already in use" in message.lowercase() -> "That email is already registered"
        "network error" in message.lowercase() -> "Network error. Check your connection and try again"
        else -> "Authentication failed. Please try again"
    }
}
