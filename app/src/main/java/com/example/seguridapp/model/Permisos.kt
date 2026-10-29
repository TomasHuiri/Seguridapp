package com.example.seguridapp.model

object Permisos {
    fun puedeCambiarEstado(rol: Rol, estado: Estado) =
        rol.puedeGestionar && !estado.esFinal

    fun puedeObservar(rol: Rol, estado: Estado, esCreador: Boolean) =
        !estado.esFinal && (rol.puedeGestionar || esCreador)

    fun puedeCancelar(rol: Rol, estado: Estado, esCreador: Boolean) =
        !estado.esFinal && (rol.puedeGestionar || (esCreador && estado == Estado.PENDIENTE))

    fun puedeAsignar(rol: Rol, estado: Estado) =
        rol.puedeAsignar && !estado.esFinal
}