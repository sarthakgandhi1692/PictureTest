package com.example.test.domain

import com.example.test.model.local.ProcessedImage
import com.example.test.model.repository.ImageRepository
import javax.inject.Inject

class AddFaceNameUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend fun addFaceName(processedImage: ProcessedImage) {
        imageRepository.updateFaceName(processedImage)
    }

}