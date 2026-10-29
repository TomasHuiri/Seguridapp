package com.example.seguridapp.util

import java.security.MessageDigest

object Hash {
    fun sha256(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(("segurity::$texto").toByteArray())
            .joinToString("") { "%02x".format(it) }
}