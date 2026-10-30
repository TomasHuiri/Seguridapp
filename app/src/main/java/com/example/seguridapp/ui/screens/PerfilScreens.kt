package com.example.seguridapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.seguridapp.data.local.UsuarioEntity
import com.example.seguridapp.model.Rol
import com.example.seguridapp.ui.components.DropdownCampo
import com.example.seguridapp.viewmodel.UsuariosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(usuario: UsuarioEntity, onUsuarios: () -> Unit, onCerrarSesion: () -> Unit) {
    val rol = Rol.desde(usuario.rol)
    var confirmar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Perfil", fontWeight = FontWeight.Bold) }) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        usuario.nombre.take(1).uppercase(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(usuario.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Email, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text(usuario.email, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(50)) {
                        Text(
                            rol.etiqueta,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            if (rol.esAdmin) {
                Text("Administración", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onUsuarios)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ManageAccounts, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("Gestión de usuarios", Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                    }
                }
            }

            Spacer(Modifier.weight(1f, fill = false))
            Button(
                onClick = { confirmar = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tendrás que ingresar nuevamente tus credenciales.") },
            confirmButton = { TextButton(onClick = { confirmar = false; onCerrarSesion() }) { Text("Cerrar sesión") } },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosScreen(vm: UsuariosViewModel, usuarioActual: UsuarioEntity, onVolver: () -> Unit) {
    val usuarios by vm.usuarios.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de usuarios", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(usuarios, key = { it.id }) { u ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(u.nombre, fontWeight = FontWeight.Bold)
                        Text(u.email, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        if (u.id == usuarioActual.id) {
                            Text("Rol: ${Rol.desde(u.rol).etiqueta} (tu cuenta)", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            DropdownCampo(
                                valor = Rol.desde(u.rol).etiqueta,
                                etiqueta = "Rol",
                                icono = Icons.Filled.ManageAccounts,
                                opciones = Rol.entries.map { it.etiqueta },
                                onSeleccion = { vm.cambiarRol(u.id, Rol.entries[it]) },
                                error = null
                            )
                        }
                    }
                }
            }
        }
    }
}
