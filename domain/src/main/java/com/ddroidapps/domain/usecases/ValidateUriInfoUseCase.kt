package com.ddroidapps.domain.usecases

import com.ddroidapps.domain.repositories.UserPreferencesRepository
import javax.inject.Inject

class ValidateUriInfoUseCase @Inject constructor(private val userPreferencesRepository: UserPreferencesRepository) {
    suspend operator fun invoke() = userPreferencesRepository.isUriInfoEnabled()
}