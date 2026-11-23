package com.example.seguridapp

import com.example.seguridapp.data.local.UsuarioDao
import com.example.seguridapp.data.local.UsuarioEntity
import com.example.seguridapp.data.model.Rol
import com.example.seguridapp.data.session.SessionStore
import com.example.seguridapp.repository.Hash
import com.example.seguridapp.repository.UsuarioRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class UsuarioRepositoryTest : StringSpec({

    "Autenticacion correcta devuelve usuario" {
        runTest {
            val dao = mockk<UsuarioDao>()
            val session = mockk<SessionStore>()

            val usuario = UsuarioEntity(
                id = 1,
                nombre = "Usuario de prueba",
                email = "usuario@correo.cl",
                passwordHash = Hash.sha256("Clave12345"),
                rol = Rol.USUARIO.name
            )

            coEvery {
                dao.buscarPorEmail("usuario@correo.cl")
            } returns usuario

            val repo = UsuarioRepository(dao, session)

            val resultado = repo.autenticar(
                "usuario@correo.cl",
                "Clave12345"
            )

            resultado shouldBe usuario
        }
    }

    "Contrasena incorrecta rechaza autenticacion" {
        runTest {
            val dao = mockk<UsuarioDao>()
            val session = mockk<SessionStore>()

            val usuario = UsuarioEntity(
                id = 1,
                nombre = "Usuario de prueba",
                email = "usuario@correo.cl",
                passwordHash = Hash.sha256("Clave12345"),
                rol = Rol.USUARIO.name
            )

            coEvery {
                dao.buscarPorEmail("usuario@correo.cl")
            } returns usuario

            val repo = UsuarioRepository(dao, session)

            repo.autenticar(
                "usuario@correo.cl",
                "Incorrecta123"
            ) shouldBe null
        }
    }

    "No permite registrar correo duplicado" {
        runTest {
            val dao = mockk<UsuarioDao>()
            val session = mockk<SessionStore>()

            val existente = UsuarioEntity(
                id = 1,
                nombre = "Usuario existente",
                email = "usuario@correo.cl",
                passwordHash = Hash.sha256("Clave12345"),
                rol = Rol.USUARIO.name
            )

            coEvery {
                dao.buscarPorEmail("usuario@correo.cl")
            } returns existente

            val repo = UsuarioRepository(dao, session)

            val resultado = repo.registrar(
                "Otro Usuario",
                "usuario@correo.cl",
                "Clave67890"
            )

            resultado shouldBe null

            coVerify(exactly = 0) {
                dao.insertar(any())
            }
        }
    }

    "Registrar usuario guarda correo normalizado" {
        runTest {
            val dao = mockk<UsuarioDao>()
            val session = mockk<SessionStore>()

            coEvery {
                dao.buscarPorEmail("nuevo@correo.cl")
            } returns null

            coEvery {
                dao.insertar(any())
            } returns 5L

            val repo = UsuarioRepository(dao, session)

            val resultado = repo.registrar(
                "  Usuario Nuevo  ",
                "  NUEVO@CORREO.CL  ",
                "Clave12345"
            )

            resultado shouldBe 5L

            coVerify(exactly = 1) {
                dao.insertar(
                    match {
                        it.nombre == "Usuario Nuevo" &&
                                it.email == "nuevo@correo.cl" &&
                                it.rol == Rol.USUARIO.name &&
                                it.passwordHash == Hash.sha256("Clave12345")
                    }
                )
            }
        }
    }

    "Un residente no puede cambiar roles" {
        runTest {
            val dao = mockk<UsuarioDao>()
            val session = mockk<SessionStore>()

            val residente = UsuarioEntity(
                id = 1,
                nombre = "Residente",
                email = "residente@correo.cl",
                passwordHash = "",
                rol = Rol.USUARIO.name
            )

            every {
                session.usuarioId
            } returns flowOf(1L)

            every {
                dao.observarPorId(1L)
            } returns flowOf(residente)

            val repo = UsuarioRepository(dao, session)

            repo.cambiarRol(
                2L,
                Rol.ADMINISTRADOR
            ) shouldBe false

            coVerify(exactly = 0) {
                dao.cambiarRol(any(), any())
            }
        }
    }

    "Administrador puede cambiar rol de otro usuario" {
        runTest {
            val dao = mockk<UsuarioDao>()
            val session = mockk<SessionStore>()

            val administrador = UsuarioEntity(
                id = 1,
                nombre = "Administrador",
                email = "admin@correo.cl",
                passwordHash = "",
                rol = Rol.ADMINISTRADOR.name
            )

            val destinatario = UsuarioEntity(
                id = 2,
                nombre = "Usuario",
                email = "usuario@correo.cl",
                passwordHash = "",
                rol = Rol.USUARIO.name
            )

            every {
                session.usuarioId
            } returns flowOf(1L)

            every {
                dao.observarPorId(1L)
            } returns flowOf(administrador)

            every {
                dao.observarPorId(2L)
            } returns flowOf(destinatario)

            coJustRun {
                dao.cambiarRol(
                    2L,
                    Rol.FUNCIONARIO.name
                )
            }

            val repo = UsuarioRepository(dao, session)

            repo.cambiarRol(
                2L,
                Rol.FUNCIONARIO
            ) shouldBe true

            coVerify(exactly = 1) {
                dao.cambiarRol(
                    2L,
                    Rol.FUNCIONARIO.name
                )
            }
        }
    }
})