package com.vector.verevcodex.presentation.promotions

import androidx.annotation.StringRes
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.PromotionType
import java.time.LocalDate

enum class PromotionFilter(@StringRes val labelRes: Int) {
    ALL(R.string.merchant_filter_all),
    ACTIVE(R.string.merchant_campaign_filter_active),
    SCHEDULED(R.string.merchant_campaign_filter_scheduled),
    EXPIRED(R.string.merchant_campaign_filter_expired),
    PAYMENT(R.string.merchant_promotions_filter_payment),
}

enum class PromotionStatus {
    ACTIVE,
    SCHEDULED,
    EXPIRED,
}

fun Campaign.promotionStatus(today: LocalDate = LocalDate.now()): PromotionStatus = when {
    !active || endDate.isBefore(today) -> PromotionStatus.EXPIRED
    startDate.isAfter(today) -> PromotionStatus.SCHEDULED
    else -> PromotionStatus.ACTIVE
}

fun Campaign.matchesPromotionFilter(filter: PromotionFilter, today: LocalDate = LocalDate.now()): Boolean = when (filter) {
    PromotionFilter.ALL -> true
    PromotionFilter.ACTIVE -> promotionStatus(today) == PromotionStatus.ACTIVE
    PromotionFilter.SCHEDULED -> promotionStatus(today) == PromotionStatus.SCHEDULED
    PromotionFilter.EXPIRED -> promotionStatus(today) == PromotionStatus.EXPIRED
    PromotionFilter.PAYMENT -> paymentFlowEnabled
}

fun Campaign.isPaymentPromotion(today: LocalDate = LocalDate.now()): Boolean =
    paymentFlowEnabled && active && !startDate.isAfter(today) && !endDate.isBefore(today)

fun PromotionType.displayLabelRes(): Int = when (this) {
    PromotionType.POINTS_MULTIPLIER -> R.string.merchant_promotion_type_points_multiplier
    PromotionType.PERCENT_DISCOUNT -> R.string.merchant_promotion_type_percent_discount
    PromotionType.FIXED_DISCOUNT -> R.string.merchant_promotion_type_fixed_discount
    PromotionType.BONUS_POINTS -> R.string.merchant_promotion_type_bonus_points
}
