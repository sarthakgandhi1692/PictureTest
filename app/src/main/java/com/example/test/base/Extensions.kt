package com.example.test.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri

fun Uri.loadBitmap(context: Context, scalingFactor: Float = 0.1f): Bitmap? {
    val contentResolver = context.contentResolver

    // Step 1: Decode EXIF orientation
    val orientation = runCatching {
        contentResolver.openInputStream(this)?.use { input ->
            ExifInterface(input).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    // Step 2: Decode image bounds only to get original dimensions
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    contentResolver.openInputStream(this)?.use { input ->
        BitmapFactory.decodeStream(input, null, options)
    }

    val originalWidth = options.outWidth
    val originalHeight = options.outHeight

    if (originalWidth <= 0 || originalHeight <= 0) return null

    // Step 3: Calculate inSampleSize based on scaling factor
    val targetWidth = (originalWidth * scalingFactor).toInt().coerceAtLeast(1)
    val targetHeight = (originalHeight * scalingFactor).toInt().coerceAtLeast(1)

    options.inJustDecodeBounds = false
    options.inSampleSize =
        calculateInSampleSize(originalWidth, originalHeight, targetWidth, targetHeight)

    // Step 4: Decode downscaled bitmap
    val scaledBitmap = contentResolver.openInputStream(this)?.use { input ->
        BitmapFactory.decodeStream(input, null, options)
    } ?: return null

    // Step 5: Apply rotation or flip if needed
    val matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> preScale(1f, -1f)
        }
    }

    return if (!matrix.isIdentity) {
        Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )
    } else {
        scaledBitmap
    }
}

fun calculateInSampleSize(
    originalWidth: Int,
    originalHeight: Int,
    reqWidth: Int,
    reqHeight: Int
): Int {
    var inSampleSize = 1
    if (originalHeight > reqHeight || originalWidth > reqWidth) {
        val halfHeight = originalHeight / 2
        val halfWidth = originalWidth / 2

        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}
