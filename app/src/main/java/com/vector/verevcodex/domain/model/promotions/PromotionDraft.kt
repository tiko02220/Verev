package com.vector.verevcodex.domain.model.promotions

import com.vector.verevcodex.domain.model.common.CampaignSegment
import java.time.LocalDate

data class PromotionDraft(
    val storeId: String,
    val name: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val promotionType: PromotionType,
    val promotionValue: Double,
    val promoCode: String?,
    val paymentFlowEnabled: Boolean,
    val active: Boolean,
    val targetSegment: CampaignSegment,
    val targetDescription: String,
)
