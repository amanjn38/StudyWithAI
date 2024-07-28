package com.geminiai.studywithai.di

import android.content.Context
import com.geminiai.studywithai.repository.BookRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBookRepository(@ApplicationContext context: Context): BookRepository {
        return BookRepository(context)
    }
}