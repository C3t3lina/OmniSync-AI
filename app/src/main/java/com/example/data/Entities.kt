package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "General" // "Email", "Study", "Finance", "General"
)

@Entity(tableName = "generated_docs")
data class GeneratedDoc(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "EMAIL", "STUDY_GUIDE", "BANK_ANALYSIS"
    val title: String,
    val content: String,
    val isEncrypted: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "connected_devices")
data class ConnectedDevice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val platform: String, // "Google", "macOS", "iPhone", "Android", "Canva", "Local"
    val status: String, // "Sincronizado", "Pendiente", "Desconectado"
    val lastSyncTime: Long = System.currentTimeMillis(),
    val syncEnabled: Boolean = true
)

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val sender: String,
    val type: String, // "SPAM", "COMERCIAL", "RESERVA", "SISTEMA"
    val resolution: String,
    val transcriptJson: String, // JSON serialization of Dialogues
    val blocked: Boolean
)

@Entity(tableName = "spaced_study_plans")
data class StudyPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topicIndex: Int,
    val title: String,
    val difficulty: String,
    val scheduledDaysJson: String, // e.g. "[1, 3, 7, 14]"
    val statusText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ui_tapping_steps")
data class UiTapStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stepIndex: Int,
    val targetComponent: String,
    val actionText: String,
    val status: String, // "COMPLETO", "REINTENTANDO", "ESPERA_CAPTCHA"
    val requiredAction: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "developer_server_audits")
data class LocalServerAudit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serverUrl: String,
    val isOnline: Boolean,
    val lastLatencyMs: Long,
    val consoleLogsJson: String, // JSON array of logs
    val buggyCode: String? = null,
    val proposedCode: String? = null,
    val fixExplanation: String? = null,
    val testStatus: String = "NEUTRAL", // "NEUTRAL", "SUCCESS", "FAILED"
    val timestamp: Long = System.currentTimeMillis()
)
