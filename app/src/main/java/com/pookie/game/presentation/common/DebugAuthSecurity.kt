package com.pookie.game.presentation.common

import com.pookie.game.BuildConfig

/**
 * Security helper to verify developer debug unlock pin and extract demo deployment key.
 * Secrets are stored exclusively in XOR encrypted form compiled into bytecode from local.properties.
 */
object DebugAuthSecurity {
    // Encrypted representation of "2206" with key 0x5A
    private val ENCRYPTED_PIN: ByteArray = byteArrayOf(0x68, 0x68, 0x6A, 0x6C)
    private const val XOR_KEY: Byte = 0x5A
    private const val DEMO_KEY_MASK: Byte = 0x5C

    fun verifyPassword(input: String): Boolean {
        val inputBytes = input.trim().toByteArray(Charsets.UTF_8)
        if (inputBytes.size != ENCRYPTED_PIN.size) return false
        for (i in ENCRYPTED_PIN.indices) {
            val encryptedInputByte = (inputBytes[i].toInt() xor XOR_KEY.toInt()).toByte()
            if (encryptedInputByte != ENCRYPTED_PIN[i]) {
                return false
            }
        }
        return true
    }

    /**
     * Decrypts the build-time encrypted deployment ID compiled from local.properties.
     * The plain text is never stored in repository or class files.
     */
    fun getDemoDeploymentId(): String {
        val hex = BuildConfig.ENCRYPTED_DEMO_KEY_HEX
        if (hex.isNullOrBlank()) return ""
        val bytes = ByteArray(hex.length / 2)
        for (i in bytes.indices) {
            val byteStr = hex.substring(i * 2, i * 2 + 2)
            val encByte = byteStr.toInt(16).toByte()
            bytes[i] = (encByte.toInt() xor DEMO_KEY_MASK.toInt()).toByte()
        }
        return String(bytes, Charsets.UTF_8)
    }
}
