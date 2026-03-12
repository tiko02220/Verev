package com.vector.verevcodex.domain.usecase.scan

import com.vector.verevcodex.domain.model.scan.ScanMethod
import com.vector.verevcodex.domain.model.scan.ScanPreferences
import com.vector.verevcodex.domain.repository.scan.ScanPreferencesRepository
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
