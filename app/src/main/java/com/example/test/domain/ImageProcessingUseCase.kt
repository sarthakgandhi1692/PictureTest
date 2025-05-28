package com.example.test.domain

import com.example.test.model.repository.ImageRepository
import javax.inject.Inject

/**
 * Use case for processing images.
 * This class encapsulates the business logic related to image processing.
 */
class ImageProcessingUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    /**
     * Processes all images available in the repository.
     * This is a suspend function, indicating it performs asynchronous operations.
     */
    suspend fun processImages() {
        imageRepository.processAllImages()
    }
}