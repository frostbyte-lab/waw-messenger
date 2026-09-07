package com.waw.messenger

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.waw.messenger.linked.LinkedDeviceWebViewActivity

/** WAW-owned pre-login shell. Account connection is delegated to the official component. */
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.WHITE
        window.navigationBarColor = android.graphics.Color.WHITE
        setContent {
            LoginScreen(onConnectWhatsApp = {
                startActivity(Intent(this, LinkedDeviceWebViewActivity::class.java))
            })
        }
    }
}

private object LoginColors {
    val background = Color(0xFFF7FBFA)
    val surface = Color.White
    val primary = Color(0xFF128C7E)
    val primaryDark = Color(0xFF075E54)
    val blue = Color(0xFF2D8CFF)
    val text = Color(0xFF173B38)
    val muted = Color(0xFF6E8581)
    val border = Color(0xFFE1ECE9)
}

@Composable
private fun LoginScreen(onConnectWhatsApp: () -> Unit) {
    Column(Modifier.fillMaxSize().background(LoginColors.background)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(LoginColors.primary), contentAlignment = Alignment.Center) {
                    Text("W", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("WAW", color = LoginColors.primaryDark, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                        Text("  BUSINESS", color = LoginColors.blue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("WhatsApp Workspace", color = LoginColors.muted, fontSize = 12.sp)
                }
                Text("RESMI", color = LoginColors.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(28.dp))
            Text("Satu ruang untuk chat dan kerja", color = LoginColors.primaryDark, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Text("Hubungkan akun melalui jalur resmi, lalu gunakan pengalaman Workspace WAW yang rapi dan aman.", color = LoginColors.muted, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp))
            Spacer(Modifier.height(22.dp))

            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = LoginColors.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("Sebelum mulai", color = LoginColors.text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    PreLoginRow(Icons.Default.Security, "Privasi terjaga", "WAW tidak meminta password akun Anda.")
                    PreLoginRow(Icons.Default.Link, "Koneksi resmi", "Proses akun dilakukan melalui komponen resmi.")
                    PreLoginRow(Icons.Default.Workspaces, "Workspace terintegrasi", "Chat, file, dokumen, tugas, dan kalender dalam satu shell WAW.")
                }
            }

            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onConnectWhatsApp,
                Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LoginColors.primary, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Link, null)
                Spacer(Modifier.width(10.dp))
                Text("Hubungkan akun resmi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onConnectWhatsApp, Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(15.dp)) {
                Text("Buka koneksi perangkat", fontSize = 14.sp, color = LoginColors.primaryDark)
            }
            Text("Anda akan menyelesaikan autentikasi pada komponen resmi, kemudian kembali ke WAW.", color = LoginColors.muted, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 14.dp))
            Text("WAW Workspace • UI milik WAW • data mengikuti izin resmi", color = LoginColors.muted, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 22.dp))
        }
    }
}

@Composable
private fun PreLoginRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFE6F4F1)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = LoginColors.primary, modifier = Modifier.size(19.dp))
        }
        Column(Modifier.padding(start = 11.dp)) {
            Text(title, color = LoginColors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = LoginColors.muted, fontSize = 12.sp)
        }
    }
}
