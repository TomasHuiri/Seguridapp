package com.example.seguridapp

import com.example.seguridapp.data.local.UsuarioEntity
import com.example.seguridapp.data.model.Categoria
import com.example.seguridapp.data.model.Prioridad
import com.example.seguridapp.repository.EventoRepository
import com.example.seguridapp.viewmodel.EventoFormViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.io.IOException

private fun personaFormulario() = UsuarioEntity(
    id = 7L,
    nombre = "Persona de prueba",
    email = "persona@correo.cl",
    passwordHash = "hash",
    rol = "USUARIO"
)

private fun completarFormulario(vm: EventoFormViewModel) {
    vm.onCategoria(Categoria.INCENDIO)
    vm.onPrioridad(Prioridad.ALTA)
    vm.onUbicacionRef("Sector Centro")
    vm.onDescripcion("Se detecta un incendio en el sector")
}

@OptIn(ExperimentalCoroutinesApi::class)
class EventoFormViewModelTest : StringSpec({

    "Formulario comienza vacio y sin errores" {
        val vm = EventoFormViewModel(mockk())
        vm.estado.value.categoria shouldBe null
        vm.estado.value.prioridad shouldBe null
        vm.estado.value.ubicacionRef shouldBe ""
        vm.estado.value.guardando shouldBe false
        vm.estado.value.errores.hayErrores shouldBe false
    }

    "Seleccionar categoria y prioridad guarda los datos" {
        val vm = EventoFormViewModel(mockk())
        vm.onCategoria(Categoria.INCENDIO)
        vm.onPrioridad(Prioridad.ALTA)

        vm.estado.value.categoria shouldBe Categoria.INCENDIO
        vm.estado.value.prioridad shouldBe Prioridad.ALTA
    }

    "Ubicacion y descripcion cortas muestran errores" {
        val vm = EventoFormViewModel(mockk())
        vm.onUbicacionRef("AB")
        vm.onDescripcion("Corto")

        (vm.estado.value.errores.ubicacion != null) shouldBe true
        (vm.estado.value.errores.descripcion != null) shouldBe true
    }

    "Al corregir campos desaparecen sus errores" {
        val vm = EventoFormViewModel(mockk())
        vm.onUbicacionRef("AB")
        vm.onDescripcion("Corto")
        completarFormulario(vm)

        vm.estado.value.errores.hayErrores shouldBe false
    }

    "Elegir coordenadas agrega ubicacion de referencia" {
        val vm = EventoFormViewModel(mockk())
        vm.onUbicacion(-33.45, -70.66)

        vm.estado.value.latitud shouldBe -33.45
        vm.estado.value.longitud shouldBe -70.66
        vm.estado.value.ubicacionRef shouldBe "Ubicación seleccionada (GPS o mapa)"
    }

    "Quitar coordenadas no borra la referencia" {
        val vm = EventoFormViewModel(mockk())
        vm.onUbicacionRef("Sector Centro")
        vm.onUbicacion(-33.45, -70.66)
        vm.quitarUbicacion()

        vm.estado.value.latitud shouldBe null
        vm.estado.value.longitud shouldBe null
        vm.estado.value.ubicacionRef shouldBe "Sector Centro"
    }

    "Formulario incompleto no llama al repositorio" {
        val repo = mockk<EventoRepository>()
        val vm = EventoFormViewModel(repo)

        vm.registrar(personaFormulario()) {} shouldBe false
        vm.estado.value.errores.hayErrores shouldBe true
        coVerify(exactly = 0) {
            repo.crear(any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    "Formulario con fotografia se rechaza antes del POST" {
        val repo = mockk<EventoRepository>()
        val vm = EventoFormViewModel(repo)
        completarFormulario(vm)
        vm.onFoto("/archivo/imagen.jpg")

        vm.registrar(personaFormulario()) {} shouldBe false
        (vm.estado.value.errorGeneral?.contains("fotografías") == true) shouldBe true
        coVerify(exactly = 0) {
            repo.crear(any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    "Formulario valido crea un evento y reinicia el estado" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<EventoRepository>()
                coEvery {
                    repo.crear(any(), any(), any(), any(), any(), any(), any(), any(), any())
                } returns 45L
                val vm = EventoFormViewModel(repo)
                completarFormulario(vm)
                var recibido: Long? = null

                vm.registrar(personaFormulario()) { recibido = it } shouldBe true
                advanceUntilIdle()

                recibido shouldBe 45L
                vm.estado.value.categoria shouldBe null
                vm.estado.value.guardando shouldBe false
                vm.estado.value.errorGeneral shouldBe null
                coVerify(exactly = 1) {
                    repo.crear(
                        Categoria.INCENDIO,
                        Prioridad.ALTA,
                        "Sector Centro",
                        null,
                        null,
                        "Se detecta un incendio en el sector",
                        null,
                        7L,
                        "Persona de prueba"
                    )
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Error del servidor mantiene datos para volver a intentar" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = mockk<EventoRepository>()
                coEvery {
                    repo.crear(any(), any(), any(), any(), any(), any(), any(), any(), any())
                } throws IOException("Servidor apagado")
                val vm = EventoFormViewModel(repo)
                completarFormulario(vm)
                var navego = false

                vm.registrar(personaFormulario()) { navego = true }
                advanceUntilIdle()

                navego shouldBe false
                vm.estado.value.guardando shouldBe false
                vm.estado.value.categoria shouldBe Categoria.INCENDIO
                vm.estado.value.ubicacionRef shouldBe "Sector Centro"
                vm.estado.value.errorGeneral shouldBe
                    "No se pudo registrar el evento en el servidor"
            } finally {
                Dispatchers.resetMain()
            }
        }
    }
})
