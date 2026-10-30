package com.example.seguridapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguridapp.data.local.EventoEntity
import com.example.seguridapp.data.repository.EventoRepository
import com.example.seguridapp.model.Categoria
import com.example.seguridapp.model.Estado
import com.example.seguridapp.model.Prioridad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.SavedStateHandle

data class ResumenUi(val total: Int = 0, val pendientes: Int = 0, val enProceso: Int = 0, val cerrados: Int = 0)


class EventosViewModel(repo: EventoRepository, savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    private val _estado = MutableStateFlow(savedStateHandle.get<String>("estado")?.let { nombre -> Estado.entries.firstOrNull { it.name == nombre }})

    private val _categoria = MutableStateFlow<Categoria?>(null)
    private val _prioridad = MutableStateFlow<Prioridad?>(null)

    val busqueda = _busqueda.asStateFlow()
    val filtroEstado = _estado.asStateFlow()
    val filtroCategoria = _categoria.asStateFlow()
    val filtroPrioridad = _prioridad.asStateFlow()

    private val todos = repo.observarTodos()

    val resumen: StateFlow<ResumenUi> = todos
        .map { lista ->
            ResumenUi(
                total = lista.size,
                pendientes = lista.count { it.estado == Estado.PENDIENTE.name },
                enProceso = lista.count { it.estado == Estado.EN_PROCESO.name },
                cerrados = lista.count { it.estado == Estado.CERRADO.name }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResumenUi())

        val eventos: StateFlow<List<EventoEntity>> = combine(
        todos, _busqueda, _estado, _categoria, _prioridad
    ) { lista, q, estado, categoria, prioridad ->
        lista.filter { e ->
            (estado == null || e.estado == estado.name) &&
                (categoria == null || e.categoria == categoria.name) &&
                (prioridad == null || e.prioridad == prioridad.name) &&
                (q.isBlank() ||
                    e.codigo.contains(q, ignoreCase = true) ||
                    e.descripcion.contains(q, ignoreCase = true) ||
                    e.ubicacionRef.contains(q, ignoreCase = true) ||
                    Categoria.desde(e.categoria).etiqueta.contains(q, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onBusqueda(v: String) { _busqueda.value = v }
    fun onEstado(e: Estado?) { _estado.value = e }
    fun onCategoria(c: Categoria?) { _categoria.value = c }
    fun onPrioridad(p: Prioridad?) { _prioridad.value = p }
}
