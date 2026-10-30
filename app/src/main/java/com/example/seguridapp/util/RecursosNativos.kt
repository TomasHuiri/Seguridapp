package com.example.seguridapp.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.File

object UbicacionHelper {
    val PERMISOS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun tienePermiso(context: Context): Boolean = PERMISOS.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun obtener(context: Context, onOk: (Double, Double) -> Unit, onError: (String) -> Unit) {
        if (!tienePermiso(context)) {
            onError("Permiso de ubicación denegado")
            return
        }
        val cliente = LocationServices.getFusedLocationProviderClient(context)
        cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { loc ->
                if (loc != null) onOk(loc.latitude, loc.longitude)
                else onError("No se pudo obtener la ubicación. Revisa que el GPS esté activo.")
            }
            .addOnFailureListener { onError("Ocurrió un error al obtener la ubicación") }
    }
}

object FotoHelper {
    fun tienePermisoCamara(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    fun crearArchivo(context: Context): File {
        val dir = File(context.filesDir, "evidencias").apply { mkdirs() }
        return File(dir, "evt_${System.currentTimeMillis()}.jpg")
    }

    fun uriDe(context: Context, archivo: File) =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

        fun cargar(path: String, maxLado: Int = 800): Bitmap? {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, limites)
        var muestra = 1
        while (limites.outWidth / (muestra * 2) >= maxLado && limites.outHeight / (muestra * 2) >= maxLado) {
            muestra *= 2
        }
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = muestra })
    }
}
