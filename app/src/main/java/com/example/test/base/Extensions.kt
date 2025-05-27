package com.example.test.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri

fun Uri.loadBitmap(context: Context): Bitmap? {
    val contentResolver = context.contentResolver

    // First, decode EXIF orientation safely
    val orientation = runCatching {
        contentResolver.openInputStream(this)?.use { input ->
            ExifInterface(input).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    // Then decode the actual bitmap
    val bitmap = contentResolver.openInputStream(this)?.use { input ->
        BitmapFactory.decodeStream(input)
    } ?: return null

    // Only rotate if needed
    val matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> preScale(1f, -1f)
            else -> return@apply
        }
    }

    return if (!matrix.isIdentity) {
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    } else {
        bitmap
    }
}