package com.example.test.model.datasource

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.test.base.loadBitmap
import com.example.test.model.local.ImagesWithDate
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface GalleryDataSource {
    suspend fun getPhotosFromGallery(): List<ImagesWithDate>
    suspend fun detectFaces(uri: Uri): List<Face>
}

/**
 * Implementation of the [GalleryDataSource] interface.
 * @property detector The ML Kit face detector.
 */
class GalleryDataSourceImpl @Inject constructor(
    private val detector: FaceDetector,
    private val contentResolver: ContentResolver
) : GalleryDataSource {

    /**
     * Retrieves a list of images from the device's gallery.
     * @return A list of [ImagesWithDate] objects containing the image URI and timestamp.
     */
    override suspend fun getPhotosFromGallery(): List<ImagesWithDate> {
        val images = mutableListOf<ImagesWithDate>()
        try {
            val projection =
                arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN)
            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateAdded = cursor.getLong(dateColumn) * 1000L
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    images.add(
                        ImagesWithDate(
                            uri = uri,
                            timestamp = dateAdded
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return images
    }

    /**
     * Detects faces in the given image URI.
     * @param uri The URI of the image to detect faces in.
     * @return A list of [Face] objects representing the detected faces.
     * @throws Exception If an error occurs during face detection.
     */
    override suspend fun detectFaces(uri: Uri): List<Face> {
        try {
            val bitmap = uri.loadBitmap(contentResolver = contentResolver) ?: return emptyList()
            val image = InputImage.fromBitmap(bitmap, 0)
            if (image.width < 32 || image.height < 32) {
                return emptyList()
            }
            val result = detector.process(image).await()
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
            return result
        } catch (e: Exception) {
            return emptyList()
        }
    }
}