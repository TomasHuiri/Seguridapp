package com.example.seguridapp.eventos.repository

import com.example.seguridapp.eventos.model.Seguimiento
import org.springframework.data.jpa.repository.JpaRepository

interface SeguimientoRepository :
    JpaRepository<Seguimiento, Long> {

    fun findAllByEventoIdOrderByFechaDescIdDesc(
        eventoId: Long
    ): List<Seguimiento>
}