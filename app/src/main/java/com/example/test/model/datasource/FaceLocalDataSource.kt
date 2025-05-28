package com.example.test.model.datasource

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.example.test.base.loadBitmap
import com.example.test.model.entity.ImageWithFacesEntity
import com.example.test.model.local.ProcessedImage
import com.example.test.model.room.dao.FaceDao
import com.example.test.utils.ImageUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

interface FaceLocalDataSource {
    suspend fun processEntitySync(entity: ImageWithFacesEntity): ProcessedImage?
    suspend fun getAllProcessedImages(): List<ImageWithFacesEntity>
    suspend fun insertFace(faces: ImageWithFacesEntity): Boolean
    suspend fun isImageProcessed(uri: String): Boolean
    suspend fun getImageEntityByUri(uri: String): ImageWithFacesEntity?
    suspend fun deleteImagesWithUri(list: List<String>)
}

/**
 * Implementation of the [FaceLocalDataSource] interface.
 * @property context The application context.
 * @property faceDao The DAO for face-related database operations.
 * @property imageUtils The utility class for image processing.
 */
@ExperimentalCoroutinesApi
class FaceLocalDataSourceImpl @Inject constructor(
    private val context: Context,
    private val faceDao: FaceDao,
    private val imageUtils: ImageUtils
) : FaceLocalDataSource {

    /**
     * Processes the given entity synchronously.
     * Returns a [ProcessedImage] object if successful, null otherwise.
     * @param entity The entity to process.
     * @return A [ProcessedImage] object if successful, null otherwise.
     * @throws Exception If an error occurs during processing.
     */
    override suspend fun processEntitySync(entity: ImageWithFacesEntity): ProcessedImage? {
        try {
            val bitmap = entity.imageUri.toUri().loadBitmap(context)
                ?: throw IllegalStateException("Cannot load bitmap for ${entity.imageUri}")

            val processedBitmap = imageUtils.drawFacesOnBitmap(bitmap, entity.faces)

            if (bitmap != processedBitmap && !bitmap.isRecycled) {
                bitmap.recycle()
            }

            return ProcessedImage(
                uri = entity.imageUri,
                bitmap = processedBitmap,
                timestamp = entity.timestamp,
                faces = entity.faces
            )
        } catch (e: Exception) {
            Log.e("FaceLocalDataSource", "Error processing entity", e)
            return null
        }
    }

    /**
     * Retrieves all processed images from the database.
     * @return A list of [ImageWithFacesEntity] objects.
     * @throws Exception If an error occurs during retrieval.
     * @see ImageWithFacesEntity
     */
    override suspend fun getAllProcessedImages(): List<ImageWithFacesEntity> {
        return try {
            faceDao.getAllImages()
        } catch (e: Exception) {
            Log.e("FaceLocalDataSource", "Error getting all processed images", e)
            emptyList<ImageWithFacesEntity>()
        }
    }

    /**
     * Inserts a face entity into the database.
     * @param faces The face entity to insert.
     * @return True if the insertion is successful, false otherwise.
     * @throws Exception If an error occurs during insertion.
     */
    override suspend fun insertFace(faces: ImageWithFacesEntity): Boolean {
        return try {
            val result = faceDao.insertFace(faces)
            result != -1L
        } catch (e: Exception) {
            Log.e("FaceLocalDataSource", "Error inserting face", e)
            return false
        }
    }

    /**
     * Checks if an image with the given URI has been processed.
     * @param uri The URI of the image to check.
     * @return True if the image has been processed, false otherwise.
     * @throws Exception If an error occurs during the check.
     */
    override suspend fun isImageProcessed(uri: String): Boolean {
        return try {
            faceDao.isImageProcessed(uri)
        } catch (e: Exception) {
            Log.e("FaceLocalDataSource", "Error checking if image processed", e)
            false
        }
    }

    /**
     * Retrieves an image entity by its URI.
     * @param uri The URI of the image to retrieve.
     * @return An [ImageWithFacesEntity] object if found, null otherwise.
     * @throws Exception If an error occurs during retrieval.
     */
    override suspend fun getImageEntityByUri(uri: String): ImageWithFacesEntity? {
        return try {
            faceDao.getFacesForImage(imageUri = uri)
        } catch (e: Exception) {
            Log.e("FaceLocalDataSource", "Error getting image entity by URI: $uri", e)
            null
        }
    }

    /**
     * Deletes images with the given URIs from the database.
     * @param list The list of URIs of the images to delete.
     * @throws Exception If an error occurs during deletion.
     */
    override suspend fun deleteImagesWithUri(list: List<String>) {
        try {
            faceDao.deleteImagesByUris(list)
        } catch (e: Exception) {
            Log.e("FaceLocalDataSource", "Error deleting images with URIs: $list", e)
        }
    }

}