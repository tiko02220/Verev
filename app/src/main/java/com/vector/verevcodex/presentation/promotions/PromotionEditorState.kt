package com.vector.verevcodex.presentation.promotions

import com.vector.verevcodex.domain.model.common.CampaignSegment
import com.vector.verevcodex.domain.model.promotions.PromotionType

data class PromotionEditorState(
    val promotionId: String? = null,
    val name: String = "",
    val description: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val promotionType: PromotionType = PromotionType.POINTS_MULTIPLIER,
    val promotionValue: String = "",
    val promoCode: String = "",
    val paymentFlowEnabled: Boolean = false,
    val active: Boolean = true,
    val targetSegment: CampaignSegment = CampaignSegment.FREQUENT_VISITORS,
    val targetDescription: String = "",
)
