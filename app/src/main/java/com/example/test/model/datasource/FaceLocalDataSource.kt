package com.example.test.model.datasource

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.net.toUri
import com.example.test.base.loadBitmap
import com.example.test.model.entity.ImageWithFacesEntity
import com.example.test.model.local.FaceInfo
import com.example.test.model.local.ProcessedImage
import com.example.test.model.room.dao.FaceDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface FaceLocalDataSource {
    fun observeAllFaces(): Flow<ProcessedImage>
    suspend fun insertFace(faces: ImageWithFacesEntity)
    suspend fun isImageProcessed(uri: String): Boolean
    suspend fun deleteFacesForImage(uri: String)
    suspend fun deleteAllFaces()
    suspend fun getProcessedImageByUri(uri: String): ProcessedImage?
}

@ExperimentalCoroutinesApi
class FaceLocalDataSourceImpl @Inject constructor(
    private val context: Context,
    private val faceDao: FaceDao
) : FaceLocalDataSource {

    override fun observeAllFaces(): Flow<ProcessedImage> =
        faceDao.getAllFacesFlow()
            .flatMapLatest { imageEntities ->
                imageEntities.asFlow().mapNotNull { entity ->
                    try {
                        val bitmap = withContext(Dispatchers.IO) {
                            entity.imageUri.toUri().loadBitmap(context)
                        } ?: return@mapNotNull null

                        val processedBitmap = drawFacesOnBitmap(bitmap, entity.faces)
                        val processedImage = ProcessedImage(
                            uri = entity.imageUri,
                            bitmap = processedBitmap,
                            timestamp = entity.timestamp,
                            faces = entity.faces
                        )

                        Log.d("Repository", "Emitting ProcessedImage for URI: ${entity.imageUri}")
                        processedImage
                    } catch (e: Exception) {
                        Log.e("Repository", "Error processing image ${entity.imageUri}", e)
                        null
                    }
                }
            }

    override suspend fun insertFace(faces: ImageWithFacesEntity) {
        faceDao.insertFace(faces)
    }

    override suspend fun isImageProcessed(uri: String): Boolean {
        return faceDao.isImageProcessed(uri)
    }


    fun drawFacesOnBitmap(bitmap: Bitmap, faces: List<FaceInfo>): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(mutableBitmap)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.RED
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 10f
        }

        for (face in faces) {
            canvas.drawRect(face.boundingBox, paint)
        }

        return mutableBitmap
    }


    override suspend fun deleteFacesForImage(uri: String) {
        faceDao.deleteFacesForImage(uri)
    }

    override suspend fun deleteAllFaces() {
        faceDao.deleteAllFaces()
    }

    override suspend fun getProcessedImageByUri(uri: String): ProcessedImage? {
        val imageWithFacesEntity = faceDao.getFacesForImage(uri)
        return try {
            val bitmap = withContext(Dispatchers.IO) {
                imageWithFacesEntity.imageUri.toUri().loadBitmap(context)
            } ?: return null

            val processedBitmap = drawFacesOnBitmap(bitmap, imageWithFacesEntity.faces)
            val processedImage = ProcessedImage(
                uri = imageWithFacesEntity.imageUri,
                bitmap = processedBitmap,
                faces = imageWithFacesEntity.faces,
                timestamp = imageWithFacesEntity.timestamp
            )

            Log.d("Repository", "Emitting ProcessedImage for URI: ${imageWithFacesEntity.imageUri}")
            processedImage
        } catch (e: Exception) {
            Log.e("Repository", "Error processing image ${imageWithFacesEntity.imageUri}", e)
            null
        }
    }

}