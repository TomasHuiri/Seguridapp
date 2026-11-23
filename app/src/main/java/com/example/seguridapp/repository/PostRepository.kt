package com.example.seguridapp.repository

import com.example.seguridapp.data.model.Post
import com.example.seguridapp.data.remote.RetrofitInstance

class PostRepository {

    suspend fun getPosts(): List<Post> {
        return RetrofitInstance.api.getPosts()
    }
}
