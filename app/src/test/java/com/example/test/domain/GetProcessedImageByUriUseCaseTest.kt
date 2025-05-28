package com.example.test.domain

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.test.model.local.FaceInfo
import com.example.test.model.local.ProcessedImage
import com.example.test.model.repository.ImageRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetProcessedImageByUriUseCaseTest {

    private lateinit var imageRepository: ImageRepository
    private lateinit var getProcessedImageByUriUseCase: GetProcessedImageByUriUseCase
    private lateinit var mockBitmap: Bitmap
    private lateinit var testRect: Rect

    @Before
    fun setUp() {
        imageRepository = mockk()
        getProcessedImageByUriUseCase = GetProcessedImageByUriUseCase(imageRepository)
        mockBitmap = mockk(relaxed = true)  // Use relaxed mock for Bitmap
        testRect = Rect(0, 0, 100, 100)     // Use real Rect instance
    }

    @Test
    fun `invoke returns processed image when image exists`() = runBlocking {
        // Given
        val uri = "content://media/external/images/123"
        val expectedImage = ProcessedImage(
            uri = uri,
            bitmap = mockBitmap,
            timestamp = System.currentTimeMillis(),
            faces = listOf(
                FaceInfo(
                    imageUri = uri,
                    boundingBox = testRect,
                    name = null
                )
            )
        )
        coEvery { imageRepository.getProcessedImageByUri(uri) } returns expectedImage

        // When
        val result = getProcessedImageByUriUseCase(uri)

        // Then
        assertEquals(expectedImage, result)
    }

    @Test
    fun `invoke returns null when image does not exist`() = runBlocking {
        // Given
        val uri = "content://media/external/images/nonexistent"
        coEvery { imageRepository.getProcessedImageByUri(uri) } returns null

        // When
        val result = getProcessedImageByUriUseCase(uri)

        // Then
        assertNull(result)
    }
} 