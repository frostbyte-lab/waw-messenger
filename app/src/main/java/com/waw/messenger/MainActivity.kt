package com.waw.messenger

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.waw.messenger.auth.SavedAccount
import com.waw.messenger.security.BiometricGate
import com.waw.messenger.ui.WawChatShell
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WawApp() }
    }
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

    fun requestUnlock() {
        if (!authenticated) return
        if (activity == null) return
        if (!biometric.canAuthenticate()) {
            unlocked = true
            return
        }
        biometric.authenticate(activity) { success -> unlocked = success }
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
            !authenticated -> AuthScreen {
                authenticated = true
                unlocked = false
            }
            !unlocked -> LockedScreen(
                onUnlock = ::requestUnlock,
                accounts = repository.savedAccounts(),
                onSwitchAccount = { accountId ->
                    if (repository.switchAccount(accountId)) {
                        unlocked = false
                    }
                }
            )
            else -> WawChatShell(onLogout = {
                scope.launch {
                    repository.logout()
                    authenticated = false
                    unlocked = false
                }
            })
        }
    }
}

@Composable
private fun LockedScreen(
    onUnlock: () -> Unit,
    accounts: List<SavedAccount>,
    onSwitchAccount: (String) -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("WAW terkunci", style = MaterialTheme.typography.headlineSmall)
        Text("Gunakan fingerprint atau kunci perangkat untuk melanjutkan.", modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
        Button(onClick = onUnlock) { Text("Buka WAW") }
        if (accounts.size > 1) {
            Text("Akun tersimpan", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 28.dp, bottom = 8.dp))
            accounts.forEach { account ->
                Button(onClick = { onSwitchAccount(account.id) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("${account.displayName} (@${account.username})")
                }
            }
        }
    }
}
