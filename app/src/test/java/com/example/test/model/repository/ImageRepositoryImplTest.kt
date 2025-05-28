package com.example.test.model.repository

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.example.test.model.datasource.FaceLocalDataSource
import com.example.test.model.datasource.GalleryDataSource
import com.example.test.model.datasource.ImageCacheDataSource
import com.example.test.model.entity.ImageWithFacesEntity
import com.example.test.model.local.FaceInfo
import com.example.test.model.local.ImagesWithDate
import com.example.test.model.local.ProcessedImage
import com.google.mlkit.vision.face.Face
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ImageRepositoryImplTest {

    @Mock
    private lateinit var faceLocalDataSource: FaceLocalDataSource

    @Mock
    private lateinit var galleryDataSource: GalleryDataSource

    @Mock
    private lateinit var imageCacheDataSource: ImageCacheDataSource

    private lateinit var imageRepository: ImageRepositoryImpl

    // Mock bitmap for testing
    private lateinit var mockBitmap: Bitmap
    private lateinit var mockFace: Face

    @Before
    fun setup() {
        mockkStatic(Log::class)
        mockkStatic(Uri::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Uri.parse(any()) } returns mock(Uri::class.java)

        imageRepository = ImageRepositoryImpl(
            faceLocalDataSource,
            galleryDataSource,
            imageCacheDataSource
        )
        mockBitmap = mock(Bitmap::class.java)
        mockFace = mock(Face::class.java)
        `when`(mockFace.boundingBox).thenReturn(Rect(0, 0, 100, 100))
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
        unmockkStatic(Uri::class)
    }

    @Test
    fun `isImageProcessed returns correct value`() = runTest {
        // Given
        val testUri = "test_uri"
        `when`(faceLocalDataSource.isImageProcessed(testUri)).thenReturn(true)

        // When
        val result = imageRepository.isImageProcessed(testUri)

        // Then
        assertTrue(result)
        verify(faceLocalDataSource).isImageProcessed(testUri)
    }

    @Test
    fun `insertImageAndFaces successfully inserts data`() = runTest {
        // Given
        val testUri = "test_uri"
        val testTimestamp = 123456789L
        val testRect = Rect(0, 0, 100, 100)
        val faceRects = listOf(testRect)

        `when`(faceLocalDataSource.insertFace(any())).thenReturn(true)

        // When
        imageRepository.insertImageAndFaces(testUri, testTimestamp, faceRects)

        // Then
        verify(faceLocalDataSource).insertFace(any())
    }

    @Test
    fun `updateFaceName updates face information correctly`() = runTest {
        // Given
        val testProcessedImage = ProcessedImage(
            uri = "test_uri",
            bitmap = mockBitmap,
            timestamp = 123456789L,
            faces = listOf(
                FaceInfo(
                    imageUri = "test_uri",
                    boundingBox = Rect(0, 0, 100, 100),
                    name = "Test Name"
                )
            )
        )

        `when`(faceLocalDataSource.insertFace(any())).thenReturn(true)

        // When
        val result = imageRepository.updateFaceName(testProcessedImage)

        // Then
        assertTrue(result)
        verify(faceLocalDataSource).insertFace(any())
    }

    @Test
    fun `getProcessedImageByUri returns cached image when available`() = runTest {
        // Given
        val testUri = "test_uri"
        val cachedImage = ProcessedImage(
            uri = testUri,
            bitmap = mockBitmap,
            timestamp = 123456789L,
            faces = emptyList()
        )

        `when`(imageCacheDataSource.getImageFromCache(testUri)).thenReturn(cachedImage)

        // When
        val result = imageRepository.getProcessedImageByUri(testUri)

        // Then
        assertEquals(cachedImage, result)
        verify(imageCacheDataSource).getImageFromCache(testUri)
        verify(faceLocalDataSource, times(0)).getImageEntityByUri(testUri)
    }

    @Test
    fun `getProcessedImageByUri returns null when image not found`() = runTest {
        // Given
        val testUri = "test_uri"
        `when`(imageCacheDataSource.getImageFromCache(testUri)).thenReturn(null)
        `when`(faceLocalDataSource.getImageEntityByUri(testUri)).thenReturn(null)

        // When
        val result = imageRepository.getProcessedImageByUri(testUri)

        // Then
        assertNull(result)
        verify(imageCacheDataSource).getImageFromCache(testUri)
        verify(faceLocalDataSource).getImageEntityByUri(testUri)
    }

    @Test
    fun `cleanupOrphanedImages removes non-existent images`() = runTest {
        // Given
        val dbImages = listOf(
            ImageWithFacesEntity("uri1", 123L, emptyList()),
            ImageWithFacesEntity("uri2", 456L, emptyList())
        )
        val mockUri = mock(Uri::class.java)
        `when`(mockUri.toString()).thenReturn("uri1")
        val galleryImages = listOf(
            ImagesWithDate(mockUri, 123L)
        )
        every { "uri1".toUri() } returns mockUri

        `when`(faceLocalDataSource.getAllProcessedImages()).thenReturn(dbImages)
        `when`(galleryDataSource.getPhotosFromGallery()).thenReturn(galleryImages)

        // When
        imageRepository.cleanupOrphanedImages()

        // Then
        verify(faceLocalDataSource).deleteImagesWithUri(eq(listOf("uri2")))
    }

    @Test
    fun `processAllImages skips already processed images`() = runTest {
        // Given
        val mockUri = mock(Uri::class.java)
        val galleryImage = ImagesWithDate(mockUri, 123L)
        every { "test_uri".toUri() } returns mockUri
        `when`(galleryDataSource.getPhotosFromGallery()).thenReturn(listOf(galleryImage))
        `when`(faceLocalDataSource.isImageProcessed(any())).thenReturn(true)

        // When
        imageRepository.processAllImages()

        // Then
        verify(galleryDataSource, times(0)).detectFaces(any())
    }

    @Test
    fun `processAllImages processes new images`() = runTest {
        // Given
        val mockUri = mock(Uri::class.java)
        val galleryImage = ImagesWithDate(mockUri, 123L)
        every { "test_uri".toUri() } returns mockUri
        
        `when`(galleryDataSource.getPhotosFromGallery()).thenReturn(listOf(galleryImage))
        `when`(faceLocalDataSource.isImageProcessed(any())).thenReturn(false)
        `when`(galleryDataSource.detectFaces(mockUri)).thenReturn(listOf(mockFace))
        `when`(faceLocalDataSource.insertFace(any())).thenReturn(true)

        // When
        imageRepository.processAllImages()

        // Then
        verify(galleryDataSource).detectFaces(mockUri)
        verify(faceLocalDataSource).insertFace(any())
    }

    @Test
    fun `observeAllProcessedFaces emits initial load event`() = runTest {
        // Given
        val entity = ImageWithFacesEntity("test_uri", 123L, emptyList())
        val processedImage = ProcessedImage(
            uri = "test_uri",
            bitmap = mockBitmap,
            timestamp = 123L,
            faces = emptyList()
        )
        
        `when`(faceLocalDataSource.getAllProcessedImages()).thenReturn(listOf(entity))
        whenever(faceLocalDataSource.processEntitySync(entity)).thenReturn(processedImage)

        // When
        val results = imageRepository.observeAllProcessedFaces().first()

        // Then
        assertEquals(processedImage, results)
    }
}