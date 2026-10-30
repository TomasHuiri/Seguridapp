package com.example.seguridapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguridapp.data.local.UsuarioEntity
import com.example.seguridapp.data.repository.UsuarioRepository
import com.example.seguridapp.model.Rol
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UsuariosViewModel(private val repo: UsuarioRepository) : ViewModel() {
    val usuarios: StateFlow<List<UsuarioEntity>> = repo.observarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cambiarRol(id: Long, rol: Rol) {
        viewModelScope.launch { repo.cambiarRol(id, rol) }
    }
}
