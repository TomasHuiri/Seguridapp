package com.example.seguridapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.seguridapp.data.local.UsuarioEntity
import com.example.seguridapp.model.Categoria
import com.example.seguridapp.model.Estado
import com.example.seguridapp.model.Prioridad
import com.example.seguridapp.model.Rol
import com.example.seguridapp.ui.components.EstadoVacio
import com.example.seguridapp.ui.components.EventoCard
import com.example.seguridapp.ui.components.ResumenCard
import com.example.seguridapp.viewmodel.EventosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    usuario: UsuarioEntity,
    vm: EventosViewModel,
    onVerEvento: (Long) -> Unit,
    onVerTodos: () -> Unit,
    onFiltrarEstado: (Estado?) -> Unit
) {
    val resumen by vm.resumen.collectAsState()
    val eventos by vm.eventos.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola, ${usuario.nombre.substringBefore(' ')}", fontWeight = FontWeight.Bold)
                        Text(
                            "Aquí tienes un resumen de la actividad · ${Rol.desde(usuario.rol).etiqueta}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ResumenCard(
                        "Total de eventos", resumen.total, Color(0xFFDCE8FF), Color(0xFF0B2E73),
                        Modifier.weight(1f), onClick = { onFiltrarEstado(null) }
                    )
                    ResumenCard(
                        "Pendientes", resumen.pendientes, Color(0xFFFFE9D6), Color(0xFFC2410C),
                        Modifier.weight(1f), onClick = { onFiltrarEstado(Estado.PENDIENTE) }
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ResumenCard(
                        "En proceso", resumen.enProceso, Color(0xFFFFF4C2), Color(0xFF8A6D00),
                        Modifier.weight(1f), onClick = { onFiltrarEstado(Estado.EN_PROCESO) }
                    )
                    ResumenCard(
                        "Cerrados", resumen.cerrados, Color(0xFFDDF5E4), Color(0xFF15803D),
                        Modifier.weight(1f), onClick = { onFiltrarEstado(Estado.CERRADO) }
                    )
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Eventos recientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onVerTodos) { Text("Ver todos") }
                }
            }
            if (eventos.isEmpty()) {
                item { EstadoVacio("Aún no hay eventos registrados", Icons.Filled.Search) }
            }
            items(eventos.take(3), key = { it.id }) { evento ->
                EventoCard(evento, onClick = { onVerEvento(evento.id) })
            }
        }
    }
}

@Composable
private fun FiltroMenu(
    titulo: String,
    opciones: List<String>,
    seleccionada: String?,
    onSeleccion: (Int?) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = seleccionada != null,
            onClick = { expandido = true },
            label = { Text(seleccionada ?: titulo) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }
        )
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            DropdownMenuItem(text = { Text("Todas") }, onClick = { onSeleccion(null); expandido = false })
            opciones.forEachIndexed { i, texto ->
                DropdownMenuItem(text = { Text(texto) }, onClick = { onSeleccion(i); expandido = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosScreen(vm: EventosViewModel, onVerEvento: (Long) -> Unit) {
    val eventos by vm.eventos.collectAsState()
    val busqueda by vm.busqueda.collectAsState()
    val estado by vm.filtroEstado.collectAsState()
    val categoria by vm.filtroCategoria.collectAsState()
    val prioridad by vm.filtroPrioridad.collectAsState()
    val prioridades = Prioridad.entries.filter { it != Prioridad.INDETERMINADO }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Eventos", fontWeight = FontWeight.Bold) }) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = vm::onBusqueda,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Buscar eventos...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (busqueda.isNotEmpty()) {
                        IconButton(onClick = { vm.onBusqueda("") }) { Icon(Icons.Filled.Close, "Limpiar búsqueda") }
                    }
                },
                singleLine = true
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(selected = estado == null, onClick = { vm.onEstado(null) }, label = { Text("Todos") })
                }
                items(Estado.entries.toList()) { e ->
                    FilterChip(selected = estado == e, onClick = { vm.onEstado(e) }, label = { Text(e.etiqueta) })
                }
            }
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FiltroMenu(
                    titulo = "Categoría",
                    opciones = Categoria.entries.map { it.etiqueta },
                    seleccionada = categoria?.etiqueta,
                    onSeleccion = { i -> vm.onCategoria(i?.let { Categoria.entries[it] }) }
                )
                FiltroMenu(
                    titulo = "Prioridad",
                    opciones = prioridades.map { it.etiqueta },
                    seleccionada = prioridad?.etiqueta,
                    onSeleccion = { i -> vm.onPrioridad(i?.let { prioridades[it] }) }
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AnimatedVisibility(visible = eventos.isEmpty()) {
                        EstadoVacio("No hay eventos que coincidan con los filtros", Icons.Filled.Search)
                    }
                }
                items(eventos, key = { it.id }) { evento ->
                    EventoCard(evento, onClick = { onVerEvento(evento.id) })
                }
            }
        }
    }
}
