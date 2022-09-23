package com.ddroidapps.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.ddroidapps.data.repositories.UserPreferencesRepositoryImpl
import com.ddroidapps.domain.repositories.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "com.assignment.user_preferences"
)

@Module
@InstallIn(SingletonComponent::class)
abstract class UserPreferencesModule {

    // binds instance of MyUserPreferencesRepository
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        myUserPreferencesRepository: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    companion object {
        // provides instance of DataStore
        @Provides
        @Singleton
        fun provideUserDataStorePreferences(
            @ApplicationContext applicationContext: Context
        ): DataStore<Preferences> {
            return applicationContext.userDataStore
        }
    }
}