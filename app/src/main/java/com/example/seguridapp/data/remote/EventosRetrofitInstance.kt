package com.example.seguridapp.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object EventosRetrofitInstance {

    val api: EventosApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EventosApiService::class.java)
    }
}