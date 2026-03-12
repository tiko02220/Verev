package com.vector.verevcodex.domain.repository.scan

import com.vector.verevcodex.domain.model.scan.ScanMethod
import com.vector.verevcodex.domain.model.scan.ScanPreferences
import kotlinx.coroutines.flow.Flow

interface ScanPreferencesRepository {
    fun observePreferences(): Flow<ScanPreferences>
    suspend fun savePreference(method: ScanMethod, skipMethodSelection: Boolean)
    suspend fun clearPreference()
}
