package com.example.seguridapp

import com.example.seguridapp.data.local.UsuarioEntity
import com.example.seguridapp.data.model.Categoria
import com.example.seguridapp.data.model.Estado
import com.example.seguridapp.data.model.Prioridad
import com.example.seguridapp.data.model.Rol
import com.example.seguridapp.data.model.Unidades
import com.example.seguridapp.data.model.EventoRemoto
import com.example.seguridapp.data.model.SeguimientoRemoto
import com.example.seguridapp.data.remote.EventosApiService
import com.example.seguridapp.repository.EventoRepository
import com.example.seguridapp.repository.UsuarioRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.IOException

private fun remotoDePrueba(
    id: Long = 10L,
    estado: String = "PENDIENTE",
    creadoPor: Long = 1L,
    fecha: Long = 1000L
) = EventoRemoto(
    id = id,
    codigo = "EVT-%03d".format(id),
    categoria = "INCENDIO",
    prioridad = "ALTA",
    estado = estado,
    ubicacionRef = "Sector centro",
    latitud = -33.45,
    longitud = -70.66,
    descripcion = "Incendio reportado en el sector",
    responsable = Unidades.SIN_ASIGNAR,
    creadoPor = creadoPor,
    fechaCreacion = fecha,
    motivoCancelacion = null
)

private fun usuarioDePrueba(
    rol: Rol = Rol.USUARIO,
    id: Long = 1L
) = UsuarioEntity(
    id = id,
    nombre = "Persona de prueba",
    email = "persona@correo.cl",
    passwordHash = "hash",
    rol = rol.name
)

class EventoRepositoryTest : StringSpec({

    "La lista inicial de eventos esta vacia" {
        val api = mockk<EventosApiService>()
        val usuarios = mockk<UsuarioRepository>()
        val repo = EventoRepository(api, usuarios)

        repo.observarTodos().value.size shouldBe 0
        repo.observarPorId(10L).first() shouldBe null
        repo.observarSeguimientos(10L).first().size shouldBe 0
    }

    "Refrescar recupera eventos del servidor en orden descendente" {
        runTest {
            val api = mockk<EventosApiService>()
            val repo = EventoRepository(api, mockk())
            coEvery { api.listar() } returns listOf(
                remotoDePrueba(id = 1, fecha = 100),
                remotoDePrueba(id = 2, fecha = 300)
            )

            repo.refrescar()

            repo.observarTodos().value.map { it.id } shouldBe listOf(2L, 1L)
            repo.observarTodos().value[0].fotoPath shouldBe null
            repo.observarPorId(1L).first()?.codigo shouldBe "EVT-001"
        }
    }

    "Un error de consulta no modifica la lista cargada anteriormente" {
        runTest {
            val api = mockk<EventosApiService>()
            val repo = EventoRepository(api, mockk())
            coEvery { api.listar() } returns listOf(remotoDePrueba())
            repo.refrescar()
            coEvery { api.listar() } throws IOException("Sin conexion")

            try {
                repo.refrescar()
            } catch (_: IOException) {
            }

            repo.observarTodos().value.single().id shouldBe 10L
        }
    }

    "Refrescar detalle carga evento y seguimiento remoto" {
        runTest {
            val api = mockk<EventosApiService>()
            val repo = EventoRepository(api, mockk())
            coEvery { api.obtener(10L) } returns remotoDePrueba()
            coEvery { api.listarSeguimientos(10L) } returns listOf(
                SeguimientoRemoto(2, 10, "OBSERVACION", "Revisado", "Funcionario", 200)
            )

            repo.refrescarDetalle(10L)

            repo.observarPorId(10L).first()?.descripcion shouldBe "Incendio reportado en el sector"
            repo.observarSeguimientos(10L).first().single().texto shouldBe "Revisado"
        }
    }

    "Crear envia datos limpios y actualiza la lista" {
        runTest {
            val api = mockk<EventosApiService>()
            val repo = EventoRepository(api, mockk())
            coEvery { api.crear(any()) } returns remotoDePrueba(id = 45)

            val id = repo.crear(
                Categoria.INCENDIO,
                Prioridad.ALTA,
                "  Sector centro  ",
                -33.45,
                -70.66,
                "  Incendio reportado en el sector  ",
                null,
                1L,
                "Persona de prueba"
            )

            id shouldBe 45L
            repo.observarTodos().value.single().id shouldBe 45L
            coVerify(exactly = 1) {
                api.crear(match {
                    it.ubicacionRef == "Sector centro" &&
                    it.descripcion == "Incendio reportado en el sector" &&
                    it.categoria == "INCENDIO" &&
                    it.creadoPor == 1L
                })
            }
        }
    }

    "Crear rechaza fotografias que el servidor no admite" {
        runTest {
            val api = mockk<EventosApiService>()
            val repo = EventoRepository(api, mockk())
            var rechazado = false

            try {
                repo.crear(Categoria.INCENDIO, Prioridad.ALTA, "Centro", null, null,
                    "Incendio reportado", "archivo.jpg", 1L, "Persona de prueba")
            } catch (_: IllegalArgumentException) {
                rechazado = true
            }

            rechazado shouldBe true
            coVerify(exactly = 0) { api.crear(any()) }
        }
    }

    "Crear rechaza un autor vacio" {
        runTest {
            val api = mockk<EventosApiService>()
            val repo = EventoRepository(api, mockk())
            var rechazado = false

            try {
                repo.crear(Categoria.INCENDIO, Prioridad.ALTA, "Centro", null, null,
                    "Incendio reportado", null, 1L, "")
            } catch (_: IllegalArgumentException) {
                rechazado = true
            }

            rechazado shouldBe true
            coVerify(exactly = 0) { api.crear(any()) }
        }
    }

    "Sin usuario actual no se permite avanzar estado" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns null
            val repo = EventoRepository(api, usuarios)

            repo.avanzarEstado(10L, "Autor") shouldBe false
            coVerify(exactly = 0) { api.obtener(any()) }
        }
    }

    "Un residente no puede avanzar estado" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioDePrueba()
            coEvery { api.obtener(10L) } returns remotoDePrueba()
            val repo = EventoRepository(api, usuarios)

            repo.avanzarEstado(10L, "Autor") shouldBe false
            coVerify(exactly = 0) { api.avanzarEstado(any(), any()) }
        }
    }

    "Un funcionario avanza estado y refresca el historial" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioDePrueba(Rol.FUNCIONARIO)
            coEvery { api.obtener(10L) } returns remotoDePrueba()
            coEvery { api.avanzarEstado(10L, any()) } returns
                remotoDePrueba(estado = Estado.EN_PROCESO.name)
            coEvery { api.listarSeguimientos(10L) } returns emptyList()
            val repo = EventoRepository(api, usuarios)

            repo.avanzarEstado(10L, "Nombre enviado por pantalla") shouldBe true
            repo.observarPorId(10L).first()?.estado shouldBe "EN_PROCESO"
            coVerify(exactly = 1) {
                api.avanzarEstado(10L, match { it.autor == "Persona de prueba" })
            }
        }
    }

    "El creador puede registrar una observacion valida" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioDePrueba()
            coEvery { api.obtener(10L) } returns remotoDePrueba()
            coEvery { api.agregarObservacion(10L, any()) } returns
                SeguimientoRemoto(3, 10, "OBSERVACION", "Nueva observacion", "Persona de prueba", 300)
            val repo = EventoRepository(api, usuarios)

            repo.agregarObservacion(10L, "  Nueva observacion  ", "Otro nombre") shouldBe true
            repo.observarSeguimientos(10L).first().single().id shouldBe 3L
            coVerify(exactly = 1) {
                api.agregarObservacion(10L, match {
                    it.detalle == "Nueva observacion" && it.autor == "Persona de prueba"
                })
            }
        }
    }

    "Un residente no puede observar el evento de otro" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioDePrueba()
            coEvery { api.obtener(10L) } returns remotoDePrueba(creadoPor = 99L)
            val repo = EventoRepository(api, usuarios)

            repo.agregarObservacion(10L, "Nueva observacion", "Autor") shouldBe false
            coVerify(exactly = 0) { api.agregarObservacion(any(), any()) }
        }
    }

    "Una observacion en blanco no se envia" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioDePrueba()
            coEvery { api.obtener(10L) } returns remotoDePrueba()
            val repo = EventoRepository(api, usuarios)

            repo.agregarObservacion(10L, "  ", "Autor") shouldBe false
            coVerify(exactly = 0) { api.agregarObservacion(any(), any()) }
        }
    }

    "Un funcionario no puede asignar una unidad" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioDePrueba(Rol.FUNCIONARIO)
            coEvery { api.obtener(10L) } returns remotoDePrueba()
            val repo = EventoRepository(api, usuarios)

            repo.asignar(10L, Unidades.lista[1], "Autor") shouldBe false
            coVerify(exactly = 0) { api.asignar(any(), any()) }
        }
    }

    "Un supervisor puede asignar una unidad conocida" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioDePrueba(Rol.SUPERVISOR)
            coEvery { api.obtener(10L) } returns remotoDePrueba()
            coEvery { api.asignar(10L, any()) } returns remotoDePrueba()
            coEvery { api.listarSeguimientos(10L) } returns emptyList()
            val repo = EventoRepository(api, usuarios)

            repo.asignar(10L, Unidades.lista[1], "Autor") shouldBe true
            coVerify(exactly = 1) {
                api.asignar(10L, match { it.detalle == Unidades.lista[1] })
            }
        }
    }

    "Un supervisor no puede asignar una unidad inventada" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioDePrueba(Rol.SUPERVISOR)
            coEvery { api.obtener(10L) } returns remotoDePrueba()
            val repo = EventoRepository(api, usuarios)

            repo.asignar(10L, "Unidad inventada", "Autor") shouldBe false
            coVerify(exactly = 0) { api.asignar(any(), any()) }
        }
    }

    "El creador puede cancelar un evento pendiente" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioDePrueba()
            coEvery { api.obtener(10L) } returns remotoDePrueba()
            coEvery { api.cancelar(10L, any()) } returns remotoDePrueba(estado = "CANCELADO")
            coEvery { api.listarSeguimientos(10L) } returns emptyList()
            val repo = EventoRepository(api, usuarios)

            repo.cancelar(10L, "  Registro duplicado  ", "Autor") shouldBe true
            repo.observarPorId(10L).first()?.estado shouldBe "CANCELADO"
            coVerify(exactly = 1) {
                api.cancelar(10L, match { it.detalle == "Registro duplicado" })
            }
        }
    }

    "No se puede cancelar con motivo vacio" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioDePrueba()
            coEvery { api.obtener(10L) } returns remotoDePrueba()
            val repo = EventoRepository(api, usuarios)

            repo.cancelar(10L, " ", "Autor") shouldBe false
            coVerify(exactly = 0) { api.cancelar(any(), any()) }
        }
    }

    "Un evento terminado no permite cancelar" {
        runTest {
            val api = mockk<EventosApiService>()
            val usuarios = mockk<UsuarioRepository>()
            coEvery { usuarios.usuarioActual() } returns usuarioDePrueba(Rol.ADMINISTRADOR)
            coEvery { api.obtener(10L) } returns remotoDePrueba(estado = "CERRADO")
            val repo = EventoRepository(api, usuarios)

            repo.cancelar(10L, "Registro duplicado", "Autor") shouldBe false
            coVerify(exactly = 0) { api.cancelar(any(), any()) }
        }
    }
})
