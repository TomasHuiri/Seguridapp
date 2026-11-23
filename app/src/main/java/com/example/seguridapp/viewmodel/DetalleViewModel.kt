package com.example.seguridapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguridapp.data.local.EventoEntity
import com.example.seguridapp.data.local.SeguimientoEntity
import com.example.seguridapp.repository.EventoRepository
import com.example.seguridapp.repository.Validators
import kotlinx.coroutines.CancellationException
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

    private val eventoId: Long =
        savedStateHandle.get<Long>("id") ?: -1L

    val evento: StateFlow<EventoEntity?> =
        repo.observarPorId(eventoId)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    val seguimientos: StateFlow<List<SeguimientoEntity>> =
        repo.observarSeguimientos(eventoId)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    private val _ui = MutableStateFlow(DetalleUi())
    val ui = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repo.refrescarDetalle(eventoId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _ui.update {
                    it.copy(
                        mensaje = "No se pudo consultar el evento del servidor"
                    )
                }
            }
        }
    }

    fun onObservacion(v: String) = _ui.update {
        it.copy(
            observacion = v,
            observacionError = if (v.isEmpty()) null
            else Validators.observacion(v)
        )
    }

    fun onMotivo(v: String) = _ui.update {
        it.copy(
            motivo = v,
            motivoError = if (v.isEmpty()) null
            else Validators.motivo(v)
        )
    }

    fun mensajeMostrado() = _ui.update {
        it.copy(mensaje = null)
    }

    private fun ejecutar(
        operacion: suspend () -> Boolean,
        mensajeExito: String,
        alExito: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val exito = operacion()

                _ui.update {
                    it.copy(
                        mensaje = if (exito)
                            mensajeExito
                        else
                            "No tienes permiso o la operación no está permitida"
                    )
                }

                if (exito) alExito()

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _ui.update {
                    it.copy(
                        mensaje = "No se pudo completar la operación en el servidor"
                    )
                }
            }
        }
    }

    fun agregarObservacion(autor: String) {
        val texto = _ui.value.observacion
        val error = Validators.observacion(texto)

        if (error != null) {
            _ui.update {
                it.copy(observacionError = error)
            }
            return
        }

        ejecutar(
            operacion = {
                repo.agregarObservacion(eventoId, texto, autor)
            },
            mensajeExito = "Observación registrada",
            alExito = {
                _ui.update {
                    it.copy(
                        observacion = "",
                        observacionError = null
                    )
                }
            }
        )
    }

    fun avanzarEstado(autor: String) {
        ejecutar(
            operacion = {
                repo.avanzarEstado(eventoId, autor)
            },
            mensajeExito = "Estado actualizado"
        )
    }

    fun asignar(unidad: String, autor: String) {
        ejecutar(
            operacion = {
                repo.asignar(eventoId, unidad, autor)
            },
            mensajeExito = "Caso derivado correctamente"
        )
    }

    fun cancelar(
        autor: String,
        onExito: () -> Unit
    ) {
        val motivo = _ui.value.motivo
        val error = Validators.motivo(motivo)

        if (error != null) {
            _ui.update {
                it.copy(motivoError = error)
            }
            return
        }

        ejecutar(
            operacion = {
                repo.cancelar(eventoId, motivo, autor)
            },
            mensajeExito = "Evento cancelado",
            alExito = {
                _ui.update {
                    it.copy(
                        motivo = "",
                        motivoError = null
                    )
                }
                onExito()
            }
        )
    }

    fun limpiarCancelacion() = _ui.update {
        it.copy(
            motivo = "",
            motivoError = null
        )
    }
}