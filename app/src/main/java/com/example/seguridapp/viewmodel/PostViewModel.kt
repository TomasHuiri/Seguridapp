package com.example.seguridapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguridapp.data.model.Post
import com.example.seguridapp.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostViewModel(
    private val repository: PostRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        cargarPosts()
    }

    fun cargarPosts() {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null

            try {
                _posts.value = repository.getPosts()
            } catch (e: Exception) {
                _error.value = "No se pudieron cargar las publicaciones"
            } finally {
                _cargando.value = false
            }
        }
    }
}
