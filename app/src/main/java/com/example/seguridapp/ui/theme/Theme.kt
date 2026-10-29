package com.example.seguridapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = AzulMarca,
    onPrimary = Color.White,
    primaryContainer = AzulContenedor,
    onPrimaryContainer = AzulSobreContenedor,
    secondary = GrisAzulado,
    background = FondoClaro,
    onBackground = TextoClaro,
    surface = Color.White,
    onSurface = TextoClaro,
    surfaceVariant = SuperficieVariante,
    onSurfaceVariant = TextoVariante,
    error = ErrorRojo
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulClaroOscuro,
    onPrimary = Color(0xFF002A6B),
    primaryContainer = AzulContenedorOscuro,
    onPrimaryContainer = AzulContenedor,
    background = FondoOscuro,
    surface = SuperficieOscura,
    surfaceVariant = SuperficieVarianteOscura,
    error = ErrorRojoClaro
)

@Composable
fun SeguridappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}