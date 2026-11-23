package com.example.seguridapp

import com.example.seguridapp.data.local.UsuarioEntity
import com.example.seguridapp.data.model.Categoria
import com.example.seguridapp.data.model.Estado
import com.example.seguridapp.data.model.EventoRemoto
import com.example.seguridapp.data.model.Prioridad
import com.example.seguridapp.data.model.Rol
import com.example.seguridapp.data.model.SeguimientoRemoto
import com.example.seguridapp.data.model.Unidades
import com.example.seguridapp.data.remote.EventosApiService
import com.example.seguridapp.repository.EventoRepository
import com.example.seguridapp.repository.UsuarioRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.IOException

private fun eventoLimite(
    id: Long = 10L,
    estado: String = Estado.PENDIENTE.name,
    creadoPor: Long = 1L,
    fecha: Long = 1000L,
    responsable: String = Unidades.SIN_ASIGNAR
) = EventoRemoto(
    id = id,
    codigo = "EVT-%03d".format(id),
    categoria = Categoria.INCENDIO.name,
    prioridad = Prioridad.ALTA.name,
    estado = estado,
    ubicacionRef = "Centro",
    latitud = null,
    longitud = null,
    descripcion = "Reporte ficticio de prueba",
    responsable = responsable,
    creadoPor = creadoPor,
    fechaCreacion = fecha,
    motivoCancelacion = null
)

private fun usuarioLimite(
    rol: Rol = Rol.USUARIO,
    id: Long = 1L
) = UsuarioEntity(
    id = id,
    nombre = "Persona autorizada",
    email = "persona@segurityapp.cl",
    passwordHash = "hash",
    rol = rol.name
)

private fun seguimientoLimite(
    id: Long,
    fecha: Long,
    texto: String = "Observacion registrada"
) = SeguimientoRemoto(
    id = id,
    eventoId = 10L,
    tipo = "OBSERVACION",
    texto = texto,
    autor = "Persona autorizada",
    fecha = fecha
)

class EventoRepositoryCasosLimiteTest : StringSpec({

    "Refrescar reemplaza los eventos previamente cargados" {
        runTest {
            val api = mockk<EventosApiService>()
            val repo = EventoRepository(api, mockk())
            coEvery { api.listar() } returnsMany listOf(
                listOf(eventoLimite(10L), eventoLimite(20L)),
                listOf(eventoLimite(30L))
            )

            repo.refrescar()
            repo.refrescar()

            repo.observarTodos().value.map { it.id } shouldBe listOf(30L)
        }
    }

    "Refrescar detalle reemplaza el evento en cache sin duplicarlo" {
        runTest {
            val api = mockk<EventosApiService>()
            val repo = EventoRepository(api, mockk())
            coEvery { api.listar() } returns listOf(eventoLimite())
            coEvery { api.obtener(10L) } returns eventoLimite(estado = Estado.EN_PROCESO.name)
            coEvery { api.listarSeguimientos(10L) } returns emptyList()

            repo.refrescar()
            repo.refrescarDetalle(10L)

            repo.observarTodos().value.size shouldBe 1
            repo.observarPorId(10L).first()?.estado shouldBe Estado.EN_PROCESO.name
        }
    }

    "Refrescar detalle conserva el evento aunque falle el historial" {
        runTest {
            val api = mockk<EventosApiService>()
            val repo = EventoRepository(api, mockk())
            coEvery { api.obtener(10L) } returns eventoLimite()
            coEvery { api.listarSeguimientos(10L) } throws IOException("Historial no disponible")
            var fallo = false

            try {
                repo.refrescarDetalle(10L)
            } catch (_: IOException) {
                fallo = true
            }

            fallo shouldBe true
            repo.observarPorId(10L).first()?.id shouldBe 10L
            repo.observarSeguimientos(10L).first() shouldBe emptyList()
        }
    }

    "Crear propaga un fallo HTTP sin agregar un evento falso" {
        runTest {
            val api = mockk<EventosApiService>()
            val repo = EventoRepository(api, mockk())
            coEvery { api.crear(any()) } throws IOException("Servidor sin conexion")
            var fallo = false

            try {
                repo.crear(
                    Categoria.INCENDIO, Prioridad.ALTA, "Centro", null, null,
                    "Incendio ficticio", null, 1L, "Persona autorizada"
                )
            } catch (_: IOException) {
                fallo = true
            }

            fallo shouldBe true
            repo.observarTodos().value shouldBe emptyList()
        }
    }

    "Sin sesion no se puede observar un evento" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns null
            val repo = EventoRepository(api, usuarios)

            repo.agregarObservacion(10L, "Se visito el sector", "Autor") shouldBe false
            coVerify(exactly = 0) { api.obtener(any()) }
        }
    }

    "Sin sesion no se puede asignar un evento" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns null
            val repo = EventoRepository(api, usuarios)

            repo.asignar(10L, Unidades.lista[1], "Autor") shouldBe false
            coVerify(exactly = 0) { api.obtener(any()) }
        }
    }

    "Sin sesion no se puede cancelar un evento" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns null
            val repo = EventoRepository(api, usuarios)

            repo.cancelar(10L, "Registro duplicado", "Autor") shouldBe false
            coVerify(exactly = 0) { api.obtener(any()) }
        }
    }

    "Un funcionario no puede avanzar un caso cerrado" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioLimite(Rol.FUNCIONARIO)
            coEvery { api.obtener(10L) } returns eventoLimite(estado = Estado.CERRADO.name)
            val repo = EventoRepository(api, usuarios)

            repo.avanzarEstado(10L, "Autor") shouldBe false
            coVerify(exactly = 0) { api.avanzarEstado(any(), any()) }
        }
    }

    "Un funcionario puede observar un evento activo ajeno" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioLimite(Rol.FUNCIONARIO)
            coEvery { api.obtener(10L) } returns eventoLimite(creadoPor = 50L)
            coEvery { api.agregarObservacion(10L, any()) } returns seguimientoLimite(3L, 300L)
            val repo = EventoRepository(api, usuarios)

            repo.agregarObservacion(10L, "  Se visito el sector  ", "Otro nombre") shouldBe true
            coVerify(exactly = 1) {
                api.agregarObservacion(10L, match {
                    it.detalle == "Se visito el sector" && it.autor == "Persona autorizada"
                })
            }
        }
    }

    "Un funcionario no puede observar un caso cancelado" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioLimite(Rol.FUNCIONARIO)
            coEvery { api.obtener(10L) } returns eventoLimite(estado = Estado.CANCELADO.name)
            val repo = EventoRepository(api, usuarios)

            repo.agregarObservacion(10L, "Se visito el sector", "Autor") shouldBe false
            coVerify(exactly = 0) { api.agregarObservacion(any(), any()) }
        }
    }

    "Seguimientos nuevos se ordenan y no se duplican por identificador" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioLimite()
            coEvery { api.obtener(10L) } returns eventoLimite()
            coEvery { api.agregarObservacion(10L, any()) } returnsMany listOf(
                seguimientoLimite(5L, 100L),
                seguimientoLimite(6L, 300L),
                seguimientoLimite(5L, 100L)
            )
            val repo = EventoRepository(api, usuarios)

            repeat(3) { repo.agregarObservacion(10L, "Se visito el sector", "Autor") shouldBe true }
            repo.observarSeguimientos(10L).first().map { it.id } shouldBe listOf(6L, 5L)
        }
    }

    "Asignacion exitosa conserva el evento si falla recargar historial" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioLimite(Rol.SUPERVISOR)
            coEvery { api.obtener(10L) } returns eventoLimite()
            coEvery { api.asignar(10L, any()) } returns
                eventoLimite(responsable = Unidades.lista[1])
            coEvery { api.listarSeguimientos(10L) } throws IOException("Historial no disponible")
            val repo = EventoRepository(api, usuarios)

            repo.asignar(10L, Unidades.lista[1], "Autor") shouldBe true
            repo.observarPorId(10L).first()?.responsable shouldBe Unidades.lista[1]
        }
    }

    "Cancelar con permiso de funcionario funciona sobre evento ajeno" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioLimite(Rol.FUNCIONARIO)
            coEvery { api.obtener(10L) } returns eventoLimite(creadoPor = 99L)
            coEvery { api.cancelar(10L, any()) } returns eventoLimite(estado = Estado.CANCELADO.name)
            coEvery { api.listarSeguimientos(10L) } returns emptyList()
            val repo = EventoRepository(api, usuarios)

            repo.cancelar(10L, "  Registro duplicado  ", "Autor") shouldBe true
            coVerify(exactly = 1) { api.cancelar(10L, match { it.detalle == "Registro duplicado" }) }
        }
    }

    "Avanzar propaga la cancelacion de la consulta de historial" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioLimite(Rol.FUNCIONARIO)
            coEvery { api.obtener(10L) } returns eventoLimite()
            coEvery { api.avanzarEstado(10L, any()) } returns eventoLimite(estado = Estado.EN_PROCESO.name)
            coEvery { api.listarSeguimientos(10L) } throws CancellationException("Peticion cancelada")
            val repo = EventoRepository(api, usuarios)
            var cancelado = false

            try {
                repo.avanzarEstado(10L, "Autor")
            } catch (_: CancellationException) {
                cancelado = true
            }

            cancelado shouldBe true
        }
    }
})
