package com.example.seguridapp

import android.app.Application
import android.content.Context
import com.example.seguridapp.data.local.SeguridadDatabase
import com.example.seguridapp.repository.EventoRepository
import com.example.seguridapp.repository.UsuarioRepository
import com.example.seguridapp.data.session.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.seguridapp.repository.PostRepository
import com.example.seguridapp.data.remote.EventosRetrofitInstance


class AppContainer(context: Context) {

    private val db = SeguridadDatabase.obtener(context)

    val sessionStore = SessionStore(context.applicationContext)

    val usuarioRepository = UsuarioRepository(
        db.usuarioDao(),
        sessionStore
    )

    val eventoRepository = EventoRepository(
        EventosRetrofitInstance.api,
        usuarioRepository
    )

    val postRepository = PostRepository()

    suspend fun sembrarDatosDePrueba() {
        usuarioRepository.sembrar()
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