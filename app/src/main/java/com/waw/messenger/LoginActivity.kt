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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.rgb(2, 16, 16)

        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = LoginColors.background) {
                LoginScreen(
                    onLogin = { username, password ->
                        // UI flow only: no authentication backend exists in the current repo.
                        // Do not persist or transmit these credentials until a real auth service is wired.
                        if (username.isBlank() || password.isBlank()) {
                            Toast.makeText(this, "Username/email dan password wajib diisi", Toast.LENGTH_SHORT).show()
                        } else {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    },
                    onGoogleLogin = {
                        Toast.makeText(this, "Google Login belum dikonfigurasi", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

private object LoginColors {
    val background = Color(0xFF021313)
    val panel = Color(0xFF0A2527)
    val field = Color(0xFF102D30)
    val border = Color(0xFF315255)
    val primary = Color(0xFF12E58A)
    val primaryDark = Color(0xFF00C978)
    val text = Color(0xFFF5FAF9)
    val muted = Color(0xFF9CB5B7)
}

@Composable
private fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onGoogleLogin: () -> Unit,
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

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
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            WawBrand()

            Spacer(Modifier.height(42.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Selamat Datang!",
                    color = LoginColors.text,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Masuk ke akun Anda untuk melanjutkan",
                    color = LoginColors.muted,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(26.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                label = { Text("Username / Email") },
                leadingIcon = { Icon(Icons.Default.PersonOutline, null) },
                colors = loginFieldColors()
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Sembunyikan password" else "Tampilkan password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = loginFieldColors()
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { onLogin(username, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LoginColors.primary,
                    contentColor = Color(0xFF002019)
                )
            ) {
                Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = LoginColors.border)
                Text("  atau  ", color = LoginColors.muted, fontSize = 13.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = LoginColors.border)
            }

            Spacer(Modifier.height(20.dp))

            OutlinedButton(
                onClick = onGoogleLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LoginColors.text),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.width(10.dp))
                Text("Login dengan Google", fontSize = 15.sp)
            }

            Spacer(Modifier.height(38.dp))
            FooterInfo()
        }
    }
}

@Composable
private fun WawBrand() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF16F58C), Color(0xFF00A968)))),
            contentAlignment = Alignment.Center
        ) {
            Text("C", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(5.dp))
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF15DCE0), Color(0xFF087E9C)))),
            contentAlignment = Alignment.Center
        ) {
            Text("W", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Text("WAW", color = LoginColors.text, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
    }
    Spacer(Modifier.height(7.dp))
    Text(
        text = "WhatsApp Workspace",
        color = LoginColors.text,
        fontSize = 16.sp
    )
}

@Composable
private fun FooterInfo() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("WAW", color = LoginColors.text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Versi 1.0.0", color = LoginColors.muted, fontSize = 12.sp)
        }
        Text("✓  Aman • Cepat • Stabil", color = LoginColors.primary, fontSize = 12.sp)
    }
}

@Composable
private fun loginFieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedContainerColor = LoginColors.field,
    unfocusedContainerColor = LoginColors.field,
    focusedBorderColor = LoginColors.primary,
    unfocusedBorderColor = LoginColors.border,
    focusedTextColor = LoginColors.text,
    unfocusedTextColor = LoginColors.text,
    focusedLabelColor = LoginColors.primary,
    unfocusedLabelColor = LoginColors.muted,
    cursorColor = LoginColors.primary,
    focusedLeadingIconColor = LoginColors.primary,
    unfocusedLeadingIconColor = LoginColors.muted,
    focusedTrailingIconColor = LoginColors.primary,
    unfocusedTrailingIconColor = LoginColors.muted
)
