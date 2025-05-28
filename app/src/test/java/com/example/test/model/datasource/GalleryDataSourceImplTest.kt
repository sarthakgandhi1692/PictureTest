package com.example.test.model.datasource

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.test.base.loadBitmap
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GalleryDataSourceImplTest {
    @MockK
    private lateinit var contentResolver: ContentResolver

    @MockK
    private lateinit var faceDetector: FaceDetector

    @MockK
    private lateinit var cursor: Cursor

    private lateinit var galleryDataSource: GalleryDataSourceImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        mockkStatic("androidx.core.net.UriKt")
        mockkStatic("com.example.test.base.ExtensionsKt")
        mockkStatic(Log::class)
        mockkStatic(Uri::class)
        mockkStatic(ContentUris::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Uri.parse(any()) } returns mock(Uri::class.java)
        every { 
            ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                any()
            ) 
        } returns mockk(relaxed = true)
        galleryDataSource = GalleryDataSourceImpl(faceDetector, contentResolver)
    }

    @Test
    fun `getPhotosFromGallery returns correct list of images`() = runBlocking {
        // Given
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} ASC"
        val imageId = 1L
        val dateAdded = 1000L
        val expectedUri = mockk<Uri>()

        every {
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )
        } returns cursor

        every { cursor.moveToNext() } returnsMany listOf(true, false)
        every { cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID) } returns 0
        every { cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED) } returns 1
        every { cursor.getLong(0) } returns imageId
        every { cursor.getLong(1) } returns dateAdded
        every { cursor.close() } just Runs
        every { 
            ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageId
            ) 
        } returns expectedUri

        // When
        val result = galleryDataSource.getPhotosFromGallery()

        // Then
        assertEquals(1, result.size)
        assertEquals(expectedUri, result[0].uri)
        assertEquals(dateAdded * 1000L, result[0].timestamp)
        verify { cursor.close() }
    }

    @Test
    fun `getPhotosFromGallery returns empty list when cursor is null`() = runBlocking {
        // Given
        every {
            contentResolver.query(
                any(),
                any(),
                null,
                null,
                any()
            )
        } returns null

        // When
        val result = galleryDataSource.getPhotosFromGallery()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `detectFaces returns faces when image is valid`() = runBlocking {
        // Given
        val uri = mockk<Uri>()
        val bitmap = mockk<Bitmap>()
        val face = mockk<Face>()
        val faceList = listOf(face)
        val task = mockk<Task<List<Face>>>()
        val inputImage = mockk<InputImage>()

        mockkStatic("com.example.test.base.ExtensionsKt")
        mockkStatic(InputImage::class)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        
        every { uri.loadBitmap(contentResolver) } returns bitmap
        every { InputImage.fromBitmap(bitmap, 0) } returns inputImage
        every { faceDetector.process(inputImage) } returns task
        coEvery { task.await() } returns faceList

        // When
        val result = galleryDataSource.detectFaces(uri)

        // Then
        assertEquals(faceList, result)
        verify { 
            uri.loadBitmap(contentResolver)
            InputImage.fromBitmap(bitmap, 0)
            faceDetector.process(inputImage)
        }
        coVerify { task.await() }
    }

    @Test
    fun `detectFaces returns empty list when bitmap is null`() = runBlocking {
        // Given
        val uri = mockk<Uri>()
        mockkStatic("com.example.test.base.ExtensionsKt")
        every { uri.loadBitmap(contentResolver) } returns null

        // When
        val result = galleryDataSource.detectFaces(uri)

        // Then
        assertTrue(result.isEmpty())
        verify(exactly = 0) { faceDetector.process(ofType(InputImage::class)) }
    }
} 