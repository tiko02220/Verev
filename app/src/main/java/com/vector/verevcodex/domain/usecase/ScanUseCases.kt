package com.vector.verevcodex.domain.usecase

import com.vector.verevcodex.domain.model.ScanMethod
import com.vector.verevcodex.domain.model.ScanPreferences
import com.vector.verevcodex.domain.repository.ScanPreferencesRepository
import kotlinx.coroutines.flow.Flow

class ObserveScanPreferencesUseCase(
    private val repository: ScanPreferencesRepository,
) {
    operator fun invoke(): Flow<ScanPreferences> = repository.observePreferences()
}

class SaveScanPreferenceUseCase(
    private val repository: ScanPreferencesRepository,
) {
    suspend operator fun invoke(method: ScanMethod, skipMethodSelection: Boolean) {
        repository.savePreference(method, skipMethodSelection)
    }
}

class ClearScanPreferenceUseCase(
    private val repository: ScanPreferencesRepository,
) {
    suspend operator fun invoke() {
        repository.clearPreference()
    }
}
