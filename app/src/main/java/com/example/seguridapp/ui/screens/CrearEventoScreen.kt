package com.example.seguridapp.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.seguridapp.data.local.UsuarioEntity
import com.example.seguridapp.model.Categoria
import com.example.seguridapp.model.Prioridad
import com.example.seguridapp.ui.components.BotonCarga
import com.example.seguridapp.ui.components.CampoTexto
import com.example.seguridapp.ui.components.DropdownCampo
import com.example.seguridapp.ui.components.EvidenciaImagen
import com.example.seguridapp.util.Formato
import com.example.seguridapp.util.FotoHelper
import com.example.seguridapp.util.UbicacionHelper
import com.example.seguridapp.viewmodel.EventoFormViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEventoScreen(
    vm: EventoFormViewModel,
    usuario: UsuarioEntity,
    onVolver: () -> Unit,
    onMapa: () -> Unit,
    onCreado: (Long) -> Unit
) {
    val s by vm.estado.collectAsState()
    val prioridades = Prioridad.entries.filter { it != Prioridad.INDETERMINADO }
    val context = LocalContext.current
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var obteniendoGps by remember { mutableStateOf(false) }
    var archivoPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    fun mensaje(texto: String) { scope.launch { snack.showSnackbar(texto) } }

    fun leerGps() {
        obteniendoGps = true
        UbicacionHelper.obtener(
            context,
            onOk = { lat, lng -> obteniendoGps = false; vm.onUbicacion(lat, lng) },
            onError = { obteniendoGps = false; mensaje(it) }
        )
    }
    val permisoUbicacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.any { it }) leerGps()
        else mensaje("Permiso de ubicación denegado. Puedes escribir la ubicación manualmente.")
    }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val ruta = archivoPendiente
        if (ok && ruta != null) vm.onFoto(ruta) else ruta?.let { File(it).delete() }
        archivoPendiente = null
    }
    fun abrirCamara() {
        val archivo = FotoHelper.crearArchivo(context)
        archivoPendiente = archivo.absolutePath
        camara.launch(FotoHelper.uriDe(context, archivo))
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) abrirCamara() else mensaje("Permiso de cámara denegado. La evidencia es opcional.")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo evento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Información del evento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))

            DropdownCampo(
                valor = s.categoria?.etiqueta ?: "",
                etiqueta = "Categoría *",
                icono = Icons.Filled.Warning,
                opciones = Categoria.entries.map { it.etiqueta },
                onSeleccion = { vm.onCategoria(Categoria.entries[it]) },
                error = s.errores.categoria,
                placeholder = "Selecciona una categoría"
            )
            DropdownCampo(
                valor = s.prioridad?.etiqueta ?: "",
                etiqueta = "Prioridad *",
                icono = Icons.Filled.Flag,
                opciones = prioridades.map { it.etiqueta },
                onSeleccion = { vm.onPrioridad(prioridades[it]) },
                error = s.errores.prioridad,
                placeholder = "Selecciona una prioridad"
            )
            CampoTexto(
                valor = s.ubicacionRef,
                onCambio = vm::onUbicacionRef,
                etiqueta = "Ubicación referencial *",
                icono = Icons.Filled.Place,
                error = s.errores.ubicacion,
                placeholder = "Ej: Parque central, sector norte",
                maxLength = 80
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        if (UbicacionHelper.tienePermiso(context)) leerGps()
                        else permisoUbicacion.launch(UbicacionHelper.PERMISOS)
                    },
                    enabled = !obteniendoGps,
                    modifier = Modifier.weight(1f)
                ) {
                    if (obteniendoGps) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Filled.MyLocation, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Mi ubicación")
                }
                OutlinedButton(onClick = onMapa, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Map, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Elegir en mapa")
                }
            }

            AnimatedVisibility(
                visible = s.latitud != null && s.longitud != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Place, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Coordenadas adjuntas", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(
                                Formato.coordenadas(s.latitud ?: 0.0, s.longitud ?: 0.0),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(onClick = vm::quitarUbicacion) { Text("Quitar") }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            CampoTexto(
                valor = s.descripcion,
                onCambio = vm::onDescripcion,
                etiqueta = "Descripción *",
                icono = Icons.Filled.Description,
                error = s.errores.descripcion,
                placeholder = "Describe la situación...",
                singleLine = false,
                minLines = 3,
                maxLength = 300,
                imeAction = ImeAction.Default
            )
            Text(
                "${s.descripcion.length}/300",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )

            Text("Evidencia (opcional)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                "Usa solo imágenes simuladas. No fotografíes personas reales.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            s.fotoPath?.let { ruta ->
                EvidenciaImagen(ruta, Modifier.padding(top = 8.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                OutlinedButton(onClick = {
                    if (FotoHelper.tienePermisoCamara(context)) abrirCamara()
                    else permisoCamara.launch(android.Manifest.permission.CAMERA)
                }) {
                    Icon(Icons.Filled.CameraAlt, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (s.fotoPath == null) "Agregar evidencia" else "Cambiar foto")
                }
                if (s.fotoPath != null) {
                    TextButton(onClick = { vm.onFoto(null) }) { Text("Quitar") }
                }
            }

            Spacer(Modifier.height(12.dp))
            BotonCarga(
                texto = "Registrar evento",
                cargando = s.guardando,
                onClick = {
                    if (!vm.registrar(usuario, onCreado)) mensaje("Revisa los campos marcados en rojo")
                },
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
