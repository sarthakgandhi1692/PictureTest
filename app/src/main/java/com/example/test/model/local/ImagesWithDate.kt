package com.example.test.model.local

import android.net.Uri

/**
 * Represents an image with its associated timestamp.
 * @property uri The URI of the image.
 * @property timestamp The timestamp when the image was added.
 */
data class ImagesWithDate(
    val uri: Uri,
    val timestamp: Long
)
