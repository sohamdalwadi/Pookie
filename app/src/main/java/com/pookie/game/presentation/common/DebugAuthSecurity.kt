package com.pookie.game.presentation.common

/**
 * Security helper to verify developer debug unlock pin.
 * Pin is stored exclusively in XOR encrypted form.
 */
object DebugAuthSecurity {
    // Encrypted representation of "2206" with key 0x5A
    private val ENCRYPTED_PIN: ByteArray = byteArrayOf(0x68, 0x68, 0x6A, 0x6C)
    private const val XOR_KEY: Byte = 0x5A

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
}
