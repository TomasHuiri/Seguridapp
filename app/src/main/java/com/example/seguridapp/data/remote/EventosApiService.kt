package com.example.seguridapp.data.remote

import com.example.seguridapp.data.model.AccionEvento
import com.example.seguridapp.data.model.EventoRemoto
import com.example.seguridapp.data.model.EventoSolicitud
import com.example.seguridapp.data.model.SeguimientoRemoto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface EventosApiService {

    @GET("api/eventos")
    suspend fun listar(): List<EventoRemoto>

    @GET("api/eventos/{id}")
    suspend fun obtener(
        @Path("id") id: Long
    ): EventoRemoto

    @GET("api/eventos/{id}/seguimientos")
    suspend fun listarSeguimientos(
        @Path("id") id: Long
    ): List<SeguimientoRemoto>

    @POST("api/eventos")
    suspend fun crear(
        @Body evento: EventoSolicitud
    ): EventoRemoto

    @POST("api/eventos/{id}/observaciones")
    suspend fun agregarObservacion(
        @Path("id") id: Long,
        @Body accion: AccionEvento
    ): SeguimientoRemoto

    @PATCH("api/eventos/{id}/avanzar")
    suspend fun avanzarEstado(
        @Path("id") id: Long,
        @Body accion: AccionEvento
    ): EventoRemoto

    @PATCH("api/eventos/{id}/asignar")
    suspend fun asignar(
        @Path("id") id: Long,
        @Body accion: AccionEvento
    ): EventoRemoto

    @PATCH("api/eventos/{id}/cancelar")
    suspend fun cancelar(
        @Path("id") id: Long,
        @Body accion: AccionEvento
    ): EventoRemoto
}