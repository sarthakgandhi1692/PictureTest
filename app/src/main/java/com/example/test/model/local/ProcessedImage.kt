package com.example.test.model.local

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName

/**
 * Represents a processed image with its associated information.
 * @property uri The URI of the image.
 * @property bitmap The processed bitmap of the image.
 * @property timestamp The timestamp when the image was processed.
 * @property faces The list of faces associated with the image.
 * @see FaceInfo
 */
data class ProcessedImage(
    @SerializedName("uri")
    val uri: String,
    @SerializedName("bitmap")
    val bitmap: Bitmap,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("faces")
    val faces: List<FaceInfo>
)