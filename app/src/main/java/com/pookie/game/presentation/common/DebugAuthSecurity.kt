package com.pookie.game.presentation.common

/**
 * Security helper to verify developer debug unlock pin.
 * Pin is stored exclusively in XOR encrypted form.
 */
object DebugAuthSecurity {
    // Encrypted representation of "2206" with key 0x5A
    private val ENCRYPTED_PIN: ByteArray = byteArrayOf(0x68, 0x68, 0x6A, 0x6C)
    private const val XOR_KEY: Byte = 0x5A

    // XOR encrypted demo deployment ID with mask 0x5C
    private val ENCRYPTED_DEMO_KEY: ByteArray = byteArrayOf(
        0x1D.toByte(), 0x17.toByte(), 0x3A.toByte(), 0x25.toByte(), 0x3F.toByte(), 0x3E.toByte(), 0x2B.toByte(), 0x16.toByte(),
        0x34.toByte(), 0x29.toByte(), 0x28.toByte(), 0x71.toByte(), 0x2B.toByte(), 0x16.toByte(), 0x0B.toByte(), 0x0F.toByte(),
        0x6D.toByte(), 0x6F.toByte(), 0x65.toByte(), 0x09.toByte(), 0x6C.toByte(), 0x06.toByte(), 0x69.toByte(), 0x39.toByte(),
        0x3F.toByte(), 0x28.toByte(), 0x3D.toByte(), 0x38.toByte(), 0x1E.toByte(), 0x64.toByte(), 0x71.toByte(), 0x36.toByte(),
        0x35.toByte(), 0x2D.toByte(), 0x0F.toByte(), 0x09.toByte(), 0x3E.toByte(), 0x17.toByte(), 0x39.toByte(), 0x2A.toByte(),
        0x3B.toByte(), 0x31.toByte(), 0x03.toByte(), 0x34.toByte(), 0x11.toByte(), 0x3E.toByte(), 0x2E.toByte(), 0x32.toByte(),
        0x65.toByte(), 0x11.toByte(), 0x06.toByte(), 0x1F.toByte(), 0x64.toByte(), 0x2A.toByte(), 0x26.toByte(), 0x37.toByte(),
        0x65.toByte(), 0x03.toByte(), 0x28.toByte(), 0x65.toByte(), 0x32.toByte(), 0x29.toByte(), 0x0D.toByte(), 0x35.toByte(),
        0x0F.toByte(), 0x24.toByte(), 0x2A.toByte(), 0x09.toByte(), 0x09.toByte(), 0x19.toByte(), 0x25.toByte(), 0x39.toByte(),
        0x25.toByte(), 0x1D.toByte()
    )
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

    fun getDemoDeploymentId(): String {
        val decrypted = ByteArray(ENCRYPTED_DEMO_KEY.size)
        for (i in ENCRYPTED_DEMO_KEY.indices) {
            decrypted[i] = (ENCRYPTED_DEMO_KEY[i].toInt() xor DEMO_KEY_MASK.toInt()).toByte()
        }
        return String(decrypted, Charsets.UTF_8)
    }
}
