package com.waw.messenger

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.waw.messenger.linked.LinkedDeviceWebViewActivity
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(2, 19, 19)
        window.navigationBarColor = android.graphics.Color.rgb(2, 19, 19)

        setContent {
            var progress by remember { mutableFloatStateOf(0f) }
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(900, easing = LinearOutSlowInEasing),
                label = "splashProgress"
            )

            LaunchedEffect(Unit) {
                progress = 1f
                delay(1100)
                startActivity(Intent(this@SplashActivity, LinkedDeviceWebViewActivity::class.java))
                finish()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF073A31), Color(0xFF021313))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF12E58A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("WAW", color = Color(0xFF002019), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.height(22.dp))
                    Text("WAW", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(5.dp))
                    Text("WhatsApp Workspace", color = Color(0xFF9CB5B7), fontSize = 15.sp)
                    Spacer(Modifier.height(34.dp))
                    Text("Menyiapkan koneksi aman…", color = Color(0xFFB8CFCC), fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .size(width = (180 * animatedProgress.coerceAtLeast(0.01f)).dp, height = 3.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF12E58A))
                    )
                }
            }
        }
    }
}
