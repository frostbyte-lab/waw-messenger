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

/**
 * WAW entry screen.
 *
 * WhatsApp credentials are never collected here. The only account connection
 * path is the official WhatsApp Web linked-device flow opened by MainActivity.
 */
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.rgb(2, 19, 19)

        setContent {
            LoginScreen(
                onConnectWhatsApp = {
                    startActivity(Intent(this, MainActivity::class.java))
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
    val border = Color(0xFF315255)
    val primary = Color(0xFF12E58A)
    val text = Color(0xFFF5FAF9)
    val muted = Color(0xFF9CB5B7)
}

@Composable
private fun LoginScreen(
    onConnectWhatsApp: () -> Unit,
    onGoogleLogin: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF063B32), LoginColors.background),
                    radius = 900f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            WawBrand()

            Spacer(Modifier.height(38.dp))

            Text(
                "Hubungkan WhatsApp Anda",
                color = LoginColors.text,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Gunakan proses resmi WhatsApp untuk menghubungkan perangkat ini.",
                color = LoginColors.muted,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(26.dp))

            InfoCard()

            Spacer(Modifier.height(22.dp))

            Button(
                onClick = onConnectWhatsApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LoginColors.primary,
                    contentColor = Color(0xFF002019)
                )
            ) {
                Icon(Icons.Default.Link, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text("Hubungkan WhatsApp", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }

            Spacer(Modifier.height(18.dp))

            OutlinedButton(
                onClick = onGoogleLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LoginColors.text)
            ) {
                Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Spacer(Modifier.width(10.dp))
                Text("Login Google WAW", fontSize = 15.sp)
            }

            Spacer(Modifier.height(34.dp))
            Text(
                "WAW tidak meminta password WhatsApp, kode verifikasi, atau\nsesi pribadi. Koneksi dilakukan melalui WhatsApp Web resmi.",
                color = LoginColors.muted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun WawBrand() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0xFF12E58A)),
            contentAlignment = Alignment.Center
        ) {
            Text("W", color = Color(0xFF002019), fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.width(10.dp))
        Text("WAW", color = LoginColors.text, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
    }
    Spacer(Modifier.height(6.dp))
    Text("WhatsApp Workspace", color = LoginColors.text, fontSize = 15.sp)
}

@Composable
private fun InfoCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(LoginColors.panel)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null, tint = LoginColors.primary, modifier = Modifier.size(25.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Koneksi resmi & aman", color = LoginColors.text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Tidak ada password WhatsApp yang dikirim ke WAW.", color = LoginColors.muted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("1. Tekan Hubungkan WhatsApp", color = LoginColors.text, fontSize = 13.sp)
        Text("2. QR WhatsApp resmi akan ditampilkan", color = LoginColors.text, fontSize = 13.sp)
        Text("3. Di WhatsApp ponsel: Perangkat tertaut → Tautkan perangkat", color = LoginColors.text, fontSize = 13.sp)
        Text("4. Setelah berhasil, chat WhatsApp tampil di WAW", color = LoginColors.text, fontSize = 13.sp)
    }
}
