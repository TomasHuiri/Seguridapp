package com.example.seguridapp.eventos.model

import jakarta.persistence.*

@Entity
@Table(name = "eventos")
class Evento(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var codigo: String = "",
    var categoria: String = "",
    var prioridad: String = "",
    var estado: String = "PENDIENTE",
    var ubicacionRef: String = "",
    var latitud: Double? = null,
    var longitud: Double? = null,

    @Column(length = 3000)
    var descripcion: String = "",

    var responsable: String = "Sin asignar",
    var creadoPor: Long = 0L,
    var fechaCreacion: Long = System.currentTimeMillis(),
    var motivoCancelacion: String? = null
)