package com.example.tavern

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.* // Import penting untuk State dan Coroutine (LaunchedEffect)
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.tavern.ui.TavernApp // Ensure this import is here
import com.example.tavern.ui.theme.TavernTheme
import kotlinx.coroutines.delay // Import untuk fungsi delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Makes the app look modern with a transparent status bar
        setContent {
            TavernTheme {
                // We replaced the default "Greeting" with your main app screen

                // 1. Buat status "Apakah sedang loading?" (Default: Benar/True)
                var isLoading by remember { mutableStateOf(true) }

                // 2. Jalankan Coroutine sekali saja saat aplikasi dibuka
                LaunchedEffect(Unit) {
                    delay(3000) // Tugas: Delay selama 3000ms (3 detik)
                    isLoading = false // Setelah 3 detik, ubah status loading jadi Salah/False
                }

                // --- PENGATURAN TAMPILAN (UI) ---

                if (isLoading) {
                    // Jika masih loading (3 detik pertama), tampilkan teks ini di tengah layar
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Sedang Memproses... ")
                    }
                } else {
                    // Jika loading selesai, tampilkan aplikasi utama (TavernApp)
                    TavernApp()
                }
            }
        }
    }
}