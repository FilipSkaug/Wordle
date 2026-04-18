package com.example.wordle.ui.settings

import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isHighContrast = MutableStateFlow(false)
    val isHighContrast: StateFlow<Boolean> = _isHighContrast.asStateFlow()

    fun initializeThemeFromSystem(context: Context) {
        val isDarkMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        _isDarkTheme.value = isDarkMode
    }

    fun toggleDarkTheme(enabled: Boolean) {
        _isDarkTheme.update { enabled }
        if (enabled) {
            _isHighContrast.update { false }
        }
    }

    fun toggleHighContrast(enabled: Boolean) {
        _isHighContrast.update { enabled }
        if (enabled) {
            _isDarkTheme.update { false }
        }
    }
}
