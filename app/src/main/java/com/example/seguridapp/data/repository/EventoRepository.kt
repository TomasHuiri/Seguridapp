package com.example.seguridapp.data.repository

import com.example.seguridapp.data.local.EventoDao
import com.example.seguridapp.data.local.EventoEntity
import com.example.seguridapp.data.local.SeguimientoEntity
import com.example.seguridapp.model.Categoria
import com.example.seguridapp.model.Estado
import com.example.seguridapp.model.Prioridad
import com.example.seguridapp.model.TipoSeguimiento
import com.example.seguridapp.model.Unidades
import kotlinx.coroutines.flow.Flow
import kotlin.compareTo
import kotlin.text.format
import com.example.seguridapp.model.Permisos
import com.example.seguridapp.model.Rol


class EventoRepository(
    private val dao: EventoDao,
    private val usuarios: UsuarioRepository
) {

    fun observarTodos(): Flow<List<EventoEntity>> = dao.observarTodos()

    fun observarPorId(id: Long): Flow<EventoEntity?> = dao.observarPorId(id)

    fun observarSeguimientos(id: Long): Flow<List<SeguimientoEntity>> = dao.observarSeguimientos(id)

    suspend fun crear(
        categoria: Categoria,
        prioridad: Prioridad,
        ubicacionRef: String,
        latitud: Double?,
        longitud: Double?,
        descripcion: String,
        fotoPath: String?,
        creadoPor: Long,
        autor: String,
        fecha: Long = System.currentTimeMillis(),
        estado: Estado = Estado.PENDIENTE,
        responsable: String = Unidades.SIN_ASIGNAR
    ): Long {
        val id = dao.insertar(
            EventoEntity(
                codigo = "",
                categoria = categoria.name,
                prioridad = prioridad.name,
                estado = estado.name,
                ubicacionRef = ubicacionRef.trim(),
                latitud = latitud,
                longitud = longitud,
                descripcion = descripcion.trim(),
                fotoPath = fotoPath,
                responsable = responsable,
                creadoPor = creadoPor,
                fechaCreacion = fecha
            )
        )
        dao.obtener(id)?.let { dao.actualizar(it.copy(codigo = "EVT-%03d".format(id))) }
        registrarSeguimiento(id, TipoSeguimiento.CREACION, "Evento registrado", autor, fecha)
        return id
    }


    suspend fun avanzarEstado(id: Long, autor: String): Boolean {
        val usuario = usuarios.usuarioActual() ?: return false
        val evento = dao.obtener(id) ?: return false
        val estado = Estado.desde(evento.estado)
        val rol = Rol.desde(usuario.rol)

        if (!Permisos.puedeCambiarEstado(rol, estado)) {
            return false
        }

        val siguiente = estado.siguiente() ?: return false

        dao.actualizar(evento.copy(estado = siguiente.name))

        registrarSeguimiento(
            id,
            TipoSeguimiento.ESTADO,
            "Estado cambiado a ${siguiente.etiqueta}",
            usuario.nombre
        )

        return true
    }


    suspend fun agregarObservacion(
        id: Long,
        texto: String,
        autor: String
    ): Boolean {
        val usuario = usuarios.usuarioActual() ?: return false
        val evento = dao.obtener(id) ?: return false

        val rol = Rol.desde(usuario.rol)
        val estado = Estado.desde(evento.estado)
        val esCreador = evento.creadoPor == usuario.id

        if (!Permisos.puedeObservar(rol, estado, esCreador)) {
            return false
        }

        if (texto.isBlank()) return false

        registrarSeguimiento(
            id,
            TipoSeguimiento.OBSERVACION,
            texto.trim(),
            usuario.nombre
        )

        return true
    }


    suspend fun asignar(
        id: Long,
        unidad: String,
        autor: String
    ): Boolean {
        val usuario = usuarios.usuarioActual() ?: return false
        val evento = dao.obtener(id) ?: return false
        val rol = Rol.desde(usuario.rol)
        val estado = Estado.desde(evento.estado)

        if (!Permisos.puedeAsignar(rol, estado)) {
            return false
        }

        if (unidad !in Unidades.lista) return false

        dao.actualizar(evento.copy(responsable = unidad))

        registrarSeguimiento(
            id,
            TipoSeguimiento.ASIGNACION,
            "Caso derivado a: $unidad",
            usuario.nombre
        )

        return true
    }


    suspend fun cancelar(
        id: Long,
        motivo: String,
        autor: String
    ): Boolean {
        val usuario = usuarios.usuarioActual() ?: return false
        val evento = dao.obtener(id) ?: return false

        val rol = Rol.desde(usuario.rol)
        val estado = Estado.desde(evento.estado)
        val esCreador = evento.creadoPor == usuario.id

        if (!Permisos.puedeCancelar(rol, estado, esCreador)) {
            return false
        }

        if (motivo.isBlank()) return false

        dao.actualizar(
            evento.copy(
                estado = Estado.CANCELADO.name,
                motivoCancelacion = motivo.trim()
            )
        )

        registrarSeguimiento(
            id,
            TipoSeguimiento.CANCELACION,
            "Evento cancelado. Motivo: ${motivo.trim()}",
            usuario.nombre
        )

        return true
    }

    private suspend fun registrarSeguimiento(
        id: Long,
        tipo: String,
        texto: String,
        autor: String,
        fecha: Long = System.currentTimeMillis()
    ) {
        dao.insertarSeguimiento(
            SeguimientoEntity(eventoId = id, tipo = tipo, texto = texto, autor = autor, fecha = fecha)
        )
    }

    suspend fun sembrar() {
        if (dao.contar() > 0) return
        val hora = 3_600_000L
        val ahora = System.currentTimeMillis()
        crear(Categoria.INCENDIO, Prioridad.ALTA, "Sector Centro", -33.4950, -70.6170,
            "Se reporta incendio en una bodega del sector. Se solicita presencia de unidades de emergencia.",
            null, 1, "Usuario de prueba", ahora - 2 * hora, Estado.EN_PROCESO, "Unidad de Bomberos (simulada)")
        crear(Categoria.ROBO, Prioridad.MEDIA, "Parque central, sector norte", -33.4890, -70.6200,
            "Se informa un robo de pertenencias en la vía pública. Sin lesionados.",
            null, 1, "Usuario de prueba", ahora - 4 * hora, Estado.PENDIENTE)
        crear(Categoria.SOSPECHOSO, Prioridad.BAJA, "Calle Ficticia 123, Sector Sur", -33.5010, -70.6130,
            "Persona merodeando en el sector durante varias horas.",
            null, 1, "Usuario de prueba", ahora - 6 * hora, Estado.CERRADO, "Unidad de Patrullaje (simulada)")
        crear(Categoria.ACCIDENTE, Prioridad.ALTA, "Avenida Principal con Calle 5", -33.4985, -70.6090,
            "Colisión entre dos vehículos. Se solicita apoyo de tránsito.",
            null, 1, "Usuario de prueba", ahora - 26 * hora, Estado.EN_PROCESO, "Unidad de Tránsito (simulada)")
        crear(Categoria.VANDALISMO, Prioridad.MEDIA, "Plaza del Sector Oriente", -33.4930, -70.6050,
            "Daños en mobiliario urbano de la plaza.",
            null, 1, "Usuario de prueba", ahora - 50 * hora, Estado.PENDIENTE)
    }
}