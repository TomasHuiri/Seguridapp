package com.example.seguridapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.seguridapp.ui.components.BotonCarga
import com.example.seguridapp.ui.components.CampoTexto
import com.example.seguridapp.viewmodel.AuthViewModel

@Composable
fun InicioScreen(onLogin: () -> Unit, onRegistro: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val escala by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "escala_logo"
    )
    val alfa by animateFloatAsState(if (visible) 1f else 0f, tween(700), label = "alfa_logo")

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background)
                )
            )
            .systemBarsPadding()
            .padding(24.dp)
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .size(140.dp)
                    .scale(escala)
                    .alpha(alfa)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Shield, null, Modifier.size(84.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Segurity App",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(alfa)
            )
            Text(
                "Tu seguridad, nuestra prioridad",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(alfa)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Reporta, gestiona y da seguimiento a situaciones de seguridad comunal.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.weight(1f))
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, delayMillis = 300)) + slideInVertically(tween(600, delayMillis = 300)) { it / 2 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onLogin,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Iniciar sesión", fontWeight = FontWeight.SemiBold) }
                    OutlinedButton(
                        onClick = onRegistro,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Crear cuenta", fontWeight = FontWeight.SemiBold) }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "La seguridad es tarea de todos",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TarjetaError(mensaje: String?) {
    AnimatedVisibility(
        visible = mensaje != null,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically()
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Text(
                mensaje ?: "",
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onExito: () -> Unit,
    onIrRegistro: () -> Unit,
    onVolver: () -> Unit
) {
    val s by vm.login.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.Shield, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Text("Segurity App", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Iniciar sesión", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Ingresa tus credenciales para continuar", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))

            TarjetaError(s.errorGeneral)

            CampoTexto(
                valor = s.email, onCambio = vm::onLoginEmail, etiqueta = "Correo electrónico",
                icono = Icons.Filled.Email, error = s.emailError, teclado = KeyboardType.Email
            )
            CampoTexto(
                valor = s.password, onCambio = vm::onLoginPassword, etiqueta = "Contraseña",
                icono = Icons.Filled.Lock, error = s.passwordError, esPassword = true,
                verPassword = s.verPassword, onToggleVer = vm::toggleLoginVerPassword,
                imeAction = ImeAction.Done
            )
            Spacer(Modifier.height(8.dp))
            BotonCarga("Iniciar sesión", s.cargando, onClick = { vm.iniciarSesion(onExito) })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿No tienes una cuenta?", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onIrRegistro) { Text("Regístrate") }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Cuentas de prueba (datos ficticios)", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold)
                    Text("usuario@segurityapp.cl · funcionario@segurityapp.cl", style = MaterialTheme.typography.bodySmall)
                    Text("supervisor@segurityapp.cl · admin@segurityapp.cl", style = MaterialTheme.typography.bodySmall)
                    Text("Contraseña: Segurity123", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(vm: AuthViewModel, onExito: () -> Unit, onVolver: () -> Unit) {
    val s by vm.registro.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Text(
                "Usa datos ficticios: esta app es una demostración académica.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            TarjetaError(s.errorGeneral)
            CampoTexto(s.nombre, vm::onRegNombre, "Nombre completo", Icons.Filled.Person, s.nombreError)
            CampoTexto(s.email, vm::onRegEmail, "Correo electrónico", Icons.Filled.Email, s.emailError,
                teclado = KeyboardType.Email)
            CampoTexto(
                s.password, vm::onRegPassword, "Contraseña", Icons.Filled.Lock, s.passwordError,
                esPassword = true, verPassword = s.verPassword, onToggleVer = vm::toggleRegVerPassword
            )
            CampoTexto(
                s.confirmacion, vm::onRegConfirmacion, "Confirmar contraseña", Icons.Filled.Lock,
                s.confirmacionError, esPassword = true, verPassword = s.verPassword,
                onToggleVer = vm::toggleRegVerPassword, imeAction = ImeAction.Done
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = s.terminos, onCheckedChange = vm::onRegTerminos)
                Text("Acepto los términos y condiciones de uso", style = MaterialTheme.typography.bodyMedium)
            }
            AnimatedVisibility(visible = s.terminosError != null, enter = fadeIn() + expandVertically(), exit = shrinkVertically()) {
                Text(s.terminosError ?: "", color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 12.dp))
            }
            Spacer(Modifier.height(12.dp))
            BotonCarga("Crear cuenta", s.cargando, onClick = { vm.registrarse(onExito) })
            Spacer(Modifier.height(24.dp))
        }
    }
}
