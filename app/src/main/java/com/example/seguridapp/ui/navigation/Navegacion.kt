package com.example.seguridapp.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import com.example.seguridapp.data.local.UsuarioEntity
import com.example.seguridapp.ui.screens.CrearEventoScreen
import com.example.seguridapp.ui.screens.DashboardScreen
import com.example.seguridapp.ui.screens.DetalleScreen
import com.example.seguridapp.ui.screens.EventosScreen
import com.example.seguridapp.ui.screens.InicioScreen
import com.example.seguridapp.ui.screens.LoginScreen
import com.example.seguridapp.ui.screens.MapaScreen
import com.example.seguridapp.ui.screens.PerfilScreen
import com.example.seguridapp.ui.screens.RegistroScreen
import com.example.seguridapp.ui.screens.UsuariosScreen
import com.example.seguridapp.viewmodel.AppViewModelProvider
import com.example.seguridapp.viewmodel.AuthViewModel
import com.example.seguridapp.viewmodel.DetalleViewModel
import com.example.seguridapp.viewmodel.EventoFormViewModel
import com.example.seguridapp.viewmodel.EventosViewModel
import com.example.seguridapp.viewmodel.UsuariosViewModel
import com.example.seguridapp.model.Estado

object Rutas {
    const val INICIO = "inicio"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val DASHBOARD = "dashboard"
    const val EVENTOS = "eventos?estado={estado}"
    const val CREAR = "crear"
    const val MAPA = "mapa"
    const val DETALLE = "detalle/{id}"
    const val PERFIL = "perfil"
    const val USUARIOS = "usuarios"

    fun detalle(id: Long) = "detalle/$id"
    fun eventos(estado: Estado? = null) =
        if (estado == null) "eventos" else "eventos?estado=${estado.name}"
}

private val rutasConBarra = setOf(Rutas.DASHBOARD, Rutas.EVENTOS, Rutas.PERFIL)

@Composable
fun SeguridAppNavigation(authVm: AuthViewModel) {
    val sesion by authVm.sesion.collectAsState()
    if (sesion.cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        val iniciaLogueado = remember { sesion.usuario != null }
        Contenido(authVm, iniciaLogueado, sesion.usuario != null)
    }
}

@Composable
private fun Contenido(authVm: AuthViewModel, iniciaLogueado: Boolean, hayUsuario: Boolean) {
    val navController = rememberNavController()
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = hayUsuario && rutaActual in rutasConBarra,
                enter = slideInVertically(tween(300)) { it },
                exit = slideOutVertically(tween(300)) { it }
            ) {
                BarraInferior(navController, rutaActual)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (iniciaLogueado) Rutas.DASHBOARD else Rutas.INICIO,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 8 } },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 8 } }
        ) {
            composable(Rutas.INICIO) {
                InicioScreen(
                    onLogin = { navController.navigate(Rutas.LOGIN) },
                    onRegistro = { navController.navigate(Rutas.REGISTRO) }
                )
            }
            composable(Rutas.LOGIN) {
                LoginScreen(
                    vm = authVm,
                    onExito = {
                        navController.navigate(Rutas.DASHBOARD) { popUpTo(Rutas.INICIO) { inclusive = true } }
                    },
                    onIrRegistro = { navController.navigate(Rutas.REGISTRO) },
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(Rutas.REGISTRO) {
                RegistroScreen(
                    vm = authVm,
                    onExito = {
                        navController.navigate(Rutas.DASHBOARD) { popUpTo(Rutas.INICIO) { inclusive = true } }
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(Rutas.DASHBOARD) {
                ConUsuario(authVm) { usuario ->
                    val vm: EventosViewModel = viewModel(factory = AppViewModelProvider.Factory)
                    DashboardScreen(
                        usuario = usuario,
                        vm = vm,
                        onVerEvento = { navController.navigate(Rutas.detalle(it)) },
                        onVerTodos = { irAEventos(navController, null) },
                        onFiltrarEstado = { irAEventos(navController, it) }
                    )
                }
            }
            composable(
                route = Rutas.EVENTOS,
                arguments = listOf(
                    navArgument("estado") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                val vm: EventosViewModel = viewModel(factory = AppViewModelProvider.Factory)
                EventosScreen(vm = vm, onVerEvento = { navController.navigate(Rutas.detalle(it)) })
            }
            composable(Rutas.CREAR) {
                ConUsuario(authVm) { usuario ->
                    val vm: EventoFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
                    CrearEventoScreen(
                        vm = vm,
                        usuario = usuario,
                        onVolver = { navController.popBackStack() },
                        onMapa = { navController.navigate(Rutas.MAPA) },
                        onCreado = { id ->
                            navController.navigate(Rutas.detalle(id)) { popUpTo(Rutas.CREAR) { inclusive = true } }
                        }
                    )
                }
            }
            composable(Rutas.MAPA) { entry ->
                val padre = remember(entry) { navController.getBackStackEntry(Rutas.CREAR) }
                val vm: EventoFormViewModel = viewModel(
                    viewModelStoreOwner = padre,
                    factory = AppViewModelProvider.Factory
                )
                MapaScreen(vm = vm, onVolver = { navController.popBackStack() })
            }
            composable(
                route = Rutas.DETALLE,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) {
                ConUsuario(authVm) { usuario ->
                    val vm: DetalleViewModel = viewModel(factory = AppViewModelProvider.Factory)
                    DetalleScreen(vm = vm, usuario = usuario, onVolver = { navController.popBackStack() })
                }
            }
            composable(Rutas.PERFIL) {
                ConUsuario(authVm) { usuario ->
                    PerfilScreen(
                        usuario = usuario,
                        onUsuarios = { navController.navigate(Rutas.USUARIOS) },
                        onCerrarSesion = {
                            authVm.cerrarSesion {
                                navController.navigate(Rutas.INICIO) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }
            composable(Rutas.USUARIOS) {
                ConUsuario(authVm) { usuario ->
                    val vm: UsuariosViewModel = viewModel(factory = AppViewModelProvider.Factory)
                    UsuariosScreen(vm = vm, usuarioActual = usuario, onVolver = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun ConUsuario(authVm: AuthViewModel, contenido: @Composable (UsuarioEntity) -> Unit) {
    val sesion by authVm.sesion.collectAsState()
    sesion.usuario?.let { contenido(it) }
}

private fun irATab(navController: NavHostController, ruta: String) {
    if (ruta == Rutas.DASHBOARD) {
        val volvio = navController.popBackStack(Rutas.DASHBOARD, inclusive = false)
        if (!volvio) {
            navController.navigate(Rutas.DASHBOARD) { launchSingleTop = true }
        }
    } else {
        navController.navigate(ruta) {
            popUpTo(Rutas.DASHBOARD)
            launchSingleTop = true
        }
    }
}
private fun irAEventos(navController: NavHostController, estado: Estado?) {
    navController.navigate(Rutas.eventos(estado)) {
        popUpTo(Rutas.DASHBOARD)
        launchSingleTop = true
    }
}

private data class ItemBarra(val ruta: String, val titulo: String, val icono: ImageVector)

@Composable
private fun BarraInferior(navController: NavHostController, rutaActual: String?) {
    val items = listOf(
        ItemBarra(Rutas.DASHBOARD, "Inicio", Icons.Filled.Home),
        ItemBarra(Rutas.EVENTOS, "Eventos", Icons.AutoMirrored.Filled.List),
        ItemBarra(Rutas.CREAR, "Nuevo", Icons.Filled.Add),
        ItemBarra(Rutas.PERFIL, "Perfil", Icons.Filled.Person)
    )
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.ruta == rutaActual,
                onClick = {
                    when (item.ruta) {
                        Rutas.CREAR -> navController.navigate(Rutas.CREAR)
                        Rutas.EVENTOS -> irAEventos(navController, null)
                        else -> irATab(navController, item.ruta)
                    }
                },                icon = { Icon(item.icono, contentDescription = item.titulo) },
                label = { Text(item.titulo) }
            )
        }
    }
}
