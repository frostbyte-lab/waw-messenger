package com.waw.messenger.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun PasswordRecoveryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { AuthRepository(context) }
    val scope = rememberCoroutineScope()
    var identifier by remember { mutableStateOf("") }
    var resetToken by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var requested by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun request() {
        error = null
        if (identifier.trim().isBlank()) {
            error = "Email atau username wajib diisi"
            return
        }
        loading = true
        scope.launch {
            runCatching { repository.requestPasswordReset(identifier.trim()) }
                .onSuccess { response -> requested = true; message = response; loading = false }
                .onFailure { failure -> error = failure.message ?: "Permintaan gagal"; loading = false }
        }
    }

    fun reset() {
        error = null
        when {
            resetToken.trim().isBlank() -> error = "Token reset wajib diisi"
            password.length < 8 -> error = "Password minimal 8 karakter"
            password != confirmPassword -> error = "Konfirmasi password tidak sama"
            else -> {
                loading = true
                scope.launch {
                    runCatching { repository.resetPassword(resetToken.trim(), password) }
                        .onSuccess { message = "Password berhasil diubah. Silakan kembali dan masuk."; loading = false }
                        .onFailure { failure -> error = failure.message ?: "Reset password gagal"; loading = false }
                }
            }
        }
    }

    Surface(Modifier.fillMaxSize(), color = Color(0xFFF6FBF8)) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Pemulihan password", style = MaterialTheme.typography.headlineSmall)
            Text("Gunakan email atau username akun WAW.", color = Color.Gray, modifier = Modifier.padding(top = 6.dp, bottom = 18.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    if (!requested) {
                        OutlinedTextField(identifier, { identifier = it }, Modifier.fillMaxWidth(), label = { Text("Email atau username") }, singleLine = true)
                        Spacer(Modifier.height(14.dp))
                        Button(onClick = ::request, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                            if (loading) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(20.dp)) else Text("Kirim instruksi")
                        }
                    } else {
                        message?.let { Text(it, color = Color(0xFF166534)) }
                        Text("Masukkan token dari email atau administrator server.", color = Color.Gray, modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
                        OutlinedTextField(resetToken, { resetToken = it }, Modifier.fillMaxWidth(), label = { Text("Token reset") }, singleLine = true)
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password baru") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(confirmPassword, { confirmPassword = it }, Modifier.fillMaxWidth(), label = { Text("Ulangi password baru") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                        Spacer(Modifier.height(14.dp))
                        Button(onClick = ::reset, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                            if (loading) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(20.dp)) else Text("Simpan password baru")
                        }
                    }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp)) }
                }
            }
            TextButton(onClick = onBack, modifier = Modifier.padding(top = 12.dp)) { Text("Kembali ke login") }
        }
    }
}
