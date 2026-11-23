package com.example.seguridapp

import com.example.seguridapp.repository.Formato
import com.example.seguridapp.repository.Hash
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FormatoHashTest : StringSpec({

    "Formato fecha devuelve dia mes ano hora y minutos" {
        Formato.fecha(0L).matches(Regex("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}")) shouldBe true
    }

    "Formato coordenadas utiliza cinco decimales" {
        Formato.coordenadas(-33.45, -70.66) shouldBe "-33.45000, -70.66000"
    }

    "Tiempo relativo indica ahora cuando no pasa un minuto" {
        Formato.tiempoRelativo(System.currentTimeMillis()) shouldBe "Ahora"
    }

    "Tiempo relativo usa minutos en intervalos cortos" {
        Formato.tiempoRelativo(System.currentTimeMillis() - 15L * 60_000 - 5_000) shouldBe "Hace 15 min"
    }

    "Tiempo relativo usa horas al superar una hora" {
        Formato.tiempoRelativo(System.currentTimeMillis() - 2L * 60 * 60_000 - 5_000) shouldBe "Hace 2 h"
    }

    "Tiempo relativo usa dias al superar un dia" {
        Formato.tiempoRelativo(System.currentTimeMillis() - 3L * 24 * 60 * 60_000 - 5_000) shouldBe "Hace 3 d"
    }

    "Hash produce el mismo resultado para la misma contrasena" {
        Hash.sha256("Clave12345") shouldBe Hash.sha256("Clave12345")
    }

    "Hash cambia cuando cambia la contrasena" {
        (Hash.sha256("Clave12345") != Hash.sha256("Clave12346")) shouldBe true
    }

    "Hash SHA256 tiene 64 caracteres hexadecimales" {
        Hash.sha256("Clave12345").matches(Regex("[0-9a-f]{64}")) shouldBe true
    }
})
