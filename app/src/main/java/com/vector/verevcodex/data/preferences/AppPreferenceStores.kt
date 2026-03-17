package com.vector.verevcodex.data.preferences

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

internal val Context.authPreferenceStore by preferencesDataStore(name = "auth_prefs")
internal val Context.backendTokenPreferenceStore by preferencesDataStore(name = "backend_tokens")
internal val Context.scanPreferenceStore by preferencesDataStore(name = "scan_prefs")
internal val Context.merchantPreferenceStore by preferencesDataStore(name = "merchant_prefs")
