package com.example.tavern

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tavern.ui.TavernApp // Ensure this import is here
import com.example.tavern.ui.theme.TavernTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Makes the app look modern with a transparent status bar
        setContent {
            TavernTheme {
                // We replaced the default "Greeting" with your main app screen
                TavernApp()
            }
        }
    }
}