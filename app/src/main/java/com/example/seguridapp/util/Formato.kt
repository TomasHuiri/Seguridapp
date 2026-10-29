package com.example.seguridapp.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formato {
    private val locale = Locale("es", "CL")

    fun fecha(ms: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", locale).format(Date(ms))

    fun tiempoRelativo(ms: Long): String {
        val minutos = (System.currentTimeMillis() - ms) / 60_000
        return when {
            minutos < 1 -> "Ahora"
            minutos < 60 -> "Hace $minutos min"
            minutos < 60 * 24 -> "Hace ${minutos / 60} h"
            else -> "Hace ${minutos / (60 * 24)} d"
        }
    }

    fun coordenadas(lat: Double, lng: Double): String =
        String.format(Locale.US, "%.5f, %.5f", lat, lng)
}