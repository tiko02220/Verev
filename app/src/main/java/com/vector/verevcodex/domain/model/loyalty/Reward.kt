package com.vector.verevcodex.domain.model.loyalty

import com.vector.verevcodex.domain.model.common.CouponBenefitType
import com.vector.verevcodex.domain.model.common.Identifiable
import com.vector.verevcodex.domain.model.common.RewardCatalogType
import com.vector.verevcodex.domain.model.common.RewardType
import java.time.LocalDate

data class Reward(
    override val id: String,
    val storeId: String,
    val name: String,
    val description: String,
    val pointsRequired: Int,
    val rewardType: RewardType,
    val imageUri: String?,
    val expirationDate: LocalDate?,
    val usageLimit: Int,
    val inventoryTracked: Boolean,
    val availableQuantity: Int?,
    val activeStatus: Boolean,
    val catalogType: RewardCatalogType = RewardCatalogType.REWARD,
    val couponCode: String? = null,
    val couponBenefitType: CouponBenefitType? = null,
    val couponDiscountPercent: Double? = null,
    val couponBonusPoints: Int? = null,
    val couponRewardId: String? = null,
    val couponRewardName: String = "",
) : Identifiable
