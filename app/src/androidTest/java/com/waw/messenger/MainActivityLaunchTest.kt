package com.waw.messenger

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import android.content.Intent
import com.waw.messenger.linked.LinkedDeviceWebViewActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityLaunchTest {
    @Test
    fun mainActivityLaunches() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
            .putExtra(LinkedDeviceWebViewActivity.EXTRA_SKIP_INITIAL_LOAD, true)
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                check(!activity.isFinishing)
            }
        }
    }
}
