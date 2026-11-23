package com.example.seguridapp.data.model

data class SeguimientoRemoto(
    val id: Long,
    val eventoId: Long,
    val tipo: String,
    val texto: String,
    val autor: String,
    val fecha: Long
)