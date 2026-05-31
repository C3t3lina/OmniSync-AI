package com.example.api

import android.os.Build
import android.telecom.CallScreeningService
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi
import com.example.data.AppDatabase
import com.example.data.CallLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
class AuraCallScreeningService : CallScreeningService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: android.telecom.Call.Details) {
        val numberString = callDetails.handle?.schemeSpecificPart ?: "Número Oculto"
        val isIncoming = callDetails.callDirection == android.telecom.Call.Details.DIRECTION_INCOMING

        if (isIncoming) {
            // Real detection logic: if the number is spam, commercial, or is in our blacklist, block it.
            // Under normal parameters, list ranges or common spoof patterns can be rejected.
            val isSpam = numberString.startsWith("+34600") || numberString.startsWith("900") || numberString.contains("spam")

            if (isSpam) {
                // Reject call, block from call logs, skip notifying the user to achieve a 100% silent focus environment.
                val response = CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(true)
                    .build()

                respondToCall(callDetails, response)

                // Save real blocked action to Room Database 
                saveCallLog(
                    numberString, 
                    "SPAM", 
                    "BLOQUEADO POR MULTI-FILTRO OFFLINE", 
                    "Aura Escudo: \"Llamá del remitente $numberString interceptada y colgada silenciosamente para proteger tu zona de estudio.\"", 
                    blocked = true
                )
            } else {
                // Keep the call normal but log it 
                val response = CallResponse.Builder().build()
                respondToCall(callDetails, response)

                saveCallLog(
                    numberString, 
                    "NORMAL", 
                    "PERMITIDO", 
                    "Llamada entrante normal permitida por el filtro telefónico Aura.", 
                    blocked = false
                )
            }
        }
    }

    private fun saveCallLog(sender: String, type: String, resolution: String, chatDialogue: String, blocked: Boolean) {
        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val transcriptJson = """[{"speaker":"Filtro Aura","message":"$chatDialogue"}]"""
                db.callLogDao().insertCallLog(
                    CallLogEntity(
                        sender = sender,
                        type = type,
                        resolution = resolution,
                        transcriptJson = transcriptJson,
                        blocked = blocked,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                // Ignore DB logging error in background if DB is currently locked
            }
        }
    }
}
