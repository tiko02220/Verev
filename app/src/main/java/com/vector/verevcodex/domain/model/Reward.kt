package com.vector.verevcodex.domain.model

import java.time.LocalDate

data class Reward(
    override val id: String,
    val storeId: String,
    val name: String,
    val description: String,
    val pointsRequired: Int,
    val rewardType: RewardType,
    val expirationDate: LocalDate?,
    val usageLimit: Int,
    val activeStatus: Boolean,
) : Identifiable
