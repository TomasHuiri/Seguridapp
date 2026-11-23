package com.example.seguridapp

import com.example.seguridapp.data.model.Estado
import com.example.seguridapp.data.model.Permisos
import com.example.seguridapp.data.model.Rol
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PermisosTest : StringSpec({

    "Un residente no puede avanzar eventos" {
        Permisos.puedeCambiarEstado(
            Rol.USUARIO,
            Estado.PENDIENTE
        ) shouldBe false
    }

    "Un funcionario puede avanzar eventos pendientes" {
        Permisos.puedeCambiarEstado(
            Rol.FUNCIONARIO,
            Estado.PENDIENTE
        ) shouldBe true
    }

    "Nadie puede avanzar eventos cerrados" {
        Rol.entries.forEach { rol ->
            Permisos.puedeCambiarEstado(
                rol,
                Estado.CERRADO
            ) shouldBe false
        }
    }

    "Un residente puede observar su propio evento activo" {
        Permisos.puedeObservar(
            Rol.USUARIO,
            Estado.PENDIENTE,
            true
        ) shouldBe true
    }

    "Un residente no puede observar eventos ajenos" {
        Permisos.puedeObservar(
            Rol.USUARIO,
            Estado.PENDIENTE,
            false
        ) shouldBe false
    }

    "Un residente solo cancela sus eventos pendientes" {
        Permisos.puedeCancelar(
            Rol.USUARIO,
            Estado.PENDIENTE,
            true
        ) shouldBe true

        Permisos.puedeCancelar(
            Rol.USUARIO,
            Estado.EN_PROCESO,
            true
        ) shouldBe false
    }

    "Solo supervisores y administradores derivan" {
        Permisos.puedeAsignar(
            Rol.USUARIO,
            Estado.PENDIENTE
        ) shouldBe false

        Permisos.puedeAsignar(
            Rol.FUNCIONARIO,
            Estado.PENDIENTE
        ) shouldBe false

        Permisos.puedeAsignar(
            Rol.SUPERVISOR,
            Estado.PENDIENTE
        ) shouldBe true

        Permisos.puedeAsignar(
            Rol.ADMINISTRADOR,
            Estado.PENDIENTE
        ) shouldBe true
    }

    "Los estados avanzan en el orden correcto" {
        Estado.PENDIENTE.siguiente() shouldBe Estado.EN_PROCESO
        Estado.EN_PROCESO.siguiente() shouldBe Estado.CERRADO
        Estado.CERRADO.siguiente() shouldBe null
        Estado.CANCELADO.siguiente() shouldBe null
    }

    "Los roles desconocidos no reciben privilegios" {
        Rol.desde("ROL_INEXISTENTE") shouldBe Rol.USUARIO
    }
})