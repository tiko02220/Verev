package com.vector.verevcodex.presentation.merchant.common

import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.domain.model.common.RewardType
import com.vector.verevcodex.domain.model.common.StaffRole
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

private val wholeCurrencyFormat = DecimalFormat("#,##0", DecimalFormatSymbols(java.util.Locale.US))
private val compactCurrencyFormat = DecimalFormat("0.#", DecimalFormatSymbols(java.util.Locale.US))

fun formatCompactCount(value: Int): String {
    val absValue = abs(value)
    return when {
        absValue >= 1_000_000 -> "${compactCurrencyFormat.format(value / 1_000_000.0)}M"
        absValue >= 1_000 -> "${compactCurrencyFormat.format(value / 1_000.0)}K"
        else -> wholeCurrencyFormat.format(value)
    }
}

fun formatWholeCurrency(value: Double, suffix: String = "AMD"): String = "${wholeCurrencyFormat.format(value)} $suffix"

fun formatCompactCurrency(value: Double, currencySymbol: String = "$", suffix: String = ""): String {
    val absValue = abs(value)
    return when {
        absValue >= 1_000_000 -> "$currencySymbol${compactCurrencyFormat.format(value / 1_000_000)}M$suffix"
        absValue >= 1_000 -> "$currencySymbol${compactCurrencyFormat.format(value / 1_000)}k$suffix"
        else -> "$currencySymbol${wholeCurrencyFormat.format(value)}$suffix"
    }
}

fun formatPercent(value: Double): String = "${(value * 100).toInt()}%"

fun formatSignedPercent(percentValue: Double): String {
    val normalized = if (percentValue <= 1.0) percentValue * 100 else percentValue
    val rounded = normalized.toInt()
    return if (rounded > 0) "+$rounded%" else "$rounded%"
}

fun formatRelativeDateTime(value: LocalDateTime?): String {
    value ?: return "-"
    val now = LocalDateTime.now()
    val minutes = ChronoUnit.MINUTES.between(value, now)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 24 * 60 -> "${minutes / 60}h ago"
        minutes < 48 * 60 -> "Yesterday"
        minutes < 7 * 24 * 60 -> "${minutes / (24 * 60)}d ago"
        else -> value.toLocalDate().toString()
    }
}

fun StaffRole.displayName(): String = when (this) {
    StaffRole.OWNER -> "Owner"
    StaffRole.STORE_MANAGER -> "Manager"
    StaffRole.CASHIER -> "Cashier"
    StaffRole.STAFF -> "Staff"
}

fun LoyaltyTier.displayName(): String = when (this) {
    LoyaltyTier.BRONZE -> "Bronze"
    LoyaltyTier.SILVER -> "Silver"
    LoyaltyTier.GOLD -> "Gold"
    LoyaltyTier.VIP -> "VIP"
}

fun RewardType.displayName(): String = when (this) {
    RewardType.FREE_PRODUCT -> "Free Product"
    RewardType.DISCOUNT_COUPON -> "Discount Coupon"
    RewardType.GIFT_ITEM -> "Gift Item"
    RewardType.SPECIAL_PROMOTION -> "Promotion"
}
