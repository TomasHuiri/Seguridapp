package com.example.seguridapp

import com.example.seguridapp.data.local.UsuarioEntity
import com.example.seguridapp.data.session.SessionStore
import com.example.seguridapp.repository.UsuarioRepository
import com.example.seguridapp.viewmodel.AuthViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private fun usuarioAutenticado() = UsuarioEntity(
    id = 5L,
    nombre = "Usuario de prueba",
    email = "usuario@correo.cl",
    passwordHash = "hash",
    rol = "USUARIO"
)

private fun sesionSinUsuario(): SessionStore = mockk<SessionStore>().also {
    every { it.usuarioId } returns flowOf(null)
}

private fun rellenarRegistro(vm: AuthViewModel) {
    vm.onRegNombre("Usuario Prueba")
    vm.onRegEmail("usuario@correo.cl")
    vm.onRegPassword("Clave12345")
    vm.onRegConfirmacion("Clave12345")
    vm.onRegTerminos(true)
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest : StringSpec({

    "Sin sesion muestra usuario vacio" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val vm = AuthViewModel(mockk(), sesionSinUsuario())
                advanceUntilIdle()

                vm.sesion.value.cargando shouldBe false
                vm.sesion.value.usuario shouldBe null
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Inicio de sesion valida campos antes de consultar usuario" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<UsuarioRepository>()
                val vm = AuthViewModel(repo, sesionSinUsuario())
                vm.iniciarSesion {}
                advanceUntilIdle()

                (vm.login.value.emailError != null) shouldBe true
                (vm.login.value.passwordError != null) shouldBe true
                coVerify(exactly = 0) { repo.autenticar(any(), any()) }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Inicio de sesion correcto guarda usuario y navega" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<UsuarioRepository>()
                val session = sesionSinUsuario()
                coEvery { repo.autenticar("usuario@correo.cl", "Clave12345") } returns
                    usuarioAutenticado()
                coJustRun { session.guardar(5L) }
                val vm = AuthViewModel(repo, session)
                vm.onLoginEmail("usuario@correo.cl")
                vm.onLoginPassword("Clave12345")
                var navego = false

                vm.iniciarSesion { navego = true }
                advanceUntilIdle()

                navego shouldBe true
                vm.login.value.email shouldBe ""
                coVerify(exactly = 1) { session.guardar(5L) }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Credenciales incorrectas muestran mensaje" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<UsuarioRepository>()
                coEvery { repo.autenticar(any(), any()) } returns null
                val vm = AuthViewModel(repo, sesionSinUsuario())
                vm.onLoginEmail("usuario@correo.cl")
                vm.onLoginPassword("ClaveIncorrecta9")
                var navego = false

                vm.iniciarSesion { navego = true }
                advanceUntilIdle()

                navego shouldBe false
                vm.login.value.cargando shouldBe false
                vm.login.value.errorGeneral shouldBe "Correo o contraseña incorrectos"
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Registro sin terminos aceptados no llama al repositorio" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<UsuarioRepository>()
                val vm = AuthViewModel(repo, sesionSinUsuario())
                rellenarRegistro(vm)
                vm.onRegTerminos(false)

                vm.registrarse {}
                advanceUntilIdle()

                vm.registro.value.terminosError shouldBe
                    "Debes aceptar los términos para continuar"
                coVerify(exactly = 0) { repo.registrar(any(), any(), any(), any()) }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Registro exitoso guarda la sesion y reinicia formulario" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<UsuarioRepository>()
                val session = sesionSinUsuario()
                coEvery {
                    repo.registrar("Usuario Prueba", "usuario@correo.cl", "Clave12345", any())
                } returns 8L
                coJustRun { session.guardar(8L) }
                val vm = AuthViewModel(repo, session)
                rellenarRegistro(vm)
                var navego = false

                vm.registrarse { navego = true }
                advanceUntilIdle()

                navego shouldBe true
                vm.registro.value.nombre shouldBe ""
                coVerify(exactly = 1) { session.guardar(8L) }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Registro de correo existente muestra error" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<UsuarioRepository>()
                coEvery { repo.registrar(any(), any(), any(), any()) } returns null
                val vm = AuthViewModel(repo, sesionSinUsuario())
                rellenarRegistro(vm)

                vm.registrarse {}
                advanceUntilIdle()

                vm.registro.value.emailError shouldBe "Este correo ya está registrado"
                vm.registro.value.cargando shouldBe false
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Cerrar sesion limpia acceso y ejecuta callback" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val session = sesionSinUsuario()
                coJustRun { session.cerrar() }
                val vm = AuthViewModel(mockk(), session)
                var cerro = false

                vm.cerrarSesion { cerro = true }
                advanceUntilIdle()

                cerro shouldBe true
                coVerify(exactly = 1) { session.cerrar() }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }
})
