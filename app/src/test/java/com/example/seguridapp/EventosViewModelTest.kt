package com.example.seguridapp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.seguridapp.data.local.EventoEntity
import com.example.seguridapp.data.model.Categoria
import com.example.seguridapp.data.model.Estado
import com.example.seguridapp.data.model.Prioridad
import com.example.seguridapp.repository.EventoRepository
import com.example.seguridapp.viewmodel.EventosViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.io.IOException

private fun eventoListaPrueba(
    id: Long,
    estado: Estado,
    categoria: Categoria,
    prioridad: Prioridad,
    descripcion: String,
    ubicacion: String
) = EventoEntity(
    id = id,
    codigo = "EVT-%03d".format(id),
    categoria = categoria.name,
    prioridad = prioridad.name,
    estado = estado.name,
    ubicacionRef = ubicacion,
    latitud = null,
    longitud = null,
    descripcion = descripcion,
    fotoPath = null,
    responsable = "Sin asignar",
    creadoPor = 1L,
    fechaCreacion = 1000L
)

@OptIn(ExperimentalCoroutinesApi::class)
class EventosViewModelTest : StringSpec({

    "Carga inicial y resumen calculan correctamente los estados" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val modelos = mutableListOf<EventosViewModel>()
            try {
                val datos = MutableStateFlow(listOf(
                    eventoListaPrueba(1, Estado.PENDIENTE, Categoria.INCENDIO, Prioridad.ALTA,
                        "Incendio en el centro", "Centro"),
                    eventoListaPrueba(2, Estado.EN_PROCESO, Categoria.ROBO, Prioridad.MEDIA,
                        "Robo en el parque", "Parque"),
                    eventoListaPrueba(3, Estado.CERRADO, Categoria.INCENDIO, Prioridad.BAJA,
                        "Incendio controlado", "Norte")
                ))
                val repo = mockk<EventoRepository>()
                every { repo.observarTodos() } returns datos
                coJustRun { repo.refrescar() }
                val vm = EventosViewModel(repo, SavedStateHandle())
                    .also { modelos.add(it) }

                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    vm.resumen.collect { }
                }
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    vm.eventos.collect { }
                }
                advanceUntilIdle()

                vm.resumen.value.total shouldBe 3
                vm.resumen.value.pendientes shouldBe 1
                vm.resumen.value.enProceso shouldBe 1
                vm.resumen.value.cerrados shouldBe 1
                vm.eventos.value.size shouldBe 3
                vm.error.value shouldBe null
            } finally {
                backgroundScope.cancel()
                modelos.forEach { it.viewModelScope.cancel() }
                runCurrent()
                Dispatchers.resetMain()
            }
        }
    }

    "Filtros y busqueda solo devuelven los eventos correspondientes" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val modelos = mutableListOf<EventosViewModel>()
            try {
                val datos = MutableStateFlow(listOf(
                    eventoListaPrueba(1, Estado.PENDIENTE, Categoria.INCENDIO, Prioridad.ALTA,
                        "Fuego en el centro", "Centro"),
                    eventoListaPrueba(2, Estado.CERRADO, Categoria.ROBO, Prioridad.MEDIA,
                        "Robo en un parque", "Parque"),
                    eventoListaPrueba(3, Estado.PENDIENTE, Categoria.INCENDIO, Prioridad.BAJA,
                        "Humo en edificio", "Norte")
                ))
                val repo = mockk<EventoRepository>()
                every { repo.observarTodos() } returns datos
                coJustRun { repo.refrescar() }
                val vm = EventosViewModel(repo, SavedStateHandle())
                    .also { modelos.add(it) }
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    vm.eventos.collect { }
                }
                advanceUntilIdle()

                vm.onEstado(Estado.PENDIENTE)
                advanceUntilIdle()
                vm.eventos.value.map { it.id } shouldBe listOf(1L, 3L)

                vm.onCategoria(Categoria.INCENDIO)
                vm.onPrioridad(Prioridad.ALTA)
                advanceUntilIdle()
                vm.eventos.value.map { it.id } shouldBe listOf(1L)

                vm.onBusqueda("CENTRO")
                advanceUntilIdle()
                vm.eventos.value.map { it.id } shouldBe listOf(1L)

                vm.onBusqueda("no-existe")
                advanceUntilIdle()
                vm.eventos.value.size shouldBe 0

                vm.onBusqueda("")
                vm.onEstado(null)
                vm.onCategoria(null)
                vm.onPrioridad(null)
                advanceUntilIdle()
                vm.eventos.value.size shouldBe 3
            } finally {
                backgroundScope.cancel()
                modelos.forEach { it.viewModelScope.cancel() }
                runCurrent()
                Dispatchers.resetMain()
            }
        }
    }

    "Filtro inicial puede recibirse desde la navegacion" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val modelos = mutableListOf<EventosViewModel>()
            try {
                val repo = mockk<EventoRepository>()
                every { repo.observarTodos() } returns MutableStateFlow(emptyList())
                coJustRun { repo.refrescar() }
                val vm = EventosViewModel(
                    repo,
                    SavedStateHandle(mapOf("estado" to "EN_PROCESO"))
                ).also { modelos.add(it) }
                advanceUntilIdle()
                vm.filtroEstado.value shouldBe Estado.EN_PROCESO
            } finally {
                backgroundScope.cancel()
                modelos.forEach { it.viewModelScope.cancel() }
                runCurrent()
                Dispatchers.resetMain()
            }
        }
    }

    "Fallo al consultar servidor muestra error y permite reintento" {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val modelos = mutableListOf<EventosViewModel>()
            try {
                val repo = mockk<EventoRepository>()
                every { repo.observarTodos() } returns MutableStateFlow(emptyList())
                coEvery { repo.refrescar() } throws IOException("Sin conexion")
                val vm = EventosViewModel(repo, SavedStateHandle())
                    .also { modelos.add(it) }
                advanceUntilIdle()
                vm.error.value shouldBe "No se pudieron cargar los eventos del servidor"

                coJustRun { repo.refrescar() }
                vm.cargar()
                advanceUntilIdle()
                vm.error.value shouldBe null
            } finally {
                backgroundScope.cancel()
                modelos.forEach { it.viewModelScope.cancel() }
                runCurrent()
                Dispatchers.resetMain()
            }
        }
    }
})
