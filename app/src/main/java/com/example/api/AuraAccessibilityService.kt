package com.example.api

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.AppDatabase
import com.example.data.UiTapStepEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuraAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AuraAccessibilityService? = null
            private set
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        logAction("SISTEMA", "Servicio inicializado", "Aura Accessibility Service se conectó con éxito al sistema de hardware local.")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Scanners run passively offline on window changes
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            val root = rootInActiveWindow
            if (root != null) {
                serviceScope.launch {
                    scanScreenContentOffline(root)
                }
            }
        }
    }

    override fun onInterrupt() {
        // Interrupted by system
    }

    /**
     * Real screen content scraper using simple DFS on Android elements
     */
    private suspend fun scanScreenContentOffline(rootNode: AccessibilityNodeInfo) {
        val textsOnScreen = mutableListOf<String>()
        dfsTraverseNode(rootNode, textsOnScreen)
        
        // If we notice price info (like "Renfe 45€", "Billete", "Total: 35€") or spam/commercial text,
        // we log them directly into our local Room DB
        val detectedPrices = textsOnScreen.filter { it.contains("€") || it.contains("$") }
        if (detectedPrices.isNotEmpty()) {
            for (priceText in detectedPrices) {
                // If we detect a price drop on-screen, save a log entry to database 
                val cleanPrice = priceText.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
                if (cleanPrice != null) {
                    val db = AppDatabase.getDatabase(applicationContext)
                    db.uiTapStepDao().insertUiTapStep(
                        UiTapStepEntity(
                            stepIndex = 100,
                            targetComponent = "Conciencia Pantalla (Scraper)",
                            actionText = "Precio detectado en pantalla activa: $priceText",
                            status = "COMPLETO",
                            requiredAction = "Verificar si el precio total es menor al límite configurado",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    private fun dfsTraverseNode(node: AccessibilityNodeInfo?, output: MutableList<String>) {
        if (node == null) return
        val text = node.text
        if (text != null && text.isNotEmpty() && text.length < 200) {
            output.add(text.toString())
        }
        for (i in 0 until node.childCount) {
            dfsTraverseNode(node.getChild(i), output)
        }
    }

    /**
     * Programmatic UI Gestures Click implementation - completely real instead of simulation!
     */
    fun performClickOnTargetWithText(textToFind: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(textToFind)
        if (nodes.isNotEmpty()) {
            for (node in nodes) {
                var current: AccessibilityNodeInfo? = node
                while (current != null) {
                    if (current.isClickable) {
                        val success = current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (success) {
                            logAction("TAP", "Click exitoso en el elemento con texto: '$textToFind'", "Estructura interactiva ejecutada.")
                            return true
                        }
                    }
                    current = current.parent
                }
            }
        }
        return false
    }

    /**
     * Programmatic Set Text implementation
     */
    fun performSetTextOnTarget(textToFind: String, contentToInject: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(textToFind)
        if (nodes.isNotEmpty()) {
            for (node in nodes) {
                var current: AccessibilityNodeInfo? = node
                while (current != null) {
                    if (current.isEditable) {
                        val arguments = Bundle()
                        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, contentToInject)
                        val success = current.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                        if (success) {
                            logAction("SET_TEXT", "Inyección de texto exitosa en: '$textToFind' -> '$contentToInject'", "Entrada de interfaz automatizada.")
                            return true
                        }
                    }
                    current = current.parent
                }
            }
        }
        return false
    }

    private fun logAction(type: String, actionText: String, detail: String) {
        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                db.uiTapStepDao().insertUiTapStep(
                    UiTapStepEntity(
                        stepIndex = 1,
                        targetComponent = type,
                        actionText = actionText,
                        status = "COMPLETO",
                        requiredAction = detail,
                        timestamp = java.lang.System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
