package com.vector.verevcodex.data.remote.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.vector.verevcodex.data.preferences.backendTokenPreferenceStore
import com.vector.verevcodex.data.preferences.TokenPreferenceKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.backendTokenPreferenceStore

    suspend fun getAccessToken(): String? = dataStore.data.map { it[TokenPreferenceKeys.accessToken] }.first()
    suspend fun getRefreshToken(): String? = dataStore.data.map { it[TokenPreferenceKeys.refreshToken] }.first()

    suspend fun setTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[TokenPreferenceKeys.accessToken] = accessToken
            prefs[TokenPreferenceKeys.refreshToken] = refreshToken
        }
    }

    suspend fun clearTokens() {
        dataStore.edit { prefs ->
            prefs.remove(TokenPreferenceKeys.accessToken)
            prefs.remove(TokenPreferenceKeys.refreshToken)
        }
    }

    suspend fun hasTokens(): Boolean =
        dataStore.data.map {
            it[TokenPreferenceKeys.accessToken] != null && it[TokenPreferenceKeys.refreshToken] != null
        }.first()
}
