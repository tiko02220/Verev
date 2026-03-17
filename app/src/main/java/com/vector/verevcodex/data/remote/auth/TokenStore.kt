package com.vector.verevcodex.data.remote.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vector.verevcodex.data.preferences.backendTokenPreferenceStore
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
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")

    suspend fun getAccessToken(): String? = dataStore.data.map { it[accessTokenKey] }.first()
    suspend fun getRefreshToken(): String? = dataStore.data.map { it[refreshTokenKey] }.first()

    suspend fun setTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[accessTokenKey] = accessToken
            prefs[refreshTokenKey] = refreshToken
        }
    }

    suspend fun clearTokens() {
        dataStore.edit { prefs ->
            prefs.remove(accessTokenKey)
            prefs.remove(refreshTokenKey)
        }
    }

    suspend fun hasTokens(): Boolean =
        dataStore.data.map { it[accessTokenKey] != null && it[refreshTokenKey] != null }.first()
}
