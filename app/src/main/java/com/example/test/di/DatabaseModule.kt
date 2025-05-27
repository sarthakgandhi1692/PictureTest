package com.example.test.di

import android.content.Context
import androidx.room.Room
import com.example.test.model.room.ImagesDatabase
import com.example.test.model.room.dao.FaceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideImageDataBase(
        context: Context
    ): ImagesDatabase {
        return Room.databaseBuilder(
            context,
            ImagesDatabase::class.java,
            "ImageDatabase"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFaceDao(
        imagesDatabase: ImagesDatabase
    ): FaceDao {
        return imagesDatabase.faceDao()
    }
}