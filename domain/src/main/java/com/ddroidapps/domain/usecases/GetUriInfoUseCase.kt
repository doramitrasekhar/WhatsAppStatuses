package com.ddroidapps.domain.usecases

import com.ddroidapps.domain.repositories.UserPreferencesRepository
import javax.inject.Inject

class GetUriInfoUseCase @Inject constructor(private val userPreferencesRepository: UserPreferencesRepository) {
    suspend operator fun invoke() = userPreferencesRepository.getUriInfo()
}