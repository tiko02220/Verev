package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.common.CouponBenefitType
import com.vector.verevcodex.domain.model.common.RewardCatalogType
import com.vector.verevcodex.domain.model.common.RewardType
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardDraft
import java.math.BigDecimal
import java.time.LocalDate

internal const val REWARD_FIELD_NAME = "name"
internal const val REWARD_FIELD_EXPIRY = "expiry"
internal const val REWARD_FIELD_AVAILABLE_QUANTITY = "availableQuantity"
internal const val REWARD_FIELD_COUPON_CODE = "couponCode"
internal const val REWARD_FIELD_COUPON_DISCOUNT_PERCENT = "couponDiscountPercent"
internal const val REWARD_FIELD_COUPON_BONUS_POINTS = "couponBonusPoints"
internal const val REWARD_FIELD_COUPON_REWARD = "couponReward"

data class RewardEditorState(
    val rewardId: String? = null,
    val name: String = "",
    val description: String = "",
    val catalogType: RewardCatalogType = RewardCatalogType.REWARD,
    val rewardType: RewardType = RewardType.FREE_PRODUCT,
    val imageUri: String = "",
    val expirationEnabled: Boolean = false,
    val expirationDate: String = "",
    val availableQuantity: String = "0",
    val activeStatus: Boolean = true,
    val usageLimit: Int = 1,
    val legacyPointsRequired: Int = 1,
    val couponCode: String = "",
    val couponBenefitType: CouponBenefitType = CouponBenefitType.DISCOUNT_PERCENT,
    val couponDiscountPercent: String = "",
    val couponBonusPoints: String = "",
    val couponRewardId: String? = null,
    val couponRewardName: String = "",
)

internal fun Reward.toEditorState() = RewardEditorState(
    rewardId = id,
    name = name,
    description = description,
    catalogType = catalogType,
    rewardType = rewardType,
    imageUri = imageUri.orEmpty(),
    expirationEnabled = expirationDate != null,
    expirationDate = expirationDate?.toString().orEmpty(),
    availableQuantity = availableQuantity?.toString().orEmpty().ifBlank { "0" },
    activeStatus = activeStatus,
    usageLimit = usageLimit.coerceAtLeast(1),
    legacyPointsRequired = pointsRequired,
    couponCode = couponCode.orEmpty(),
    couponBenefitType = couponBenefitType ?: CouponBenefitType.DISCOUNT_PERCENT,
    couponDiscountPercent = couponDiscountPercent?.let { BigDecimal.valueOf(it).stripTrailingZeros().toPlainString() }.orEmpty(),
    couponBonusPoints = couponBonusPoints?.toString().orEmpty(),
    couponRewardId = couponRewardId,
    couponRewardName = couponRewardName,
)

internal fun RewardEditorState.toDraft(storeId: String): RewardDraft = RewardDraft(
    storeId = storeId,
    name = name.trim(),
    description = description.trim(),
    pointsRequired = legacyPointsRequired.coerceAtLeast(1),
    rewardType = if (catalogType == RewardCatalogType.COUPON) RewardType.DISCOUNT_COUPON else rewardType,
    imageUri = imageUri.trim().ifBlank { null }.takeIf { catalogType == RewardCatalogType.REWARD },
    expirationDate = expirationDate.trim().takeIf { expirationEnabled && it.isNotEmpty() }?.let(LocalDate::parse),
    usageLimit = usageLimit.coerceAtLeast(1),
    inventoryTracked = true,
    availableQuantity = availableQuantity.trim().ifBlank { "0" }.toInt(),
    activeStatus = activeStatus,
    catalogType = catalogType,
    couponCode = couponCode.trim().takeIf { catalogType == RewardCatalogType.COUPON && it.isNotBlank() },
    couponBenefitType = couponBenefitType.takeIf { catalogType == RewardCatalogType.COUPON },
    couponDiscountPercent = couponDiscountPercent.trim().toDoubleOrNull()?.takeIf { catalogType == RewardCatalogType.COUPON },
    couponBonusPoints = couponBonusPoints.trim().toIntOrNull()?.takeIf { catalogType == RewardCatalogType.COUPON },
    couponRewardId = couponRewardId.takeIf { catalogType == RewardCatalogType.COUPON && couponBenefitType == CouponBenefitType.REWARD },
)

internal fun RewardEditorState.validate(): Map<String, Int> {
    val errors = linkedMapOf<String, Int>()
    if (name.isBlank()) errors[REWARD_FIELD_NAME] = com.vector.verevcodex.R.string.merchant_reward_error_name_required
    if (availableQuantity.trim().toIntOrNull()?.takeIf { it >= 0 } == null) {
        errors[REWARD_FIELD_AVAILABLE_QUANTITY] = com.vector.verevcodex.R.string.merchant_reward_error_inventory_required
    }
    val expiry = expirationDate.trim()
    if (expirationEnabled && expiry.isNotEmpty()) {
        runCatching { LocalDate.parse(expiry) }.onFailure {
            errors[REWARD_FIELD_EXPIRY] = com.vector.verevcodex.R.string.merchant_reward_error_expiration_invalid
        }
    } else if (expirationEnabled && expiry.isEmpty()) {
        errors[REWARD_FIELD_EXPIRY] = com.vector.verevcodex.R.string.merchant_reward_error_expiration_invalid
    }
    if (catalogType == RewardCatalogType.COUPON) {
        if (couponCode.isBlank()) {
            errors[REWARD_FIELD_COUPON_CODE] = com.vector.verevcodex.R.string.merchant_coupon_error_code_required
        }
        when (couponBenefitType) {
            CouponBenefitType.DISCOUNT_PERCENT -> {
                if (couponDiscountPercent.trim().toDoubleOrNull()?.takeIf { it > 0 } == null) {
                    errors[REWARD_FIELD_COUPON_DISCOUNT_PERCENT] = com.vector.verevcodex.R.string.merchant_coupon_error_discount_percent_required
                }
            }
            CouponBenefitType.BONUS_POINTS -> {
                if (couponBonusPoints.trim().toIntOrNull()?.takeIf { it > 0 } == null) {
                    errors[REWARD_FIELD_COUPON_BONUS_POINTS] = com.vector.verevcodex.R.string.merchant_coupon_error_bonus_points_required
                }
            }
            CouponBenefitType.REWARD -> {
                if (couponRewardId.isNullOrBlank()) {
                    errors[REWARD_FIELD_COUPON_REWARD] = com.vector.verevcodex.R.string.merchant_coupon_error_reward_required
                }
            }
        }
    }
    return errors
}
