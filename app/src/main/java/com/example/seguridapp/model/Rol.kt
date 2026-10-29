package com.example.seguridapp.model

enum class Rol(val etiqueta: String) {
    USUARIO("Residente"),
    FUNCIONARIO("Funcionario"),
    SUPERVISOR("Supervisor"),
    ADMINISTRADOR("Administrador");

    val puedeGestionar: Boolean
        get() = this != USUARIO

    val puedeAsignar: Boolean
        get() = this == SUPERVISOR || this == ADMINISTRADOR

    val esAdmin: Boolean
        get() = this == ADMINISTRADOR

    companion object {
        fun desde(nombre: String): Rol =
            entries.firstOrNull { it.name == nombre } ?: USUARIO
    }
}