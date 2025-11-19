package com.roastkoff.displayposter.di

import com.google.firebase.firestore.FirebaseFirestore
import com.roastkoff.displayposter.repository.DisplayRepository
import com.roastkoff.displayposter.repository.DisplayRepositoryImpl
import com.roastkoff.displayposter.repository.PairingRepository
import com.roastkoff.displayposter.repository.PairingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindDisplayRepository(
        displayRepositoryImpl: DisplayRepositoryImpl
    ): DisplayRepository

    @Binds
    @Singleton
    abstract fun bindPairingRepository(
        pairingRepositoryImpl: PairingRepositoryImpl
    ): PairingRepository
}