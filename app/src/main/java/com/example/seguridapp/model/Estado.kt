package com.example.seguridapp.model

enum class Estado(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_PROCESO("En proceso"),
    CERRADO("Cerrado"),
    CANCELADO("Cancelado");
    val esFinal: Boolean
        get() = this == CERRADO || this == CANCELADO

    fun siguiente(): Estado? = when (this) {
        PENDIENTE -> EN_PROCESO
        EN_PROCESO -> CERRADO
        else -> null
    }

    companion object {
        fun desde(nombre: String): Estado =
            entries.firstOrNull{it.name == nombre}?: PENDIENTE
    }

}