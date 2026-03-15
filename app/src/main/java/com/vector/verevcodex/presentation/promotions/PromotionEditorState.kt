package com.vector.verevcodex.presentation.promotions

import com.vector.verevcodex.domain.model.common.CampaignSegment
import com.vector.verevcodex.domain.model.promotions.PromotionBoostLevel
import com.vector.verevcodex.domain.model.promotions.PromotionType
import com.vector.verevcodex.domain.model.promotions.PromotionVisibility

data class PromotionEditorState(
    val promotionId: String? = null,
    val name: String = "",
    val description: String = "",
    val imageUri: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val promotionType: PromotionType = PromotionType.PERCENT_DISCOUNT,
    val promotionValue: String = "",
    val minimumPurchaseAmount: String = "",
    val usageLimit: String = "",
    val promoCode: String = "",
    val visibility: PromotionVisibility = PromotionVisibility.BUSINESS_ONLY,
    val boostLevel: PromotionBoostLevel = PromotionBoostLevel.STANDARD,
    val paymentFlowEnabled: Boolean = false,
    val active: Boolean = true,
    val targetSegment: CampaignSegment = CampaignSegment.ALL_CUSTOMERS,
    val targetDescription: String = CampaignSegment.ALL_CUSTOMERS.name,
)
