package com.vector.verevcodex.data.remote.loyalty

import com.vector.verevcodex.data.remote.api.loyalty.ActiveScanActionsResponseDto
import com.vector.verevcodex.data.remote.api.loyalty.CampaignRequestDto
import com.vector.verevcodex.data.remote.api.loyalty.CampaignViewDto
import com.vector.verevcodex.data.remote.api.loyalty.LoyaltyProgramRequestDto
import com.vector.verevcodex.data.remote.api.loyalty.LoyaltyProgramViewDto
import com.vector.verevcodex.data.remote.api.loyalty.RewardRequestDto
import com.vector.verevcodex.data.remote.api.loyalty.RewardViewDto
import com.vector.verevcodex.data.remote.api.loyalty.VerevCampaignsApi
import com.vector.verevcodex.data.remote.api.loyalty.VerevProgramsApi
import com.vector.verevcodex.data.remote.api.loyalty.VerevRewardsApi
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyAction
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyDomain
import com.vector.verevcodex.data.remote.core.buildRemoteIdempotencyKey
import com.vector.verevcodex.data.remote.core.unwrap
import com.vector.verevcodex.domain.model.common.CampaignSegment
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.common.RewardType
import com.vector.verevcodex.domain.model.loyalty.CashbackProgramRule
import com.vector.verevcodex.domain.model.loyalty.CheckInProgramRule
import com.vector.verevcodex.domain.model.loyalty.CouponProgramRule
import com.vector.verevcodex.domain.model.loyalty.PointsProgramRule
import com.vector.verevcodex.domain.model.loyalty.PurchaseFrequencyProgramRule
import com.vector.verevcodex.domain.model.loyalty.ReferralProgramRule
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramConfiguration
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.model.loyalty.TierProgramRule
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.CampaignTarget
import com.vector.verevcodex.domain.model.promotions.PromotionDraft
import com.vector.verevcodex.domain.model.promotions.PromotionBoostLevel
import com.vector.verevcodex.domain.model.promotions.PromotionType
import com.vector.verevcodex.domain.model.promotions.PromotionVisibility
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoyaltyRemoteDataSource @Inject constructor(
    private val programsApi: VerevProgramsApi,
    private val rewardsApi: VerevRewardsApi,
    private val campaignsApi: VerevCampaignsApi,
) {

    suspend fun listPrograms(storeId: String?): Result<List<RewardProgram>> = runCatching {
        val response = programsApi.list()
        val list = response.unwrap { it.map { dto -> dto.toDomain() } }
        if (storeId != null) list.filter { it.storeId == storeId } else list
    }

    suspend fun listRewards(storeId: String?): Result<List<Reward>> = runCatching {
        val response = rewardsApi.list()
        val list = response.unwrap { it.map { dto -> dto.toDomain() } }
        if (storeId != null) list.filter { it.storeId == storeId } else list
    }

    suspend fun listCampaigns(storeId: String?): Result<List<Campaign>> = runCatching {
        val response = campaignsApi.list()
        val list = response.unwrap { it.map { dto -> dto.toDomain() } }
        if (storeId != null) list.filter { it.storeId == storeId } else list
    }

    suspend fun activeScanActions(storeId: String?): Result<List<RewardProgramScanAction>> = runCatching {
        val response = programsApi.activeScanActions(storeId)
        response.unwrap { dto ->
            (dto.scanActions.orEmpty()).mapNotNull { s ->
                kotlin.runCatching { RewardProgramScanAction.valueOf(s) }.getOrNull()
            }
        }
    }

    suspend fun createProgram(draft: com.vector.verevcodex.domain.model.loyalty.RewardProgramDraft): Result<RewardProgram> = runCatching {
        val request = LoyaltyProgramRequestDto(
            storeId = draft.storeId,
            name = draft.name,
            description = draft.description,
            type = draft.type.name,
            active = draft.active,
            earningEnabled = draft.configuration.earningEnabled,
            rewardRedemptionEnabled = draft.configuration.rewardRedemptionEnabled,
            visitCheckInEnabled = draft.configuration.visitCheckInEnabled,
            cashbackEnabled = draft.configuration.cashbackEnabled,
            tierTrackingEnabled = draft.configuration.tierTrackingEnabled,
            couponEnabled = draft.configuration.couponEnabled,
            purchaseFrequencyEnabled = draft.configuration.purchaseFrequencyEnabled,
            referralEnabled = draft.configuration.referralEnabled,
            pointsSpendStepAmount = draft.configuration.pointsRule.spendStepAmount,
            pointsAwardedPerStep = draft.configuration.pointsRule.pointsAwardedPerStep,
            pointsWelcomeBonus = draft.configuration.pointsRule.welcomeBonusPoints,
            pointsMinimumRedeem = draft.configuration.pointsRule.minimumRedeemPoints,
            cashbackPercent = draft.configuration.cashbackRule.cashbackPercent.toDouble(),
            cashbackMinimumSpendAmount = draft.configuration.cashbackRule.minimumSpendAmount.toDouble(),
            tierSilverThreshold = draft.configuration.tierRule.silverThreshold,
            tierGoldThreshold = draft.configuration.tierRule.goldThreshold,
            tierVipThreshold = draft.configuration.tierRule.vipThreshold,
            tierBonusPercent = draft.configuration.tierRule.tierBonusPercent,
            couponName = draft.configuration.couponRule.couponName,
            couponPointsCost = draft.configuration.couponRule.pointsCost,
            couponDiscountAmount = draft.configuration.couponRule.discountAmount.toDouble(),
            couponMinimumSpendAmount = draft.configuration.couponRule.minimumSpendAmount.toDouble(),
            checkInVisitsRequired = draft.configuration.checkInRule.visitsRequired,
            checkInRewardPoints = draft.configuration.checkInRule.rewardPoints,
            checkInRewardName = draft.configuration.checkInRule.rewardName,
            purchaseFrequencyCount = draft.configuration.purchaseFrequencyRule.purchaseCount,
            purchaseFrequencyWindowDays = draft.configuration.purchaseFrequencyRule.windowDays,
            purchaseFrequencyRewardPoints = draft.configuration.purchaseFrequencyRule.rewardPoints,
            purchaseFrequencyRewardName = draft.configuration.purchaseFrequencyRule.rewardName,
            referralReferrerRewardPoints = draft.configuration.referralRule.referrerRewardPoints,
            referralRefereeRewardPoints = draft.configuration.referralRule.refereeRewardPoints,
            referralCodePrefix = draft.configuration.referralRule.referralCodePrefix,
        )
        val response = programsApi.create(
            request = request,
            idempotencyKey = programIdempotencyKey(
                action = RemoteIdempotencyAction.CREATE,
                draft.storeId,
                draft.name,
                draft.type.name,
                draft.active.toString(),
            ),
        )
        response.unwrap { it.toDomain() }
    }

    suspend fun updateProgram(programId: String, draft: com.vector.verevcodex.domain.model.loyalty.RewardProgramDraft): Result<RewardProgram> = runCatching {
        val request = LoyaltyProgramRequestDto(
            storeId = draft.storeId,
            name = draft.name,
            description = draft.description,
            type = draft.type.name,
            active = draft.active,
            earningEnabled = draft.configuration.earningEnabled,
            rewardRedemptionEnabled = draft.configuration.rewardRedemptionEnabled,
            visitCheckInEnabled = draft.configuration.visitCheckInEnabled,
            cashbackEnabled = draft.configuration.cashbackEnabled,
            tierTrackingEnabled = draft.configuration.tierTrackingEnabled,
            couponEnabled = draft.configuration.couponEnabled,
            purchaseFrequencyEnabled = draft.configuration.purchaseFrequencyEnabled,
            referralEnabled = draft.configuration.referralEnabled,
            pointsSpendStepAmount = draft.configuration.pointsRule.spendStepAmount,
            pointsAwardedPerStep = draft.configuration.pointsRule.pointsAwardedPerStep,
            pointsWelcomeBonus = draft.configuration.pointsRule.welcomeBonusPoints,
            pointsMinimumRedeem = draft.configuration.pointsRule.minimumRedeemPoints,
            cashbackPercent = draft.configuration.cashbackRule.cashbackPercent.toDouble(),
            cashbackMinimumSpendAmount = draft.configuration.cashbackRule.minimumSpendAmount.toDouble(),
            tierSilverThreshold = draft.configuration.tierRule.silverThreshold,
            tierGoldThreshold = draft.configuration.tierRule.goldThreshold,
            tierVipThreshold = draft.configuration.tierRule.vipThreshold,
            tierBonusPercent = draft.configuration.tierRule.tierBonusPercent,
            couponName = draft.configuration.couponRule.couponName,
            couponPointsCost = draft.configuration.couponRule.pointsCost,
            couponDiscountAmount = draft.configuration.couponRule.discountAmount.toDouble(),
            couponMinimumSpendAmount = draft.configuration.couponRule.minimumSpendAmount.toDouble(),
            checkInVisitsRequired = draft.configuration.checkInRule.visitsRequired,
            checkInRewardPoints = draft.configuration.checkInRule.rewardPoints,
            checkInRewardName = draft.configuration.checkInRule.rewardName,
            purchaseFrequencyCount = draft.configuration.purchaseFrequencyRule.purchaseCount,
            purchaseFrequencyWindowDays = draft.configuration.purchaseFrequencyRule.windowDays,
            purchaseFrequencyRewardPoints = draft.configuration.purchaseFrequencyRule.rewardPoints,
            purchaseFrequencyRewardName = draft.configuration.purchaseFrequencyRule.rewardName,
            referralReferrerRewardPoints = draft.configuration.referralRule.referrerRewardPoints,
            referralRefereeRewardPoints = draft.configuration.referralRule.refereeRewardPoints,
            referralCodePrefix = draft.configuration.referralRule.referralCodePrefix,
        )
        val response = programsApi.update(
            programId = programId,
            request = request,
            idempotencyKey = programIdempotencyKey(
                action = RemoteIdempotencyAction.UPDATE,
                programId,
                draft.storeId,
                draft.name,
                draft.type.name,
                draft.active.toString(),
            ),
        )
        response.unwrap { it.toDomain() }
    }

    suspend fun setProgramEnabled(programId: String, enabled: Boolean): Result<RewardProgram> = runCatching {
        val response = if (enabled) {
            programsApi.enable(
                programId = programId,
                idempotencyKey = programIdempotencyKey(
                    action = RemoteIdempotencyAction.ENABLE,
                    programId,
                ),
            )
        } else {
            programsApi.disable(
                programId = programId,
                idempotencyKey = programIdempotencyKey(
                    action = RemoteIdempotencyAction.DISABLE,
                    programId,
                ),
            )
        }
        response.unwrap { it.toDomain() }
    }

    suspend fun deleteProgram(programId: String): Result<Unit> = runCatching {
        val response = programsApi.delete(
            programId = programId,
            idempotencyKey = programIdempotencyKey(
                action = RemoteIdempotencyAction.DELETE,
                programId,
            ),
        )
        response.unwrap { Unit }
    }

    suspend fun createReward(draft: com.vector.verevcodex.domain.model.loyalty.RewardDraft): Result<Reward> = runCatching {
        val request = RewardRequestDto(
            storeId = draft.storeId,
            name = draft.name,
            description = draft.description,
            pointsRequired = draft.pointsRequired,
            rewardType = draft.rewardType.name,
            expirationDate = draft.expirationDate?.toString(),
            usageLimit = draft.usageLimit,
            activeStatus = draft.activeStatus,
        )
        val response = rewardsApi.create(
            request = request,
            idempotencyKey = rewardIdempotencyKey(
                action = RemoteIdempotencyAction.CREATE,
                draft.storeId,
                draft.name,
                draft.rewardType.name,
                draft.pointsRequired.toString(),
                draft.activeStatus.toString(),
            ),
        )
        response.unwrap { it.toDomain() }
    }

    suspend fun updateReward(rewardId: String, draft: com.vector.verevcodex.domain.model.loyalty.RewardDraft): Result<Reward> = runCatching {
        val request = RewardRequestDto(
            storeId = draft.storeId,
            name = draft.name,
            description = draft.description,
            pointsRequired = draft.pointsRequired,
            rewardType = draft.rewardType.name,
            expirationDate = draft.expirationDate?.toString(),
            usageLimit = draft.usageLimit,
            activeStatus = draft.activeStatus,
        )
        val response = rewardsApi.update(
            rewardId = rewardId,
            request = request,
            idempotencyKey = rewardIdempotencyKey(
                action = RemoteIdempotencyAction.UPDATE,
                rewardId,
                draft.storeId,
                draft.name,
                draft.rewardType.name,
                draft.pointsRequired.toString(),
                draft.activeStatus.toString(),
            ),
        )
        response.unwrap { it.toDomain() }
    }

    suspend fun setRewardEnabled(rewardId: String, enabled: Boolean): Result<Reward> = runCatching {
        val response = if (enabled) {
            rewardsApi.enable(
                rewardId = rewardId,
                idempotencyKey = rewardIdempotencyKey(
                    action = RemoteIdempotencyAction.ENABLE,
                    rewardId,
                ),
            )
        } else {
            rewardsApi.disable(
                rewardId = rewardId,
                idempotencyKey = rewardIdempotencyKey(
                    action = RemoteIdempotencyAction.DISABLE,
                    rewardId,
                ),
            )
        }
        response.unwrap { it.toDomain() }
    }

    suspend fun deleteReward(rewardId: String): Result<Unit> = runCatching {
        val response = rewardsApi.delete(
            rewardId = rewardId,
            idempotencyKey = rewardIdempotencyKey(
                action = RemoteIdempotencyAction.DELETE,
                rewardId,
            ),
        )
        response.unwrap { Unit }
    }

    suspend fun createCampaign(draft: PromotionDraft): Result<Campaign> = runCatching {
        val request = CampaignRequestDto(
            storeId = draft.storeId,
            name = draft.name,
            description = draft.description,
            imageUri = draft.imageUri,
            startDate = draft.startDate.toString(),
            endDate = draft.endDate.toString(),
            promotionType = draft.promotionType.name,
            promotionValue = draft.promotionValue,
            minimumPurchaseAmount = draft.minimumPurchaseAmount,
            usageLimit = draft.usageLimit,
            promoCode = draft.promoCode,
            visibility = draft.visibility.name,
            boostLevel = draft.boostLevel?.name,
            paymentFlowEnabled = draft.paymentFlowEnabled,
            active = draft.active,
            segments = emptyList(),
            targetSegment = draft.targetSegment.name,
            targetDescription = draft.targetDescription,
        )
        val response = campaignsApi.create(
            request = request,
            idempotencyKey = campaignIdempotencyKey(
                action = RemoteIdempotencyAction.CREATE,
                draft.storeId,
                draft.name,
                draft.promotionType.name,
                draft.startDate.toString(),
                draft.endDate.toString(),
                draft.active.toString(),
            ),
        )
        response.unwrap { it.toDomain() }
    }

    suspend fun updateCampaign(campaignId: String, draft: PromotionDraft): Result<Campaign> = runCatching {
        val request = CampaignRequestDto(
            storeId = draft.storeId,
            name = draft.name,
            description = draft.description,
            imageUri = draft.imageUri,
            startDate = draft.startDate.toString(),
            endDate = draft.endDate.toString(),
            promotionType = draft.promotionType.name,
            promotionValue = draft.promotionValue,
            minimumPurchaseAmount = draft.minimumPurchaseAmount,
            usageLimit = draft.usageLimit,
            promoCode = draft.promoCode,
            visibility = draft.visibility.name,
            boostLevel = draft.boostLevel?.name,
            paymentFlowEnabled = draft.paymentFlowEnabled,
            active = draft.active,
            segments = emptyList(),
            targetSegment = draft.targetSegment.name,
            targetDescription = draft.targetDescription,
        )
        val response = campaignsApi.update(
            campaignId = campaignId,
            request = request,
            idempotencyKey = campaignIdempotencyKey(
                action = RemoteIdempotencyAction.UPDATE,
                campaignId,
                draft.storeId,
                draft.name,
                draft.promotionType.name,
                draft.startDate.toString(),
                draft.endDate.toString(),
                draft.active.toString(),
            ),
        )
        response.unwrap { it.toDomain() }
    }

    suspend fun setCampaignEnabled(campaignId: String, enabled: Boolean): Result<Campaign> = runCatching {
        val response = if (enabled) {
            campaignsApi.enable(
                campaignId = campaignId,
                idempotencyKey = campaignIdempotencyKey(
                    action = RemoteIdempotencyAction.ENABLE,
                    campaignId,
                ),
            )
        } else {
            campaignsApi.disable(
                campaignId = campaignId,
                idempotencyKey = campaignIdempotencyKey(
                    action = RemoteIdempotencyAction.DISABLE,
                    campaignId,
                ),
            )
        }
        response.unwrap { it.toDomain() }
    }

    suspend fun deleteCampaign(campaignId: String): Result<Unit> = runCatching {
        val response = campaignsApi.delete(
            campaignId = campaignId,
            idempotencyKey = campaignIdempotencyKey(
                action = RemoteIdempotencyAction.DELETE,
                campaignId,
            ),
        )
        response.unwrap { Unit }
    }

    private fun programIdempotencyKey(
        action: RemoteIdempotencyAction,
        vararg parts: String?,
    ): String = buildRemoteIdempotencyKey(RemoteIdempotencyDomain.PROGRAM, action, *parts)

    private fun rewardIdempotencyKey(
        action: RemoteIdempotencyAction,
        vararg parts: String?,
    ): String = buildRemoteIdempotencyKey(RemoteIdempotencyDomain.REWARD, action, *parts)

    private fun campaignIdempotencyKey(
        action: RemoteIdempotencyAction,
        vararg parts: String?,
    ): String = buildRemoteIdempotencyKey(RemoteIdempotencyDomain.CAMPAIGN, action, *parts)
}

private fun LoyaltyProgramViewDto.toDomain(): RewardProgram {
    val programType = kotlin.runCatching { LoyaltyProgramType.valueOf(type.orEmpty()) }.getOrElse { LoyaltyProgramType.POINTS }
    val scanActionsSet = (scanActions?.mapNotNull { kotlin.runCatching { RewardProgramScanAction.valueOf(it) }.getOrNull() }?.toSet()).orEmpty()
    val config = RewardProgramConfiguration(
        earningEnabled = earningEnabled ?: false,
        rewardRedemptionEnabled = rewardRedemptionEnabled ?: false,
        visitCheckInEnabled = visitCheckInEnabled ?: false,
        cashbackEnabled = cashbackEnabled ?: false,
        tierTrackingEnabled = tierTrackingEnabled ?: false,
        couponEnabled = couponEnabled ?: false,
        purchaseFrequencyEnabled = purchaseFrequencyEnabled ?: false,
        referralEnabled = referralEnabled ?: false,
        scanActions = scanActionsSet,
        pointsRule = PointsProgramRule(
            spendStepAmount = pointsSpendStepAmount ?: 0,
            pointsAwardedPerStep = pointsAwardedPerStep ?: 0,
            welcomeBonusPoints = pointsWelcomeBonus ?: 0,
            minimumRedeemPoints = pointsMinimumRedeem ?: 0,
        ),
        cashbackRule = CashbackProgramRule(
            cashbackPercent = cashbackPercent ?: 0.0,
            minimumSpendAmount = cashbackMinimumSpendAmount ?: 0.0,
        ),
        tierRule = TierProgramRule(
            silverThreshold = tierSilverThreshold ?: 0,
            goldThreshold = tierGoldThreshold ?: 0,
            vipThreshold = tierVipThreshold ?: 0,
            tierBonusPercent = tierBonusPercent ?: 0,
        ),
        couponRule = CouponProgramRule(
            couponName = couponName.orEmpty(),
            pointsCost = couponPointsCost ?: 0,
            discountAmount = couponDiscountAmount ?: 0.0,
            minimumSpendAmount = couponMinimumSpendAmount ?: 0.0,
        ),
        checkInRule = CheckInProgramRule(
            visitsRequired = checkInVisitsRequired ?: 0,
            rewardPoints = checkInRewardPoints ?: 0,
            rewardName = checkInRewardName.orEmpty(),
        ),
        purchaseFrequencyRule = PurchaseFrequencyProgramRule(
            purchaseCount = purchaseFrequencyCount ?: 0,
            windowDays = purchaseFrequencyWindowDays ?: 0,
            rewardPoints = purchaseFrequencyRewardPoints ?: 0,
            rewardName = purchaseFrequencyRewardName.orEmpty(),
        ),
        referralRule = ReferralProgramRule(
            referrerRewardPoints = referralReferrerRewardPoints ?: 0,
            refereeRewardPoints = referralRefereeRewardPoints ?: 0,
            referralCodePrefix = referralCodePrefix.orEmpty(),
        ),
    )
    return RewardProgram(
        id = id.orEmpty(),
        storeId = storeId.orEmpty(),
        name = name.orEmpty(),
        description = description.orEmpty(),
        type = programType,
        rulesSummary = rulesSummary.orEmpty(),
        active = active ?: false,
        configuration = config,
    )
}

private fun RewardViewDto.toDomain() = Reward(
    id = id.orEmpty(),
    storeId = storeId.orEmpty(),
    name = name.orEmpty(),
    description = description.orEmpty(),
    pointsRequired = pointsRequired ?: 0,
    rewardType = kotlin.runCatching { RewardType.valueOf(rewardType.orEmpty()) }.getOrElse { RewardType.DISCOUNT_COUPON },
    expirationDate = expirationDate?.let { LocalDate.parse(it.take(10)) },
    usageLimit = usageLimit ?: 0,
    activeStatus = activeStatus ?: false,
)

private fun CampaignViewDto.toDomain(): Campaign {
    val firstTarget = targets?.firstOrNull()
    val segment = kotlin.runCatching { CampaignSegment.valueOf(firstTarget?.segment ?: "ALL_CUSTOMERS") }.getOrElse { CampaignSegment.ALL_CUSTOMERS }
    val target = CampaignTarget(
        id = firstTarget?.id ?: id.orEmpty(),
        campaignId = id.orEmpty(),
        segment = segment,
        description = firstTarget?.description ?: "",
    )
    val promoType = kotlin.runCatching { PromotionType.valueOf(promotionType.orEmpty()) }.getOrElse { PromotionType.PERCENT_DISCOUNT }
    return Campaign(
        id = id.orEmpty(),
        storeId = storeId.orEmpty(),
        name = name.orEmpty(),
        description = description.orEmpty(),
        imageUri = imageUri,
        startDate = LocalDate.parse(startDate.orEmpty().take(10).ifBlank { LocalDate.now().toString() }),
        endDate = LocalDate.parse(endDate.orEmpty().take(10).ifBlank { LocalDate.now().toString() }),
        promotionType = promoType,
        promotionValue = promotionValue ?: 0.0,
        minimumPurchaseAmount = minimumPurchaseAmount ?: 0.0,
        usageLimit = usageLimit ?: 0,
        promoCode = promoCode,
        visibility = kotlin.runCatching { PromotionVisibility.valueOf(visibility.orEmpty()) }.getOrElse { PromotionVisibility.BUSINESS_ONLY },
        boostLevel = boostLevel?.let { kotlin.runCatching { PromotionBoostLevel.valueOf(it) }.getOrNull() },
        paymentFlowEnabled = paymentFlowEnabled ?: false,
        active = active ?: false,
        target = target,
    )
}
