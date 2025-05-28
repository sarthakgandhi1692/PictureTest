package com.example.test.di

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class ResourcesModule {

    @Singleton
    @Provides
    fun provideContentResolver(
        context: Context
    ): ContentResolver {
        return context.contentResolver
    }
}