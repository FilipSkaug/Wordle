package com.example.wordle.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordle.BuildConfig
import com.example.wordle.ui.theme.WordleBackground
import com.example.wordle.ui.theme.WordlePrimaryAction
import com.example.wordle.ui.theme.WordleSurface
import com.example.wordle.ui.theme.WordleTitle

@Composable
fun MenuScreen(
    isAuthenticated: Boolean,
    onProfileClick: () -> Unit,
    onPlayDaily: () -> Unit,
    onPlayCustom: () -> Unit,
    onLoginClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WordleBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "WORDLE",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            color = WordleTitle,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(64.dp))

        MenuButton(
            text = "Daily Wordle",
            icon = Icons.Default.PlayArrow,
            onClick = onPlayDaily,
            containerColor = WordlePrimaryAction,
            contentColor = WordleSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(
            text = "Custom Game",
            icon = Icons.Default.Edit,
            onClick = onPlayCustom,
            containerColor = WordleSurface,
            contentColor = WordleTitle
        )

        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(
            text = "Custom Wordle",
            icon = Icons.Default.PlayArrow,
            onClick = onPlayCustom,
            containerColor = WordlePrimaryAction,
            contentColor = WordleSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            MenuButton(
                text = "Statistics",
                icon = Icons.Default.BarChart,
                onClick = onStatsClick,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            MenuButton(
                text = "Settings",
                icon = Icons.Default.Settings,
                onClick = onSettingsClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        if (isAuthenticated) {
            MenuButton(
                text = "Profile",
                icon = Icons.Default.AccountCircle,
                onClick = onProfileClick
            )
        } else {
            MenuButton(
                text = "Login / Sign up",
                icon = Icons.Default.AccountCircle,
                onClick = onLoginClick
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        TextButton(onClick = { /* TODO: How to play */ }) {
            Text(
                text = "How to play",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = WordleTitle.copy(alpha = 0.7f)
            )
        }
        
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = WordleTitle.copy(alpha = 0.4f),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = WordleSurface,
    contentColor: androidx.compose.ui.graphics.Color = WordleTitle
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MenuScreenPreview() {
    MenuScreen(
        isAuthenticated = false,
        onProfileClick = {},
        onPlayDaily = {},
        onPlayCustom = {},
        onLoginClick = {},
        onSettingsClick = {},
        onStatsClick = {}
    )
}

