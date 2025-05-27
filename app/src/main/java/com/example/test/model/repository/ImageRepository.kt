package com.example.test.model.repository

import android.graphics.Rect
import com.example.test.model.datasource.FaceLocalDataSource
import com.example.test.model.datasource.FaceRecognizerDataSource
import com.example.test.model.datasource.GalleryDataSource
import com.example.test.model.entity.ImageWithFacesEntity
import com.example.test.model.local.FaceInfo
import com.example.test.model.local.ProcessedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ImageRepository {
    fun observeAllProcessedFaces(): Flow<ProcessedImage>
    suspend fun isImageProcessed(uri: String): Boolean
    suspend fun insertImageAndFaces(uri: String, faceRects: List<Rect>)
    suspend fun updateFaceName(processedImage: ProcessedImage)
    suspend fun processAllImages()
    suspend fun getProcessedImageByUri(uri: String): ProcessedImage?
}

class ImageRepositoryImpl @Inject constructor(
    private val faceLocalDataSource: FaceLocalDataSource,
    private val galleryDataSource: GalleryDataSource,
    private val faceRecognizerDataSource: FaceRecognizerDataSource
) : ImageRepository {

    override fun observeAllProcessedFaces(): Flow<ProcessedImage> {
        return faceLocalDataSource.observeAllFaces()
    }

    override suspend fun isImageProcessed(uri: String): Boolean {
        return faceLocalDataSource.isImageProcessed(uri)
    }

    override suspend fun insertImageAndFaces(uri: String, faceRects: List<Rect>) {
        withContext(Dispatchers.IO) {
            faceLocalDataSource.insertFace(
                ImageWithFacesEntity(
                    imageUri = uri,
                    timestamp = System.currentTimeMillis(),
                    faces = faceRects.map {
                        FaceInfo(
                            imageUri = uri,
                            boundingBox = it,
                            name = null
                        )
                    }
                ))

        }
    }

    override suspend fun updateFaceName(processedImage: ProcessedImage) {
        faceLocalDataSource.insertFace(
            ImageWithFacesEntity(
                imageUri = processedImage.uri,
                timestamp = processedImage.timestamp,
                faces = processedImage.faces
            )
        )
    }

    override suspend fun processAllImages() {
        val images = galleryDataSource.getPhotosFromGallery()
        images.forEach {
            if (faceLocalDataSource.isImageProcessed(it.toString()).not()) {
                val faces = faceRecognizerDataSource.detectFaces(it)
                val faceRects = faces.map { face ->
                    face.boundingBox
                }
                insertImageAndFaces(
                    uri = it.toString(),
                    faceRects = faceRects
                )
            }
        }
    }

    override suspend fun getProcessedImageByUri(uri: String): ProcessedImage? {
        return faceLocalDataSource.getProcessedImageByUri(uri)
    }


}