package com.example.wordle.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wordle.ui.theme.WordleBackground
import com.example.wordle.ui.theme.WordleTextSecondary
import com.example.wordle.ui.theme.WordleTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }

    // Show Snackbars for success/error messages
    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            // Real apps use SnackbarHost, but we'll keep it simple for now
            println(uiState.message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WordleBackground,
                    titleContentColor = WordleTitle,
                    navigationIconContentColor = WordleTitle
                )
            )
        },
        containerColor = WordleBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(100.dp), tint = WordleTitle)
            Spacer(modifier = Modifier.height(16.dp))

            Text(uiState.username, style = MaterialTheme.typography.headlineMedium, color = WordleTitle, fontWeight = FontWeight.Black)
            Text(uiState.email, style = MaterialTheme.typography.bodyLarge, color = WordleTextSecondary)

            Spacer(modifier = Modifier.height(48.dp))

            Button(onClick = { showPasswordDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Change Password")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text("Logout")
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { showDeleteDialog = true }) {
                Text("Delete Account", color = MaterialTheme.colorScheme.error)
            }
        }

        // Change Password Dialog
        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showPasswordDialog = false },
                title = { Text("New Password") },
                text = {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.changePassword(newPassword)
                        showPasswordDialog = false
                    }) { Text("Update") }
                },
                dismissButton = {
                    TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Delete Account Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Account") },
                text = { Text("Are you sure? This action cannot be undone and you will lose all game stats.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            viewModel.deleteAccount(onSuccess = onLogout)
                            showDeleteDialog = false
                        }
                    ) { Text("Delete Forever") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}