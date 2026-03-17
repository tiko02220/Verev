package com.vector.verevcodex.data.repository.loyalty

import com.vector.verevcodex.data.remote.loyalty.LoyaltyRemoteDataSource
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.PromotionDraft
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardDraft
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramDraft
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.repository.loyalty.LoyaltyRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@Singleton
class LoyaltyRepositoryImpl @Inject constructor(
    private val loyaltyRemote: LoyaltyRemoteDataSource,
) : LoyaltyRepository {
    private val refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun observePrograms(storeId: String?): Flow<List<RewardProgram>> {
        return refreshRequests
            .onStart { emit(Unit) }
            .map { loyaltyRemote.listPrograms(storeId).getOrElse { emptyList() } }
    }

    override fun observeRewards(storeId: String?): Flow<List<Reward>> {
        return refreshRequests
            .onStart { emit(Unit) }
            .map { loyaltyRemote.listRewards(storeId).getOrElse { emptyList() } }
    }

    override fun observeCampaigns(storeId: String?): Flow<List<Campaign>> {
        return refreshRequests
            .onStart { emit(Unit) }
            .map { loyaltyRemote.listCampaigns(storeId).getOrElse { emptyList() } }
    }

    override fun observeActiveScanActions(storeId: String?): Flow<List<RewardProgramScanAction>> {
        return refreshRequests
            .onStart { emit(Unit) }
            .map { loyaltyRemote.activeScanActions(storeId).getOrElse { emptyList() } }
    }

    override suspend fun createReward(draft: RewardDraft): Reward =
        loyaltyRemote.createReward(draft).getOrThrow().also {
            refreshRequests.tryEmit(Unit)
        }

    override suspend fun updateReward(rewardId: String, draft: RewardDraft): Reward =
        loyaltyRemote.updateReward(rewardId, draft).getOrThrow().also {
            refreshRequests.tryEmit(Unit)
        }

    override suspend fun setRewardEnabled(rewardId: String, enabled: Boolean) {
        loyaltyRemote.setRewardEnabled(rewardId, enabled).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }

    override suspend fun deleteReward(rewardId: String) {
        loyaltyRemote.deleteReward(rewardId).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }

    override suspend fun createProgram(draft: RewardProgramDraft): RewardProgram =
        loyaltyRemote.createProgram(draft).getOrThrow().also {
            refreshRequests.tryEmit(Unit)
        }

    override suspend fun updateProgram(programId: String, draft: RewardProgramDraft): RewardProgram =
        loyaltyRemote.updateProgram(programId, draft).getOrThrow().also {
            refreshRequests.tryEmit(Unit)
        }

    override suspend fun setProgramEnabled(programId: String, enabled: Boolean) {
        loyaltyRemote.setProgramEnabled(programId, enabled).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }

    override suspend fun deleteProgram(programId: String) {
        loyaltyRemote.deleteProgram(programId).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }

    override suspend fun createCampaign(draft: PromotionDraft): Campaign =
        loyaltyRemote.createCampaign(draft).getOrThrow().also {
            refreshRequests.tryEmit(Unit)
        }

    override suspend fun updateCampaign(campaignId: String, draft: PromotionDraft): Campaign =
        loyaltyRemote.updateCampaign(campaignId, draft).getOrThrow().also {
            refreshRequests.tryEmit(Unit)
        }

    override suspend fun setCampaignEnabled(campaignId: String, enabled: Boolean) {
        loyaltyRemote.setCampaignEnabled(campaignId, enabled).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }

    override suspend fun deleteCampaign(campaignId: String) {
        loyaltyRemote.deleteCampaign(campaignId).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }
}
