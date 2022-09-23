package com.ddroidapps.domain.repositories

interface UserPreferencesRepository {
    suspend fun setUriInfo(
        name: String,
    )

    suspend fun getUriInfo(): String

    suspend fun isUriInfoEnabled(): Boolean
}