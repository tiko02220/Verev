package com.vector.verevcodex.data.repository.scan

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.vector.verevcodex.data.preferences.ScanPreferenceKeys
import com.vector.verevcodex.data.preferences.scanPreferenceStore
import com.vector.verevcodex.domain.model.scan.ScanMethod
import com.vector.verevcodex.domain.model.scan.ScanPreferences
import com.vector.verevcodex.domain.repository.scan.ScanPreferencesRepository
import com.vector.verevcodex.domain.repository.auth.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class ScanPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val authRepository: AuthRepository,
) : ScanPreferencesRepository {
    private val dataStore = context.scanPreferenceStore

    override fun observePreferences(): Flow<ScanPreferences> = combine(
        authRepository.observeSession().map { it?.user?.id },
        dataStore.data,
    ) { accountId, preferences ->
        val method = preferences[ScanPreferenceKeys.preferredScanMethod(accountId)]
            ?.let { runCatching { ScanMethod.valueOf(it) }.getOrNull() }
        val skip = preferences[ScanPreferenceKeys.skipMethodSelection(accountId)] ?: false
        ScanPreferences(
            preferredMethod = method,
            skipMethodSelection = skip && method != null,
        )
    }

    override suspend fun savePreference(method: ScanMethod, skipMethodSelection: Boolean) {
        val accountId = authRepository.observeSession().first()?.user?.id
        dataStore.edit { preferences ->
            preferences[ScanPreferenceKeys.preferredScanMethod(accountId)] = method.name
            preferences[ScanPreferenceKeys.skipMethodSelection(accountId)] = skipMethodSelection
        }
    }

    override suspend fun clearPreference() {
        val accountId = authRepository.observeSession().first()?.user?.id
        dataStore.edit { preferences ->
            preferences.remove(ScanPreferenceKeys.preferredScanMethod(accountId))
            preferences.remove(ScanPreferenceKeys.skipMethodSelection(accountId))
        }
    }
}
