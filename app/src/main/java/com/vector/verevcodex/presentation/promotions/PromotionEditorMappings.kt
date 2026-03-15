package com.vector.verevcodex.presentation.promotions

import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.CampaignSegment
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.PromotionDraft
import com.vector.verevcodex.domain.model.promotions.PromotionType
import java.time.LocalDate

const val PROMOTION_FIELD_NAME = "promotion_name"
const val PROMOTION_FIELD_DESCRIPTION = "promotion_description"
const val PROMOTION_FIELD_START = "promotion_start"
const val PROMOTION_FIELD_END = "promotion_end"
const val PROMOTION_FIELD_VALUE = "promotion_value"
const val PROMOTION_FIELD_TARGET = "promotion_target"

fun defaultPromotionEditorState() = PromotionEditorState(
    startDate = LocalDate.now().toString(),
    endDate = LocalDate.now().plusDays(7).toString(),
    promotionType = PromotionType.PERCENT_DISCOUNT,
    promotionValue = "10",
    targetDescription = CampaignSegment.ALL_CUSTOMERS.name,
)

fun Campaign.toPromotionEditorState() = PromotionEditorState(
    promotionId = id,
    name = name,
    description = description,
    imageUri = imageUri.orEmpty(),
    startDate = startDate.toString(),
    endDate = endDate.toString(),
    promotionType = promotionType,
    promotionValue = if (promotionValue.rem(1.0) == 0.0) promotionValue.toInt().toString() else promotionValue.toString(),
    minimumPurchaseAmount = if (minimumPurchaseAmount == 0.0) "" else minimumPurchaseAmount.toInt().toString(),
    usageLimit = if (usageLimit == 0) "" else usageLimit.toString(),
    promoCode = promoCode.orEmpty(),
    visibility = visibility,
    boostLevel = boostLevel ?: com.vector.verevcodex.domain.model.promotions.PromotionBoostLevel.STANDARD,
    paymentFlowEnabled = paymentFlowEnabled,
    active = active,
    targetSegment = target.segment,
    targetDescription = target.description,
)

fun validatePromotionEditor(state: PromotionEditorState): Map<String, Int> {
    val errors = linkedMapOf<String, Int>()
    if (state.name.isBlank()) {
        errors[PROMOTION_FIELD_NAME] = R.string.merchant_promotion_error_name_required
    }
    if (state.description.isBlank()) {
        errors[PROMOTION_FIELD_DESCRIPTION] = R.string.merchant_promotion_error_description_required
    }
    val start = state.startDate.toLocalDateOrNull()
    val end = state.endDate.toLocalDateOrNull()
    if (start == null) {
        errors[PROMOTION_FIELD_START] = R.string.merchant_promotion_error_start_required
    }
    if (end == null) {
        errors[PROMOTION_FIELD_END] = R.string.merchant_promotion_error_end_required
    }
    if (start != null && end != null && end.isBefore(start)) {
        errors[PROMOTION_FIELD_END] = R.string.merchant_promotion_error_end_before_start
    }
    val value = state.promotionValue.toDoubleOrNull()
    if (state.promotionType.requiresNumericValue() && (value == null || value <= 0.0)) {
        errors[PROMOTION_FIELD_VALUE] = R.string.merchant_promotion_error_value_required
    }
    return errors
}

fun PromotionEditorState.toDraft(storeId: String): PromotionDraft = PromotionDraft(
    storeId = storeId,
    name = name.trim(),
    description = description.trim(),
    imageUri = imageUri.trim().ifBlank { null },
    startDate = startDate.toLocalDateOrNull() ?: LocalDate.now(),
    endDate = endDate.toLocalDateOrNull() ?: LocalDate.now().plusDays(7),
    promotionType = promotionType,
    promotionValue = promotionType.defaultValueForInput(promotionValue),
    minimumPurchaseAmount = minimumPurchaseAmount.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0,
    usageLimit = usageLimit.toIntOrNull()?.coerceAtLeast(0) ?: 0,
    promoCode = promoCode.trim().ifBlank { null },
    visibility = visibility,
    boostLevel = if (visibility == com.vector.verevcodex.domain.model.promotions.PromotionVisibility.NETWORK_WIDE) boostLevel else null,
    paymentFlowEnabled = visibility == com.vector.verevcodex.domain.model.promotions.PromotionVisibility.NETWORK_WIDE,
    active = active,
    targetSegment = targetSegment,
    targetDescription = targetDescription.trim().ifBlank { targetSegment.name },
)

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

internal fun PromotionType.requiresNumericValue(): Boolean = when (this) {
    PromotionType.BUY_ONE_GET_ONE,
    PromotionType.FREE_ITEM -> false
    else -> true
}

internal fun PromotionType.defaultValueForInput(rawValue: String): Double = when (this) {
    PromotionType.BUY_ONE_GET_ONE,
    PromotionType.FREE_ITEM -> 1.0
    else -> rawValue.toDoubleOrNull() ?: 0.0
}
