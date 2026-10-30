package com.example.seguridapp.util

object Validators {
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun nombre(v: String): String? = when {
        v.isBlank() -> "El nombre es obligatorio"
        v.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        !v.all { it.isLetter() || it == ' ' } -> "Solo se permiten letras y espacios"
        else -> null
    }

    fun email(v: String): String? = when {
        v.isBlank() -> "El correo es obligatorio"
        !EMAIL.matches(v.trim()) -> "Ingresa un correo válido (ej: nombre@dominio.cl)"
        else -> null
    }

    fun password(v: String): String? = when {
        v.isBlank() -> "La contraseña es obligatoria"
        v.length < 8 -> "Debe tener al menos 8 caracteres"
        !v.any { it.isLetter() } || !v.any { it.isDigit() } -> "Debe incluir letras y números"
        else -> null
    }

    fun confirmacion(clave: String, confirmacion: String): String? = when {
        confirmacion.isBlank() -> "Confirma tu contraseña"
        clave != confirmacion -> "Las contraseñas no coinciden"
        else -> null
    }

    fun terminos(aceptados: Boolean): String? =
        if (aceptados) null else "Debes aceptar los términos para continuar"

    fun seleccion(valor: Any?, nombre: String): String? =
        if (valor == null) "Selecciona $nombre" else null

    fun ubicacion(v: String): String? = when {
        v.isBlank() -> "La ubicación referencial es obligatoria"
        v.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        v.length > 80 -> "Máximo 80 caracteres"
        else -> null
    }

    fun descripcion(v: String): String? = when {
        v.isBlank() -> "La descripción es obligatoria"
        v.trim().length < 10 -> "Describe la situación con al menos 10 caracteres"
        v.length > 300 -> "Máximo 300 caracteres"
        else -> null
    }

    fun motivo(v: String): String? = when {
        v.isBlank() -> "El motivo es obligatorio"
        v.trim().length < 5 -> "Explica el motivo con al menos 5 caracteres"
        else -> null
    }

    fun observacion(v: String): String? = when {
        v.isBlank() -> "Escribe una observación"
        v.trim().length < 5 -> "Debe tener al menos 5 caracteres"
        v.length > 200 -> "Máximo 200 caracteres"
        else -> null
    }
}
