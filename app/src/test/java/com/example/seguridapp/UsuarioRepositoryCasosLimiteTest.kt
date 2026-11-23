package com.example.seguridapp

import com.example.seguridapp.data.local.UsuarioDao
import com.example.seguridapp.data.local.UsuarioEntity
import com.example.seguridapp.data.model.Rol
import com.example.seguridapp.data.session.SessionStore
import com.example.seguridapp.repository.UsuarioRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

private fun usuarioParaLimites(
    id: Long = 1L,
    rol: Rol = Rol.USUARIO
) = UsuarioEntity(
    id = id,
    nombre = "Persona de prueba",
    email = "persona@correo.cl",
    passwordHash = "hash",
    rol = rol.name
)

class UsuarioRepositoryCasosLimiteTest : StringSpec({

    "Autenticar rechaza un correo que no existe" {
        runTest {
            val dao = mockk<UsuarioDao>()
            val sesion = mockk<SessionStore>()
            coEvery { dao.buscarPorEmail("ausente@correo.cl") } returns null
            val repo = UsuarioRepository(dao, sesion)

            repo.autenticar(" AUSENTE@CORREO.CL ", "Clave12345") shouldBe null
            coVerify(exactly = 1) { dao.buscarPorEmail("ausente@correo.cl") }
        }
    }

    "Usuario actual es nulo cuando no hay sesion" {
        runTest {
            val dao = mockk<UsuarioDao>()
            val sesion = mockk<SessionStore>()
            every { sesion.usuarioId } returns flowOf(null)
            val repo = UsuarioRepository(dao, sesion)

            repo.usuarioActual() shouldBe null
            coVerify(exactly = 0) { dao.buscarPorEmail(any()) }
        }
    }

    "Usuario actual se recupera con el identificador de la sesion" {
        runTest {
            val dao = mockk<UsuarioDao>()
            val sesion = mockk<SessionStore>()
            val usuario = usuarioParaLimites(7L)
            every { sesion.usuarioId } returns flowOf(7L)
            every { dao.observarPorId(7L) } returns flowOf(usuario)
            val repo = UsuarioRepository(dao, sesion)

            repo.usuarioActual() shouldBe usuario
        }
    }

    "Observar usuario y listado delega las consultas al DAO" {
        runTest {
            val dao = mockk<UsuarioDao>()
            val sesion = mockk<SessionStore>()
            val usuario = usuarioParaLimites()
            every { dao.observarPorId(1L) } returns flowOf(usuario)
            every { dao.observarTodos() } returns flowOf(listOf(usuario))
            val repo = UsuarioRepository(dao, sesion)

            repo.observar(1L).first() shouldBe usuario
            repo.observarTodos().first() shouldBe listOf(usuario)
        }
    }

    "Sin sesion no se permite cambiar roles" {
        runTest {
            val dao = mockk<UsuarioDao>()
            val sesion = mockk<SessionStore>()
            every { sesion.usuarioId } returns flowOf(null)
            val repo = UsuarioRepository(dao, sesion)

            repo.cambiarRol(2L, Rol.FUNCIONARIO) shouldBe false
            coVerify(exactly = 0) { dao.cambiarRol(any(), any()) }
        }
    }

    "Administrador no puede cambiar su propio rol" {
        runTest {
            val dao = mockk<UsuarioDao>()
            val sesion = mockk<SessionStore>()
            every { sesion.usuarioId } returns flowOf(1L)
            every { dao.observarPorId(1L) } returns flowOf(usuarioParaLimites(1L, Rol.ADMINISTRADOR))
            val repo = UsuarioRepository(dao, sesion)

            repo.cambiarRol(1L, Rol.USUARIO) shouldBe false
            coVerify(exactly = 0) { dao.cambiarRol(any(), any()) }
        }
    }

    "Administrador no puede cambiar el rol de un usuario inexistente" {
        runTest {
            val dao = mockk<UsuarioDao>()
            val sesion = mockk<SessionStore>()
            every { sesion.usuarioId } returns flowOf(1L)
            every { dao.observarPorId(1L) } returns flowOf(usuarioParaLimites(1L, Rol.ADMINISTRADOR))
            every { dao.observarPorId(99L) } returns flowOf(null)
            val repo = UsuarioRepository(dao, sesion)

            repo.cambiarRol(99L, Rol.FUNCIONARIO) shouldBe false
            coVerify(exactly = 0) { dao.cambiarRol(any(), any()) }
        }
    }

    "Sembrar no agrega usuarios cuando la base ya contiene registros" {
        runTest {
            val dao = mockk<UsuarioDao>()
            val sesion = mockk<SessionStore>()
            coEvery { dao.contar() } returns 5
            val repo = UsuarioRepository(dao, sesion)

            repo.sembrar()
            coVerify(exactly = 0) { dao.insertar(any()) }
        }
    }

    "Sembrar crea los cuatro perfiles cuando la base esta vacia" {
        runTest {
            val dao = mockk<UsuarioDao>()
            val sesion = mockk<SessionStore>()
            coEvery { dao.contar() } returns 0
            coEvery { dao.buscarPorEmail(any()) } returns null
            coEvery { dao.insertar(any()) } returns 1L
            val repo = UsuarioRepository(dao, sesion)

            repo.sembrar()

            coVerify(exactly = 4) { dao.insertar(any()) }
            coVerify(exactly = 1) { dao.insertar(match { it.rol == Rol.USUARIO.name }) }
            coVerify(exactly = 1) { dao.insertar(match { it.rol == Rol.FUNCIONARIO.name }) }
            coVerify(exactly = 1) { dao.insertar(match { it.rol == Rol.SUPERVISOR.name }) }
            coVerify(exactly = 1) { dao.insertar(match { it.rol == Rol.ADMINISTRADOR.name }) }
        }
    }
})
