package com.vector.verevcodex.domain.model.promotions

import com.vector.verevcodex.domain.model.common.Identifiable
import java.time.LocalDate

data class Campaign(
    override val id: String,
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
    val target: CampaignTarget,
) : Identifiable
