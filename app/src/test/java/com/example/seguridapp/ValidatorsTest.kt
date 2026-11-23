package com.example.seguridapp

import com.example.seguridapp.repository.Validators
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ValidatorsTest : StringSpec({

    "Nombre valido es aceptado" {
        Validators.nombre("Juan Perez") shouldBe null
    }

    "Nombre demasiado corto es rechazado" {
        Validators.nombre("Jo") shouldBe
                "Debe tener al menos 3 caracteres"
    }

    "Nombre con numeros es rechazado" {
        Validators.nombre("Juan123") shouldBe
                "Solo se permiten letras y espacios"
    }

    "Correo valido es aceptado" {
        Validators.email("usuario@correo.cl") shouldBe null
    }

    "Correo invalido es rechazado" {
        Validators.email("correo-invalido") shouldBe
                "Ingresa un correo válido (ej: nombre@dominio.cl)"
    }

    "Contrasena segura es aceptada" {
        Validators.password("Clave12345") shouldBe null
    }

    "Contrasena corta es rechazada" {
        Validators.password("Abc12") shouldBe
                "Debe tener al menos 8 caracteres"
    }

    "Contrasena sin numeros es rechazada" {
        Validators.password("SoloLetras") shouldBe
                "Debe incluir letras y números"
    }

    "Confirmacion diferente es rechazada" {
        Validators.confirmacion(
            "Clave12345",
            "OtraClave123"
        ) shouldBe "Las contraseñas no coinciden"
    }

    "Terminos deben estar aceptados" {
        Validators.terminos(false) shouldBe
                "Debes aceptar los términos para continuar"

        Validators.terminos(true) shouldBe null
    }

    "Ubicacion valida es aceptada" {
        Validators.ubicacion("Sector Centro") shouldBe null
    }

    "Ubicacion demasiado corta es rechazada" {
        Validators.ubicacion("AB") shouldBe
                "Debe tener al menos 3 caracteres"
    }

    "Descripcion completa es aceptada" {
        Validators.descripcion(
            "Se reporta un incendio en el sector"
        ) shouldBe null
    }

    "Descripcion corta es rechazada" {
        Validators.descripcion("Incendio") shouldBe
                "Describe la situación con al menos 10 caracteres"
    }

    "Observacion valida es aceptada" {
        Validators.observacion(
            "Se reviso el incidente"
        ) shouldBe null
    }

    "Observacion vacia es rechazada" {
        Validators.observacion("") shouldBe
                "Escribe una observación"
    }

    "Motivo valido es aceptado" {
        Validators.motivo(
            "Registro duplicado"
        ) shouldBe null
    }

    "Motivo demasiado corto es rechazado" {
        Validators.motivo("No") shouldBe
                "Explica el motivo con al menos 5 caracteres"
    }
})