package com.waw.messenger.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val AuthGreen = Color(0xFF059669)

@Composable
fun AuthScreen(onAuthenticated: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember(context) { AuthRepository(context) }
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(0) }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var identifier by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun submit() {
        error = null
        val validation = if (tab == 0) {
            when {
                identifier.isBlank() -> "Email atau username wajib diisi"
                password.length < 8 -> "Password minimal 8 karakter"
                else -> null
            }
        } else {
            when {
                username.trim().length < 3 -> "Username minimal 3 karakter"
                !email.contains("@") -> "Email belum valid"
                displayName.trim().isBlank() -> "Nama tampilan wajib diisi"
                password.length < 8 -> "Password minimal 8 karakter"
                else -> null
            }
        }
        if (validation != null) { error = validation; return }
        loading = true
        scope.launch {
            runCatching {
                if (tab == 0) repository.login(identifier.trim(), password)
                else repository.register(username.trim(), email.trim(), password, displayName.trim())
            }.onSuccess {
                loading = false
                onAuthenticated()
            }.onFailure {
                loading = false
                error = it.message ?: "Autentikasi gagal. Periksa koneksi dan data Anda."
            }
        }
    }

    Surface(Modifier.fillMaxSize(), color = Color(0xFFF6FBF8)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))
            Surface(color = AuthGreen, shape = RoundedCornerShape(24.dp)) {
                Text("W", color = Color.White, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp))
            }
            Text("Selamat datang di WAW", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 18.dp))
            Text("Workspace yang aman untuk chat dan kolaborasi.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(22.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp)) {
                    TabRow(selectedTabIndex = tab) {
                        Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Masuk") })
                        Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Daftar") })
                    }
                    Spacer(Modifier.height(18.dp))
                    if (tab == 1) {
                        OutlinedTextField(username, { username = it }, Modifier.fillMaxWidth(), label = { Text("Username") }, singleLine = true)
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(displayName, { displayName = it }, Modifier.fillMaxWidth(), label = { Text("Nama tampilan") }, singleLine = true)
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true)
                    } else {
                        OutlinedTextField(identifier, { identifier = it }, Modifier.fillMaxWidth(), label = { Text("Email atau username") }, singleLine = true)
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                    if (tab == 0) {
                        TextButton(onClick = {
                            if (identifier.trim().isBlank()) {
                                notice = "Masukkan email atau username terlebih dahulu."
                            } else {
                                loading = true
                                scope.launch {
                                    runCatching { repository.requestPasswordReset(identifier.trim()) }
                                        .onSuccess { message -> loading = false; notice = message }
                                        .onFailure { failure -> loading = false; notice = failure.message ?: "Permintaan pemulihan gagal." }
                                }
                            }
                        }, enabled = !loading, modifier = Modifier.align(Alignment.End)) { Text("Lupa password?") }
                    }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp)) }
                    Button(onClick = ::submit, enabled = !loading, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        if (loading) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(20.dp)) else Text(if (tab == 0) "Masuk ke WAW" else "Buat akun")
                    }
                    if (tab == 0) {
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
                            Text("Belum punya akun?", color = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            TextButton(onClick = { tab = 1 }) { Text("Daftar sekarang") }
                        }
                    }
                }
            }
        }
    }
    notice?.let { message ->
        AlertDialog(onDismissRequest = { notice = null }, title = { Text("WAW") }, text = { Text(message) }, confirmButton = { TextButton(onClick = { notice = null }) { Text("Mengerti") } })
    }
}
