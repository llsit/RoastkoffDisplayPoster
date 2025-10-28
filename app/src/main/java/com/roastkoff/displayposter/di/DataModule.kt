package com.roastkoff.displayposter.di

import com.roastkoff.displayposter.repository.DisplayRepository
import com.roastkoff.displayposter.repository.DisplayRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindDisplayRepository(
        displayRepositoryImpl: DisplayRepositoryImpl
    ): DisplayRepository
}