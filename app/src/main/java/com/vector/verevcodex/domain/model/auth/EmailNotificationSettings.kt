package com.vector.verevcodex.domain.model.auth

data class EmailNotificationSettings(
    val emailEnabled: Boolean = true,
    val pushEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val transactionEmails: Boolean = true,
    val dailyBusinessSummary: Boolean = true,
    val weeklyBusinessSummary: Boolean = true,
    val marketingEmails: Boolean = false,
    val newCustomerPush: Boolean = true,
    val transactionPush: Boolean = true,
    val rewardRedeemedPush: Boolean = true,
    val programUpdatesPush: Boolean = true,
    val staffActivityPush: Boolean = false,
    val systemAlertsPush: Boolean = true,
)
