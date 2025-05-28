package com.example.test.model.repository

import android.graphics.Rect
import android.util.Log
import com.example.test.model.datasource.FaceLocalDataSource
import com.example.test.model.datasource.GalleryDataSource
import com.example.test.model.datasource.ImageCacheDataSource
import com.example.test.model.entity.ImageWithFacesEntity
import com.example.test.model.events.ProcessedFaceEvent
import com.example.test.model.local.FaceInfo
import com.example.test.model.local.ProcessedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ImageRepository {
    fun observeAllProcessedFaces(): Flow<ProcessedImage>
    suspend fun isImageProcessed(uri: String): Boolean
    suspend fun insertImageAndFaces(
        uri: String,
        dateAdded: Long,
        faceRects: List<Rect>
    )

    suspend fun updateFaceName(processedImage: ProcessedImage): Boolean
    suspend fun processAllImages()
    suspend fun getProcessedImageByUri(uri: String): ProcessedImage?
    suspend fun cleanupOrphanedImages()
}

/**
 * Implementation of the [ImageRepository] interface.
 * @property faceLocalDataSource The local data source for face-related operations.
 * @property galleryDataSource The data source for gallery-related operations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ImageRepositoryImpl @Inject constructor(
    private val faceLocalDataSource: FaceLocalDataSource,
    private val galleryDataSource: GalleryDataSource,
    private val imageCacheDataSource: ImageCacheDataSource
) : ImageRepository {

    private val _processedFaceUpdates = MutableSharedFlow<ProcessedFaceEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )

    /**
     * Observes all processed faces.
     * @return A [Flow] emitting [ProcessedImage] objects.
     * @see ProcessedImage
     */
    override fun observeAllProcessedFaces(): Flow<ProcessedImage> {
        return _processedFaceUpdates
            .onStart {
                Log.d(TAG, "Starting face observation")
                emit(ProcessedFaceEvent.InitialLoad)
            }
            .flatMapConcat { event ->
                when (event) {
                    is ProcessedFaceEvent.NewFaceAdded -> {
                        Log.d(TAG, "Processing new face: ${event.entity.imageUri}")
                        processEntity(event.entity)
                    }

                    is ProcessedFaceEvent.FaceUpdated -> {
                        Log.d(
                            TAG,
                            "Processing updated face: ${event.entity.imageUri}"
                        )
                        imageCacheDataSource.deleteImageFromCache(uri = event.entity.imageUri)
                        processEntity(event.entity)
                    }

                    is ProcessedFaceEvent.InitialLoad -> {
                        cleanupOrphanedImages()
                        val list = faceLocalDataSource.getAllProcessedImages()
                        list.asFlow().flatMapConcat { entity ->
                            processEntity(entity)
                        }

                    }
                }
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Checks if an image with the given URI has been processed.
     * @param uri The URI of the image to check.
     * @return True if the image has been processed, false otherwise.
     */
    override suspend fun isImageProcessed(uri: String): Boolean {
        return faceLocalDataSource.isImageProcessed(uri)
    }

    /**
     * Inserts an image and its associated faces into the database.
     * @param uri The URI of the image.
     * @param dateAdded The timestamp when the image was added.
     * @param faceRects The list of face rectangles associated with the image.
     */
    override suspend fun insertImageAndFaces(
        uri: String,
        dateAdded: Long,
        faceRects: List<Rect>
    ) {
        withContext(Dispatchers.IO) {
            val entity = ImageWithFacesEntity(
                imageUri = uri,
                timestamp = dateAdded,
                faces = faceRects.map {
                    FaceInfo(
                        imageUri = uri,
                        boundingBox = it,
                        name = null
                    )
                }
            )
            val result = faceLocalDataSource.insertFace(
                entity
            )

            if (result) {
                _processedFaceUpdates.emit(
                    ProcessedFaceEvent.NewFaceAdded(
                        entity
                    )
                )
            }
        }
    }

    /**
     * Updates the face name for a processed image.
     * @param processedImage The processed image with the updated face name.
     * @return True if the update is successful, false otherwise.
     */
    override suspend fun updateFaceName(processedImage: ProcessedImage): Boolean {
        val entity = ImageWithFacesEntity(
            imageUri = processedImage.uri,
            timestamp = processedImage.timestamp,
            faces = processedImage.faces
        )

        val result = faceLocalDataSource.insertFace(
            entity
        )

        if (result) {
            _processedFaceUpdates.emit(
                ProcessedFaceEvent.FaceUpdated(
                    entity
                )
            )
        }
        return result
    }

    /**
     * Processes all images from the gallery.
     * @throws Exception If an error occurs during processing.
     * @see GalleryDataSource
     */
    override suspend fun processAllImages() {
        val images = galleryDataSource.getPhotosFromGallery()
        images.forEach {
            if (faceLocalDataSource.isImageProcessed(it.uri.toString()).not()) {
                val faces = galleryDataSource.detectFaces(it.uri)
                val faceRects = faces.map { face ->
                    face.boundingBox
                }
                insertImageAndFaces(
                    uri = it.uri.toString(),
                    dateAdded = it.timestamp,
                    faceRects = faceRects
                )
            }
        }
    }

    /**
     * Retrieves a processed image by its URI.
     * @param uri The URI of the image to retrieve.
     * @return A [ProcessedImage] object if found, null otherwise.
     */
    override suspend fun getProcessedImageByUri(uri: String): ProcessedImage? {

        imageCacheDataSource.getImageFromCache(uri = uri)?.let { cached ->
            Log.d(TAG, "Found cached result for $uri")
            return cached
        }

        return try {
            val entity = faceLocalDataSource.getImageEntityByUri(uri)
            if (entity == null) {
                return null
            }
            val result = faceLocalDataSource.processEntitySync(entity)
            result?.let {
                imageCacheDataSource.addImageToCache(image = it)
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image $uri", e)
            null
        }
    }

    /**
     * Processes an image entity.
     * @param entity The image entity to process.
     * @return A [Flow] emitting a [ProcessedImage] object.
     */
    private fun processEntity(entity: ImageWithFacesEntity): Flow<ProcessedImage> {
        return flow {
            // Check cache first
            imageCacheDataSource.getImageFromCache(uri = entity.imageUri)?.let { cached ->
                Log.d(TAG, "Found cached result for ${entity.imageUri}")
                emit(cached)
                return@flow
            }

            try {
                val result = faceLocalDataSource.processEntitySync(entity)
                result?.let {
                    imageCacheDataSource.addImageToCache(image = it)
                    emit(it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image ${entity.imageUri}", e)
            }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Cleans up orphaned images from the database.
     */
    override suspend fun cleanupOrphanedImages() {
        withContext(Dispatchers.IO) {
            try {
                // Get all image URIs from database
                val dbImages = faceLocalDataSource.getAllProcessedImages()

                // Get all valid gallery URIs
                val galleryImages = galleryDataSource.getPhotosFromGallery()

                // Find orphaned URIs (in DB but not in gallery)
                val orphanedUris = dbImages.filterNot { dbUri ->
                    galleryImages.any { galleryImage ->
                        dbUri.imageUri == galleryImage.uri.toString()
                    }
                }

                if (orphanedUris.isNotEmpty()) {
                    Log.d(TAG, "Found ${orphanedUris.size} orphaned images")

                    // Delete orphaned images from database
                    faceLocalDataSource.deleteImagesWithUri(orphanedUris.map {
                        it.imageUri
                    })

                    Log.d(
                        TAG,
                        "Deleted ${orphanedUris.size} orphaned images from database"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up orphaned images", e)
            }
        }
    }

    companion object {
        private const val TAG = "ImageRepository"
    }
}
