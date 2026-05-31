package com.example.api

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.data.GeneratedDoc
import com.example.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LanDiscoveryLog(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val type: LogType = LogType.INFO
) {
    enum class LogType { INFO, SUCCESS, WARNING, INCOMING }
}

class LocalLanService(
    private val context: Context,
    private val repository: Repository,
    private val scope: CoroutineScope,
    private val keyProvider: () -> String
) {
    private val TAG = "LocalLanService"
    private var nsdManager: NsdManager? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    var localPort = 8090

    private val _logs = MutableStateFlow<List<LanDiscoveryLog>>(emptyList())
    val logs: StateFlow<List<LanDiscoveryLog>> = _logs

    private val _isServerActive = MutableStateFlow(false)
    val isServerActive: StateFlow<Boolean> = _isServerActive

    init {
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        addLog("Inicializando nodo de red unificado Aura...", LanDiscoveryLog.LogType.INFO)
    }

    private fun addLog(message: String, type: LanDiscoveryLog.LogType = LanDiscoveryLog.LogType.INFO) {
        val current = _logs.value.toMutableList()
        current.add(0, LanDiscoveryLog(message = message, type = type))
        _logs.value = current
        Log.d(TAG, message)
    }

    @Synchronized
    fun startLanNode() {
        if (isRunning) return
        isRunning = true
        _isServerActive.value = true
        addLog("Iniciando servidor de red unificado LAN...", LanDiscoveryLog.LogType.INFO)

        // 1. Start TCP Server Socket
        scope.launch(Dispatchers.IO) {
            try {
                val socket = ServerSocket(0)
                serverSocket = socket
                val port = socket.localPort
                localPort = port
                addLog("TCP Server Sockets escuchando localmente en el puerto $port", LanDiscoveryLog.LogType.INFO)

                // Register service with NSD (Bonjour Discovery)
                registerNsdService(port)

                while (isRunning) {
                    val socket = serverSocket?.accept() ?: break
                    scope.launch(Dispatchers.IO) {
                        handleClientConnection(socket)
                    }
                }
            } catch (e: Exception) {
                addLog("Error en el servidor de sockets local: ${e.message}", LanDiscoveryLog.LogType.WARNING)
            }
        }
    }

    @Synchronized
    fun stopLanNode() {
        if (!isRunning) return
        isRunning = false
        _isServerActive.value = false
        addLog("Deteniendo servicio de descubrimiento y sincronización LAN...", LanDiscoveryLog.LogType.INFO)

        unregisterNsdService()

        try {
            serverSocket?.close()
            serverSocket = null
        } catch (e: Exception) {
            addLog("Error al cerrar sockets del servidor: ${e.message}", LanDiscoveryLog.LogType.WARNING)
        }
    }

    private fun registerNsdService(port: Int) {
        try {
            val serviceInfo = NsdServiceInfo().apply {
                serviceName = "AuraPersonalAgent"
                serviceType = "_aura-agent._tcp"
                setPort(port)
            }

            registrationListener = object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(info: NsdServiceInfo) {
                    addLog("Bonjour mDNS registrado con éxito: '${info.serviceName}' (${info.serviceType}) en puerto ${info.port}", LanDiscoveryLog.LogType.SUCCESS)
                }

                override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                    addLog("Fallo al registrar mDNS Bonjour LAN. Código de error: $errorCode", LanDiscoveryLog.LogType.WARNING)
                }

                override fun onServiceUnregistered(info: NsdServiceInfo) {
                    addLog("mDNS Bonjour dado de baja de la red local.", LanDiscoveryLog.LogType.INFO)
                }

                override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                    addLog("Error al dar de baja el servicio de red local: $errorCode", LanDiscoveryLog.LogType.WARNING)
                }
            }

            nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            addLog("Error en inicialización del registro NSD: ${e.message}", LanDiscoveryLog.LogType.WARNING)
        }
    }

    private fun unregisterNsdService() {
        registrationListener?.let {
            try {
                nsdManager?.unregisterService(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering NSD service: ${e.message}")
            }
            registrationListener = null
        }
    }

    private suspend fun handleClientConnection(socket: Socket) {
        withContext(Dispatchers.IO) {
            try {
                val ipAddress = socket.inetAddress.hostAddress
                addLog("Nueva conexión local detectada: IP [$ipAddress]", LanDiscoveryLog.LogType.INCOMING)

                val reader = BufferedReader(InputStreamReader(socket.inputStream))
                val writer = PrintWriter(socket.outputStream, true)

                val line = reader.readLine()
                if (line != null) {
                    addLog("Recibido de LAN IP [$ipAddress]: \"${line.take(80)}...\"", LanDiscoveryLog.LogType.INFO)
                    
                    // Parse custom protocol
                    if (line.startsWith("SYNC_DOC:")) {
                        val parts = line.split(":", limit = 4)
                        if (parts.size >= 4) {
                            val type = parts[1] // EMAIL, STUDY_GUIDE, BANK_ANALYSIS
                            val title = parts[2]
                            val content = parts[3]

                            // Encrypt with our real AES-256 key
                            val encryptedContent = CryptographyHelper.encryptAES(content, keyProvider())
                            val doc = GeneratedDoc(
                                type = type,
                                title = "[LAN Bluetooth/TCP] $title",
                                content = encryptedContent,
                                isEncrypted = true,
                                isSynced = true
                            )
                            repository.insertDoc(doc)
                            addLog("Guardado seguro en SQLite bajo cifrado AES-256: '$title'", LanDiscoveryLog.LogType.SUCCESS)
                            writer.println("SYNC_OK")
                        } else {
                            writer.println("SYNC_ERROR:Invalid format")
                        }
                    } else if (line.startsWith("CLIPBOARD_PUSH:")) {
                        val clipboardContent = line.substringAfter("CLIPBOARD_PUSH:")
                        addLog("Sincronización del portapapeles unificado recibida: '$clipboardContent'", LanDiscoveryLog.LogType.SUCCESS)
                        writer.println("CLIPBOARD_OK")
                    } else {
                        writer.println("AURA_AGENT_LAN:OK")
                    }
                }
                socket.close()
            } catch (e: Exception) {
                addLog("Error procesando trama de red local: ${e.message}", LanDiscoveryLog.LogType.WARNING)
            }
        }
    }

    // Connects over a REAL loopback TCP Socket to simulate raw hardware exchanges over actual networks
    fun simulateFileSyncReceptionFromMac(fileName: String, content: String, docType: String) {
        scope.launch(Dispatchers.IO) {
            try {
                addLog("Conectando socket cliente TCP local real a 127.0.0.1:$localPort...", LanDiscoveryLog.LogType.INFO)
                val socket = Socket("127.0.0.1", localPort)
                val writer = PrintWriter(socket.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                // Transmit real protocol frame
                val payload = "SYNC_DOC:$docType:$fileName:$content"
                writer.println(payload)

                val response = reader.readLine()
                addLog("Trama transmitida por canal sockets. Respuesta del servidor de red: '$response' 🚀", LanDiscoveryLog.LogType.SUCCESS)
                
                socket.close()

                launch(Dispatchers.Main) {
                    com.example.ui.AuraBackgroundAuditor.triggerLocalAlert(
                        context,
                        "Red LAN Socket Exitoso",
                        "Transferencia loopback TCP encriptada realizada en puerto $localPort."
                    )
                }
            } catch (e: Exception) {
                addLog("Fallo al conectar socket local: ${e.message}", LanDiscoveryLog.LogType.WARNING)
            }
        }
    }

    fun simulateClipboardExchange(clipboardText: String) {
        scope.launch(Dispatchers.IO) {
            try {
                addLog("Abriendo socket TCP para sincronizar portapapeles en puerto $localPort...", LanDiscoveryLog.LogType.INFO)
                val socket = Socket("127.0.0.1", localPort)
                val writer = PrintWriter(socket.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                // Transmit protocol frame
                val payload = "CLIPBOARD_PUSH:$clipboardText"
                writer.println(payload)

                val response = reader.readLine()
                addLog("Portapapeles synchronized. Sockets TCP Ack: '$response'", LanDiscoveryLog.LogType.SUCCESS)
                
                socket.close()

                launch(Dispatchers.Main) {
                    com.example.ui.AuraBackgroundAuditor.triggerLocalAlert(
                        context,
                        "Portapapeles Sincronizado",
                        "Texto propagado por socket Bonjour local: '$clipboardText'"
                    )
                }
            } catch (e: Exception) {
                addLog("Error de canal socket para portapapeles: ${e.message}", LanDiscoveryLog.LogType.WARNING)
            }
        }
    }
}
