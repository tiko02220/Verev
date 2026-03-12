package com.vector.verevcodex.data.repository.reports

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

internal object ReportPreferenceKeys {
    val enabled = booleanPreferencesKey("reports_enabled")
    val frequency = stringPreferencesKey("reports_frequency")
    val format = stringPreferencesKey("reports_format")
    val includeAllStores = booleanPreferencesKey("reports_all_stores")
}
