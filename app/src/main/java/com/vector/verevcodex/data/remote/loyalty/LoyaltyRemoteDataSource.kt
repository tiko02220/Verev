package com.vector.verevcodex.data.remote.loyalty

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.vector.verevcodex.data.remote.api.ApiEnvelope
import com.vector.verevcodex.data.remote.api.loyalty.ActiveScanActionsResponseDto
import com.vector.verevcodex.data.remote.api.loyalty.CampaignRequestDto
import com.vector.verevcodex.data.remote.api.loyalty.CampaignViewDto
import com.vector.verevcodex.data.remote.api.loyalty.LoyaltyProgramRequestDto
import com.vector.verevcodex.data.remote.api.loyalty.LoyaltyProgramViewDto
import com.vector.verevcodex.data.remote.api.loyalty.RewardRequestDto
import com.vector.verevcodex.data.remote.api.loyalty.RewardInventoryAdjustmentRequestDto
import com.vector.verevcodex.data.remote.api.loyalty.RewardViewDto
import com.vector.verevcodex.data.remote.api.loyalty.TierLevelDto
import com.vector.verevcodex.data.remote.api.loyalty.VerevCampaignsApi
import com.vector.verevcodex.data.remote.api.loyalty.VerevProgramsApi
import com.vector.verevcodex.data.remote.api.loyalty.VerevRewardsApi
import com.vector.verevcodex.data.remote.api.media.VerevMediaApi
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyAction
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyDomain
import com.vector.verevcodex.data.remote.core.buildRemoteIdempotencyKey
import com.vector.verevcodex.data.remote.core.remoteResult
import com.vector.verevcodex.data.remote.core.unwrap
import com.vector.verevcodex.domain.model.common.CampaignSegment
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.common.RewardType
import com.vector.verevcodex.domain.model.loyalty.CashbackProgramRule
import com.vector.verevcodex.domain.model.loyalty.CheckInProgramRule
import com.vector.verevcodex.domain.model.loyalty.CouponProgramRule
import com.vector.verevcodex.domain.model.loyalty.PointsProgramRule
import com.vector.verevcodex.domain.model.loyalty.ProgramBenefitResetPolicy
import com.vector.verevcodex.domain.model.loyalty.ProgramBenefitResetType
import com.vector.verevcodex.domain.model.loyalty.ProgramRewardOutcome
import com.vector.verevcodex.domain.model.loyalty.ProgramRewardOutcomeType
import com.vector.verevcodex.domain.model.loyalty.PurchaseFrequencyProgramRule
import com.vector.verevcodex.domain.model.loyalty.ReferralProgramRule
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramConfiguration
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.model.loyalty.TierLevelRule
import com.vector.verevcodex.domain.model.loyalty.TierProgramRule
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.CampaignTarget
import com.vector.verevcodex.domain.model.promotions.PromotionDraft
import com.vector.verevcodex.domain.model.promotions.PromotionBoostLevel
import com.vector.verevcodex.domain.model.promotions.PromotionType
import com.vector.verevcodex.domain.model.promotions.PromotionVisibility
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

@Singleton
class LoyaltyRemoteDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val programsApi: VerevProgramsApi,
    private val rewardsApi: VerevRewardsApi,
    private val campaignsApi: VerevCampaignsApi,
    private val mediaApi: VerevMediaApi,
) {

    suspend fun listPrograms(storeId: String?): Result<List<RewardProgram>> = remoteResult {
        listScoped(
            storeId = storeId,
            request = programsApi::list,
            mapper = { it.toDomain() },
            storeIdOf = RewardProgram::storeId,
        )
    }

    suspend fun listRewards(storeId: String?): Result<List<Reward>> = remoteResult {
        listScoped(
            storeId = storeId,
            request = rewardsApi::list,
            mapper = { it.toDomain() },
            storeIdOf = Reward::storeId,
        )
    }

    suspend fun listCampaigns(storeId: String?): Result<List<Campaign>> = remoteResult {
        listScoped(
            storeId = storeId,
            request = campaignsApi::list,
            mapper = { it.toDomain() },
            storeIdOf = Campaign::storeId,
        )
    }

    suspend fun activeScanActions(storeId: String?): Result<List<RewardProgramScanAction>> = remoteResult {
        val response = programsApi.activeScanActions(storeId)
        response.unwrap { dto ->
            (dto.scanActions.orEmpty()).mapNotNull { s ->
                kotlin.runCatching { RewardProgramScanAction.valueOf(s) }.getOrNull()
            }
        }
    }

    suspend fun createProgram(draft: com.vector.verevcodex.domain.model.loyalty.RewardProgramDraft): Result<RewardProgram> = remoteResult {
        val request = LoyaltyProgramRequestDto(
            storeId = draft.storeId,
            name = draft.name,
            description = draft.description,
            type = draft.type.name,
            active = draft.active,
            autoScheduleEnabled = draft.autoScheduleEnabled,
            scheduleStartDate = draft.scheduleStartDate?.toString(),
            scheduleEndDate = draft.scheduleEndDate?.toString(),
            scheduleDurationDays = draft.scheduleStartDate?.let { start ->
                draft.scheduleEndDate?.let { end -> ChronoUnit.DAYS.between(start, end).toInt() + 1 }
            },
            annualRepeatEnabled = draft.annualRepeatEnabled,
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
            tierLevels = draft.configuration.tierRule.sortedLevels.map { it.toDto() },
            couponName = draft.configuration.couponRule.couponName,
            couponPointsCost = draft.configuration.couponRule.pointsCost,
            couponDiscountAmount = draft.configuration.couponRule.discountAmount.toDouble(),
            couponMinimumSpendAmount = draft.configuration.couponRule.minimumSpendAmount.toDouble(),
            checkInVisitsRequired = draft.configuration.checkInRule.visitsRequired,
            checkInRewardPoints = draft.configuration.checkInRule.rewardPoints,
            checkInRewardName = draft.configuration.checkInRule.rewardName,
            checkInReward = draft.configuration.checkInRule.rewardOutcome.toDto(),
            purchaseFrequencyCount = draft.configuration.purchaseFrequencyRule.purchaseCount,
            purchaseFrequencyWindowDays = draft.configuration.purchaseFrequencyRule.windowDays,
            purchaseFrequencyRewardPoints = draft.configuration.purchaseFrequencyRule.rewardPoints,
            purchaseFrequencyRewardName = draft.configuration.purchaseFrequencyRule.rewardName,
            purchaseFrequencyReward = draft.configuration.purchaseFrequencyRule.rewardOutcome.toDto(),
            referralReferrerRewardPoints = draft.configuration.referralRule.referrerRewardPoints,
            referralRefereeRewardPoints = draft.configuration.referralRule.refereeRewardPoints,
            referralReferrerReward = draft.configuration.referralRule.referrerRewardOutcome.toDto(),
            referralRefereeReward = draft.configuration.referralRule.refereeRewardOutcome.toDto(),
            referralCodePrefix = draft.configuration.referralRule.referralCodePrefix,
            targetGender = draft.targetGender,
            targetAgeMin = draft.targetAgeMin,
            targetAgeMax = draft.targetAgeMax,
            oneTimePerCustomer = draft.oneTimePerCustomer,
            benefitResetType = draft.benefitResetPolicy.type.name,
            benefitResetCustomDays = draft.benefitResetPolicy.customDays,
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

    suspend fun updateProgram(programId: String, draft: com.vector.verevcodex.domain.model.loyalty.RewardProgramDraft): Result<RewardProgram> = remoteResult {
        val request = LoyaltyProgramRequestDto(
            storeId = draft.storeId,
            name = draft.name,
            description = draft.description,
            type = draft.type.name,
            active = draft.active,
            autoScheduleEnabled = draft.autoScheduleEnabled,
            scheduleStartDate = draft.scheduleStartDate?.toString(),
            scheduleEndDate = draft.scheduleEndDate?.toString(),
            scheduleDurationDays = draft.scheduleStartDate?.let { start ->
                draft.scheduleEndDate?.let { end -> ChronoUnit.DAYS.between(start, end).toInt() + 1 }
            },
            annualRepeatEnabled = draft.annualRepeatEnabled,
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
            tierLevels = draft.configuration.tierRule.sortedLevels.map { it.toDto() },
            couponName = draft.configuration.couponRule.couponName,
            couponPointsCost = draft.configuration.couponRule.pointsCost,
            couponDiscountAmount = draft.configuration.couponRule.discountAmount.toDouble(),
            couponMinimumSpendAmount = draft.configuration.couponRule.minimumSpendAmount.toDouble(),
            checkInVisitsRequired = draft.configuration.checkInRule.visitsRequired,
            checkInRewardPoints = draft.configuration.checkInRule.rewardPoints,
            checkInRewardName = draft.configuration.checkInRule.rewardName,
            checkInReward = draft.configuration.checkInRule.rewardOutcome.toDto(),
            purchaseFrequencyCount = draft.configuration.purchaseFrequencyRule.purchaseCount,
            purchaseFrequencyWindowDays = draft.configuration.purchaseFrequencyRule.windowDays,
            purchaseFrequencyRewardPoints = draft.configuration.purchaseFrequencyRule.rewardPoints,
            purchaseFrequencyRewardName = draft.configuration.purchaseFrequencyRule.rewardName,
            purchaseFrequencyReward = draft.configuration.purchaseFrequencyRule.rewardOutcome.toDto(),
            referralReferrerRewardPoints = draft.configuration.referralRule.referrerRewardPoints,
            referralRefereeRewardPoints = draft.configuration.referralRule.refereeRewardPoints,
            referralReferrerReward = draft.configuration.referralRule.referrerRewardOutcome.toDto(),
            referralRefereeReward = draft.configuration.referralRule.refereeRewardOutcome.toDto(),
            referralCodePrefix = draft.configuration.referralRule.referralCodePrefix,
            targetGender = draft.targetGender,
            targetAgeMin = draft.targetAgeMin,
            targetAgeMax = draft.targetAgeMax,
            oneTimePerCustomer = draft.oneTimePerCustomer,
            benefitResetType = draft.benefitResetPolicy.type.name,
            benefitResetCustomDays = draft.benefitResetPolicy.customDays,
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

    suspend fun setProgramEnabled(programId: String, enabled: Boolean): Result<RewardProgram> = remoteResult {
        toggleEnabled(
            entityId = programId,
            enabled = enabled,
            enable = { idempotencyKey -> programsApi.enable(programId = programId, idempotencyKey = idempotencyKey) },
            disable = { idempotencyKey -> programsApi.disable(programId = programId, idempotencyKey = idempotencyKey) },
            idempotencyKey = { action, entity, operationId -> programIdempotencyKey(action, entity, operationId) },
        ) { it.toDomain() }
    }

    suspend fun deleteProgram(programId: String): Result<Unit> = remoteResult {
        val response = programsApi.delete(
            programId = programId,
            idempotencyKey = programIdempotencyKey(
                action = RemoteIdempotencyAction.DELETE,
                programId,
            ),
        )
        response.unwrap { Unit }
    }

    suspend fun createReward(draft: com.vector.verevcodex.domain.model.loyalty.RewardDraft): Result<Reward> = remoteResult {
        val imageUri = resolveUploadedImageUri(draft.imageUri)
        val request = RewardRequestDto(
            storeId = draft.storeId,
            name = draft.name,
            description = draft.description,
            pointsRequired = draft.pointsRequired,
            rewardType = draft.rewardType.name,
            imageUri = imageUri,
            expirationDate = draft.expirationDate?.toString(),
            usageLimit = draft.usageLimit,
            inventoryTracked = draft.inventoryTracked,
            availableQuantity = draft.availableQuantity,
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

    suspend fun updateReward(rewardId: String, draft: com.vector.verevcodex.domain.model.loyalty.RewardDraft): Result<Reward> = remoteResult {
        val imageUri = resolveUploadedImageUri(draft.imageUri)
        val request = RewardRequestDto(
            storeId = draft.storeId,
            name = draft.name,
            description = draft.description,
            pointsRequired = draft.pointsRequired,
            rewardType = draft.rewardType.name,
            imageUri = imageUri,
            expirationDate = draft.expirationDate?.toString(),
            usageLimit = draft.usageLimit,
            inventoryTracked = draft.inventoryTracked,
            availableQuantity = draft.availableQuantity,
            activeStatus = draft.activeStatus,
        )
        val response = rewardsApi.update(
            rewardId = rewardId,
            request = request,
            idempotencyKey = rewardIdempotencyKey(
                action = RemoteIdempotencyAction.UPDATE,
                rewardId,
                System.currentTimeMillis().toString(),
            ),
        )
        response.unwrap { it.toDomain() }
    }

    suspend fun setRewardEnabled(rewardId: String, enabled: Boolean): Result<Reward> = remoteResult {
        toggleEnabled(
            entityId = rewardId,
            enabled = enabled,
            enable = { idempotencyKey -> rewardsApi.enable(rewardId = rewardId, idempotencyKey = idempotencyKey) },
            disable = { idempotencyKey -> rewardsApi.disable(rewardId = rewardId, idempotencyKey = idempotencyKey) },
            idempotencyKey = { action, entity, operationId -> rewardIdempotencyKey(action, entity, operationId) },
        ) { it.toDomain() }
    }

    suspend fun deleteReward(rewardId: String): Result<Unit> = remoteResult {
        val response = rewardsApi.delete(
            rewardId = rewardId,
            idempotencyKey = rewardIdempotencyKey(
                action = RemoteIdempotencyAction.DELETE,
                rewardId,
            ),
        )
        response.unwrap { Unit }
    }

    suspend fun adjustRewardInventory(rewardId: String, delta: Int): Result<Reward> = remoteResult {
        val response = rewardsApi.adjustInventory(
            rewardId = rewardId,
            request = RewardInventoryAdjustmentRequestDto(delta = delta),
            idempotencyKey = rewardIdempotencyKey(
                action = RemoteIdempotencyAction.UPDATE,
                rewardId,
                "inventory",
                delta.toString(),
                System.currentTimeMillis().toString(),
            ),
        )
        response.unwrap { it.toDomain() }
    }

    suspend fun createCampaign(draft: PromotionDraft): Result<Campaign> = remoteResult {
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

    suspend fun updateCampaign(campaignId: String, draft: PromotionDraft): Result<Campaign> = remoteResult {
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

    suspend fun setCampaignEnabled(campaignId: String, enabled: Boolean): Result<Campaign> = remoteResult {
        toggleEnabled(
            entityId = campaignId,
            enabled = enabled,
            enable = { idempotencyKey -> campaignsApi.enable(campaignId = campaignId, idempotencyKey = idempotencyKey) },
            disable = { idempotencyKey -> campaignsApi.disable(campaignId = campaignId, idempotencyKey = idempotencyKey) },
            idempotencyKey = { action, entity, operationId -> campaignIdempotencyKey(action, entity, operationId) },
        ) { it.toDomain() }
    }

    suspend fun deleteCampaign(campaignId: String): Result<Unit> = remoteResult {
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

    private suspend fun resolveUploadedImageUri(imageUri: String?): String? {
        val normalized = imageUri?.trim().orEmpty()
        if (normalized.isEmpty()) return null
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) return normalized

        val uri = Uri.parse(normalized)
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Unable to read selected reward image")
        val mimeType = context.contentResolver.getType(uri)?.takeIf { it.startsWith("image/") } ?: "image/jpeg"
        val response = mediaApi.uploadImage(
            file = MultipartBody.Part.createFormData(
                name = "file",
                filename = queryDisplayName(uri) ?: "reward-image.${extensionForMimeType(mimeType)}",
                body = bytes.toRequestBody(mimeType.toMediaType()),
            ),
            scope = "rewards".toRequestBody("text/plain".toMediaType()),
        )
        return response.unwrap { it.url }
    }

    private fun queryDisplayName(uri: Uri): String? =
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) cursor.getString(index) else null
        }

    private fun extensionForMimeType(mimeType: String): String = when (mimeType.lowercase()) {
        "image/png" -> "png"
        "image/gif" -> "gif"
        "image/webp" -> "webp"
        else -> "jpg"
    }

    private suspend inline fun <Dto, Domain> listScoped(
        storeId: String?,
        crossinline request: suspend () -> Response<ApiEnvelope<List<Dto>>>,
        crossinline mapper: (Dto) -> Domain,
        crossinline storeIdOf: (Domain) -> String,
    ): List<Domain> = filterByStore(
        storeId = storeId,
        items = request().unwrap { list -> list.map(mapper) },
        storeIdOf = storeIdOf,
    )

    private inline fun <Domain> filterByStore(
        storeId: String?,
        items: List<Domain>,
        crossinline storeIdOf: (Domain) -> String,
    ): List<Domain> = storeId?.let { targetStoreId -> items.filter { storeIdOf(it) == targetStoreId } } ?: items

    private suspend fun <Dto, Domain> toggleEnabled(
        entityId: String,
        enabled: Boolean,
        enable: suspend (String) -> Response<ApiEnvelope<Dto>>,
        disable: suspend (String) -> Response<ApiEnvelope<Dto>>,
        idempotencyKey: (RemoteIdempotencyAction, String, String) -> String,
        mapper: (Dto) -> Domain,
    ): Domain {
        val requestKey = buildMutableActionKey(entityId, enabled, idempotencyKey)
        val response = if (enabled) enable(requestKey) else disable(requestKey)
        return response.unwrap(mapper)
    }

    private fun buildMutableActionKey(
        entityId: String,
        enabled: Boolean,
        idempotencyKey: (RemoteIdempotencyAction, String, String) -> String,
    ): String {
        val action = if (enabled) RemoteIdempotencyAction.ENABLE else RemoteIdempotencyAction.DISABLE
        return idempotencyKey(action, entityId, UUID.randomUUID().toString())
    }
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
            levels = tierLevels
                ?.mapNotNull { it.toDomain() }
                ?.takeIf { it.isNotEmpty() }
                ?: TierProgramRule(
                    levels = listOf(
                        TierLevelRule(id = "tier_1", name = "Tier 1", threshold = 0, bonusPercent = 0),
                        TierLevelRule(id = "tier_2", name = "Tier 2", threshold = tierSilverThreshold ?: 250, bonusPercent = 5),
                        TierLevelRule(id = "tier_3", name = "Tier 3", threshold = tierGoldThreshold ?: 500, bonusPercent = tierBonusPercent ?: 10),
                        TierLevelRule(id = "tier_4", name = "Tier 4", threshold = tierVipThreshold ?: 1000, bonusPercent = (tierBonusPercent ?: 10) + 5),
                    ),
                ).sortedLevels,
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
            rewardOutcome = checkInReward.toDomainOutcome(
                fallbackPoints = checkInRewardPoints ?: 0,
                fallbackLabel = checkInRewardName.orEmpty(),
            ),
        ),
        purchaseFrequencyRule = PurchaseFrequencyProgramRule(
            purchaseCount = purchaseFrequencyCount ?: 0,
            windowDays = purchaseFrequencyWindowDays ?: 0,
            rewardPoints = purchaseFrequencyRewardPoints ?: 0,
            rewardName = purchaseFrequencyRewardName.orEmpty(),
            rewardOutcome = purchaseFrequencyReward.toDomainOutcome(
                fallbackPoints = purchaseFrequencyRewardPoints ?: 0,
                fallbackLabel = purchaseFrequencyRewardName.orEmpty(),
            ),
        ),
        referralRule = ReferralProgramRule(
            referrerRewardPoints = referralReferrerRewardPoints ?: 0,
            refereeRewardPoints = referralRefereeRewardPoints ?: 0,
            referralCodePrefix = referralCodePrefix.orEmpty(),
            referrerRewardOutcome = referralReferrerReward.toDomainOutcome(
                fallbackPoints = referralReferrerRewardPoints ?: 0,
                fallbackLabel = "Referrer reward",
            ),
            refereeRewardOutcome = referralRefereeReward.toDomainOutcome(
                fallbackPoints = referralRefereeRewardPoints ?: 0,
                fallbackLabel = "Friend reward",
            ),
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
        autoScheduleEnabled = autoScheduleEnabled ?: false,
        scheduleStartDate = scheduleStartDate?.take(10)?.takeIf { it.isNotBlank() }?.let(LocalDate::parse),
        scheduleEndDate = scheduleEndDate?.take(10)?.takeIf { it.isNotBlank() }?.let(LocalDate::parse)
            ?: scheduleStartDate?.take(10)?.takeIf { it.isNotBlank() }?.let(LocalDate::parse)?.let { start ->
                scheduleDurationDays?.let { start.plusDays(it.toLong() - 1L) }
            },
        annualRepeatEnabled = annualRepeatEnabled ?: false,
        configuration = config,
        targetGender = targetGender?.uppercase().orEmpty().ifBlank { "ALL" },
        targetAgeMin = targetAgeMin,
        targetAgeMax = targetAgeMax,
        oneTimePerCustomer = oneTimePerCustomer ?: false,
        benefitResetPolicy = ProgramBenefitResetPolicy(
            type = benefitResetType
                ?.uppercase()
                ?.let { raw -> kotlin.runCatching { ProgramBenefitResetType.valueOf(raw) }.getOrNull() }
                ?: ProgramBenefitResetType.NEVER,
            customDays = benefitResetCustomDays,
        ),
    )
}

private fun TierLevelRule.toDto(): TierLevelDto = TierLevelDto(
    id = id,
    name = name,
    threshold = threshold,
    bonusPercent = bonusPercent,
    rewardOutcome = rewardOutcome.toDto(),
)

private fun TierLevelDto.toDomain(): TierLevelRule? {
    val tierId = id?.trim().orEmpty()
    val tierName = name?.trim().orEmpty()
    val tierThreshold = threshold
    val tierBonusPercent = bonusPercent
    if (tierId.isBlank() || tierName.isBlank() || tierThreshold == null || tierBonusPercent == null) return null
    return TierLevelRule(
        id = tierId,
        name = tierName,
        threshold = tierThreshold,
        bonusPercent = tierBonusPercent,
        rewardOutcome = rewardOutcome.toDomainOutcome(
            fallbackPoints = 0,
            fallbackLabel = "$tierName reward",
        ),
    )
}

private fun RewardViewDto.toDomain() = Reward(
    id = id.orEmpty(),
    storeId = storeId.orEmpty(),
    name = name.orEmpty(),
    description = description.orEmpty(),
    pointsRequired = pointsRequired ?: 0,
    rewardType = when (rewardType.orEmpty()) {
        "GIFT_ITEM" -> RewardType.FREE_PRODUCT
        else -> kotlin.runCatching { RewardType.valueOf(rewardType.orEmpty()) }.getOrElse { RewardType.DISCOUNT_COUPON }
    },
    imageUri = imageUri,
    expirationDate = expirationDate?.let { LocalDate.parse(it.take(10)) },
    usageLimit = usageLimit ?: 0,
    inventoryTracked = inventoryTracked ?: false,
    availableQuantity = availableQuantity,
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

private fun ProgramRewardOutcome.toDto() = com.vector.verevcodex.data.remote.api.loyalty.ProgramRewardOutcomeDto(
    type = type.name,
    label = label,
    pointsAmount = pointsAmount,
    rewardId = rewardId,
    rewardName = rewardName,
    rewardType = rewardType?.name,
    programId = programId,
    programName = programName,
)

private fun com.vector.verevcodex.data.remote.api.loyalty.ProgramRewardOutcomeDto?.toDomainOutcome(
    fallbackPoints: Int,
    fallbackLabel: String,
): ProgramRewardOutcome {
    val dto = this
    val resolvedType = ProgramRewardOutcomeType.fromApi(dto?.type)
    return ProgramRewardOutcome(
        type = resolvedType,
        label = dto?.label.orEmpty().ifBlank { fallbackLabel },
        pointsAmount = dto?.pointsAmount ?: fallbackPoints,
        rewardId = dto?.rewardId,
        rewardName = dto?.rewardName.orEmpty(),
        rewardType = dto?.rewardType?.let { kotlin.runCatching { RewardType.valueOf(it) }.getOrNull() },
        programId = dto?.programId,
        programName = dto?.programName.orEmpty(),
    )
}
