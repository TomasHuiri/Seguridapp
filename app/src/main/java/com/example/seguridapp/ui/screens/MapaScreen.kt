package com.example.seguridapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.seguridapp.util.Formato
import com.example.seguridapp.util.UbicacionHelper
import com.example.seguridapp.viewmodel.EventoFormViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(vm: EventoFormViewModel, onVolver: () -> Unit) {
    val estado by vm.estado.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    val inicial = LatLng(estado.latitud ?: -33.4950, estado.longitud ?: -70.6170)
    val camara = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(inicial, 15f)
    }
    var seleccion by remember {
        mutableStateOf(
            if (estado.latitud != null && estado.longitud != null) LatLng(estado.latitud!!, estado.longitud!!) else null
        )
    }
    var permiso by remember { mutableStateOf(UbicacionHelper.tienePermiso(context)) }

    fun irAMiUbicacion() {
        UbicacionHelper.obtener(
            context,
            onOk = { lat, lng ->
                val punto = LatLng(lat, lng)
                seleccion = punto
                scope.launch { camara.animate(CameraUpdateFactory.newLatLngZoom(punto, 16f)) }
            },
            onError = { scope.launch { snack.showSnackbar(it) } }
        )
    }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        permiso = r.values.any { it }
        if (permiso) irAMiUbicacion()
        else scope.launch { snack.showSnackbar("Permiso de ubicación denegado. Toca el mapa para elegir un punto.") }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Elegir ubicación", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = camara,
                    properties = MapProperties(isMyLocationEnabled = permiso),
                    uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = false),
                    onMapClick = { seleccion = it }
                ) {
                    seleccion?.let { punto ->
                        Marker(
                            state = remember(punto) { MarkerState(position = punto) },
                            title = "Ubicación del evento"
                        )
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        seleccion?.let { "Punto: ${Formato.coordenadas(it.latitude, it.longitude)}" }
                            ?: "Toca el mapa para colocar un marcador",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Si el mapa aparece en gris, falta configurar MAPS_API_KEY (ver la guía). Aun así puedes usar «Mi ubicación».",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                if (permiso) irAMiUbicacion() else pedirPermiso.launch(UbicacionHelper.PERMISOS)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.MyLocation, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Mi ubicación")
                        }
                        Button(
                            onClick = {
                                seleccion?.let { vm.onUbicacion(it.latitude, it.longitude) }
                                onVolver()
                            },
                            enabled = seleccion != null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Check, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Confirmar")
                        }
                    }
                }
            }
        }
    }
}
