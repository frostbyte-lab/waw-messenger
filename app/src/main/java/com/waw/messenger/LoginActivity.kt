package com.waw.messenger

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.waw.messenger.linked.LinkedDeviceWebViewActivity

/** WAW authentication shell. WhatsApp linking is delegated to the official component. */
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.rgb(2, 19, 19)
        setContent {
            LoginScreen(
                onConnectWhatsApp = {
                    startActivity(Intent(this, LinkedDeviceWebViewActivity::class.java))
                },
                onGoogleLogin = {
                    Toast.makeText(this, "Login Google WAW belum dikonfigurasi", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

private object LoginColors {
    val background = Color(0xFF021313)
    val panel = Color(0xFF0A2527)
    val primary = Color(0xFF12E58A)
    val text = Color(0xFFF5FAF9)
    val muted = Color(0xFF9CB5B7)
}

@Composable
private fun LoginScreen(onConnectWhatsApp: () -> Unit, onGoogleLogin: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Brush.radialGradient(colors = listOf(Color(0xFF063B32), LoginColors.background), radius = 900f))
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(52.dp).clip(CircleShape).background(LoginColors.primary), contentAlignment = Alignment.Center) {
                    Text("W", color = Color(0xFF002019), fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.width(10.dp))
                Text("WAW", color = LoginColors.text, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
            }
            Text("WhatsApp Workspace", color = LoginColors.text, fontSize = 15.sp)
            Spacer(Modifier.height(38.dp))
            Text("Hubungkan WhatsApp Anda", color = LoginColors.text, fontSize = 27.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("UI WAW tetap milik WAW. Koneksi akun dilakukan melalui komponen WhatsApp resmi.", color = LoginColors.muted, fontSize = 15.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(26.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(LoginColors.panel).padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = LoginColors.primary, modifier = Modifier.size(25.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Koneksi resmi", color = LoginColors.text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("WAW tidak meminta password WhatsApp.", color = LoginColors.muted, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("1. Hubungkan akun", color = LoginColors.text, fontSize = 13.sp)
                Text("2. QR/login resmi WhatsApp ditampilkan", color = LoginColors.text, fontSize = 13.sp)
                Text("3. Selesaikan Perangkat Tertaut di WhatsApp", color = LoginColors.text, fontSize = 13.sp)
                Text("4. Kembali ke UI WAW", color = LoginColors.text, fontSize = 13.sp)
            }
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = onConnectWhatsApp,
                Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LoginColors.primary, contentColor = Color(0xFF002019))
            ) {
                Icon(Icons.Default.Link, null)
                Spacer(Modifier.width(10.dp))
                Text("Hubungkan WhatsApp", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null)
            }
            Spacer(Modifier.height(18.dp))
            OutlinedButton(onClick = onGoogleLogin, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(15.dp)) {
                Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Spacer(Modifier.width(10.dp))
                Text("Login Google WAW", fontSize = 15.sp, color = LoginColors.text)
            }
        }
    }
}
