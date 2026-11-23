package com.example.seguridapp.data.model

data class EventoSolicitud(
    val categoria: String,
    val prioridad: String,
    val ubicacionRef: String,
    val descripcion: String,
    val creadoPor: Long,
    val latitud: Double? = null,
    val longitud: Double? = null
)