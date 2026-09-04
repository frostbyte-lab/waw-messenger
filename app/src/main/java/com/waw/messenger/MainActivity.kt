package com.waw.messenger

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.waw.messenger.security.BiometricGate
import com.waw.messenger.ui.WawChatShell

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
    val biometric = remember(context) { BiometricGate(context) }
    var unlocked by remember { mutableStateOf(false) }

    fun requestUnlock() {
        if (activity == null) return
        if (!biometric.canAuthenticate()) {
            unlocked = false
            return
        }
        biometric.authenticate(activity) { success -> unlocked = success }
    }

    DisposableEffect(activity) {
        val lifecycle = activity?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START && !unlocked) requestUnlock()
            if (event == Lifecycle.Event.ON_STOP) unlocked = false
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    MaterialTheme {
        if (!unlocked) LockedScreen(onUnlock = ::requestUnlock)
        else WawChatShell()
    }
}

@Composable
private fun LockedScreen(onUnlock: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("WAW terkunci", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Gunakan fingerprint atau kunci perangkat untuk melanjutkan.",
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )
        Button(onClick = onUnlock) { Text("Buka WAW") }
    }
}
