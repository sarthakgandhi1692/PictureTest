package com.example.test.composeCommon

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Remembers a scaled bitmap based on the provided original bitmap and target width in pixels.
 * @param originalBitmap The original bitmap to be scaled.
 * @param targetWidthPx The target width in pixels for the scaled bitmap.
 * @return The scaled bitmap.
 */
@Composable
fun rememberScaledBitmap(
    originalBitmap: Bitmap,
    targetWidthPx: Float
): Bitmap? {
    var scaledBitmap by remember(
        originalBitmap,
        targetWidthPx
    ) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(originalBitmap, targetWidthPx) {
        withContext(Dispatchers.IO) {
            try {
                if (originalBitmap.isRecycled) return@withContext

                val targetWidth = targetWidthPx.toInt()
                val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height
                val targetHeight = (targetWidth / aspectRatio).toInt()

                scaledBitmap = originalBitmap.scale(targetWidth, targetHeight)
            } catch (e: Exception) {
                Log.e("BitmapScale", "Failed to scale bitmap", e)
                scaledBitmap = originalBitmap // fallback
            }
        }
    }

    return scaledBitmap
}