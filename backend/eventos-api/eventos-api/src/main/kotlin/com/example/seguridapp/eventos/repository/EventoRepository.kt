package com.example.seguridapp.eventos.repository

import com.example.seguridapp.eventos.model.Evento
import org.springframework.data.jpa.repository.JpaRepository

interface EventoRepository : JpaRepository<Evento, Long>
