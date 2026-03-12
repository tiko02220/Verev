package com.vector.verevcodex.domain.usecase.promotions

import com.vector.verevcodex.domain.model.promotions.PromotionDraft
import com.vector.verevcodex.domain.repository.loyalty.LoyaltyRepository

class CreatePromotionUseCase(private val repository: LoyaltyRepository) {
    suspend operator fun invoke(draft: PromotionDraft) = repository.createCampaign(draft)
}

class UpdatePromotionUseCase(private val repository: LoyaltyRepository) {
    suspend operator fun invoke(campaignId: String, draft: PromotionDraft) = repository.updateCampaign(campaignId, draft)
}

class SetPromotionEnabledUseCase(private val repository: LoyaltyRepository) {
    suspend operator fun invoke(campaignId: String, enabled: Boolean) = repository.setCampaignEnabled(campaignId, enabled)
}

class DeletePromotionUseCase(private val repository: LoyaltyRepository) {
    suspend operator fun invoke(campaignId: String) = repository.deleteCampaign(campaignId)
}
