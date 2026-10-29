package com.example.seguridapp.model

enum class Prioridad(val etiqueta: String) {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta"),
    INDETERMINADO("Sin definir");

    companion object {
        fun desde(nombre: String): Prioridad =
            entries.firstOrNull { it.name == nombre } ?: INDETERMINADO
    }
}