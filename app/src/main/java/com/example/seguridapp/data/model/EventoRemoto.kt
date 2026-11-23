package com.example.seguridapp.data.model

data class EventoRemoto(
    val id: Long,
    val codigo: String,
    val categoria: String,
    val prioridad: String,
    val estado: String,
    val ubicacionRef: String,
    val latitud: Double?,
    val longitud: Double?,
    val descripcion: String,
    val responsable: String,
    val creadoPor: Long,
    val fechaCreacion: Long,
    val motivoCancelacion: String?
)