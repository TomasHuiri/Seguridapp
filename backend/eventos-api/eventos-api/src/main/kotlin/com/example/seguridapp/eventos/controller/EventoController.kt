package com.example.seguridapp.eventos.controller

import com.example.seguridapp.eventos.dto.EventoActualizacion
import com.example.seguridapp.eventos.dto.EventoSolicitud
import com.example.seguridapp.eventos.model.Evento
import com.example.seguridapp.eventos.repository.EventoRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import com.example.seguridapp.eventos.dto.AccionEvento
import com.example.seguridapp.eventos.model.Seguimiento
import com.example.seguridapp.eventos.repository.SeguimientoRepository
import org.springframework.transaction.annotation.Transactional

@RestController
@RequestMapping("/api/eventos")
class EventoController(
    private val repository: EventoRepository,
    private val seguimientosRepository: SeguimientoRepository
) {

    private val categorias = setOf(
        "INCENDIO", "ROBO", "SOSPECHOSO",
        "ACCIDENTE", "VANDALISMO", "OTRO"
    )

    private val prioridades = setOf(
        "BAJA", "MEDIA", "ALTA", "INDETERMINADO"
    )

    @GetMapping
    fun listar(): List<Evento> {
        return repository.findAll()
    }

    @GetMapping("/{id}")
    fun obtener(@PathVariable id: Long): Evento {
        return repository.findById(id).orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Evento no encontrado"
            )
        }
    }

    @Transactional
    @PostMapping
    fun crear(
        @RequestBody datos: EventoSolicitud
    ): ResponseEntity<Evento> {

        if (datos.categoria !in categorias ||
            datos.prioridad !in prioridades ||
            datos.ubicacionRef.isBlank() ||
            datos.descripcion.isBlank() ||
            datos.creadoPor <= 0
        ) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Datos del evento invalidos"
            )
        }

        val evento = Evento(
            categoria = datos.categoria,
            prioridad = datos.prioridad,
            ubicacionRef = datos.ubicacionRef.trim(),
            descripcion = datos.descripcion.trim(),
            creadoPor = datos.creadoPor,
            latitud = datos.latitud,
            longitud = datos.longitud
        )

        val guardado = repository.save(evento)

        guardado.codigo = "EVT-%03d".format(guardado.id)

        guardarSeguimiento(
            guardado.id!!,
            "CREACION",
            "Evento registrado",
            "Usuario ${guardado.creadoPor}"
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(repository.save(guardado))
    }

    @Transactional
    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: Long,
        @RequestBody datos: EventoActualizacion
    ): Evento {

        val evento = obtener(id)

        if (evento.estado == "CERRADO" ||
            evento.estado == "CANCELADO"
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "El evento ya esta finalizado"
            )
        }

        if (datos.prioridad !in prioridades ||
            datos.ubicacionRef.isBlank() ||
            datos.descripcion.isBlank() ||
            datos.autor.isBlank()
        ) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Datos invalidos"
            )
        }

        evento.prioridad = datos.prioridad
        evento.ubicacionRef = datos.ubicacionRef.trim()
        evento.descripcion = datos.descripcion.trim()

        val actualizado = repository.save(evento)

        guardarSeguimiento(
            id,
            "ACTUALIZACION",
            "Datos del evento actualizados",
            datos.autor.trim()
        )

        return actualizado
    }

    @Transactional
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun eliminar(@PathVariable id: Long) {

        val evento = obtener(id)

        val seguimientos = seguimientosRepository
            .findAllByEventoIdOrderByFechaDescIdDesc(id)

        seguimientosRepository.deleteAll(seguimientos)

        repository.delete(evento)
    }

    @GetMapping("/{id}/seguimientos")
    fun listarSeguimientos(
        @PathVariable id: Long
    ): List<Seguimiento> {
        obtener(id)

        return seguimientosRepository
            .findAllByEventoIdOrderByFechaDescIdDesc(id)
    }

    @PostMapping("/{id}/observaciones")
    @Transactional
    fun agregarObservacion(
        @PathVariable id: Long,
        @RequestBody accion: AccionEvento
    ): Seguimiento {
        val evento = obtener(id)

        if (evento.estado == "CERRADO" ||
            evento.estado == "CANCELADO"
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Evento finalizado"
            )
        }

        val texto = accion.detalle?.trim().orEmpty()

        if (texto.isBlank() || accion.autor.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Observacion o autor invalido"
            )
        }

        return guardarSeguimiento(
            id,
            "OBSERVACION",
            texto,
            accion.autor
        )
    }

    private fun guardarSeguimiento(
        id: Long,
        tipo: String,
        texto: String,
        autor: String
    ): Seguimiento {
        return seguimientosRepository.save(
            Seguimiento(
                eventoId = id,
                tipo = tipo,
                texto = texto,
                autor = autor
            )
        )
    }

    @PatchMapping("/{id}/avanzar")
    @Transactional
    fun avanzarEstado(
        @PathVariable id: Long,
        @RequestBody accion: AccionEvento
    ): Evento {
        val evento = obtener(id)

        if (accion.autor.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Autor invalido"
            )
        }

        val siguiente = when (evento.estado) {
            "PENDIENTE" -> "EN_PROCESO"
            "EN_PROCESO" -> "CERRADO"
            else -> throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "El estado no permite avanzar"
            )
        }

        evento.estado = siguiente

        val actualizado = repository.save(evento)

        guardarSeguimiento(
            id,
            "ESTADO",
            "Estado cambiado a $siguiente",
            accion.autor
        )

        return actualizado
    }


    @PatchMapping("/{id}/asignar")
    @Transactional
    fun asignar(
        @PathVariable id: Long,
        @RequestBody accion: AccionEvento
    ): Evento {
        val evento = obtener(id)

        if (evento.estado == "CERRADO" ||
            evento.estado == "CANCELADO"
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Evento finalizado"
            )
        }

        val unidades = setOf(
            "Sin asignar",
            "Unidad de Patrullaje (simulada)",
            "Unidad de Bomberos (simulada)",
            "Unidad de Tránsito (simulada)",
            "Unidad de Inspección (simulada)"
        )

        val unidad = accion.detalle?.trim().orEmpty()

        if (unidad !in unidades || accion.autor.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unidad o autor invalido"
            )
        }

        evento.responsable = unidad

        val actualizado = repository.save(evento)

        guardarSeguimiento(
            id,
            "ASIGNACION",
            "Caso derivado a: $unidad",
            accion.autor
        )

        return actualizado
    }

    @PatchMapping("/{id}/cancelar")
    @Transactional
    fun cancelar(
        @PathVariable id: Long,
        @RequestBody accion: AccionEvento
    ): Evento {
        val evento = obtener(id)

        if (evento.estado == "CERRADO" ||
            evento.estado == "CANCELADO"
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Evento finalizado"
            )
        }

        val motivo = accion.detalle?.trim().orEmpty()

        if (motivo.isBlank() || accion.autor.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Motivo o autor invalido"
            )
        }

        evento.estado = "CANCELADO"
        evento.motivoCancelacion = motivo

        val actualizado = repository.save(evento)

        guardarSeguimiento(
            id,
            "CANCELACION",
            "Evento cancelado. Motivo: $motivo",
            accion.autor
        )

        return actualizado
    }
}