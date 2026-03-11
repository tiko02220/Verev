package com.vector.verevcodex.data.repository.scan

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vector.verevcodex.domain.model.ScanMethod
import com.vector.verevcodex.domain.model.ScanPreferences
import com.vector.verevcodex.domain.repository.ScanPreferencesRepository
import com.vector.verevcodex.domain.repository.auth.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.scanDataStore by preferencesDataStore(name = "scan_prefs")

@Singleton
class ScanPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val authRepository: AuthRepository,
) : ScanPreferencesRepository {
    private val dataStore = context.scanDataStore

    override fun observePreferences(): Flow<ScanPreferences> = combine(
        authRepository.observeSession().map { it?.user?.id ?: "anonymous" },
        dataStore.data,
    ) { accountId, preferences ->
        val method = preferences[stringPreferencesKey("${accountId}_preferred_scan_method")]
            ?.let { runCatching { ScanMethod.valueOf(it) }.getOrNull() }
        val skip = preferences[booleanPreferencesKey("${accountId}_skip_scan_method_selection")] ?: false
        ScanPreferences(
            preferredMethod = method,
            skipMethodSelection = skip && method != null,
        )
    }

    override suspend fun savePreference(method: ScanMethod, skipMethodSelection: Boolean) {
        val accountId = authRepository.observeSession().first()?.user?.id ?: "anonymous"
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("${accountId}_preferred_scan_method")] = method.name
            preferences[booleanPreferencesKey("${accountId}_skip_scan_method_selection")] = skipMethodSelection
        }
    }

    override suspend fun clearPreference() {
        val accountId = authRepository.observeSession().first()?.user?.id ?: "anonymous"
        dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey("${accountId}_preferred_scan_method"))
            preferences.remove(booleanPreferencesKey("${accountId}_skip_scan_method_selection"))
        }
    }
}
