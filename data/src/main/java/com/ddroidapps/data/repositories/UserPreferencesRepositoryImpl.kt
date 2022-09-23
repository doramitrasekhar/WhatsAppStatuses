package com.ddroidapps.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ddroidapps.domain.repositories.UserPreferencesRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import java.io.IOException
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val userDataStorePreferences: DataStore<Preferences>,
) : UserPreferencesRepository {

    override suspend fun setUriInfo(name: String) {
        Result.runCatching {
            userDataStorePreferences.edit { preferences ->
                preferences[KEY_NAME] = name
            }
        }
    }

    override suspend fun getUriInfo(): String {
        val flow = userDataStorePreferences.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[KEY_NAME]
            }
        return flow.firstOrNull() ?: "NO_KEY_FOUND"
    }

    override suspend fun isUriInfoEnabled(): Boolean {
        val flow = userDataStorePreferences.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences.contains(KEY_NAME)
            }
        flow.onEmpty {
            emptyPreferences()
        }
        return flow.firstOrNull() ?: false
    }

    private companion object {
        val KEY_NAME = stringPreferencesKey(
            name = "uri_info"
        )
    }
}