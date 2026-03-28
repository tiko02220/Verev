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
import com.vector.verevcodex.domain.repository.realtime.RealtimeRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@Singleton
class LoyaltyRepositoryImpl @Inject constructor(
    private val loyaltyRemote: LoyaltyRemoteDataSource,
    private val realtimeRepository: RealtimeRepository,
) : LoyaltyRepository {
    private val refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val refreshSignals = merge(
        refreshRequests.onStart { emit(Unit) },
        realtimeRepository.observeRefreshSignals(),
    )

    override fun observePrograms(storeId: String?): Flow<List<RewardProgram>> = observeRemote { loyaltyRemote.listPrograms(storeId) }

    override fun observeRewards(storeId: String?): Flow<List<Reward>> = observeRemote { loyaltyRemote.listRewards(storeId) }

    override fun observeCampaigns(storeId: String?): Flow<List<Campaign>> = observeRemote { loyaltyRemote.listCampaigns(storeId) }

    override fun observeActiveScanActions(storeId: String?): Flow<List<RewardProgramScanAction>> =
        observeRemote { loyaltyRemote.activeScanActions(storeId) }

    override suspend fun createReward(draft: RewardDraft): Reward =
        refreshAfterResult { loyaltyRemote.createReward(draft).getOrThrow() }

    override suspend fun updateReward(rewardId: String, draft: RewardDraft): Reward =
        refreshAfterResult { loyaltyRemote.updateReward(rewardId, draft).getOrThrow() }

    override suspend fun setRewardEnabled(rewardId: String, enabled: Boolean) =
        refreshAfter { loyaltyRemote.setRewardEnabled(rewardId, enabled).getOrThrow() }

    override suspend fun adjustRewardInventory(rewardId: String, delta: Int): Reward =
        refreshAfterResult { loyaltyRemote.adjustRewardInventory(rewardId, delta).getOrThrow() }

    override suspend fun deleteReward(rewardId: String) =
        refreshAfter { loyaltyRemote.deleteReward(rewardId).getOrThrow() }

    override suspend fun createProgram(draft: RewardProgramDraft): RewardProgram =
        refreshAfterResult { loyaltyRemote.createProgram(draft).getOrThrow() }

    override suspend fun updateProgram(programId: String, draft: RewardProgramDraft): RewardProgram =
        refreshAfterResult { loyaltyRemote.updateProgram(programId, draft).getOrThrow() }

    override suspend fun setProgramEnabled(programId: String, enabled: Boolean) =
        refreshAfter { loyaltyRemote.setProgramEnabled(programId, enabled).getOrThrow() }

    override suspend fun deleteProgram(programId: String) =
        refreshAfter { loyaltyRemote.deleteProgram(programId).getOrThrow() }

    override suspend fun createCampaign(draft: PromotionDraft): Campaign =
        refreshAfterResult { loyaltyRemote.createCampaign(draft).getOrThrow() }

    override suspend fun updateCampaign(campaignId: String, draft: PromotionDraft): Campaign =
        refreshAfterResult { loyaltyRemote.updateCampaign(campaignId, draft).getOrThrow() }

    override suspend fun setCampaignEnabled(campaignId: String, enabled: Boolean) =
        refreshAfter { loyaltyRemote.setCampaignEnabled(campaignId, enabled).getOrThrow() }

    override suspend fun deleteCampaign(campaignId: String) =
        refreshAfter { loyaltyRemote.deleteCampaign(campaignId).getOrThrow() }

    private fun <T> observeRemote(fetch: suspend () -> Result<List<T>>): Flow<List<T>> =
        refreshSignals.map { fetch().getOrElse { emptyList() } }

    private suspend fun refreshAfter(action: suspend () -> Unit) {
        action()
        triggerRefresh()
    }

    private suspend fun <T> refreshAfterResult(action: suspend () -> T): T =
        action().also { triggerRefresh() }

    private fun triggerRefresh() {
        refreshRequests.tryEmit(Unit)
    }
}
