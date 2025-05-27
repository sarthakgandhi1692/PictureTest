package com.example.test.model.local

import android.graphics.Bitmap

data class ProcessedImage(
    val uri: String,
    val bitmap: Bitmap,
    val timestamp: Long,
    val faces: List<FaceInfo>
)