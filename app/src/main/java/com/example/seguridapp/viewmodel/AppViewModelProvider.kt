package com.example.seguridapp.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.seguridapp.SeguridApp

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { AuthViewModel(app().container.usuarioRepository, app().container.sessionStore) }
        initializer { EventoFormViewModel(app().container.eventoRepository) }
        initializer { EventosViewModel(app().container.eventoRepository, this.createSavedStateHandle()) }
        initializer { DetalleViewModel(this.createSavedStateHandle(), app().container.eventoRepository) }
        initializer { UsuariosViewModel(app().container.usuarioRepository) }
    }
}

fun CreationExtras.app(): SeguridApp = this[APPLICATION_KEY] as SeguridApp
