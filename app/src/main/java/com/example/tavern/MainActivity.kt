package com.example.tavern

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.tavern.ui.TavernApp // Ensure this import is here
import com.example.tavern.ui.theme.TavernTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Makes the app look modern with a transparent status bar
        setContent {
            // Kita buat status "Loading" menggunakan state
            val statusMemuat = remember { mutableStateOf("Memulai Aplikasi...") }
            val context = LocalContext.current

            // Ini adalah COROUTINE di JETPACK COMPOSE
            // LaunchedEffect akan berjalan otomatis saat aplikasi dibuka
            LaunchedEffect(Unit) {
                // Step 1: Simulasi cek koneksi database (Thread Background)
                statusMemuat.value = "Menghubungkan ke Tavern Database..."
                delay(2000) // Tunggu 2 detik

                // Step 2: Simulasi ambil data User (seperti UserEntity di folder datamu)
                statusMemuat.value = "Mengambil data User..."
                delay(2000) // Tunggu 2 detik lagi

                // Step 3: Selesai! Munculkan notifikasi singkat
                statusMemuat.value = "Selamat Datang di Tavern!"
                Toast.makeText(context, "Data Berhasil Dimuat via Coroutine", Toast.LENGTH_LONG).show()
            }
            TavernTheme {
                // We replaced the default "Greeting" with your main app screen
              TavernApp()
            }
        }
    }
}