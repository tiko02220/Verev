package com.vector.verevcodex.domain.model.promotions

import com.vector.verevcodex.domain.model.common.CampaignSegment
import com.vector.verevcodex.domain.model.common.Identifiable

data class CampaignTarget(
    override val id: String,
    val campaignId: String,
    val segment: CampaignSegment,
    val description: String,
) : Identifiable
