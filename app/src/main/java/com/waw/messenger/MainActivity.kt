package com.waw.messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WawApp() }
    }
}

@Composable
fun WawApp() {
    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("WAW") }) }) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("WAW Messenger", style = MaterialTheme.typography.headlineMedium)
                Text("Fondasi aplikasi siap. Tahap berikutnya: Chat real-time.", modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
