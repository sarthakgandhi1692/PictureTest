package com.example.test.di

import com.example.test.model.datasource.FaceLocalDataSource
import com.example.test.model.datasource.FaceRecognizerDataSource
import com.example.test.model.datasource.GalleryDataSource
import com.example.test.model.repository.ImageRepository
import com.example.test.model.repository.ImageRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideTestRepository(
        faceLocalDataSource: FaceLocalDataSource,
        galleryDataSource: GalleryDataSource,
        faceRecognizerDataSource: FaceRecognizerDataSource

    ): ImageRepository {
        return ImageRepositoryImpl(
            faceLocalDataSource = faceLocalDataSource,
            galleryDataSource = galleryDataSource,
            faceRecognizerDataSource = faceRecognizerDataSource
        )
    }
}