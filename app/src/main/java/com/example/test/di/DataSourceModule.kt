package com.example.test.di

import android.content.ContentResolver
import com.example.test.model.datasource.FaceLocalDataSource
import com.example.test.model.datasource.FaceLocalDataSourceImpl
import com.example.test.model.datasource.GalleryDataSource
import com.example.test.model.datasource.GalleryDataSourceImpl
import com.example.test.model.datasource.ImageCacheDataSource
import com.example.test.model.datasource.ImageCacheDataSourceImpl
import com.example.test.model.room.dao.FaceDao
import com.example.test.utils.ImageUtils
import com.google.mlkit.vision.face.FaceDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@OptIn(ExperimentalCoroutinesApi::class)
class DataSourceModule {

    @Provides
    @Singleton
    fun provideFaceLocalDataSource(
        faceDao: FaceDao,
        imageUtils: ImageUtils,
        contentResolver: ContentResolver
    ): FaceLocalDataSource {
        return FaceLocalDataSourceImpl(
            faceDao = faceDao,
            imageUtils = imageUtils,
            contentResolver = contentResolver
        )
    }

    @Provides
    @Singleton
    fun provideGalleryDataSource(
        contentResolver: ContentResolver,
        faceDetector: FaceDetector
    ): GalleryDataSource {
        return GalleryDataSourceImpl(
            contentResolver = contentResolver,
            detector = faceDetector
        )
    }

    @Provides
    @Singleton
    fun provideImageCacheDataSource(): ImageCacheDataSource {
        return ImageCacheDataSourceImpl()
    }
}