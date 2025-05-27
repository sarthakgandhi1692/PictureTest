package com.example.test.model.local

import android.graphics.Bitmap
import android.net.Uri

data class FaceImage(
    val uri: Uri,
    val bitmap: Bitmap?
)