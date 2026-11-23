package com.example.seguridapp.repository

import com.example.seguridapp.data.local.UsuarioDao
import com.example.seguridapp.data.local.UsuarioEntity
import com.example.seguridapp.data.session.SessionStore
import com.example.seguridapp.data.model.Rol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class UsuarioRepository(
    private val dao: UsuarioDao,
    private val session: SessionStore
) {

    fun observar(id: Long): Flow<UsuarioEntity?> = dao.observarPorId(id)

    fun observarTodos(): Flow<List<UsuarioEntity>> = dao.observarTodos()

    suspend fun autenticar(email: String, password: String): UsuarioEntity? {
        val usuario = dao.buscarPorEmail(email.trim().lowercase()) ?: return null
        return if (usuario.passwordHash == Hash.sha256(password)) usuario else null
    }

    suspend fun registrar(nombre: String, email: String, password: String, rol: Rol = Rol.USUARIO): Long? {
        val correo = email.trim().lowercase()
        if (dao.buscarPorEmail(correo) != null) return null
        return dao.insertar(
            UsuarioEntity(
                nombre = nombre.trim(),
                email = correo,
                passwordHash = Hash.sha256(password),
                rol = rol.name
            )
        )
    }

    suspend fun usuarioActual(): UsuarioEntity? {
        val id = session.usuarioId.first() ?: return null
        return dao.observarPorId(id).first()
    }

    suspend fun cambiarRol(id: Long, rol: Rol): Boolean {
        val actual = usuarioActual() ?: return false

        if (!Rol.desde(actual.rol).esAdmin) return false
        if (actual.id == id) return false
        if (dao.observarPorId(id).first() == null) return false

        dao.cambiarRol(id, rol.name)
        return true
    }


    suspend fun sembrar() {
        if (dao.contar() > 0) return
        registrar("Usuario de prueba", "usuario@segurityapp.cl", "Segurity123", Rol.USUARIO)
        registrar("Funcionario de prueba", "funcionario@segurityapp.cl", "Segurity123", Rol.FUNCIONARIO)
        registrar("Supervisor de prueba", "supervisor@segurityapp.cl", "Segurity123", Rol.SUPERVISOR)
        registrar("Administrador de prueba", "admin@segurityapp.cl", "Segurity123", Rol.ADMINISTRADOR)
    }
}