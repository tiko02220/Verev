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
    val segments: List<CampaignSegment> = emptyList(),
    val targetDescription: String,
    val sendMode: GiveawaySendMode = GiveawaySendMode.IMMEDIATE,
    val scheduledDate: LocalDate? = null,
    val expirationEnabled: Boolean = false,
    val expirationDate: LocalDate? = null,
    val giveawayType: GiveawayType? = null,
    val bonusPointsAmount: Int? = null,
    val discountPercent: Double? = null,
    val rewardId: String? = null,
    val audienceAll: Boolean = true,
    val audienceGender: String = "ALL",
    val audienceAgeMin: Int? = null,
    val audienceAgeMax: Int? = null,
    val audienceTierName: String? = null,
)
