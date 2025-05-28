package com.example.test.domain

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.test.model.local.FaceInfo
import com.example.test.model.local.ProcessedImage
import com.example.test.model.repository.ImageRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.intArrayOf

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class AddUpdateFaceNameUseCaseTest {

    private lateinit var imageRepository: ImageRepository
    private lateinit var addUpdateFaceNameUseCase: AddUpdateFaceNameUseCase
    private lateinit var mockBitmap: Bitmap
    private lateinit var testRect: Rect

    @Before
    fun setup() {
        imageRepository = mockk()
        addUpdateFaceNameUseCase = AddUpdateFaceNameUseCase(imageRepository)
        mockBitmap = mockk(relaxed = true)
        testRect = Rect(0, 0, 100, 100)
    }

    @Test
    fun `invoke returns true when repository update succeeds`() = runBlocking {
        // Given
        val processedImage = ProcessedImage(
            uri = "path/to/image.jpg",
            bitmap = mockBitmap,
            timestamp = 123456789L,
            faces = listOf(
                FaceInfo(
                    imageUri = "path/to/image.jpg",
                    boundingBox = testRect,
                    name = "John Doe"
                )
            )
        )
        coEvery { imageRepository.updateFaceName(processedImage) } returns true

        // When
        val result = addUpdateFaceNameUseCase(processedImage)

        // Then
        assertTrue(result)
    }

    @Test
    fun `invoke returns false when repository update fails`() = runBlocking {
        // Given
        val processedImage = ProcessedImage(
            uri = "path/to/image.jpg",
            bitmap = mockBitmap,
            timestamp = 123456789L,
            faces = listOf(
                FaceInfo(
                    imageUri = "path/to/image.jpg",
                    boundingBox = testRect,
                    name = "John Doe"
                )
            )
        )
        coEvery { imageRepository.updateFaceName(processedImage) } returns false

        // When
        val result = addUpdateFaceNameUseCase(processedImage)

        // Then
        assertFalse(result)
    }
}