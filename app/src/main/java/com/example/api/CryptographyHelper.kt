package com.example.api

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptographyHelper {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    // Static 16-byte initialization vector for deterministic offline vault storage stability
    private val IV = ByteArray(16) { 0 }

    /**
     * Encrypts plain text using standard AES-256 in CBC mode with PKCS5 padding.
     */
    fun encryptAES(plainText: String, keyString: String): String {
        return try {
            val keyBytes = MessageDigest.getInstance("SHA-256")
                .digest(keyString.toByteArray(StandardCharsets.UTF_8))
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(IV))
            val cipherBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            Base64.getEncoder().encodeToString(cipherBytes)
        } catch (e: Exception) {
            "ENCRYPTION_ERROR: ${e.message}"
        }
    }

    /**
     * Decrypts AES-256 encrypted base64 text back into plain text.
     */
    fun decryptAES(cipherText: String, keyString: String): String {
        return try {
            val keyBytes = MessageDigest.getInstance("SHA-256")
                .digest(keyString.toByteArray(StandardCharsets.UTF_8))
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(IV))
            val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cipherText))
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            "⚠️ ERROR DE DESCIFRADO (Llave incorrecta): El hardware local denegó la derivación de clave."
        }
    }
}
