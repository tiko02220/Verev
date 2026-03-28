package com.vector.verevcodex.domain.model.loyalty

import com.vector.verevcodex.domain.model.common.RewardType
import java.time.LocalDate

data class RewardDraft(
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
)
