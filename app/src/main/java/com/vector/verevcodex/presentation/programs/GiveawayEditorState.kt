package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.CampaignSegment
import com.vector.verevcodex.domain.model.common.CouponBenefitType
import com.vector.verevcodex.domain.model.common.RewardCatalogType
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.GiveawaySendMode
import com.vector.verevcodex.domain.model.promotions.GiveawayType
import com.vector.verevcodex.domain.model.promotions.PromotionBoostLevel
import com.vector.verevcodex.domain.model.promotions.PromotionDraft
import com.vector.verevcodex.domain.model.promotions.PromotionType
import com.vector.verevcodex.domain.model.promotions.PromotionVisibility
import java.time.LocalDate

const val GIVEAWAY_FIELD_NAME = "giveaway_name"
const val GIVEAWAY_FIELD_DESCRIPTION = "giveaway_description"
const val GIVEAWAY_FIELD_BONUS_POINTS = "giveaway_bonus_points"
const val GIVEAWAY_FIELD_DISCOUNT_PERCENT = "giveaway_discount_percent"
const val GIVEAWAY_FIELD_COUPON_CODE = "giveaway_coupon_code"
const val GIVEAWAY_FIELD_COUPON_DISCOUNT_PERCENT = "giveaway_coupon_discount_percent"
const val GIVEAWAY_FIELD_COUPON_BONUS_POINTS = "giveaway_coupon_bonus_points"
const val GIVEAWAY_FIELD_COUPON_QUANTITY = "giveaway_coupon_quantity"
const val GIVEAWAY_FIELD_SCHEDULED_DATE = "giveaway_scheduled_date"
const val GIVEAWAY_FIELD_EXPIRATION_DATE = "giveaway_expiration_date"
const val GIVEAWAY_FIELD_AUDIENCE = "giveaway_audience"
const val GIVEAWAY_FIELD_AGE_RANGE = "giveaway_age_range"

data class GiveawayEditorState(
    val campaignId: String? = null,
    val name: String = "",
    val description: String = "",
    val giveawayType: GiveawayType = GiveawayType.BONUS_POINTS,
    val bonusPointsAmount: String = "",
    val discountPercent: String = "",
    val rewardId: String? = null,
    val rewardName: String = "",
    val rewardCatalogType: RewardCatalogType? = null,
    val couponBenefitType: CouponBenefitType = CouponBenefitType.DISCOUNT_PERCENT,
    val couponCode: String = "",
    val couponDiscountPercent: String = "",
    val couponBonusPoints: String = "",
    val couponAvailableQuantity: String = "0",
    val sendMode: GiveawaySendMode = GiveawaySendMode.IMMEDIATE,
    val scheduledDate: String = LocalDate.now().plusDays(1).toString(),
    val expirationEnabled: Boolean = false,
    val expirationDate: String = LocalDate.now().plusDays(14).toString(),
    val audienceAll: Boolean = true,
    val audienceGender: String = "ALL",
    val audienceAgeMin: String = "",
    val audienceAgeMax: String = "",
    val audienceTierName: String? = null,
    val audienceSegments: Set<CampaignSegment> = emptySet(),
    val active: Boolean = true,
)

fun defaultGiveawayEditorState() = GiveawayEditorState(
    bonusPointsAmount = "100",
    discountPercent = "10",
    couponDiscountPercent = "10",
    couponAvailableQuantity = "100",
)

fun Campaign.toGiveawayEditorState(linkedCoupon: com.vector.verevcodex.domain.model.loyalty.Reward? = null) = GiveawayEditorState(
    campaignId = id,
    name = name,
    description = description,
    giveawayType = giveawayType ?: GiveawayType.BONUS_POINTS,
    bonusPointsAmount = bonusPointsAmount?.toString().orEmpty(),
    discountPercent = discountPercent?.let {
        if (it.rem(1.0) == 0.0) it.toInt().toString() else it.toString()
    }.orEmpty(),
    rewardId = rewardId,
    rewardName = rewardName,
    rewardCatalogType = rewardCatalogType
        ?.uppercase()
        ?.let { kotlin.runCatching { RewardCatalogType.valueOf(it) }.getOrNull() },
    couponBenefitType = linkedCoupon?.couponBenefitType
        ?.takeUnless { it == CouponBenefitType.REWARD }
        ?: CouponBenefitType.DISCOUNT_PERCENT,
    couponCode = promoCode.orEmpty(),
    couponDiscountPercent = linkedCoupon?.couponDiscountPercent?.let {
        if (it.rem(1.0) == 0.0) it.toInt().toString() else it.toString()
    }.orEmpty(),
    couponBonusPoints = linkedCoupon?.couponBonusPoints?.toString().orEmpty(),
    couponAvailableQuantity = linkedCoupon?.availableQuantity?.toString().orEmpty().ifBlank { "0" },
    sendMode = sendMode,
    scheduledDate = scheduledDate?.toString() ?: startDate.toString(),
    expirationEnabled = expirationEnabled,
    expirationDate = expirationDate?.toString().orEmpty(),
    audienceAll = audienceAll,
    audienceGender = audienceGender.ifBlank { "ALL" },
    audienceAgeMin = audienceAgeMin?.toString().orEmpty(),
    audienceAgeMax = audienceAgeMax?.toString().orEmpty(),
    audienceTierName = audienceTierName,
    audienceSegments = emptySet(),
    active = active,
)

fun validateGiveawayEditor(state: GiveawayEditorState): Map<String, Int> {
    val errors = linkedMapOf<String, Int>()
    if (state.name.isBlank()) errors[GIVEAWAY_FIELD_NAME] = R.string.merchant_giveaway_error_name_required
    if (state.description.isBlank()) errors[GIVEAWAY_FIELD_DESCRIPTION] = R.string.merchant_giveaway_error_description_required
    when (state.giveawayType) {
        GiveawayType.BONUS_POINTS -> if ((state.bonusPointsAmount.toIntOrNull() ?: 0) <= 0) {
            errors[GIVEAWAY_FIELD_BONUS_POINTS] = R.string.merchant_giveaway_error_bonus_points_required
        }
        GiveawayType.DISCOUNT_PERCENT -> if ((state.discountPercent.toDoubleOrNull() ?: 0.0) <= 0.0) {
            errors[GIVEAWAY_FIELD_DISCOUNT_PERCENT] = R.string.merchant_giveaway_error_discount_required
        }
        GiveawayType.COUPON -> {
            if (state.couponCode.isBlank()) {
                errors[GIVEAWAY_FIELD_COUPON_CODE] = R.string.merchant_giveaway_error_coupon_code_required
            }
            if ((state.couponAvailableQuantity.toIntOrNull() ?: -1) < 0) {
                errors[GIVEAWAY_FIELD_COUPON_QUANTITY] = R.string.merchant_reward_error_inventory_required
            }
            when (state.couponBenefitType) {
                CouponBenefitType.DISCOUNT_PERCENT -> if ((state.couponDiscountPercent.toDoubleOrNull() ?: 0.0) <= 0.0) {
                    errors[GIVEAWAY_FIELD_COUPON_DISCOUNT_PERCENT] = R.string.merchant_coupon_error_discount_percent_required
                }
                CouponBenefitType.BONUS_POINTS -> if ((state.couponBonusPoints.toIntOrNull() ?: 0) <= 0) {
                    errors[GIVEAWAY_FIELD_COUPON_BONUS_POINTS] = R.string.merchant_coupon_error_bonus_points_required
                }
                CouponBenefitType.REWARD -> {
                    errors[GIVEAWAY_FIELD_AUDIENCE] = R.string.merchant_giveaway_error_save_failed
                }
            }
        }
        GiveawayType.REWARD -> if (state.rewardId.isNullOrBlank()) {
            errors[GIVEAWAY_FIELD_AUDIENCE] = R.string.merchant_giveaway_error_reward_required
        }
    }
    if (state.sendMode == GiveawaySendMode.SCHEDULED && state.scheduledDate.toLocalDateOrNull() == null) {
        errors[GIVEAWAY_FIELD_SCHEDULED_DATE] = R.string.merchant_giveaway_error_scheduled_date_required
    }
    if (state.expirationEnabled && state.expirationDate.toLocalDateOrNull() == null) {
        errors[GIVEAWAY_FIELD_EXPIRATION_DATE] = R.string.merchant_giveaway_error_expiration_date_required
    }
    val ageMin = state.audienceAgeMin.toIntOrNull()
    val ageMax = state.audienceAgeMax.toIntOrNull()
    if (ageMin != null && ageMax != null && ageMax < ageMin) {
        errors[GIVEAWAY_FIELD_AGE_RANGE] = R.string.merchant_program_error_age_range_invalid
    }
    if (!state.audienceAll &&
        state.audienceGender.equals("ALL", ignoreCase = true) &&
        ageMin == null &&
        ageMax == null &&
        state.audienceTierName.isNullOrBlank()
    ) {
        errors[GIVEAWAY_FIELD_AUDIENCE] = R.string.merchant_giveaway_error_audience_required
    }
    return errors
}

fun GiveawayEditorState.toDraft(storeId: String, resolvedRewardId: String? = rewardId): PromotionDraft {
    val scheduled = scheduledDate.toLocalDateOrNull() ?: LocalDate.now().plusDays(1)
    val start = if (sendMode == GiveawaySendMode.SCHEDULED) scheduled else LocalDate.now()
    val resolvedExpiration = expirationDate.toLocalDateOrNull()
    val segments = buildAudienceSegments(this)
    return PromotionDraft(
        storeId = storeId,
        name = name.trim(),
        description = description.trim(),
        imageUri = null,
        startDate = start,
        endDate = if (expirationEnabled) resolvedExpiration ?: start.plusDays(14) else start.plusYears(50),
        promotionType = when (giveawayType) {
            GiveawayType.BONUS_POINTS -> PromotionType.BONUS_POINTS
            GiveawayType.DISCOUNT_PERCENT -> PromotionType.PERCENT_DISCOUNT
            GiveawayType.REWARD,
            GiveawayType.COUPON -> PromotionType.FREE_ITEM
        },
        promotionValue = when (giveawayType) {
            GiveawayType.BONUS_POINTS -> bonusPointsAmount.toDoubleOrNull() ?: 0.0
            GiveawayType.DISCOUNT_PERCENT -> discountPercent.toDoubleOrNull() ?: 0.0
            GiveawayType.REWARD,
            GiveawayType.COUPON -> 1.0
        },
        minimumPurchaseAmount = 0.0,
        usageLimit = 0,
        promoCode = couponCode.trim().takeIf { giveawayType == GiveawayType.COUPON && it.isNotBlank() },
        visibility = PromotionVisibility.BUSINESS_ONLY,
        boostLevel = PromotionBoostLevel.STANDARD,
        paymentFlowEnabled = false,
        active = active,
        targetSegment = segments.firstOrNull() ?: CampaignSegment.ALL_CUSTOMERS,
        segments = segments,
        targetDescription = buildAudienceDescription(this),
        sendMode = sendMode,
        scheduledDate = if (sendMode == GiveawaySendMode.SCHEDULED) scheduled else start,
        expirationEnabled = expirationEnabled,
        expirationDate = resolvedExpiration,
        giveawayType = giveawayType,
        bonusPointsAmount = bonusPointsAmount.toIntOrNull(),
        discountPercent = discountPercent.toDoubleOrNull(),
        rewardId = resolvedRewardId,
        audienceAll = audienceAll,
        audienceGender = audienceGender.ifBlank { "ALL" },
        audienceAgeMin = audienceAgeMin.toIntOrNull(),
        audienceAgeMax = audienceAgeMax.toIntOrNull(),
        audienceTierName = audienceTierName?.takeIf { it.isNotBlank() },
    )
}

private fun buildAudienceDescription(state: GiveawayEditorState): String {
    if (state.audienceAll) return "All customers"
    val parts = mutableListOf<String>()
    when (state.audienceGender.uppercase()) {
        "MALE" -> parts += "Men"
        "FEMALE" -> parts += "Women"
    }
    val minAge = state.audienceAgeMin.toIntOrNull()
    val maxAge = state.audienceAgeMax.toIntOrNull()
    when {
        minAge != null && maxAge != null -> parts += "Ages $minAge-$maxAge"
        minAge != null -> parts += "Age $minAge+"
        maxAge != null -> parts += "Up to $maxAge"
    }
    state.audienceTierName?.takeIf { it.isNotBlank() }?.let { parts += "$it tier" }
    return parts.joinToString(" • ").ifBlank { "Filtered customers" }
}

private fun buildAudienceSegments(state: GiveawayEditorState): List<CampaignSegment> {
    if (state.audienceAll) return listOf(CampaignSegment.ALL_CUSTOMERS)
    val segments = mutableSetOf<CampaignSegment>()
    if (!state.audienceTierName.isNullOrBlank()) {
        segments += CampaignSegment.TIER_MEMBERS
    }
    return segments.toList()
}

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()
