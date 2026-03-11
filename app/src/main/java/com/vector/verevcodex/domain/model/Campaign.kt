package com.vector.verevcodex.domain.model

import java.time.LocalDate

data class Campaign(
    override val id: String,
    val storeId: String,
    val name: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val rewardMultiplier: Double,
    val active: Boolean,
    val target: CampaignTarget,
) : Identifiable
