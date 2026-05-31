package com.example.kmp

/**
 * MOCKUP KOTLIN MULTIPLATFORM (KMP) CORE ARCHITECTURE
 * Este código representa la arquitectura compartida ("shared" module) compilable de forma nativa
 * para Android (Aarch64), macOS (Intel/Apple Silicon) e iOS (Cocoa Touch Core).
 */

object KmpCodebase {

    val commonDatabaseKotlin = """
        // commonMain/src/kotlin/com/aura/data/CommonDatabase.kt
        package com.aura.data

        import androidx.room.Database
        import androidx.room.RoomDatabase
        import androidx.sqlite.SQLiteConnection
        import androidx.sqlite.driver.bundled.BundledSQLiteDriver

        @Database(
            entities = [ChatMessageEntity::class, GeneratedDocEntity::class, ConnectedDeviceEntity::class],
            version = 1
        )
        abstract class AppDatabase : RoomDatabase() {
            abstract fun chatMessageDao(): ChatMessageCommonDao
            abstract fun generatedDocDao(): GeneratedDocCommonDao
        }

        // Factory expect/actual para instanciar la base de datos de forma nativa
        expect class DbFactory {
            fun createDatabase(): AppDatabase
        }
    """.trimIndent()

    val cryptographyExpectActual = """
        // commonMain/src/kotlin/com/aura/security/Cryptography.kt
        package com.aura.security

        // expect platform-specific secure key storage API
        expect class SecureKeyHardwareStore {
            fun generateSymmetricKey(keyAlias: String): ByteArray
            fun getSymmetricKey(keyAlias: String): ByteArray?
            fun encryptAesGcm(plainText: ByteArray, key: ByteArray): ByteArray
            fun decryptAesGcm(cipherText: ByteArray, key: ByteArray): ByteArray
        }

        // iosMain/src/kotlin/com/aura/security/Cryptography.ios.kt
        package com.aura.security
        import platform.Foundation.*
        import platform.Security.* // Keychain & Secure Enclave APIs

        actual class SecureKeyHardwareStore {
            actual fun generateSymmetricKey(keyAlias: String): ByteArray {
                // Guarda de forma nativa en el Apple Keychain del iPhone con protección de Secure Enclave
                val query = mutableMapOf<Any?, Any?>()
                query[kSecClass] = kSecClassGenericPassword
                query[kSecAttrAccount] = keyAlias
                query[kSecValueData] = ... // Genera claves seguras usando CC_SHA256
                SecItemAdd(query as CFDictionaryRef, null)
                return ...
            }
            
            actual fun getSymmetricKey(keyAlias: String): ByteArray? {
                // Recuperar llave de Apple Keychain usando FaceID/TouchID en iOS/macOS
                ...
            }
            
            actual fun encryptAesGcm(plainText: ByteArray, key: ByteArray): ByteArray {
                // iOS CryptoKit o CommonCryptor para AES-256-GCM nativo
                return ...
            }
            
            actual fun decryptAesGcm(cipherText: ByteArray, key: ByteArray): ByteArray {
                return ...
            }
        }

        // androidMain/src/kotlin/com/aura/security/Cryptography.android.kt
        package com.aura.security
        import android.security.keystore.KeyGenParameterSpec
        import android.security.keystore.KeyProperties
        import java.security.KeyStore
        import javax.crypto.Cipher
        import javax.crypto.KeyGenerator
        import javax.crypto.spec.GCMParameterSpec

        actual class SecureKeyHardwareStore {
            actual fun generateSymmetricKey(keyAlias: String): ByteArray {
                // Implementación nativa usando Android KeyStore de hardware (TEE / StrongBox)
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                keyGenerator.init(
                    KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build()
                )
                return keyGenerator.generateKey().encoded
            }
            
            actual fun getSymmetricKey(keyAlias: String): ByteArray? {
                val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
                return entry?.secretKey?.encoded
            }
            
            actual fun encryptAesGcm(plainText: ByteArray, key: ByteArray): ByteArray {
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                ...
                return cipher.doFinal(plainText)
            }
            
            actual fun decryptAesGcm(cipherText: ByteArray, key: ByteArray): ByteArray {
                ...
            }
        }
    """.trimIndent()

    val mdnsSyncEngineCommon = """
        // commonMain/src/kotlin/com/aura/net/MdnsSyncCommon.kt
        package com.aura.net

        import io.ktor.server.engine.*
        import io.ktor.server.cio.*
        import io.ktor.server.routing.*
        import io.ktor.server.websocket.*
        import io.ktor.websocket.*
        import kotlinx.coroutines.flow.SharedFlow

        // Servicio de sincronización LAN común para todas las plataformas usando sockets Web de Ktor
        class SharedLanSyncService(private val incomingDataFlow: MutableSharedFlow<String>) {
            
            fun startLocalEngine(port: Int) {
                embeddedServer(CIO, port = port) {
                    install(WebSockets)
                    routing {
                        webSocket("/sync") {
                            for (frame in incoming) {
                                if (frame is Frame.Text) {
                                    val text = frame.readText()
                                    incomingDataFlow.emit(text)
                                    send("SYNC_RECEIVED_OK")
                                }
                            }
                        }
                    }
                }.start(wait = false)
            }
        }
    """.trimIndent()
}
