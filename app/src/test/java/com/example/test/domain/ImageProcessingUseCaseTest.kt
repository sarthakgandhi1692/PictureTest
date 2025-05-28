package com.example.test.domain

import com.example.test.model.repository.ImageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

@ExperimentalCoroutinesApi
class ImageProcessingUseCaseTest {

    @Mock
    private lateinit var imageRepository: ImageRepository

    private lateinit var imageProcessingUseCase: ImageProcessingUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        imageProcessingUseCase = ImageProcessingUseCase(imageRepository)
    }

    @Test
    fun `invoke should call processAllImages on repository`() = runTest {
        // When
        imageProcessingUseCase.invoke()

        // Then
        verify(imageRepository).processAllImages()
        verifyNoMoreInteractions(imageRepository)
    }
} 