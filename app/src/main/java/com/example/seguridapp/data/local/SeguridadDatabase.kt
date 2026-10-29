package com.example.seguridapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UsuarioEntity::class, EventoEntity::class, SeguimientoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SeguridadDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun eventoDao(): EventoDao

    companion object {
        @Volatile
        private var instancia: SeguridadDatabase? = null

        fun obtener(context: Context): SeguridadDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    SeguridadDatabase::class.java,
                    "segurity.db"
                ).build().also { instancia = it }
            }
    }
}