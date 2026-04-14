package com.example.wordle.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wordle.ui.theme.WordleBackground
import com.example.wordle.ui.theme.WordleTheme
import com.example.wordle.ui.theme.WordleTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WordleBackground,
                    titleContentColor = WordleTitle
                )
            )
        },
        containerColor = WordleBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Game Settings",
                style = MaterialTheme.typography.titleMedium,
                color = WordleTitle,
                fontWeight = FontWeight.Bold
            )
            
            SettingsToggle(label = "Hard Mode", description = "Any revealed hints must be used in subsequent guesses")
            SettingsToggle(label = "Dark Theme", description = "Toggle between light and dark themes")
            SettingsToggle(label = "High Contrast Mode", description = "For improved color vision")

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Feedback & Support",
                style = MaterialTheme.typography.titleMedium,
                color = WordleTitle,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(onClick = { /* TODO */ }) {
                Text("Help")
            }
            TextButton(onClick = { /* TODO */ }) {
                Text("Contact Support")
            }
        }
    }
}

@Composable
fun SettingsToggle(
    label: String,
    description: String,
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = WordleTitle.copy(alpha = 0.6f))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    WordleTheme {
        SettingsScreen(onBack = {})
    }
}
