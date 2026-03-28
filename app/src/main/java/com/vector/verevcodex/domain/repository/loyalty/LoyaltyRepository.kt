package com.vector.verevcodex.domain.repository.loyalty

import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.PromotionDraft
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardDraft
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramDraft
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import kotlinx.coroutines.flow.Flow

interface LoyaltyRepository {
    fun observePrograms(storeId: String? = null): Flow<List<RewardProgram>>
    fun observeRewards(storeId: String? = null): Flow<List<Reward>>
    fun observeCampaigns(storeId: String? = null): Flow<List<Campaign>>
    fun observeActiveScanActions(storeId: String? = null): Flow<List<RewardProgramScanAction>>
    suspend fun createReward(draft: RewardDraft): Reward
    suspend fun updateReward(rewardId: String, draft: RewardDraft): Reward
    suspend fun setRewardEnabled(rewardId: String, enabled: Boolean)
    suspend fun adjustRewardInventory(rewardId: String, delta: Int): Reward
    suspend fun deleteReward(rewardId: String)
    suspend fun createProgram(draft: RewardProgramDraft): RewardProgram
    suspend fun updateProgram(programId: String, draft: RewardProgramDraft): RewardProgram
    suspend fun setProgramEnabled(programId: String, enabled: Boolean)
    suspend fun deleteProgram(programId: String)
    suspend fun createCampaign(draft: PromotionDraft): Campaign
    suspend fun updateCampaign(campaignId: String, draft: PromotionDraft): Campaign
    suspend fun setCampaignEnabled(campaignId: String, enabled: Boolean)
    suspend fun deleteCampaign(campaignId: String)
}
