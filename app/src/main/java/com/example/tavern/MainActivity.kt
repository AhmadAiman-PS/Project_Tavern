package com.example.tavern

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.* // Penting untuk mutableStateOf dan LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.tavern.ui.TavernApp // Ensure this import is here
import com.example.tavern.ui.theme.TavernTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Makes the app look modern with a transparent status bar
        setContent {
            TavernTheme {
                // 1. Siapkan variable untuk menampung teks status
                // 'remember' berguna agar tampilan mengingat tulisan ini saat berubah
                var statusTeks by remember { mutableStateOf("Loading... Harap Tunggu 3 Detik") }

                // 2. Jalankan Coroutine
                // LaunchedEffect(Unit) artinya: Jalankan blok kode ini SATU KALI saat layar muncul
                LaunchedEffect(Unit) {
                    delay(3000) // Tahan/Delay selama 3000ms (3 detik)
                    statusTeks = "Sukses! Data berhasil dimuat (Delay selesai)." // Ubah teks
                }

                // 3. Tampilkan Teks di tengah layar
                Box(
                    modifier = Modifier.fillMaxSize(), // Memenuhi seluruh layar
                    contentAlignment = Alignment.Center // Teks di tengah-tengah
                ) {
                    Text(text = statusTeks)
                }

                // Catatan: Baris 'TavernApp()' aslinya saya hapus dulu supaya kita fokus ke tugas delay
              TavernApp()
            }
        }
    }
}