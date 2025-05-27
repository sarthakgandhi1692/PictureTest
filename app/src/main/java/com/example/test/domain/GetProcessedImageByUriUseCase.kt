package com.example.test.domain

import com.example.test.model.local.ProcessedImage
import com.example.test.model.repository.ImageRepository
import javax.inject.Inject

class GetProcessedImageByUriUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend fun getProcessedImageByUri(uri: String): ProcessedImage? {
        return imageRepository.getProcessedImageByUri(uri)
    }

}