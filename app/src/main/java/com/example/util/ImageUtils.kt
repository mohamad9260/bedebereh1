package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object ImageUtils {

  suspend fun uriToDataUrl(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
    try {
      context.contentResolver.openInputStream(uri)?.use { stream ->
        val bitmap = BitmapFactory.decodeStream(stream) ?: return@withContext uri.toString()
        val maxDim = 800
        val scale = if (bitmap.width > maxDim || bitmap.height > maxDim) {
          maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
        } else 1f

        val scaledBitmap = if (scale < 1f) {
          Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true
          )
        } else {
          bitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        "data:image/jpeg;base64,$base64"
      } ?: uri.toString()
    } catch (e: Exception) {
      android.util.Log.e("ImageUtils", "Error converting uri to data URL: ${e.localizedMessage}")
      uri.toString()
    }
  }
}
