package com.vector.verevcodex.data.preferences

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

private const val ANONYMOUS_ACCOUNT_NAMESPACE = "anonymous"

private fun scopedPreferenceName(accountId: String?, suffix: String): String =
    "${accountId?.takeIf { it.isNotBlank() } ?: ANONYMOUS_ACCOUNT_NAMESPACE}_$suffix"

private fun scopedStringKey(accountId: String?, suffix: String): Preferences.Key<String> =
    stringPreferencesKey(scopedPreferenceName(accountId, suffix))

private fun scopedBooleanKey(accountId: String?, suffix: String): Preferences.Key<Boolean> =
    booleanPreferencesKey(scopedPreferenceName(accountId, suffix))

object AuthPreferenceKeys {
    val currentAccountId = stringPreferencesKey("current_account_id")
    val currentSession = stringPreferencesKey("current_session_json")
    val pendingSignupSession = stringPreferencesKey("pending_signup_session_json")
    val signupOnboardingPending = booleanPreferencesKey("signup_onboarding_pending")
    val signupOnboardingPendingAccountId = stringPreferencesKey("signup_onboarding_pending_account_id")
    val signupOnboardingCompletedAccountId = stringPreferencesKey("signup_onboarding_completed_account_id")
    val signupOnboardingStage = stringPreferencesKey("signup_onboarding_stage")
    val signupOnboardingStoreId = stringPreferencesKey("signup_onboarding_store_id")
    val signupOnboardingPinSkipped = booleanPreferencesKey("signup_onboarding_pin_skipped")
}

object TokenPreferenceKeys {
    val accessToken = stringPreferencesKey("access_token")
    val refreshToken = stringPreferencesKey("refresh_token")
}

object AccountPreferenceKeys {
    fun quickPin(accountId: String?) = scopedStringKey(accountId, "quick_pin")
    fun biometricEnabled(accountId: String?) = scopedBooleanKey(accountId, "biometric_enabled")
    fun selectedStoreId(accountId: String?) = scopedStringKey(accountId, "selected_store_id")

    fun emailEnabled(accountId: String?) = scopedBooleanKey(accountId, "notify_email_enabled")
    fun pushEnabled(accountId: String?) = scopedBooleanKey(accountId, "notify_push_enabled")
    fun soundEnabled(accountId: String?) = scopedBooleanKey(accountId, "notify_sound_enabled")
    fun transactionEmails(accountId: String?) = scopedBooleanKey(accountId, "notify_email_transactions")
    fun dailyBusinessSummary(accountId: String?) = scopedBooleanKey(accountId, "notify_email_daily_summary")
    fun weeklyBusinessSummary(accountId: String?) = scopedBooleanKey(accountId, "notify_email_weekly_summary")
    fun legacyWeeklyBusinessSummary(accountId: String?) = scopedBooleanKey(accountId, "notify_summary")
    fun marketingEmails(accountId: String?) = scopedBooleanKey(accountId, "notify_email_marketing")
    fun legacyMarketingEmails(accountId: String?) = scopedBooleanKey(accountId, "notify_promotions")
    fun newCustomerPush(accountId: String?) = scopedBooleanKey(accountId, "notify_push_new_customer")
    fun transactionPush(accountId: String?) = scopedBooleanKey(accountId, "notify_push_transactions")
    fun rewardRedeemedPush(accountId: String?) = scopedBooleanKey(accountId, "notify_push_reward_redeemed")
    fun legacyRewardRedeemedPush(accountId: String?) = scopedBooleanKey(accountId, "notify_loyalty")
    fun programUpdatesPush(accountId: String?) = scopedBooleanKey(accountId, "notify_push_program_updates")
    fun staffActivityPush(accountId: String?) = scopedBooleanKey(accountId, "notify_push_staff_activity")
    fun systemAlertsPush(accountId: String?) = scopedBooleanKey(accountId, "notify_push_system_alerts")
    fun legacySystemAlertsPush(accountId: String?) = scopedBooleanKey(accountId, "notify_security")
}

object ScanPreferenceKeys {
    fun preferredScanMethod(accountId: String?) = scopedStringKey(accountId, "preferred_scan_method")
    fun skipMethodSelection(accountId: String?) = scopedBooleanKey(accountId, "skip_scan_method_selection")
}
