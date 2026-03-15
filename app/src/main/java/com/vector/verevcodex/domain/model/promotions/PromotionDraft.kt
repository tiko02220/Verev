package com.vector.verevcodex.domain.model.promotions

import com.vector.verevcodex.domain.model.common.CampaignSegment
import java.time.LocalDate

data class PromotionDraft(
    val storeId: String,
    val name: String,
    val description: String,
    val imageUri: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val promotionType: PromotionType,
    val promotionValue: Double,
    val minimumPurchaseAmount: Double,
    val usageLimit: Int,
    val promoCode: String?,
    val visibility: PromotionVisibility,
    val boostLevel: PromotionBoostLevel?,
    val paymentFlowEnabled: Boolean,
    val active: Boolean,
    val targetSegment: CampaignSegment,
    val targetDescription: String,
)
