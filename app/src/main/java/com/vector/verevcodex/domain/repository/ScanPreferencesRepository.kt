package com.vector.verevcodex.domain.repository

import com.vector.verevcodex.domain.model.ScanMethod
import com.vector.verevcodex.domain.model.ScanPreferences
import kotlinx.coroutines.flow.Flow

interface ScanPreferencesRepository {
    fun observePreferences(): Flow<ScanPreferences>
    suspend fun savePreference(method: ScanMethod, skipMethodSelection: Boolean)
    suspend fun clearPreference()
}
