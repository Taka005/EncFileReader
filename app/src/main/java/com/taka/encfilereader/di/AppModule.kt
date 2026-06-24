package com.taka.encfilereader.di

import com.taka.encfilereader.manager.StorageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideStorageManager(): StorageManager {
        return StorageManager()
    }
}