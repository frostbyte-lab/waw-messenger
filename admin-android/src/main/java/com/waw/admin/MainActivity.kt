package com.waw.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.ScreenShare
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ControlMobileScreen() }
    }
}

private val Ink = Color(0xFF07110F)
private val Panel = Color(0xFF10231F)
private val Green = Color(0xFF20D486)
private val Muted = Color(0xFF9AB5AC)

@Composable
private fun ControlMobileScreen() {
    val client = remember { AdminRelayClient() }
    val status by client.status.collectAsState()
    val frame by client.frame.collectAsState()
    var relayUrl by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    DisposableEffect(Unit) { onDispose { client.disconnect() } }

    Surface(color = Ink) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF102A23), Ink, Ink)))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(46.dp).background(Green, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Rounded.ScreenShare, null, tint = Ink, modifier = Modifier.size(26.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("WAW Control", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Mobile admin console", color = Muted, fontSize = 13.sp)
                    }
                    StatusPill(status)
                }

                Text("Kendalikan perangkat dengan persetujuan", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
                Text("Masukkan OTP yang tampil di WAW Remote User. Koneksi hanya aktif setelah User menyetujui sesi.", color = Muted, lineHeight = 20.sp)

                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Label("SECURE RELAY")
                        OutlinedTextField(
                            value = relayUrl,
                            onValueChange = { relayUrl = it },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Rounded.Link, null, tint = Green) },
                            label = { Text("Relay URL wss://") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = otp,
                            onValueChange = { value -> otp = value.filter(Char::isDigit).take(6) },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Rounded.Key, null, tint = Green) },
                            label = { Text("OTP 6 digit") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { client.connect(relayUrl.trim(), otp) },
                            enabled = relayUrl.startsWith("wss://") && otp.length == 6 && status != "CONNECTING",
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink),
                            shape = RoundedCornerShape(15.dp)
                        ) { Icon(Icons.Rounded.PhoneAndroid, null); Spacer(Modifier.width(8.dp)); Text("Hubungkan User", fontWeight = FontWeight.Bold) }
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1B18)), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ScreenShare, null, tint = Green)
                            Spacer(Modifier.width(8.dp))
                            Text("Live preview", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Text(status.replace('_', ' '), color = Muted, fontSize = 11.sp)
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth().height(230.dp)
                                .border(1.dp, Color(0xFF27574A), RoundedCornerShape(16.dp))
                                .background(Color(0xFF06100E), RoundedCornerShape(16.dp))
                                .pointerInput(status) { detectTapGestures { offset -> client.sendTouch(offset.x, offset.y) } },
                            contentAlignment = Alignment.Center
                        ) {
                            if (frame != null) androidx.compose.foundation.Image(frame!!.asImageBitmap(), "Remote screen", Modifier.fillMaxSize())
                            else Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Rounded.Lock, null, tint = Muted, modifier = Modifier.size(30.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(if (status == "WAITING_FOR_USER_APPROVAL") "Menunggu persetujuan User" else "Belum ada screen preview", color = Muted)
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { client.sendKey(4) }, enabled = status == "CONNECTED", modifier = Modifier.weight(1f)) { Icon(Icons.Rounded.Key, null); Spacer(Modifier.width(4.dp)); Text("Back") }
                    Button(
                        onClick = { client.disconnect() },
                        enabled = status != "DISCONNECTED",
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C2C3D)),
                        modifier = Modifier.weight(1f)
                    ) { Icon(Icons.Rounded.PowerSettingsNew, null); Spacer(Modifier.width(4.dp)); Text("Putuskan") }
                }
            }
        }
    }
}

@Composable
private fun Label(text: String) = Text(text, color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)

@Composable
private fun StatusPill(status: String) {
    val online = status == "CONNECTED"
    Box(Modifier.background(if (online) Color(0x3320D486) else Color(0x332F4D45), RoundedCornerShape(50))) {
        Text(if (online) "ONLINE" else status.replace('_', ' '), color = if (online) Green else Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp))
    }
}
