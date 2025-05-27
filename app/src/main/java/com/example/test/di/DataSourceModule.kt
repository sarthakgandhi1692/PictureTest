package com.example.test.di

import android.content.Context
import com.example.test.model.datasource.FaceLocalDataSource
import com.example.test.model.datasource.FaceLocalDataSourceImpl
import com.example.test.model.datasource.FaceRecognizerDataSource
import com.example.test.model.datasource.FaceRecognizerDataSourceImpl
import com.example.test.model.datasource.GalleryDataSource
import com.example.test.model.datasource.GalleryDataSourceImpl
import com.example.test.model.room.dao.FaceDao
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
        context: Context,
        faceDao: FaceDao
    ): FaceLocalDataSource {
        return FaceLocalDataSourceImpl(
            context = context,
            faceDao = faceDao
        )
    }

    @Provides
    @Singleton
    fun provideGalleryDataSource(
        context: Context
    ): GalleryDataSource {
        return GalleryDataSourceImpl(
            context = context
        )
    }

    @Provides
    @Singleton
    fun provideFaceRecognizerDataSource(
        context: Context
    ): FaceRecognizerDataSource {
        return FaceRecognizerDataSourceImpl(
            context = context
        )
    }
}