package com.example.test.model.local

import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.face.Face

data class FaceImage(
    val uri: Uri,
    val bitmap: Bitmap?
)