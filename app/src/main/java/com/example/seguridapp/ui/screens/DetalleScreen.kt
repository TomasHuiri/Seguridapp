package com.example.seguridapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.seguridapp.data.local.EventoEntity
import com.example.seguridapp.data.local.SeguimientoEntity
import com.example.seguridapp.data.local.UsuarioEntity
import com.example.seguridapp.model.Categoria
import com.example.seguridapp.model.Estado
import com.example.seguridapp.model.Permisos
import com.example.seguridapp.model.Prioridad
import com.example.seguridapp.model.Rol
import com.example.seguridapp.model.Unidades
import com.example.seguridapp.ui.components.CampoTexto
import com.example.seguridapp.ui.components.DropdownCampo
import com.example.seguridapp.ui.components.EstadoChip
import com.example.seguridapp.ui.components.EvidenciaImagen
import com.example.seguridapp.ui.components.colorPrioridad
import com.example.seguridapp.util.Formato
import com.example.seguridapp.viewmodel.DetalleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(vm: DetalleViewModel, usuario: UsuarioEntity, onVolver: () -> Unit) {
    val evento by vm.evento.collectAsState()
    val seguimientos by vm.seguimientos.collectAsState()
    val ui by vm.ui.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var mostrarCancelar by rememberSaveable { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(ui.mensaje) {
        ui.mensaje?.let {
            snack.showSnackbar(it)
            vm.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del evento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { padding ->
        val e = evento
        if (e == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val rol = Rol.desde(usuario.rol)
            val estado = Estado.desde(e.estado)
            val esCreador = e.creadoPor == usuario.id

            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Encabezado(e) }
                item {
                    TabRow(selectedTabIndex = tab) {
                        Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Información") })
                        Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Seguimiento (${seguimientos.size})") })
                    }
                }
                item {
                    AnimatedContent(targetState = tab, label = "tabs_detalle") { actual ->
                        if (actual == 0) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Descripción", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(e.descripcion)
                                    }
                                }
                                e.fotoPath?.let { EvidenciaImagen(it) }
                                if (estado == Estado.CANCELADO && e.motivoCancelacion != null) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(Modifier.padding(12.dp)) {
                                            Text("Motivo de cancelación", fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onErrorContainer)
                                            Text(e.motivoCancelacion, color = MaterialTheme.colorScheme.onErrorContainer)
                                        }
                                    }
                                }
                                if (Permisos.puedeAsignar(rol, estado)) {
                                    DropdownCampo(
                                        valor = e.responsable,
                                        etiqueta = "Derivar a responsable",
                                        icono = Icons.Filled.Business,
                                        opciones = Unidades.lista,
                                        onSeleccion = { vm.asignar(Unidades.lista[it], usuario.nombre) },
                                        error = null
                                    )
                                }
                                Acciones(
                                    puedeCancelar = Permisos.puedeCancelar(rol, estado, esCreador),
                                    siguiente = if (Permisos.puedeCambiarEstado(rol, estado)) estado.siguiente() else null,
                                    onCancelar = { mostrarCancelar = true },
                                    onAvanzar = { vm.avanzarEstado(usuario.nombre) }
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (Permisos.puedeObservar(rol, estado, esCreador)) {
                                    CampoTexto(
                                        valor = ui.observacion,
                                        onCambio = vm::onObservacion,
                                        etiqueta = "Nueva observación",
                                        icono = Icons.AutoMirrored.Filled.Comment,
                                        error = ui.observacionError,
                                        placeholder = "Describe la acción o novedad",
                                        singleLine = false,
                                        minLines = 2,
                                        maxLength = 200,
                                        imeAction = ImeAction.Default
                                    )
                                    Button(onClick = { vm.agregarObservacion(usuario.nombre) }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Agregar observación")
                                    }
                                }
                                if (seguimientos.isEmpty()) Text("Sin movimientos todavía")
                                seguimientos.forEach { SeguimientoItem(it) }
                            }
                        }
                    }
                }
            }

            if (mostrarCancelar) {
                DialogoCancelar(
                    motivo = ui.motivo,
                    error = ui.motivoError,
                    onMotivo = vm::onMotivo,
                    onConfirmar = { vm.cancelar(usuario.nombre) { mostrarCancelar = false } },
                    onCerrar = { mostrarCancelar = false; vm.limpiarCancelacion() }
                )
            }
        }
    }
}

@Composable
private fun Encabezado(e: EventoEntity) {
    val categoria = Categoria.desde(e.categoria)
    val prioridad = Prioridad.desde(e.prioridad)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("#${e.codigo}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            EstadoChip(Estado.desde(e.estado))
        }
        Text(categoria.etiqueta, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Fila(Icons.Filled.AccessTime, "Fecha y hora", Formato.fecha(e.fechaCreacion))
        Fila(Icons.Filled.Place, "Ubicación referencial", e.ubicacionRef)
        if (e.latitud != null && e.longitud != null) {
            Fila(Icons.Filled.Place, "Coordenadas", Formato.coordenadas(e.latitud, e.longitud))
        }
        Fila(Icons.Filled.Flag, "Prioridad", prioridad.etiqueta, colorPrioridad(prioridad))
        Fila(Icons.Filled.Business, "Responsable", e.responsable)
    }
}

@Composable
private fun Fila(
    icono: ImageVector,
    titulo: String,
    valor: String,
    colorValor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(titulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = colorValor)
        }
    }
}

@Composable
private fun Acciones(
    puedeCancelar: Boolean,
    siguiente: Estado?,
    onCancelar: () -> Unit,
    onAvanzar: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AnimatedVisibility(visible = puedeCancelar, modifier = Modifier.weight(1f),
            enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            OutlinedButton(
                onClick = onCancelar,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar evento") }
        }
        AnimatedVisibility(visible = siguiente != null, modifier = Modifier.weight(1f),
            enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Button(onClick = onAvanzar, modifier = Modifier.fillMaxWidth()) {
                Text(if (siguiente == Estado.CERRADO) "Cerrar evento" else "Pasar a ${siguiente?.etiqueta ?: ""}")
            }
        }
    }
}

@Composable
private fun SeguimientoItem(s: SeguimientoEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(s.texto, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${s.autor} · ${Formato.fecha(s.fecha)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DialogoCancelar(
    motivo: String,
    error: String?,
    onMotivo: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        icon = { Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("¿Cancelar evento?") },
        text = {
            Column {
                Text("Esta acción no se puede deshacer. Por favor, indica el motivo de la cancelación.")
                Spacer(Modifier.height(12.dp))
                CampoTexto(
                    valor = motivo,
                    onCambio = onMotivo,
                    etiqueta = "Motivo de cancelación *",
                    icono = Icons.AutoMirrored.Filled.Comment,
                    error = error,
                    placeholder = "Escribe el motivo...",
                    imeAction = ImeAction.Done
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Volver") } }
    )
}
