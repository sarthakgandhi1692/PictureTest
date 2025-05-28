package com.example.test.domain

import com.example.test.model.local.ProcessedImage
import com.example.test.model.repository.ImageRepository
import javax.inject.Inject

/**
 * Use case for adding or updating a face name in a processed image.
 *
 * @property imageRepository The repository for accessing and modifying image data.
 */
class AddUpdateFaceNameUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    /**
     * Adds or updates the face name for a given processed image.
     *
     * @param processedImage The processed image with the face name to be added or updated.
     * @return True if the operation was successful, false otherwise.
     */
    suspend fun addUpdateFaceName(processedImage: ProcessedImage): Boolean {
        return imageRepository.updateFaceName(processedImage)
    }
}