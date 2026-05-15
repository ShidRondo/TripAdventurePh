package com.example.tripadventureph

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val file = File(context.cacheDir, "proof_${System.currentTimeMillis()}.jpg")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        stream.flush()
        stream.close()

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    } catch (_: Exception) {
        null
    }
}