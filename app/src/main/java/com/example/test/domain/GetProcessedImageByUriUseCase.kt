package com.example.test.domain

import com.example.test.model.local.ProcessedImage
import com.example.test.model.repository.ImageRepository
import javax.inject.Inject

/**
 * Use case for retrieving a processed image by its URI.
 *
 * @property imageRepository The repository for accessing image data.
 */
class GetProcessedImageByUriUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    /**
     * Retrieves a processed image by its URI.
     *
     * @param uri The URI of the image to retrieve.
     * @return The [ProcessedImage] if found, or null otherwise.
     */
    suspend fun getProcessedImageByUri(uri: String): ProcessedImage? {
        return imageRepository.getProcessedImageByUri(uri)
    }
}