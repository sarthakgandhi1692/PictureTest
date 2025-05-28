package com.example.test.model.datasource

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.test.model.local.FaceInfo
import com.example.test.model.local.ProcessedImage
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ImageCacheDataSourceImplTest {

    private lateinit var imageCacheDataSource: ImageCacheDataSourceImpl
    private lateinit var mockBitmap: Bitmap

    @Before
    fun setup() {
        imageCacheDataSource = ImageCacheDataSourceImpl()
        mockBitmap = mockk<Bitmap>(relaxed = true)
    }

    @Test
    fun `test add and get image from cache`() {
        // Given
        val uri = "test_uri"
        val processedImage = ProcessedImage(
            uri = uri,
            bitmap = mockBitmap,
            timestamp = 123456789L,
            faces = listOf(
                FaceInfo(
                    imageUri = uri,
                    boundingBox = Rect(0, 0, 100, 100),
                    name = "Test Name"
                )
            )
        )

        // When
        imageCacheDataSource.addImageToCache(processedImage)
        val cachedImage = imageCacheDataSource.getImageFromCache(uri)

        // Then
        assertNotNull(cachedImage)
        assertEquals(uri, cachedImage?.uri)
        assertEquals(mockBitmap, cachedImage?.bitmap)
        assertEquals(processedImage.timestamp, cachedImage?.timestamp)
        assertEquals(processedImage.faces, cachedImage?.faces)
    }

    @Test
    fun `test delete image from cache`() {
        // Given
        val uri = "test_uri"
        val processedImage = ProcessedImage(
            uri = uri,
            bitmap = mockBitmap,
            timestamp = 123456789L,
            faces = emptyList()
        )
        imageCacheDataSource.addImageToCache(processedImage)

        // When
        imageCacheDataSource.deleteImageFromCache(uri)
        val cachedImage = imageCacheDataSource.getImageFromCache(uri)

        // Then
        assertNull(cachedImage)
    }

    @Test
    fun `test get non-existent image returns null`() {
        // When
        val cachedImage = imageCacheDataSource.getImageFromCache("non_existent_uri")

        // Then
        assertNull(cachedImage)
    }

    @Test
    fun `test cache size limit`() {
        // Given
        val images = (1..51).map { index ->
            ProcessedImage(
                uri = "test_uri_$index",
                bitmap = mockBitmap,
                timestamp = 123456789L + index,
                faces = emptyList()
            )
        }

        // When
        images.forEach { imageCacheDataSource.addImageToCache(it) }

        // Then
        // First image should be evicted due to LRU cache size limit of 50
        assertNull(imageCacheDataSource.getImageFromCache("test_uri_1"))
        // Last image should still be in cache
        assertNotNull(imageCacheDataSource.getImageFromCache("test_uri_51"))
    }
} 