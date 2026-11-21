package com.example.seguridapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguridapp.data.local.EventoEntity
import com.example.seguridapp.data.local.SeguimientoEntity
import com.example.seguridapp.data.repository.EventoRepository
import com.example.seguridapp.data.repository.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleUi(
    val observacion: String = "",
    val observacionError: String? = null,
    val motivo: String = "",
    val motivoError: String? = null,
    val mensaje: String? = null
)

class DetalleViewModel(
    savedStateHandle: SavedStateHandle,
    private val repo: EventoRepository
) : ViewModel() {

    private val eventoId: Long = savedStateHandle.get<Long>("id") ?: -1L

    val evento: StateFlow<EventoEntity?> = repo.observarPorId(eventoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val seguimientos: StateFlow<List<SeguimientoEntity>> = repo.observarSeguimientos(eventoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _ui = MutableStateFlow(DetalleUi())
    val ui = _ui.asStateFlow()

    fun onObservacion(v: String) = _ui.update {
        it.copy(observacion = v, observacionError = if (v.isEmpty()) null else Validators.observacion(v))
    }

    fun onMotivo(v: String) = _ui.update {
        it.copy(motivo = v, motivoError = if (v.isEmpty()) null else Validators.motivo(v))
    }

    fun mensajeMostrado() = _ui.update { it.copy(mensaje = null) }

    fun agregarObservacion(autor: String) {
        val error = Validators.observacion(_ui.value.observacion)
        if (error != null) {
            _ui.update { it.copy(observacionError = error) }
            return
        }

        viewModelScope.launch {
            val exito = repo.agregarObservacion(
                eventoId, _ui.value.observacion, autor
            )

            _ui.update {
                if (exito) it.copy(
                    observacion = "",
                    observacionError = null,
                    mensaje = "Observación registrada"
                )
                else it.copy(mensaje = "No se pudo agregar la observación")
            }
        }
    }
    fun avanzarEstado(autor: String) {
        viewModelScope.launch {
            val exito = repo.avanzarEstado(eventoId, autor)

            _ui.update {
                it.copy(mensaje = if (exito)
                    "Estado actualizado"
                else
                    "No tienes permiso o el estado no permite avanzar")
            }
        }
    }

    fun asignar(unidad: String, autor: String) {
        viewModelScope.launch {
            val exito = repo.asignar(eventoId, unidad, autor)

            _ui.update {
                it.copy(mensaje = if (exito)
                    "Caso derivado correctamente"
                else
                    "No se pudo derivar el evento")
            }
        }
    }


    fun cancelar(autor: String, onExito: () -> Unit) {
        val error = Validators.motivo(_ui.value.motivo)
        if (error != null) {
            _ui.update { it.copy(motivoError = error) }
            return
        }

        viewModelScope.launch {
            val exito = repo.cancelar(eventoId, _ui.value.motivo, autor)

            _ui.update {
                if (exito) it.copy(
                    motivo = "",
                    motivoError = null,
                    mensaje = "Evento cancelado"
                )
                else it.copy(mensaje = "No se pudo cancelar el evento")
            }

            if (exito) onExito()
        }
    }

    fun limpiarCancelacion() = _ui.update { it.copy(motivo = "", motivoError = null) }
}
