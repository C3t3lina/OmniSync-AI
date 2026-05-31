package com.example.api

import android.content.Context
import com.example.data.GeneratedDoc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.BufferedReader
import java.io.StringReader
import java.util.Locale
import kotlin.math.sqrt

// ==========================================
// 1. LOCAL RAG VECTOR EMBEDDING SYSTEM
// ==========================================

data class DocumentChunk(
    val id: Int,
    val docTitle: String,
    val text: String,
    val vector: Map<String, Double>
)

object LocalRagSimulator {
    private val indexedChunks = mutableListOf<DocumentChunk>()

    // Splitting and processing words to simulate TF-IDF/Bindi vector representation
    private fun computeTermFrequencyVector(text: String): Map<String, Double> {
        val words = text.lowercase(Locale.ROOT)
            .replace("[^a-zA-Záéíóúñ0-9\\s]".toRegex(), "")
            .split("\\s+".toRegex())
            .filter { it.length > 3 } // Filter short words/stopwords
        
        val freq = mutableMapOf<String, Int>()
        for (w in words) {
            freq[w] = (freq[w] ?: 0) + 1
        }
        
        val sum = freq.values.sum().toDouble()
        if (sum == 0.0) return emptyMap()
        
        return freq.mapValues { it.value.toDouble() / sum }
    }

    private fun computeCosineSimilarity(vecA: Map<String, Double>, vecB: Map<String, Double>): Double {
        var dotProduct = 0.0
        for ((k, v) in vecA) {
            dotProduct += v * (vecB[k] ?: 0.0)
        }
        val normA = sqrt(vecA.values.sumOf { it * it })
        val normB = sqrt(vecB.values.sumOf { it * it })
        
        if (normA == 0.0 || normB == 0.0) return 0.0
        return dotProduct / (normA * normB)
    }

    fun indexDocument(title: String, fullText: String): Int {
        // Split fullText into chunks of ~300 characters with overlapping
        val paragraphs = fullText.split("\n+")
        var chunkId = indexedChunks.size
        var chunksLoaded = 0

        for (p in paragraphs) {
            if (p.trim().length < 20) continue
            // Split paragraph into chunks of ~60 words
            val words = p.split("\\s+".toRegex())
            val chunkSize = 60
            for (i in words.indices step 40) {
                val end = minOf(i + chunkSize, words.size)
                val chunkText = words.subList(i, end).joinToString(" ")
                if (chunkText.trim().length > 30) {
                    val vec = computeTermFrequencyVector(chunkText)
                    indexedChunks.add(
                        DocumentChunk(
                            id = chunkId++,
                            docTitle = title,
                            text = chunkText,
                            vector = vec
                        )
                    )
                    chunksLoaded++
                }
                if (end == words.size) break
            }
        }
        return chunksLoaded
    }

    fun queryRagLocal(query: String, targetDocTitle: String? = null): List<Pair<DocumentChunk, Double>> {
        val queryVec = computeTermFrequencyVector(query)
        if (queryVec.isEmpty()) return emptyList()

        return indexedChunks
            .filter { targetDocTitle == null || it.docTitle == targetDocTitle }
            .map { chunk ->
                val sim = computeCosineSimilarity(queryVec, chunk.vector)
                Pair(chunk, sim)
            }
            .filter { it.second > 0.05 }
            .sortedByDescending { it.second }
            .take(3)
    }

    fun getIndexedDocuments(): List<String> {
        return indexedChunks.map { it.docTitle }.distinct()
    }

    fun clearIndex() {
        indexedChunks.clear()
    }
}

// ==========================================
// 2. NORMA 43 (.n43 / .csv) BANK PORT STATEMEN_PARSER
// ==========================================

data class BankTransaction(
    val date: String,
    val concept: String,
    val amount: Double,
    val reference: String,
    val rawRecord: String
)

data class Norma43Report(
    val accountIban: String,
    val currentBalance: Double,
    val transactions: List<BankTransaction>,
    val duplicateAlerts: List<String>,
    val commissionAlerts: List<String>,
    val totalIvaEstimate: Double
)

object Norma43Parser {

    fun parseContent(content: String): Norma43Report {
        var iban = "ES21-4809-XXXX-XXXX-XXXX"
        var balance = 0.0
        val txs = mutableListOf<BankTransaction>()
        
        val reader = BufferedReader(StringReader(content))
        var line: String? = reader.readLine()
        
        while (line != null) {
            val t = line.trim()
            if (t.isNotEmpty()) {
                when {
                    t.startsWith("11") -> {
                        // Account identification record
                        if (t.length >= 40) {
                            iban = "ES" + t.substring(6, 10) + "-" + t.substring(10, 14) + "-" + "XXXX-XXXX-XXXX"
                        }
                    }
                    t.startsWith("22") -> {
                        // Transaction central record
                        if (t.length >= 52) {
                            val sign = if (t.substring(27, 28) == "1") -1.0 else 1.0
                            val centsStr = t.substring(28, 42).trimStart('0')
                            val cents = if (centsStr.isEmpty()) 0.0 else centsStr.toDouble()
                            val amt = (cents / 100.0) * sign
                            val dStr = t.substring(10, 12) + "/" + t.substring(8, 10) + "/2026"
                            val desc = t.substring(52, minOf(90, t.length)).trim()
                            
                            txs.add(
                                BankTransaction(
                                    date = dStr,
                                    concept = desc,
                                    amount = amt,
                                    reference = t.substring(16, 26).trim(),
                                    rawRecord = t
                                )
                            )
                        }
                    }
                    t.startsWith("33") -> {
                        // Complementary description record
                        if (txs.isNotEmpty() && t.length >= 10) {
                            val lastIdx = txs.size - 1
                            val current = txs[lastIdx]
                            val extra = t.substring(10, minOf(60, t.length)).trim()
                            txs[lastIdx] = current.copy(concept = "${current.concept} $extra".trim())
                        }
                    }
                    t.startsWith("88") -> {
                        // End of file balance record
                        if (t.length >= 40) {
                            val cents = t.substring(20, 34).trimStart('0').toDoubleOrNull() ?: 0.0
                            balance = cents / 100.0
                        }
                    }
                    // Handle standard CSV matching
                    t.contains(",") || t.contains(";") -> {
                        val parts = if (t.contains(";")) t.split(";") else t.split(",")
                        if (parts.size >= 3) {
                            val dateStr = parts[0].trim()
                            val amountStr = parts[1].trim().replace("€", "").replace("$", "").trim()
                            val conc = parts[2].trim()
                            val parsedAmount = amountStr.toDoubleOrNull() ?: 0.0
                            if (parsedAmount != 0.0 || conc.isNotEmpty()) {
                                txs.add(
                                    BankTransaction(
                                        date = dateStr,
                                        concept = conc,
                                        amount = parsedAmount,
                                        reference = "Ref-CSV-" + (1000..9999).random(),
                                        rawRecord = t
                                    )
                                )
                            }
                        }
                    }
                }
            }
            line = reader.readLine()
        }

        // --- SECURITY ALGORITHMS FOR DETECTING COMISIÓN OR DUPLICATES ---
        val duplicateAlerts = mutableListOf<String>()
        val commissionAlerts = mutableListOf<String>()
        var totalIva = 0.0

        // Track repeated transactions
        val seenAmounts = mutableMapOf<String, MutableList<BankTransaction>>()
        
        for (tx in txs) {
            val amountKey = String.format(Locale.US, "%.2f", tx.amount)
            if (!seenAmounts.containsKey(amountKey)) {
                seenAmounts[amountKey] = mutableListOf()
            }
            seenAmounts[amountKey]?.add(tx)

            // Detect maintenance & surprise expenses
            val conceptLower = tx.concept.lowercase(Locale.ROOT)
            if (conceptLower.contains("comis") || conceptLower.contains("manten") || conceptLower.contains("recarg") || conceptLower.contains("descub")) {
                commissionAlerts.add("⚠️ Comisión detectada el ${tx.date}: '${tx.concept}' de ${tx.amount}€.")
            }

            // Estimate IVA (Standard VAT)
            if (tx.amount < 0 && (conceptLower.contains("servici") || conceptLower.contains("compra") || conceptLower.contains("licenc") || conceptLower.contains("nube"))) {
                totalIva += (tx.amount * -1) * 0.21
            }
        }

        for ((amt, matches) in seenAmounts) {
            if (matches.size > 1 && amt.toDouble() != 0.0) {
                val detail = matches.joinToString { "[${it.date}: '${it.concept}']" }
                duplicateAlerts.add("🔄 Cargo potencialmente duplicado de ${amt}€: Encontrado ${matches.size} veces. Detalles: $detail")
            }
        }

        return Norma43Report(
            accountIban = iban,
            currentBalance = if (balance == 0.0 && txs.isNotEmpty()) txs.sumOf { it.amount } else balance,
            transactions = txs,
            duplicateAlerts = duplicateAlerts,
            commissionAlerts = commissionAlerts,
            totalIvaEstimate = totalIva
        )
    }

    // Default simulation file content to let users test instantly!
    fun getSampleNorma43(): String {
        return """
            1100018092ES214809202633456789
            2200000100150526000000000012991CANVA MONTHLY PRO
            33000000100RECARGO SUSCRIPCION ANUAL CANVA DESIGN CO
            2200000200220526000000000012991CANVA MONTHLY PRO
            33000000100REPETICION CARGO FACTURA DUPLICADA
            2200000300250526000000000015001COMISION MANTENIMIENTO
            33000000100SURPRISE ACCOUNT FEE SANTANDER
            2200000400260526000000000120002NOMINA INGRESO MENSUAL
            8800018092ES21480900000000119202
        """.trimIndent()
    }
}

// ==========================================
// 3. SECURE P2P MESH NETWORKS (BLE / WI-FI DIRECT)
// ==========================================

data class MeshNode(
    val id: String,
    val name: String,
    val type: String, // "BLE-Adhoc" or "Wi-Fi Direct"
    val strengthDbm: Int, // e.g. -45 dBm
    val isCompanion: Boolean,
    val statusText: String
)

object P2pMeshSimulator {
    private val _isMeshActive = MutableStateFlow(false)
    val isMeshActive: StateFlow<Boolean> = _isMeshActive

    private val _peersInMesh = MutableStateFlow<List<MeshNode>>(emptyList())
    val peersInMesh: StateFlow<List<MeshNode>> = _peersInMesh

    fun toggleMeshNetwork(enabled: Boolean) {
        _isMeshActive.value = enabled
        if (enabled) {
            _peersInMesh.value = listOf(
                MeshNode("m1", "MacBook Pro de Carlos", "Wi-Fi Direct", -42, true, "Enlazado - Portapapeles Sincronizado"),
                MeshNode("m2", "iPhone 15 Aura", "BLE-Adhoc", -56, true, "Espera de Transmisión"),
                MeshNode("m3", "Servidor Aura-Gateway-Mesh", "Wi-Fi Direct", -72, false, "En Rango Local (Seguro)"),
                MeshNode("m4", "iPad Pro Secundario", "BLE-Adhoc", -81, true, "No Sincronizado")
            )
        } else {
            _peersInMesh.value = emptyList()
        }
    }

    suspend fun executeMutilateralMeshSync(onProgress: (String, Float) -> Unit) {
        if (!_isMeshActive.value) return
        onProgress("Buscando canales de salto de frecuencia P2P BLE/Wi-Fi Direct...", 0.1f)
        kotlinx.coroutines.delay(800)
        onProgress("Estableciendo túnel binario difuso descentralizado (Zero-Knowledge)...", 0.4f)
        kotlinx.coroutines.delay(1000)
        onProgress("Codificando terna de Bóveda y sincronizando portapapeles local...", 0.7f)
        kotlinx.coroutines.delay(900)
        onProgress("Sincronización P2P en Malla completada con éxito. 3 acompañantes actualizados.", 1.0f)
    }
}
