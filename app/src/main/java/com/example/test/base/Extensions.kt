package com.example.test.base

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri

/**
 * Extension function to load a bitmap from a Uri, applying scaling and EXIF orientation correction.
 *
 * @param contentResolver The ContentResolver to use for opening the Uri.
 * @param scalingFactor The factor by which to scale the image (e.g., 0.5 for 50% size). Defaults to 0.1f.
 * @return The loaded and processed Bitmap, or null if loading fails.
 */
fun Uri.loadBitmap(
    contentResolver: ContentResolver,
    scalingFactor: Float = 0.1f // Default scaling factor
): Bitmap? {

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
        // The bitmap was rotated or flipped.
    } else {
        // The bitmap did not need any orientation correction.
        scaledBitmap
    }
}

/**
 * Calculates the `inSampleSize` for BitmapFactory.Options to decode a bitmap
 * that is close to the requested width and height, while maintaining aspect ratio.
 * This helps in loading a downscaled version of the image into memory, saving memory.
 *
 * @param originalWidth The original width of the bitmap.
 * @param originalHeight The original height of the bitmap.
 * @param reqWidth The desired width of the bitmap after decoding.
 * @param reqHeight The desired height of the bitmap after decoding.
 * @return The calculated `inSampleSize` value.
 */
fun calculateInSampleSize(
    originalWidth: Int, // The original width of the image
    originalHeight: Int, // The original height of the image
    reqWidth: Int, // The target width for the downscaled image
    reqHeight: Int // The target height for the downscaled image
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
