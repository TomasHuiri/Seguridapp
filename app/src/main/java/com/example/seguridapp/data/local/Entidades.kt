package com.example.seguridapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios", indices = [Index(value = ["email"], unique = true)])
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val email: String,
    val passwordHash: String,
    val rol: String
)

@Entity(tableName = "eventos")
data class EventoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val categoria: String,
    val prioridad: String,
    val estado: String,
    val ubicacionRef: String,
    val latitud: Double?,
    val longitud: Double?,
    val descripcion: String,
    val fotoPath: String?,
    val responsable: String,
    val creadoPor: Long,
    val fechaCreacion: Long,
    val motivoCancelacion: String? = null
)

@Entity(tableName = "seguimientos")
data class SeguimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventoId: Long,
    val tipo: String,
    val texto: String,
    val autor: String,
    val fecha: Long
)