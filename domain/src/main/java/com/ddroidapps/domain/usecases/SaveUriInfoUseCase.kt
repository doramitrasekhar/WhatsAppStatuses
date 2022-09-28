package com.ddroidapps.domain.usecases

import com.ddroidapps.domain.repositories.UserPreferencesRepository
import javax.inject.Inject

class SaveUriInfoUseCase @Inject constructor(private val userPreferencesRepository: UserPreferencesRepository) {
    suspend operator fun invoke(uri: String) = userPreferencesRepository.setUriInfo(uri)
}