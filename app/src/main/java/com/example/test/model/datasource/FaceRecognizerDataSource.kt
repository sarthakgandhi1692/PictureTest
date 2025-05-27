package com.example.test.model.datasource

import android.content.Context
import android.net.Uri
import com.example.test.base.loadBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface FaceRecognizerDataSource {
    suspend fun detectFaces(uri: Uri): List<Face>
}

class FaceRecognizerDataSourceImpl @Inject constructor(
    private val context: Context
) : FaceRecognizerDataSource {

    override suspend fun detectFaces(uri: Uri): List<Face> {
        val bitmap = uri.loadBitmap(
            context = context
        ) ?: return emptyList()
        val image = InputImage.fromBitmap(bitmap, 0)
        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()
        )
        return detector.process(image).await()
    }


}