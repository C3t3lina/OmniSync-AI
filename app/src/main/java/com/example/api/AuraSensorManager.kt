package com.example.api

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuraSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Observable States
    private val _ambientLight = MutableStateFlow(0f)
    val ambientLight: StateFlow<Float> = _ambientLight

    private val _phoneStable = MutableStateFlow(true)
    val phoneStable: StateFlow<Boolean> = _phoneStable

    private val _isFacingDown = MutableStateFlow(false)
    val isFacingDown: StateFlow<Boolean> = _isFacingDown

    private val _ringerModeText = MutableStateFlow("Normal")
    val ringerModeText: StateFlow<String> = _ringerModeText

    private val _wifiSsid = MutableStateFlow("Local / Desconectado")
    val wifiSsid: StateFlow<String> = _wifiSsid

    private var lightSensor: Sensor? = null
    private var accelSensor: Sensor? = null

    init {
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        updateNetworkAndRingerInfo()
    }

    fun startListening() {
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        accelSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        updateNetworkAndRingerInfo()
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    fun updateNetworkAndRingerInfo() {
        // Read real audio profile
        _ringerModeText.value = when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> "Silencio Total / No molestar"
            AudioManager.RINGER_MODE_VIBRATE -> "Vibración"
            else -> "Normal (Sonido Activo)"
        }

        // Read real Wi-Fi SSID or Connection Type
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val info = wifiManager.connectionInfo
                    val ssid = info.ssid.replace("\"", "")
                    if (ssid != "<unknown ssid>") {
                        _wifiSsid.value = "Wi-Fi: $ssid"
                    } else {
                        _wifiSsid.value = "Conexión Inalámbrica Local"
                    }
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    _wifiSsid.value = "Datos Celulares Soportados"
                } else {
                    _wifiSsid.value = "Red de Área Local Privada"
                }
            } else {
                _wifiSsid.value = "Modo Desconectado Seguro"
            }
        } catch (e: Exception) {
            _wifiSsid.value = "Desconectado Militar Offline"
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_LIGHT -> {
                _ambientLight.value = event.values[0]
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Check stability (variance in force close to gravity)
                val force = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val isStableValue = Math.abs(force - 9.8f) < 0.35f
                _phoneStable.value = isStableValue

                // Check face down (z negative, near -9.8f when phone face-down on table)
                val isFaceDownValue = z < -8.0f
                _isFacingDown.value = isFaceDownValue
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
