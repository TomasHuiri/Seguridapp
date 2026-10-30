package com.example.seguridapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguridapp.data.local.UsuarioEntity
import com.example.seguridapp.data.repository.EventoRepository
import com.example.seguridapp.model.Categoria
import com.example.seguridapp.model.Prioridad
import com.example.seguridapp.util.Validators
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventoErrores(
    val categoria: String? = null,
    val prioridad: String? = null,
    val ubicacion: String? = null,
    val descripcion: String? = null
) {
    val hayErrores get() = listOf(categoria, prioridad, ubicacion, descripcion).any { it != null }
}

data class EventoFormState(
    val categoria: Categoria? = null,
    val prioridad: Prioridad? = null,
    val ubicacionRef: String = "",
    val descripcion: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoPath: String? = null,
    val errores: EventoErrores = EventoErrores(),
    val guardando: Boolean = false
)

class EventoFormViewModel(private val repo: EventoRepository) : ViewModel() {

    private val _estado = MutableStateFlow(EventoFormState())
    val estado = _estado.asStateFlow()

    fun onCategoria(c: Categoria) = _estado.update {
        it.copy(categoria = c, errores = it.errores.copy(categoria = null))
    }

    fun onPrioridad(p: Prioridad) = _estado.update {
        it.copy(prioridad = p, errores = it.errores.copy(prioridad = null))
    }

    fun onUbicacionRef(v: String) = _estado.update {
        it.copy(ubicacionRef = v, errores = it.errores.copy(ubicacion = if (v.isEmpty()) null else Validators.ubicacion(v)))
    }

    fun onDescripcion(v: String) = _estado.update {
        it.copy(descripcion = v, errores = it.errores.copy(descripcion = if (v.isEmpty()) null else Validators.descripcion(v)))
    }

        fun onUbicacion(lat: Double, lng: Double) = _estado.update {
        it.copy(
            latitud = lat,
            longitud = lng,
            ubicacionRef = if (it.ubicacionRef.isBlank()) "Ubicación seleccionada (GPS o mapa)" else it.ubicacionRef,
            errores = it.errores.copy(ubicacion = null)
        )
    }

    fun quitarUbicacion() = _estado.update { it.copy(latitud = null, longitud = null) }

    fun onFoto(path: String?) = _estado.update { it.copy(fotoPath = path) }

    private fun validar(): Boolean {
        val s = _estado.value
        val errores = EventoErrores(
            categoria = Validators.seleccion(s.categoria, "una categoría"),
            prioridad = Validators.seleccion(s.prioridad, "una prioridad"),
            ubicacion = Validators.ubicacion(s.ubicacionRef),
            descripcion = Validators.descripcion(s.descripcion)
        )
        _estado.update { it.copy(errores = errores) }
        return !errores.hayErrores
    }

        fun registrar(usuario: UsuarioEntity, onExito: (Long) -> Unit): Boolean {
        if (!validar()) return false
        val s = _estado.value
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            delay(500)
            val id = repo.crear(
                categoria = s.categoria!!,
                prioridad = s.prioridad!!,
                ubicacionRef = s.ubicacionRef,
                latitud = s.latitud,
                longitud = s.longitud,
                descripcion = s.descripcion,
                fotoPath = s.fotoPath,
                creadoPor = usuario.id,
                autor = usuario.nombre
            )
            _estado.value = EventoFormState()
            onExito(id)
        }
        return true
    }
}
