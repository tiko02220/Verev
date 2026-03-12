package com.vector.verevcodex.data.repository.reports

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

internal object ReportPreferenceKeys {
    val enabled = booleanPreferencesKey("reports_enabled")
    val frequency = stringPreferencesKey("reports_frequency")
    val format = stringPreferencesKey("reports_format")
    val includeAllStores = booleanPreferencesKey("reports_all_stores")
    val scheduledTime = stringPreferencesKey("reports_scheduled_time")
    val scheduledWeekday = stringPreferencesKey("reports_scheduled_weekday")
    val scheduledMonthDay = intPreferencesKey("reports_scheduled_month_day")
    val recipientEmails = stringSetPreferencesKey("reports_recipient_emails")
    val includedSections = stringSetPreferencesKey("reports_included_sections")
}
