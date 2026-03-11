package com.vector.verevcodex.domain.model

data class CampaignTarget(
    override val id: String,
    val campaignId: String,
    val segment: CampaignSegment,
    val description: String,
) : Identifiable
