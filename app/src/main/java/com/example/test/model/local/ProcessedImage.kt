package com.example.test.model.local

import android.graphics.Bitmap

/**
 * Represents a processed image with its associated information.
 * @property uri The URI of the image.
 * @property bitmap The processed bitmap of the image.
 * @property timestamp The timestamp when the image was processed.
 * @property faces The list of faces associated with the image.
 * @see FaceInfo
 */
data class ProcessedImage(
    val uri: String,
    val bitmap: Bitmap,
    val timestamp: Long,
    val faces: List<FaceInfo>
)