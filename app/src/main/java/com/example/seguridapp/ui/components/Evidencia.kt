package com.example.seguridapp.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.seguridapp.util.FotoHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun EvidenciaImagen(path: String, modifier: Modifier = Modifier) {
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = path) {
        value = withContext(Dispatchers.IO) { FotoHelper.cargar(path) }
    }
    AnimatedVisibility(visible = bitmap != null, enter = fadeIn(tween(400))) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Evidencia fotográfica",
                modifier = modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}
