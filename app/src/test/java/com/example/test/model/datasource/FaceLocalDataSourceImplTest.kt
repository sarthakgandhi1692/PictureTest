package com.example.test.model.datasource

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.test.base.loadBitmap
import com.example.test.model.entity.ImageWithFacesEntity
import com.example.test.model.local.FaceInfo
import com.example.test.model.room.dao.FaceDao
import com.example.test.utils.ImageUtils
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE, sdk = [34])
class FaceLocalDataSourceImplTest {

    private lateinit var faceLocalDataSource: FaceLocalDataSourceImpl
    private lateinit var faceDao: FaceDao
    private lateinit var imageUtils: ImageUtils
    private lateinit var contentResolver: ContentResolver
    private lateinit var mockBitmap: Bitmap
    private lateinit var mockProcessedBitmap: Bitmap

    @Before
    fun setup() {
        faceDao = mockk()
        imageUtils = mockk()
        contentResolver = mockk()
        mockBitmap = mockk()
        mockProcessedBitmap = mockk()

        mockkStatic(Log::class)
        mockkStatic(Uri::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Uri.parse(any()) } returns mockk(relaxed = true)

        mockkStatic("androidx.core.net.UriKt")
        mockkStatic("com.example.test.base.ExtensionsKt")

        faceLocalDataSource = FaceLocalDataSourceImpl(faceDao, imageUtils, contentResolver)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `processEntitySync returns ProcessedImage when successful`() = runTest {
        // Given
        val testUri = "test_uri"
        val timestamp = 123456789L
        val faces = listOf(
            FaceInfo(
                imageUri = testUri,
                boundingBox = Rect(0, 0, 100, 100),
                name = "Test Name"
            )
        )
        val entity = ImageWithFacesEntity(
            imageUri = testUri,
            timestamp = timestamp,
            faces = faces
        )

        every { testUri.toUri() } returns mockk(relaxed = true)
        every { any<Uri>().loadBitmap(contentResolver) } returns mockBitmap
        every { imageUtils.drawFacesOnBitmap(mockBitmap, faces) } returns mockProcessedBitmap
        every { mockBitmap.isRecycled } returns false
        every { mockBitmap.recycle() } just Runs

        // When
        val result = faceLocalDataSource.processEntitySync(entity)

        // Then
        assertNotNull(result)
        result?.let {
            assertEquals(testUri, it.uri)
            assertEquals(timestamp, it.timestamp)
            assertEquals(faces, it.faces)
            assertEquals(mockProcessedBitmap, it.bitmap)
        }
        verify { mockBitmap.recycle() }
    }

    @Test
    fun `processEntitySync returns null when bitmap loading fails`() = runTest {
        // Given
        val testUri = "test_uri"
        val entity = ImageWithFacesEntity(
            imageUri = testUri,
            timestamp = 123456789L,
            faces = emptyList()
        )

        every { testUri.toUri() } returns mockk()
        every { any<Uri>().loadBitmap(contentResolver) } returns null

        // When
        val result = faceLocalDataSource.processEntitySync(entity)

        // Then
        assertNull(result)
    }

    @Test
    fun `getAllProcessedImages returns list from dao`() = runTest {
        // Given
        val expectedList = listOf(
            ImageWithFacesEntity(
                imageUri = "test_uri",
                timestamp = 123456789L,
                faces = emptyList()
            )
        )
        coEvery { faceDao.getAllImages() } returns expectedList

        // When
        val result = faceLocalDataSource.getAllProcessedImages()

        // Then
        assertEquals(expectedList, result)
        coVerify { faceDao.getAllImages() }
    }

    @Test
    fun `getAllProcessedImages returns empty list on error`() = runTest {
        // Given
        coEvery { faceDao.getAllImages() } throws Exception("Test exception")

        // When
        val result = faceLocalDataSource.getAllProcessedImages()

        // Then
        assertTrue(result.isEmpty())
        coVerify { faceDao.getAllImages() }
    }

    @Test
    fun `insertFace returns true when successful`() = runTest {
        // Given
        val entity = ImageWithFacesEntity(
            imageUri = "test_uri",
            timestamp = 123456789L,
            faces = emptyList()
        )
        coEvery { faceDao.insertFace(entity) } returns 1L

        // When
        val result = faceLocalDataSource.insertFace(entity)

        // Then
        assertTrue(result)
        coVerify { faceDao.insertFace(entity) }
    }

    @Test
    fun `insertFace returns false when dao returns -1`() = runTest {
        // Given
        val entity = ImageWithFacesEntity(
            imageUri = "test_uri",
            timestamp = 123456789L,
            faces = emptyList()
        )
        coEvery { faceDao.insertFace(entity) } returns -1L

        // When
        val result = faceLocalDataSource.insertFace(entity)

        // Then
        assertFalse(result)
        coVerify { faceDao.insertFace(entity) }
    }

    @Test
    fun `isImageProcessed returns value from dao`() = runTest {
        // Given
        val testUri = "test_uri"
        coEvery { faceDao.isImageProcessed(testUri) } returns true

        // When
        val result = faceLocalDataSource.isImageProcessed(testUri)

        // Then
        assertTrue(result)
        coVerify { faceDao.isImageProcessed(testUri) }
    }

    @Test
    fun `isImageProcessed returns false on error`() = runTest {
        // Given
        val testUri = "test_uri"
        coEvery { faceDao.isImageProcessed(testUri) } throws Exception("Test exception")

        // When
        val result = faceLocalDataSource.isImageProcessed(testUri)

        // Then
        assertFalse(result)
        coVerify { faceDao.isImageProcessed(testUri) }
    }

    @Test
    fun `getImageEntityByUri returns entity from dao`() = runTest {
        // Given
        val testUri = "test_uri"
        val expectedEntity = ImageWithFacesEntity(
            imageUri = testUri,
            timestamp = 123456789L,
            faces = emptyList()
        )
        coEvery { faceDao.getFacesForImage(testUri) } returns expectedEntity

        // When
        val result = faceLocalDataSource.getImageEntityByUri(testUri)

        // Then
        assertEquals(expectedEntity, result)
        coVerify { faceDao.getFacesForImage(testUri) }
    }

    @Test
    fun `getImageEntityByUri returns null on error`() = runTest {
        // Given
        val testUri = "test_uri"
        coEvery { faceDao.getFacesForImage(testUri) } throws Exception("Test exception")

        // When
        val result = faceLocalDataSource.getImageEntityByUri(testUri)

        // Then
        assertNull(result)
        coVerify { faceDao.getFacesForImage(testUri) }
    }

    @Test
    fun `deleteImagesWithUri calls dao correctly`() = runTest {
        // Given
        val uris = listOf("test_uri1", "test_uri2")
        coEvery { faceDao.deleteImagesByUris(uris) } returns 2

        // When
        faceLocalDataSource.deleteImagesWithUri(uris)

        // Then
        coVerify { faceDao.deleteImagesByUris(uris) }
    }

    @Test
    fun `deleteImagesWithUri handles error gracefully`() = runTest {
        // Given
        val uris = listOf("test_uri1", "test_uri2")
        coEvery { faceDao.deleteImagesByUris(uris) } throws Exception("Test exception")

        // When
        faceLocalDataSource.deleteImagesWithUri(uris)

        // Then
        coVerify { faceDao.deleteImagesByUris(uris) }
    }
} 