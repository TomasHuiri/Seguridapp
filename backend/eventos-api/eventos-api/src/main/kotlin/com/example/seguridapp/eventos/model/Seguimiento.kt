package com.example.seguridapp.eventos.model

import jakarta.persistence.*

@Entity
@Table(name = "seguimientos")
class Seguimiento(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var eventoId: Long = 0,

    var tipo: String = "",

    @Column(length = 3000)
    var texto: String = "",

    var autor: String = "",

    var fecha: Long = System.currentTimeMillis()
)