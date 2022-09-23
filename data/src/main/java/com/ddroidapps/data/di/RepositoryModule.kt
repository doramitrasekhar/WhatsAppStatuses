package com.ddroidapps.data.di

import com.ddroidapps.data.repositories.UserPreferencesRepositoryImpl
import com.ddroidapps.domain.repositories.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun provideUserPreferenceRepoImpl(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl,
    ): UserPreferencesRepository
}