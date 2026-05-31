package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.data.ChatMessage
import com.example.data.GeneratedDoc
import com.example.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object AuraBackgroundAuditor {
    private const val CHANNEL_ID = "aura_agent_alerts"
    private const val CHANNEL_NAME = "Alertas de Inteligencia Aura"

    // Set up a Notification Channel in Android Oreo +
    fun setupNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones proactivas de seguridad local de Aura Agent"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // Trigger standard native Android notification compat alert
    fun triggerLocalAlert(context: Context, title: String, message: String) {
        setupNotificationChannel(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        manager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }

    // 24/7 background guard loop that periodically scans the SQLite Room DB to check for double subscription commissions
    fun runContinuousSecurityAuditor(context: Context, repository: Repository, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            while (true) {
                delay(60000) // Scan every 60 seconds for simulated real-time anomalies
                
                // Fetch the latest docs from the database
                val allDocs = repository.allDocs.first()
                val duplicatesFound = checkForDuplicateMismatches(allDocs)
                
                if (duplicatesFound.isNotEmpty()) {
                    for (item in duplicatesFound) {
                        launch(Dispatchers.Main) {
                            triggerLocalAlert(
                                context,
                                "🛡️ Aura: Alerta de Auditoría",
                                "Se ha auditado un cobro sospechoso duplicado en la cuenta de: ${item.title}. Recomendamos revisar de inmediato."
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkForDuplicateMismatches(docs: List<GeneratedDoc>): List<GeneratedDoc> {
        // Find docs containing recurring patterns indicating bank analysis warnings or Canva double payments
        return docs.filter { doc ->
            doc.type == "BANK_ANALYSIS" && 
            doc.content.contains("duplicado", ignoreCase = true) && 
            !doc.title.endsWith("(Auditado)")
        }
    }
}
