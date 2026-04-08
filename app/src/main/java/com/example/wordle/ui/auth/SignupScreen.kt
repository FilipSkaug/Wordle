package com.example.wordle.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wordle.ui.theme.WordleError
import com.example.wordle.ui.theme.WordlePrimaryAction

@Composable
fun SignupScreen(
    uiState: AuthUiState,
    onUsernameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onSwitchToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Sign up with email and password, then we will create your profile in Firestore.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = uiState.username,
            onValueChange = onUsernameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Username") },
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                color = WordleError,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.padding(2.dp)
                )
            } else {
                Text("Sign Up")
            }
        }

        TextButton(
            onClick = onSwitchToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Already have an account? Log in",
                color = WordlePrimaryAction,
                textAlign = TextAlign.Center
            )
        }
    }
}
