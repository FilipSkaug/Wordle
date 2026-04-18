package com.example.wordle.ui.user

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    var showUsernameDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var newUsername by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadUser()
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            Toast.makeText(context, uiState.message, Toast.LENGTH_LONG).show()
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

            Button(onClick = { showUsernameDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Change Username")
            }
            Spacer(modifier = Modifier.height(16.dp))

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

        if (showUsernameDialog) {
            AlertDialog(
                onDismissRequest = { showUsernameDialog = false },
                title = { Text("Change Username") },
                text = {
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        singleLine = true,
                        label = { Text("New Username") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (newUsername.isNotBlank()) {
                            viewModel.changeUsername(newUsername)
                            showUsernameDialog = false
                            newUsername = "" // Clear the input field
                        }
                    }) { Text("Update") }
                },
                dismissButton = {
                    TextButton(onClick = { showUsernameDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showPasswordDialog = false },
                title = { Text("Change Password") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            label = { Text("Current Password") }
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            label = { Text("New Password") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (currentPassword.isNotBlank() && newPassword.isNotBlank()) {
                            viewModel.changePassword(currentPassword, newPassword)
                            showPasswordDialog = false
                            currentPassword = ""
                            newPassword = ""
                        }
                    }) { Text("Update") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showPasswordDialog = false
                        currentPassword = ""
                        newPassword = ""
                    }) { Text("Cancel") }
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Account") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("This action cannot be undone. Please enter your password to confirm.")
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            label = { Text("Confirm Password") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            if (currentPassword.isNotBlank()) {
                                viewModel.deleteAccount(currentPassword, onSuccess = onLogout)
                                showDeleteDialog = false
                                currentPassword = ""
                            }
                        }
                    ) { Text("Delete Forever") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        currentPassword = ""
                    }) { Text("Cancel") }
                }
            )
        }
    }
}