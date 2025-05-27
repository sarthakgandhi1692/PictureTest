package com.example.test.domain

import com.example.test.model.repository.ImageRepository
import javax.inject.Inject

class ImageProcessingUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {

    suspend fun processImages() {
        imageRepository.processAllImages()
    }
}