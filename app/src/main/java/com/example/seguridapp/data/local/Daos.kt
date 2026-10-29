package com.example.seguridapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun observarPorId(id: Long): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios ORDER BY nombre")
    fun observarTodos(): Flow<List<UsuarioEntity>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Query("UPDATE usuarios SET rol = :rol WHERE id = :id")
    suspend fun cambiarRol(id: Long, rol: String)
}

@Dao
interface EventoDao {
    @Insert
    suspend fun insertar(evento: EventoEntity): Long

    @Update
    suspend fun actualizar(evento: EventoEntity)

    @Query("SELECT * FROM eventos WHERE id = :id")
    suspend fun obtener(id: Long): EventoEntity?

    @Query("SELECT * FROM eventos ORDER BY fechaCreacion DESC")
    fun observarTodos(): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos WHERE id = :id")
    fun observarPorId(id: Long): Flow<EventoEntity?>

    @Query("SELECT COUNT(*) FROM eventos")
    suspend fun contar(): Int

    @Insert
    suspend fun insertarSeguimiento(seguimiento: SeguimientoEntity): Long

    @Query("SELECT * FROM seguimientos WHERE eventoId = :eventoId ORDER BY fecha DESC, id DESC")
    fun observarSeguimientos(eventoId: Long): Flow<List<SeguimientoEntity>>
}