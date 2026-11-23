package com.example.seguridapp

import androidx.lifecycle.SavedStateHandle
import com.example.seguridapp.data.local.EventoEntity
import com.example.seguridapp.data.local.SeguimientoEntity
import com.example.seguridapp.repository.EventoRepository
import com.example.seguridapp.viewmodel.DetalleViewModel
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
import java.io.IOException

private fun repoDetalleSimulado(): EventoRepository {
    val repo = mockk<EventoRepository>()
    every { repo.observarPorId(10L) } returns flowOf<EventoEntity?>(null)
    every { repo.observarSeguimientos(10L) } returns
        flowOf<List<SeguimientoEntity>>(emptyList())
    coJustRun { repo.refrescarDetalle(10L) }
    return repo
}

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleViewModelTest : StringSpec({

    "Al abrir detalle solicita evento e historial del servidor" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = repoDetalleSimulado()
                DetalleViewModel(SavedStateHandle(mapOf("id" to 10L)), repo)
                advanceUntilIdle()

                coVerify(exactly = 1) { repo.refrescarDetalle(10L) }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Fallo al abrir detalle muestra error" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = repoDetalleSimulado()
                coEvery { repo.refrescarDetalle(10L) } throws IOException("Sin conexion")
                val vm = DetalleViewModel(SavedStateHandle(mapOf("id" to 10L)), repo)
                advanceUntilIdle()

                vm.ui.value.mensaje shouldBe "No se pudo consultar el evento del servidor"
                vm.mensajeMostrado()
                vm.ui.value.mensaje shouldBe null
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Observacion corta impide enviar una peticion" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = repoDetalleSimulado()
                val vm = DetalleViewModel(SavedStateHandle(mapOf("id" to 10L)), repo)
                vm.onObservacion("abc")
                vm.agregarObservacion("Funcionario")
                advanceUntilIdle()

                vm.ui.value.observacionError shouldBe "Debe tener al menos 5 caracteres"
                coVerify(exactly = 0) { repo.agregarObservacion(any(), any(), any()) }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Observacion exitosa limpia el formulario" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = repoDetalleSimulado()
                coEvery {
                    repo.agregarObservacion(10L, "Incidente revisado", "Funcionario")
                } returns true
                val vm = DetalleViewModel(SavedStateHandle(mapOf("id" to 10L)), repo)
                vm.onObservacion("Incidente revisado")
                vm.agregarObservacion("Funcionario")
                advanceUntilIdle()

                vm.ui.value.observacion shouldBe ""
                vm.ui.value.observacionError shouldBe null
                vm.ui.value.mensaje shouldBe "Observación registrada"
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Operación sin permiso informa rechazo" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = repoDetalleSimulado()
                coEvery { repo.avanzarEstado(10L, "Residente") } returns false
                val vm = DetalleViewModel(SavedStateHandle(mapOf("id" to 10L)), repo)
                vm.avanzarEstado("Residente")
                advanceUntilIdle()

                vm.ui.value.mensaje shouldBe
                    "No tienes permiso o la operación no está permitida"
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Error de servidor al avanzar estado informa fallo" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = repoDetalleSimulado()
                coEvery { repo.avanzarEstado(10L, "Funcionario") } throws IOException("Error")
                val vm = DetalleViewModel(SavedStateHandle(mapOf("id" to 10L)), repo)
                vm.avanzarEstado("Funcionario")
                advanceUntilIdle()

                vm.ui.value.mensaje shouldBe
                    "No se pudo completar la operación en el servidor"
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Asignacion correcta muestra confirmacion" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = repoDetalleSimulado()
                coEvery {
                    repo.asignar(10L, "Unidad de Bomberos (simulada)", "Supervisor")
                } returns true
                val vm = DetalleViewModel(SavedStateHandle(mapOf("id" to 10L)), repo)
                vm.asignar("Unidad de Bomberos (simulada)", "Supervisor")
                advanceUntilIdle()

                vm.ui.value.mensaje shouldBe "Caso derivado correctamente"
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Cancelacion sin motivo valido no llama al repositorio" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = repoDetalleSimulado()
                val vm = DetalleViewModel(SavedStateHandle(mapOf("id" to 10L)), repo)
                var navego = false
                vm.onMotivo("No")
                vm.cancelar("Residente") { navego = true }
                advanceUntilIdle()

                vm.ui.value.motivoError shouldBe
                    "Explica el motivo con al menos 5 caracteres"
                navego shouldBe false
                coVerify(exactly = 0) { repo.cancelar(any(), any(), any()) }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Cancelacion exitosa confirma y limpia el motivo" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repo = repoDetalleSimulado()
                coEvery {
                    repo.cancelar(10L, "Registro duplicado", "Residente")
                } returns true
                val vm = DetalleViewModel(SavedStateHandle(mapOf("id" to 10L)), repo)
                var navego = false
                vm.onMotivo("Registro duplicado")
                vm.cancelar("Residente") { navego = true }
                advanceUntilIdle()

                navego shouldBe true
                vm.ui.value.motivo shouldBe ""
                vm.ui.value.motivoError shouldBe null
                vm.ui.value.mensaje shouldBe "Evento cancelado"
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Limpiar cancelacion borra motivo y error" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val vm = DetalleViewModel(
                    SavedStateHandle(mapOf("id" to 10L)),
                    repoDetalleSimulado()
                )
                vm.onMotivo("No")
                vm.limpiarCancelacion()
                advanceUntilIdle()

                vm.ui.value.motivo shouldBe ""
                vm.ui.value.motivoError shouldBe null
            } finally {
                Dispatchers.resetMain()
            }
        }
    }
})
