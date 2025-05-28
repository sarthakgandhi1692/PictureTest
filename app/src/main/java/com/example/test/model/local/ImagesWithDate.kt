package com.example.test.model.local

import android.net.Uri
import com.google.gson.annotations.SerializedName

/**
 * Represents an image with its associated timestamp.
 * @property uri The URI of the image.
 * @property timestamp The timestamp when the image was added.
 */
data class ImagesWithDate(
    @SerializedName("uri")
    val uri: Uri,
    @SerializedName("timestamp")
    val timestamp: Long
)
