package com.example.seguridapp.repository

import com.example.seguridapp.data.local.EventoEntity
import com.example.seguridapp.data.local.SeguimientoEntity
import com.example.seguridapp.data.model.*
import com.example.seguridapp.data.remote.EventosApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class EventoRepository(
    private val api: EventosApiService,
    private val usuarios: UsuarioRepository
) {
    private val _eventos =
        MutableStateFlow<List<EventoEntity>>(emptyList())

    private val _seguimientos =
        MutableStateFlow<Map<Long, List<SeguimientoEntity>>>(emptyMap())

    fun observarTodos() = _eventos.asStateFlow()

    fun observarPorId(id: Long) =
        _eventos.map { lista ->
            lista.firstOrNull { it.id == id }
        }

    fun observarSeguimientos(id: Long) =
        _seguimientos.map { datos ->
            datos[id].orEmpty()
        }

    suspend fun refrescar() {
        _eventos.value = api.listar()
            .map { it.aLocal() }
            .sortedByDescending { it.fechaCreacion }
    }

    suspend fun refrescarDetalle(id: Long) {
        val evento = api.obtener(id)
        actualizarCache(evento)

        val historial = api.listarSeguimientos(id)
            .map { it.aLocal() }

        _seguimientos.update {
            it + (id to historial)
        }
    }

    suspend fun crear(
        categoria: Categoria,
        prioridad: Prioridad,
        ubicacionRef: String,
        latitud: Double?,
        longitud: Double?,
        descripcion: String,
        fotoPath: String?,
        creadoPor: Long,
        autor: String
    ): Long {
        require(fotoPath == null) {
            "La evidencia fotográfica aún no está disponible en el servidor"
        }

        require(autor.isNotBlank())

        val nuevo = api.crear(
            EventoSolicitud(
                categoria = categoria.name,
                prioridad = prioridad.name,
                ubicacionRef = ubicacionRef.trim(),
                descripcion = descripcion.trim(),
                creadoPor = creadoPor,
                latitud = latitud,
                longitud = longitud
            )
        )

        actualizarCache(nuevo)
        return nuevo.id
    }

    suspend fun avanzarEstado(
        id: Long,
        autor: String
    ): Boolean {
        val usuario = usuarios.usuarioActual() ?: return false
        val evento = api.obtener(id)

        val rol = Rol.desde(usuario.rol)
        val estado = Estado.desde(evento.estado)

        if (!Permisos.puedeCambiarEstado(rol, estado)) {
            return false
        }

        val actualizado = api.avanzarEstado(
            id,
            AccionEvento(usuario.nombre)
        )

        sincronizar(actualizado)
        return true
    }

    suspend fun agregarObservacion(
        id: Long,
        texto: String,
        autor: String
    ): Boolean {
        val usuario = usuarios.usuarioActual() ?: return false
        val evento = api.obtener(id)

        val rol = Rol.desde(usuario.rol)
        val estado = Estado.desde(evento.estado)
        val esCreador = evento.creadoPor == usuario.id

        if (!Permisos.puedeObservar(rol, estado, esCreador)) {
            return false
        }

        if (texto.isBlank()) return false

        val nuevo = api.agregarObservacion(
            id,
            AccionEvento(usuario.nombre, texto.trim())
        )

        _seguimientos.update { datos ->
            val historial = datos[id].orEmpty()

            datos + (
                    id to (listOf(nuevo.aLocal()) + historial)
                        .distinctBy { it.id }
                        .sortedWith(
                            compareByDescending<SeguimientoEntity> { it.fecha }
                                .thenByDescending { it.id }
                        )
                    )
        }

        return true
    }

    suspend fun asignar(
        id: Long,
        unidad: String,
        autor: String
    ): Boolean {
        val usuario = usuarios.usuarioActual() ?: return false
        val evento = api.obtener(id)

        val rol = Rol.desde(usuario.rol)
        val estado = Estado.desde(evento.estado)

        if (!Permisos.puedeAsignar(rol, estado)) {
            return false
        }

        if (unidad !in Unidades.lista) return false

        val actualizado = api.asignar(
            id,
            AccionEvento(usuario.nombre, unidad)
        )

        sincronizar(actualizado)
        return true
    }

    suspend fun cancelar(
        id: Long,
        motivo: String,
        autor: String
    ): Boolean {
        val usuario = usuarios.usuarioActual() ?: return false
        val evento = api.obtener(id)

        val rol = Rol.desde(usuario.rol)
        val estado = Estado.desde(evento.estado)
        val esCreador = evento.creadoPor == usuario.id

        if (!Permisos.puedeCancelar(rol, estado, esCreador)) {
            return false
        }

        if (motivo.isBlank()) return false

        val actualizado = api.cancelar(
            id,
            AccionEvento(usuario.nombre, motivo.trim())
        )

        sincronizar(actualizado)
        return true
    }

    private fun actualizarCache(evento: EventoRemoto) {
        _eventos.update { lista ->
            (lista.filterNot { it.id == evento.id } +
                    evento.aLocal())
                .sortedByDescending { it.fechaCreacion }
        }
    }

    private suspend fun sincronizar(evento: EventoRemoto) {
        actualizarCache(evento)

        try {
            val historial = api.listarSeguimientos(evento.id)
                .map { it.aLocal() }

            _seguimientos.update {
                it + (evento.id to historial)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    private fun EventoRemoto.aLocal() = EventoEntity(
        id = id,
        codigo = codigo,
        categoria = categoria,
        prioridad = prioridad,
        estado = estado,
        ubicacionRef = ubicacionRef,
        latitud = latitud,
        longitud = longitud,
        descripcion = descripcion,
        fotoPath = null,
        responsable = responsable,
        creadoPor = creadoPor,
        fechaCreacion = fechaCreacion,
        motivoCancelacion = motivoCancelacion
    )

    private fun SeguimientoRemoto.aLocal() = SeguimientoEntity(
        id = id,
        eventoId = eventoId,
        tipo = tipo,
        texto = texto,
        autor = autor,
        fecha = fecha
    )
}