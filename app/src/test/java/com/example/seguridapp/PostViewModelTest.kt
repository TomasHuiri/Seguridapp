package com.example.seguridapp

import com.example.seguridapp.data.model.Post
import com.example.seguridapp.repository.PostRepository
import com.example.seguridapp.viewmodel.PostViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PostViewModelTest : StringSpec({

    "Carga publicaciones correctamente" {
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(dispatcher)

            try {
                val repo = mockk<PostRepository>()

                val publicaciones = listOf(
                    Post(
                        userId = 1,
                        id = 1,
                        title = "Publicacion de prueba",
                        body = "Contenido de prueba"
                    )
                )

                coEvery {
                    repo.getPosts()
                } returns publicaciones

                val vm = PostViewModel(repo)

                advanceUntilIdle()

                vm.posts.value shouldBe publicaciones
                vm.cargando.value shouldBe false
                vm.error.value shouldBe null

                coVerify(exactly = 1) {
                    repo.getPosts()
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    "Error de conexion se comunica a la pantalla" {
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(dispatcher)

            try {
                val repo = mockk<PostRepository>()

                coEvery {
                    repo.getPosts()
                } throws IOException("Sin conexion")

                val vm = PostViewModel(repo)

                advanceUntilIdle()

                vm.posts.value shouldBe emptyList<Post>()
                vm.cargando.value shouldBe false
                vm.error.value shouldBe
                        "No se pudieron cargar las publicaciones"
            } finally {
                Dispatchers.resetMain()
            }
        }
    }
})