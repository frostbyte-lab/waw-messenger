package com.waw.messenger

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.waw.messenger.auth.AuthRepository
import com.waw.messenger.auth.AuthScreen
import com.waw.messenger.auth.AuthUser
import com.waw.messenger.auth.SavedAccount
import com.waw.messenger.chat.LiveChatRepository
import com.waw.messenger.security.BiometricGate
import com.waw.messenger.ui.WawChatShell
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { WawApp() } }
}

@Composable
fun WawApp() {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val repository = remember(context) { AuthRepository(context) }
    val biometric = remember(context) { BiometricGate(context) }
    val scope = rememberCoroutineScope()
    var authenticated by remember { mutableStateOf(repository.hasSession()) }
    var unlocked by remember { mutableStateOf(false) }
    var user by remember { mutableStateOf<AuthUser?>(null) }

    fun requestUnlock() {
        if (!authenticated || activity == null) return
        if (!biometric.canAuthenticate()) { unlocked = true; return }
        biometric.authenticate(activity) { success -> unlocked = success }
    }

    LaunchedEffect(authenticated, unlocked) {
        if (authenticated && unlocked && user == null) user = runCatching { repository.me() }.getOrNull()
        if (!authenticated) user = null
    }

    DisposableEffect(activity, authenticated) {
        val lifecycle = activity?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START && authenticated && !unlocked) requestUnlock()
            if (event == Lifecycle.Event.ON_STOP) unlocked = false
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    MaterialTheme {
        when {
            !authenticated -> AuthScreen { authenticated = true; unlocked = false }
            !unlocked -> LockedScreen(::requestUnlock, repository.savedAccounts(), { id -> if (repository.switchAccount(id)) { user = null; unlocked = false } }, repository::removeSavedAccount)
            user == null -> LoadingScreen()
            else -> WawChatShell(user!!.id, user!!.displayName, repository.baseUrl, repository.token().orEmpty()) { scope.launch { repository.logout(); authenticated = false; unlocked = false } }
        }
    }
}

@Composable private fun LoadingScreen() {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(); Text("Memuat akun…", modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun LockedScreen(onUnlock: () -> Unit, accounts: List<SavedAccount>, onSwitchAccount: (String) -> Unit, onRemoveAccount: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("WAW terkunci", style = MaterialTheme.typography.headlineSmall)
        Text("Gunakan fingerprint atau kunci perangkat untuk melanjutkan.", modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
        androidx.compose.material3.Button(onClick = onUnlock) { Text("Buka WAW") }
        if (accounts.size > 1) {
            Text("Akun tersimpan", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 28.dp, bottom = 8.dp))
            accounts.forEach { account ->
                androidx.compose.material3.Button(onClick = { onSwitchAccount(account.id) }) { Text("${account.displayName} (@${account.username})") }
                androidx.compose.material3.TextButton(onClick = { onRemoveAccount(account.id) }) { Text("Hapus session tersimpan") }
            }
        }
    }
}
