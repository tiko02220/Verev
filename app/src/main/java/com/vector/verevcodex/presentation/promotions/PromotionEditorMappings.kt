package com.vector.verevcodex.presentation.promotions

import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.PromotionDraft
import java.time.LocalDate

const val PROMOTION_FIELD_NAME = "promotion_name"
const val PROMOTION_FIELD_START = "promotion_start"
const val PROMOTION_FIELD_END = "promotion_end"
const val PROMOTION_FIELD_VALUE = "promotion_value"
const val PROMOTION_FIELD_TARGET = "promotion_target"

fun defaultPromotionEditorState() = PromotionEditorState(
    startDate = LocalDate.now().toString(),
    endDate = LocalDate.now().plusDays(7).toString(),
    promotionValue = "2",
)

fun Campaign.toPromotionEditorState() = PromotionEditorState(
    promotionId = id,
    name = name,
    description = description,
    startDate = startDate.toString(),
    endDate = endDate.toString(),
    promotionType = promotionType,
    promotionValue = if (promotionValue.rem(1.0) == 0.0) promotionValue.toInt().toString() else promotionValue.toString(),
    promoCode = promoCode.orEmpty(),
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
    if (value == null || value <= 0.0) {
        errors[PROMOTION_FIELD_VALUE] = R.string.merchant_promotion_error_value_required
    }
    if (state.targetDescription.isBlank()) {
        errors[PROMOTION_FIELD_TARGET] = R.string.merchant_promotion_error_target_required
    }
    return errors
}

fun PromotionEditorState.toDraft(storeId: String): PromotionDraft = PromotionDraft(
    storeId = storeId,
    name = name.trim(),
    description = description.trim(),
    startDate = startDate.toLocalDateOrNull() ?: LocalDate.now(),
    endDate = endDate.toLocalDateOrNull() ?: LocalDate.now().plusDays(7),
    promotionType = promotionType,
    promotionValue = promotionValue.toDoubleOrNull() ?: 0.0,
    promoCode = promoCode.trim().ifBlank { null },
    paymentFlowEnabled = paymentFlowEnabled,
    active = active,
    targetSegment = targetSegment,
    targetDescription = targetDescription.trim(),
)

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()
