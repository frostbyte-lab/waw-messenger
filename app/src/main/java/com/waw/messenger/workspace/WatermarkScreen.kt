package com.waw.messenger.workspace

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class WatermarkField(val label: String, val value: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var brandUri by remember { mutableStateOf<Uri?>(null) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var brandPreview by remember { mutableStateOf<Bitmap?>(null) }
    var includeTimestamp by remember { mutableStateOf(true) }
    var includeLocation by remember { mutableStateOf(false) }
    var includeCompass by remember { mutableStateOf(false) }
    var locationText by remember { mutableStateOf("Lokasi belum diizinkan") }
    var compassText by remember { mutableStateOf("Kompas tidak tersedia") }
    var notice by remember { mutableStateOf<String?>(null) }
    val fields = remember { mutableStateListOf(WatermarkField("Nama", ""), WatermarkField("Keterangan", "")) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        sourceUri = uri
        preview = uri?.let { decodeBitmap(context, it) }
        notice = if (preview == null) "Gambar tidak bisa dibaca" else null
    }
    val brandPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        brandUri = uri
        brandPreview = uri?.let { decodeBitmap(context, it) }
    }
    val outputPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        val bitmap = preview
        if (uri != null && bitmap != null) {
            val result = WatermarkRenderer.render(bitmap, fields, includeTimestamp, includeLocation, includeCompass, locationText, compassText, brandPreview)
            val saved = context.contentResolver.openOutputStream(uri)?.use { result.compress(Bitmap.CompressFormat.PNG, 100, it) } == true
            notice = if (saved) "Watermark berhasil disimpan" else "Gagal menyimpan watermark"
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) locationText = readLocation(context) else locationText = "Lokasi tidak diizinkan"
    }
    DisposableEffect(includeCompass) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            override fun onSensorChanged(event: SensorEvent) {
                val rotation = FloatArray(9)
                val orientation = FloatArray(3)
                SensorManager.getRotationMatrixFromVector(rotation, event.values)
                SensorManager.getOrientation(rotation, orientation)
                var degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (degrees < 0) degrees += 360f
                compassText = "${degrees.roundToInt()}° ${compassDirection(degrees)}"
            }
        }
        if (includeCompass && sensor != null) sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("WAW Watermark") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Kembali") } }) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Watermark resmi WAW", color = Color(0xFF0F766E))
            Text("Watermark dapat dipakai langsung di Workspace WAW. Branding WAW dan Made by Frostbyte Tech Ltd selalu ikut serta.")
            OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Image, "Pilih gambar"); Text("  Pilih gambar") }
            preview?.let { Image(it.asImageBitmap(), "Pratinjau", Modifier.fillMaxWidth().height(220.dp)) }
            OutlinedButton(onClick = { brandPicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null); Text("  Tambah logo perusahaan / brand") }
            brandPreview?.let { Image(it.asImageBitmap(), "Logo brand", Modifier.size(72.dp)) }
            Text("Kolom kustom", color = Color(0xFF0F766E))
            fields.forEachIndexed { index, field ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(field.label, { fields[index] = field.copy(label = it) }, Modifier.weight(0.42f), label = { Text("Label") }, singleLine = true)
                    OutlinedTextField(field.value, { fields[index] = field.copy(value = it) }, Modifier.weight(0.48f), label = { Text("Isi") }, singleLine = true)
                    IconButton(onClick = { if (fields.size > 1) fields.removeAt(index) }) { Icon(Icons.Default.Delete, "Hapus kolom") }
                }
            }
            OutlinedButton(onClick = { fields.add(WatermarkField("Label", "")) }) { Icon(Icons.Default.Add, null); Text("  Tambah kolom") }
            MetadataToggle("Timestamp otomatis", includeTimestamp) { includeTimestamp = it }
            MetadataToggle("Lokasi GPS aktual", includeLocation) {
                includeLocation = it
                if (it) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) locationText = readLocation(context)
                    else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            if (includeLocation) Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocationOn, null); Text(locationText) }
            MetadataToggle("Arah kompas", includeCompass) { includeCompass = it }
            if (includeCompass) Text("Arah: $compassText")
            Spacer(Modifier.height(4.dp))
            Button(onClick = { if (preview == null) notice = "Pilih gambar dahulu" else outputPicker.launch("waw-watermark-${System.currentTimeMillis()}.png") }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Save, null); Text("  Simpan watermark") }
            notice?.let { Text(it, color = Color(0xFF0F766E)) }
        }
    }
}

@Composable
private fun MetadataToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked, onCheckedChange); Text(label) }
}

private fun decodeBitmap(context: Context, uri: Uri): Bitmap? = runCatching { context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) }.getOrNull()

private fun readLocation(context: Context): String = runCatching {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val provider = when {
        manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> return "Lokasi provider tidak aktif"
    }
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return "Izin lokasi diperlukan"
    val location = manager.getLastKnownLocation(provider) ?: return "Lokasi belum tersedia"
    "${"%.5f".format(Locale.US, location.latitude)}, ${"%.5f".format(Locale.US, location.longitude)}"
}.getOrElse { "Lokasi tidak tersedia" }

private fun compassDirection(degrees: Float): String = listOf("U", "TL", "T", "TG", "S", "BD", "B", "BL")[(degrees / 45f).roundToInt() % 8]

private object WatermarkRenderer {
    fun render(source: Bitmap, fields: List<WatermarkField>, timestamp: Boolean, location: Boolean, compass: Boolean, locationText: String, compassText: String, brand: Bitmap?): Bitmap {
        val result = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.WHITE; textSize = (result.width * 0.035f).coerceAtLeast(28f); typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); setShadowLayer(5f, 1f, 1f, AndroidColor.BLACK) }
        val x = result.width * 0.04f
        var y = result.height * 0.08f
        fields.filter { it.label.isNotBlank() && it.value.isNotBlank() }.forEach { canvas.drawText("${it.label}: ${it.value}", x, y, paint); y += paint.textSize * 1.35f }
        if (timestamp) { canvas.drawText("Waktu: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}", x, y, paint); y += paint.textSize * 1.35f }
        if (location) { canvas.drawText("Lokasi: $locationText", x, y, paint); y += paint.textSize * 1.35f }
        if (compass) { canvas.drawText("Arah: $compassText", x, y, paint) }
        brand?.let { logo ->
            val size = (result.width * 0.16f).toInt().coerceAtLeast(80)
            val scaled = Bitmap.createScaledBitmap(logo, size, size, true)
            canvas.drawBitmap(scaled, result.width - size - x, result.height - size * 2.5f, paint)
        }
        paint.textSize = (result.width * 0.035f).coerceAtLeast(26f)
        canvas.drawText("WAW", x, result.height - paint.textSize * 2.0f, paint)
        paint.textSize = (result.width * 0.022f).coerceAtLeast(18f)
        canvas.drawText("Made by Frostbyte Tech Ltd", x, result.height - paint.textSize * 0.55f, paint)
        return result
    }
}
