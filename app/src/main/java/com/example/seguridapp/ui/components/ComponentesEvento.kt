package com.example.seguridapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.seguridapp.data.local.EventoEntity
import com.example.seguridapp.model.Categoria
import com.example.seguridapp.model.Estado
import com.example.seguridapp.model.Prioridad
import com.example.seguridapp.ui.theme.CanceladoFondo
import com.example.seguridapp.ui.theme.CanceladoTexto
import com.example.seguridapp.ui.theme.CerradoFondo
import com.example.seguridapp.ui.theme.CerradoTexto
import com.example.seguridapp.ui.theme.EnProcesoFondo
import com.example.seguridapp.ui.theme.EnProcesoTexto
import com.example.seguridapp.ui.theme.PendienteFondo
import com.example.seguridapp.ui.theme.PendienteTexto
import com.example.seguridapp.ui.theme.PrioridadAlta
import com.example.seguridapp.ui.theme.PrioridadBaja
import com.example.seguridapp.ui.theme.PrioridadMedia
import com.example.seguridapp.ui.theme.PrioridadSinDefinir
import com.example.seguridapp.util.Formato

fun iconoCategoria(c: Categoria): ImageVector = when (c) {
    Categoria.INCENDIO -> Icons.Filled.LocalFireDepartment
    Categoria.ROBO -> Icons.Filled.Warning
    Categoria.SOSPECHOSO -> Icons.Filled.Visibility
    Categoria.ACCIDENTE -> Icons.Filled.DirectionsCar
    Categoria.VANDALISMO -> Icons.Filled.Build
    Categoria.OTRO -> Icons.Filled.Info
}

fun coloresEstado(e: Estado): Pair<Color, Color> = when (e) {
    Estado.PENDIENTE -> PendienteFondo to PendienteTexto
    Estado.EN_PROCESO -> EnProcesoFondo to EnProcesoTexto
    Estado.CERRADO -> CerradoFondo to CerradoTexto
    Estado.CANCELADO -> CanceladoFondo to CanceladoTexto
}

fun colorPrioridad(p: Prioridad): Color = when (p) {
    Prioridad.ALTA -> PrioridadAlta
    Prioridad.MEDIA -> PrioridadMedia
    Prioridad.BAJA -> PrioridadBaja
    Prioridad.INDETERMINADO -> PrioridadSinDefinir
}

@Composable
fun EstadoChip(estado: Estado) {
    val (fondo, texto) = coloresEstado(estado)
    val colorFondo by animateColorAsState(fondo, tween(400), label = "fondo_estado")
    val colorTexto by animateColorAsState(texto, tween(400), label = "texto_estado")
    Surface(color = colorFondo, shape = RoundedCornerShape(50)) {
        Text(
            estado.etiqueta,
            color = colorTexto,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EventoCard(evento: EventoEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val categoria = Categoria.desde(evento.categoria)
    val estado = Estado.desde(evento.estado)
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconoCategoria(categoria), null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    categoria.etiqueta,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    "#${evento.codigo} · ${Formato.fecha(evento.fechaCreacion)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    evento.ubicacionRef,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                EstadoChip(estado)
                Spacer(Modifier.height(6.dp))
                Text(
                    Formato.tiempoRelativo(evento.fechaCreacion),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ResumenCard(
    titulo: String,
    valor: Int,
    fondo: Color,
    contenido: Color,
    modifier: Modifier = Modifier
) {
    val animado by animateIntAsState(valor, tween(800), label = "contador")
    Card(
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = fondo)
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = contenido)
            Text(
                "$animado",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = contenido
            )
        }
    }
}

@Composable
fun EstadoVacio(texto: String, icono: ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icono, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(texto, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}