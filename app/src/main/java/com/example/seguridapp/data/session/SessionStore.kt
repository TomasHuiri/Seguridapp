package com.example.seguridapp.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.text.get

private val Context.sesionDataStore by preferencesDataStore(name = "sesion")

class SessionStore(private val context: Context) {
    private val claveUsuario = longPreferencesKey("usuario_id")

    val usuarioId: Flow<Long?> = context.sesionDataStore.data.map { it[claveUsuario] }

    suspend fun guardar(id: Long) {
        context.sesionDataStore.edit { it[claveUsuario] = id }
    }

    suspend fun cerrar() {
        context.sesionDataStore.edit { it.remove(claveUsuario) }
    }
}