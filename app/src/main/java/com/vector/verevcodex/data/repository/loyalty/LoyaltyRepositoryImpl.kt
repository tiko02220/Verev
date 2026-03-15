package com.vector.verevcodex.data.repository.loyalty

import com.vector.verevcodex.data.db.AppDatabase
import com.vector.verevcodex.data.db.entity.loyalty.CampaignTargetEntity
import com.vector.verevcodex.data.mapper.toDomain
import com.vector.verevcodex.data.mapper.toEntity
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.CampaignTarget
import com.vector.verevcodex.domain.model.promotions.PromotionDraft
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardDraft
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramDraft
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.repository.loyalty.LoyaltyRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

@Singleton
class LoyaltyRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
) : LoyaltyRepository {
    override fun observePrograms(storeId: String?): Flow<List<RewardProgram>> =
        database.loyaltyDao().observePrograms(storeId).map { list -> list.map { it.toDomain() } }

    override fun observeRewards(storeId: String?) =
        database.loyaltyDao().observeRewards(storeId).map { list -> list.map { it.toDomain() } }

    override fun observeCampaigns(storeId: String?): Flow<List<Campaign>> = combine(
        database.loyaltyDao().observeCampaigns(storeId),
        database.loyaltyDao().observeCampaignTargets(),
    ) { campaigns, targets ->
        campaigns.mapNotNull { campaign -> targets.firstOrNull { it.campaignId == campaign.id }?.let { campaign.toDomain(it) } }
    }

    override fun observeActiveScanActions(storeId: String?): Flow<List<RewardProgramScanAction>> =
        observePrograms(storeId).map { programs ->
            programs
                .asSequence()
                .filter { it.active }
                .flatMap { it.configuration.scanActions.asSequence() }
                .distinct()
                .sortedBy { it.ordinal }
                .toList()
        }

    override suspend fun createReward(draft: RewardDraft): Reward {
        val reward = Reward(
            id = UUID.randomUUID().toString(),
            storeId = draft.storeId,
            name = draft.name.trim(),
            description = draft.description.trim(),
            pointsRequired = draft.pointsRequired,
            rewardType = draft.rewardType,
            expirationDate = draft.expirationDate,
            usageLimit = draft.usageLimit,
            activeStatus = draft.activeStatus,
        )
        database.loyaltyDao().insertReward(reward.toEntity())
        return reward
    }

    override suspend fun updateReward(rewardId: String, draft: RewardDraft): Reward {
        val existing = database.loyaltyDao().getReward(rewardId) ?: error("Reward $rewardId does not exist")
        val updated = Reward(
            id = existing.id,
            storeId = draft.storeId,
            name = draft.name.trim(),
            description = draft.description.trim(),
            pointsRequired = draft.pointsRequired,
            rewardType = draft.rewardType,
            expirationDate = draft.expirationDate,
            usageLimit = draft.usageLimit,
            activeStatus = draft.activeStatus,
        )
        database.loyaltyDao().insertReward(updated.toEntity())
        return updated
    }

    override suspend fun setRewardEnabled(rewardId: String, enabled: Boolean) {
        val current = database.loyaltyDao().getReward(rewardId) ?: return
        database.loyaltyDao().insertReward(current.copy(activeStatus = enabled))
    }

    override suspend fun deleteReward(rewardId: String) {
        database.loyaltyDao().deleteReward(rewardId)
    }

    override suspend fun createProgram(draft: RewardProgramDraft): RewardProgram {
        val program = RewardProgram(
            id = UUID.randomUUID().toString(),
            storeId = draft.storeId,
            name = draft.name.trim(),
            description = draft.description.trim(),
            type = draft.type,
            rulesSummary = draft.rulesSummary.trim(),
            active = draft.active,
            configuration = draft.configuration,
        )
        database.loyaltyDao().insertProgram(program.toEntity())
        return program
    }

    override suspend fun updateProgram(programId: String, draft: RewardProgramDraft): RewardProgram {
        val existing = database.loyaltyDao().getProgram(programId) ?: error("Program $programId does not exist")
        val updated = RewardProgram(
            id = existing.id,
            storeId = draft.storeId,
            name = draft.name.trim(),
            description = draft.description.trim(),
            type = draft.type,
            rulesSummary = draft.rulesSummary.trim(),
            active = draft.active,
            configuration = draft.configuration,
        )
        database.loyaltyDao().insertProgram(updated.toEntity())
        return updated
    }

    override suspend fun setProgramEnabled(programId: String, enabled: Boolean) {
        val current = database.loyaltyDao().getProgram(programId) ?: return
        database.loyaltyDao().insertProgram(current.copy(active = enabled))
    }

    override suspend fun deleteProgram(programId: String) {
        database.loyaltyDao().deleteProgram(programId)
    }

    override suspend fun createCampaign(draft: PromotionDraft): Campaign {
        val campaignId = UUID.randomUUID().toString()
        val targetId = UUID.randomUUID().toString()
        val campaign = Campaign(
            id = campaignId,
            storeId = draft.storeId,
            name = draft.name.trim(),
            description = draft.description.trim(),
            imageUri = draft.imageUri?.trim()?.ifBlank { null },
            startDate = draft.startDate,
            endDate = draft.endDate,
            promotionType = draft.promotionType,
            promotionValue = draft.promotionValue,
            minimumPurchaseAmount = draft.minimumPurchaseAmount,
            usageLimit = draft.usageLimit,
            promoCode = draft.promoCode?.trim()?.ifBlank { null },
            visibility = draft.visibility,
            boostLevel = draft.boostLevel,
            paymentFlowEnabled = draft.paymentFlowEnabled,
            active = draft.active,
            target = CampaignTarget(
                id = targetId,
                campaignId = campaignId,
                segment = draft.targetSegment,
                description = draft.targetDescription.trim(),
            ),
        )
        database.loyaltyDao().insertCampaign(campaign.toEntity())
        database.loyaltyDao().insertCampaignTarget(
            CampaignTargetEntity(
                id = targetId,
                campaignId = campaignId,
                segment = draft.targetSegment.name,
                description = draft.targetDescription.trim(),
            )
        )
        return campaign
    }

    override suspend fun updateCampaign(campaignId: String, draft: PromotionDraft): Campaign {
        val existing = database.loyaltyDao().getCampaign(campaignId) ?: error("Campaign $campaignId does not exist")
        val existingTarget = database.loyaltyDao().getCampaignTarget(campaignId)
            ?: error("Campaign target for $campaignId does not exist")
        val updated = Campaign(
            id = existing.id,
            storeId = draft.storeId,
            name = draft.name.trim(),
            description = draft.description.trim(),
            imageUri = draft.imageUri?.trim()?.ifBlank { null },
            startDate = draft.startDate,
            endDate = draft.endDate,
            promotionType = draft.promotionType,
            promotionValue = draft.promotionValue,
            minimumPurchaseAmount = draft.minimumPurchaseAmount,
            usageLimit = draft.usageLimit,
            promoCode = draft.promoCode?.trim()?.ifBlank { null },
            visibility = draft.visibility,
            boostLevel = draft.boostLevel,
            paymentFlowEnabled = draft.paymentFlowEnabled,
            active = draft.active,
            target = CampaignTarget(
                id = existingTarget.id,
                campaignId = existing.id,
                segment = draft.targetSegment,
                description = draft.targetDescription.trim(),
            ),
        )
        database.loyaltyDao().insertCampaign(updated.toEntity())
        database.loyaltyDao().insertCampaignTarget(
            CampaignTargetEntity(
                id = existingTarget.id,
                campaignId = existing.id,
                segment = draft.targetSegment.name,
                description = draft.targetDescription.trim(),
            )
        )
        return updated
    }

    override suspend fun setCampaignEnabled(campaignId: String, enabled: Boolean) {
        val current = database.loyaltyDao().getCampaign(campaignId) ?: return
        database.loyaltyDao().insertCampaign(current.copy(active = enabled))
    }

    override suspend fun deleteCampaign(campaignId: String) {
        database.loyaltyDao().deleteCampaignTarget(campaignId)
        database.loyaltyDao().deleteCampaign(campaignId)
    }
}
