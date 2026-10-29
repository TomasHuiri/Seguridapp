package com.example.seguridapp

import android.app.Application
import android.content.Context
import com.example.seguridapp.data.local.SeguridadDatabase
import com.example.seguridapp.data.repository.EventoRepository
import com.example.seguridapp.data.repository.UsuarioRepository
import com.example.seguridapp.data.session.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    private val db = SeguridadDatabase.obtener(context)
    val usuarioRepository = UsuarioRepository(db.usuarioDao())
    val eventoRepository = EventoRepository(db.eventoDao())
    val sessionStore = SessionStore(context.applicationContext)

    suspend fun sembrarDatosDePrueba() {
        usuarioRepository.sembrar()
        eventoRepository.sembrar()
    }
}

class SeguridApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        CoroutineScope(Dispatchers.IO).launch { container.sembrarDatosDePrueba() }
    }
}