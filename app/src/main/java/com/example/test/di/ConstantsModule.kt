package com.example.test.di

import com.example.test.di.qualifiers.BaseUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ConstantsModule {

    @Provides
    @BaseUrl
    @Singleton
    fun provideBaseUrl(): String {
        return "https://www.google.com/"
    }
}