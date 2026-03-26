package com.example.wordle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement // Added import
import androidx.compose.foundation.layout.Column      // Added import
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.wordle.ui.WordleKeyboard          // Added import since it's in the .ui folder now
import com.example.wordle.ui.theme.WordleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Use a Column to push the keyboard to the bottom of the screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Bottom // Pushes keyboard to bottom
                    ) {
                        WordleKeyboard(
                            onKeyPress = { pressedKey ->
                                // Here is where you will handle the key press later!
                                println("Key pressed: $pressedKey")
                            }
                        )
                    }
                }
            }
        }
    }
} // <- Added this missing closing brace for the MainActivity class!