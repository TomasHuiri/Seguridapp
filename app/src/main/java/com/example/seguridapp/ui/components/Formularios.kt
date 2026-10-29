package com.example.seguridapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
private fun MensajeError(error: String?) {
    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(error ?: "", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    esPassword: Boolean = false,
    verPassword: Boolean = false,
    onToggleVer: (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLength: Int? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { if (maxLength == null || it.length <= maxLength) onCambio(it) },
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        placeholder = if (placeholder != null) { { Text(placeholder) } } else null,
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            if (esPassword && onToggleVer != null) {
                IconButton(onClick = onToggleVer) {
                    Icon(
                        if (verPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (verPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            } else if (error != null) {
                Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
            }
        },
        isError = error != null,
        supportingText = { MensajeError(error) },
        visualTransformation = if (esPassword && !verPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (esPassword) KeyboardType.Password else teclado,
            imeAction = imeAction
        ),
        singleLine = singleLine,
        minLines = minLines
    )
}

@Composable
fun DropdownCampo(
    valor: String,
    etiqueta: String,
    icono: ImageVector,
    opciones: List<String>,
    onSeleccion: (Int) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
    placeholder: String = "Selecciona una opción"
) {
    var expandido by remember { mutableStateOf(false) }
    Box(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(etiqueta) },
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = {
                if (error != null) Icon(Icons.Filled.Error, "Error", tint = MaterialTheme.colorScheme.error)
                else Icon(Icons.Filled.ArrowDropDown, contentDescription = "Abrir lista")
            },
            isError = error != null,
            supportingText = { MensajeError(error) }
        )
        Box(Modifier.matchParentSize().clickable { expandido = true })
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEachIndexed { i, texto ->
                DropdownMenuItem(
                    text = { Text(texto) },
                    onClick = { onSeleccion(i); expandido = false }
                )
            }
        }
    }
}

@Composable
fun BotonCarga(
    texto: String,
    cargando: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled && !cargando,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        AnimatedContent(targetState = cargando, label = "boton_carga") { enCarga ->
            if (enCarga) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current
                )
            } else {
                Text(texto, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}