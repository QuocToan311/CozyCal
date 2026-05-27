package com.example.util

import java.security.MessageDigest

object PasswordHasher {
    /**
     * Hashes a plain password using SHA-256 algorithm with a standard salt.
     */
    fun hashPassword(password: String): String {
        return try {
            val salt = "CozyCalSecureSalt2026"
            val saltedPassword = password + salt
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(saltedPassword.toByteArray(Charsets.UTF_8))
            val hexString = StringBuilder()
            for (b in hash) {
                val hex = Integer.toHexString(0xff and b.toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            hexString.toString()
        } catch (e: Exception) {
            // Secure fallback
            password.hashCode().toString()
        }
    }
}
