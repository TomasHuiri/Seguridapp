package com.example.seguridapp.data.remote

import com.example.seguridapp.data.model.Post
import retrofit2.http.GET

interface ApiService {

    @GET("posts")
    suspend fun getPosts(): List<Post>
}
