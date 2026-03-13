package com.vector.verevcodex.data.mapper

import com.vector.verevcodex.data.db.entity.loyalty.CampaignEntity
import com.vector.verevcodex.data.db.entity.loyalty.CampaignTargetEntity
import com.vector.verevcodex.data.db.entity.customer.CustomerBonusActionEntity
import com.vector.verevcodex.data.db.entity.customer.CustomerBusinessRelationEntity
import com.vector.verevcodex.data.db.entity.customer.CustomerCredentialEntity
import com.vector.verevcodex.data.db.entity.customer.CustomerEntity
import com.vector.verevcodex.data.db.entity.business.OwnerEntity
import com.vector.verevcodex.data.db.entity.loyalty.PointsLedgerEntity
import com.vector.verevcodex.data.db.entity.loyalty.RewardEntity
import com.vector.verevcodex.data.db.entity.loyalty.RewardProgramEntity
import com.vector.verevcodex.data.db.entity.business.StaffMemberEntity
import com.vector.verevcodex.data.db.entity.business.StoreEntity
import com.vector.verevcodex.data.db.entity.loyalty.TransactionEntity
import com.vector.verevcodex.data.db.entity.loyalty.TransactionItemEntity
import com.vector.verevcodex.domain.model.business.BusinessOwner
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.common.CampaignSegment
import com.vector.verevcodex.domain.model.promotions.CampaignTarget
import com.vector.verevcodex.domain.model.loyalty.CashbackProgramRule
import com.vector.verevcodex.domain.model.loyalty.CheckInProgramRule
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerBonusAction
import com.vector.verevcodex.domain.model.customer.CustomerBonusActionType
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.model.customer.CustomerCredential
import com.vector.verevcodex.domain.model.customer.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.customer.CustomerCredentialStatus
import com.vector.verevcodex.domain.model.loyalty.CouponProgramRule
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.domain.model.loyalty.PointsLedger
import com.vector.verevcodex.domain.model.loyalty.PointsProgramRule
import com.vector.verevcodex.domain.model.promotions.PromotionType
import com.vector.verevcodex.domain.model.loyalty.PurchaseFrequencyProgramRule
import com.vector.verevcodex.domain.model.loyalty.ReferralProgramRule
import com.vector.verevcodex.domain.model.loyalty.RewardProgramConfiguration
import com.vector.verevcodex.domain.model.loyalty.RewardProgramConfigurationFactory
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.common.RewardType
import com.vector.verevcodex.domain.model.business.StaffMember
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.model.loyalty.TierProgramRule
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.domain.model.transactions.TransactionItem
import java.time.LocalDate
import java.time.LocalDateTime

fun OwnerEntity.toDomain() = BusinessOwner(id, firstName, lastName, email, phoneNumber)
fun StoreEntity.toDomain() = Store(id, ownerId, name, address, contactInfo, category, workingHours, logoUrl, primaryColor, secondaryColor, active)
fun Store.toEntity() = StoreEntity(id, ownerId, name, address, contactInfo, category, workingHours, logoUrl, primaryColor, secondaryColor, active)
fun StaffMemberEntity.toDomain() = StaffMember(id, storeId, firstName, lastName, email, phoneNumber, StaffRole.valueOf(role), active, permissionsSummary)
fun CustomerEntity.toDomain() = Customer(
    id = id,
    firstName = firstName,
    lastName = lastName,
    phoneNumber = phoneNumber,
    email = email,
    loyaltyId = loyaltyId,
    enrolledDate = LocalDate.parse(enrolledDate),
    totalVisits = totalVisits,
    totalSpent = totalSpent,
    currentPoints = currentPoints,
    loyaltyTier = LoyaltyTier.valueOf(loyaltyTier),
    lastVisit = lastVisit?.let(LocalDateTime::parse),
    favoriteStoreId = favoriteStoreId,
)
fun CustomerBusinessRelationEntity.toDomain() = CustomerBusinessRelation(
    id = id,
    customerId = customerId,
    storeId = storeId,
    joinedAt = LocalDateTime.parse(joinedAt),
    notes = notes,
    tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
)
fun CustomerBonusActionEntity.toDomain() = CustomerBonusAction(
    id = id,
    customerId = customerId,
    storeId = storeId,
    type = CustomerBonusActionType.valueOf(type),
    title = title,
    details = details,
    createdAt = LocalDateTime.parse(createdAt),
)
fun RewardProgramEntity.toDomain(): RewardProgram {
    val programType = LoyaltyProgramType.valueOf(type)
    val scanActions = if (active) {
        buildSet {
            if (earningEnabled) add(RewardProgramScanAction.EARN_POINTS)
            if (rewardRedemptionEnabled || couponEnabled) add(RewardProgramScanAction.REDEEM_REWARDS)
            if (visitCheckInEnabled) add(RewardProgramScanAction.CHECK_IN)
            if (cashbackEnabled) add(RewardProgramScanAction.APPLY_CASHBACK)
            if (tierTrackingEnabled) add(RewardProgramScanAction.TRACK_TIER_PROGRESS)
        }
    } else {
        emptySet()
    }
    val configuration = RewardProgramConfiguration(
        earningEnabled = if (active) earningEnabled else false,
        rewardRedemptionEnabled = if (active) rewardRedemptionEnabled else false,
        visitCheckInEnabled = if (active) visitCheckInEnabled else false,
        cashbackEnabled = if (active) cashbackEnabled else false,
        tierTrackingEnabled = if (active) tierTrackingEnabled else false,
        couponEnabled = if (active) couponEnabled else false,
        purchaseFrequencyEnabled = if (active) purchaseFrequencyEnabled else false,
        referralEnabled = if (active) referralEnabled else false,
        scanActions = scanActions,
        pointsRule = PointsProgramRule(
            spendStepAmount = pointsSpendStepAmount,
            pointsAwardedPerStep = pointsAwardedPerStep,
            welcomeBonusPoints = pointsWelcomeBonus,
            minimumRedeemPoints = pointsMinimumRedeem,
        ),
        cashbackRule = CashbackProgramRule(
            cashbackPercent = cashbackPercent,
            minimumSpendAmount = cashbackMinimumSpendAmount,
        ),
        tierRule = TierProgramRule(
            silverThreshold = tierSilverThreshold,
            goldThreshold = tierGoldThreshold,
            vipThreshold = tierVipThreshold,
            tierBonusPercent = tierBonusPercent,
        ),
        couponRule = CouponProgramRule(
            couponName = couponName,
            pointsCost = couponPointsCost,
            discountAmount = couponDiscountAmount,
            minimumSpendAmount = couponMinimumSpendAmount,
        ),
        checkInRule = CheckInProgramRule(
            visitsRequired = checkInVisitsRequired,
            rewardPoints = checkInRewardPoints,
            rewardName = checkInRewardName,
        ),
        purchaseFrequencyRule = PurchaseFrequencyProgramRule(
            purchaseCount = purchaseFrequencyCount,
            windowDays = purchaseFrequencyWindowDays,
            rewardPoints = purchaseFrequencyRewardPoints,
            rewardName = purchaseFrequencyRewardName,
        ),
        referralRule = ReferralProgramRule(
            referrerRewardPoints = referralReferrerRewardPoints,
            refereeRewardPoints = referralRefereeRewardPoints,
            referralCodePrefix = referralCodePrefix,
        ),
    )
    return RewardProgram(
        id = id,
        storeId = storeId,
        name = name,
        description = description,
        type = programType,
        rulesSummary = rulesSummary,
        active = active,
        configuration = configuration,
    )
}
fun RewardProgram.toEntity() = RewardProgramEntity(
    id = id,
    storeId = storeId,
    name = name,
    description = description,
    type = type.name,
    rulesSummary = rulesSummary,
    active = active,
    earningEnabled = configuration.earningEnabled,
    rewardRedemptionEnabled = configuration.rewardRedemptionEnabled,
    visitCheckInEnabled = configuration.visitCheckInEnabled,
    cashbackEnabled = configuration.cashbackEnabled,
    tierTrackingEnabled = configuration.tierTrackingEnabled,
    couponEnabled = configuration.couponEnabled,
    purchaseFrequencyEnabled = configuration.purchaseFrequencyEnabled,
    referralEnabled = configuration.referralEnabled,
    pointsSpendStepAmount = configuration.pointsRule.spendStepAmount,
    pointsAwardedPerStep = configuration.pointsRule.pointsAwardedPerStep,
    pointsWelcomeBonus = configuration.pointsRule.welcomeBonusPoints,
    pointsMinimumRedeem = configuration.pointsRule.minimumRedeemPoints,
    cashbackPercent = configuration.cashbackRule.cashbackPercent,
    cashbackMinimumSpendAmount = configuration.cashbackRule.minimumSpendAmount,
    tierSilverThreshold = configuration.tierRule.silverThreshold,
    tierGoldThreshold = configuration.tierRule.goldThreshold,
    tierVipThreshold = configuration.tierRule.vipThreshold,
    tierBonusPercent = configuration.tierRule.tierBonusPercent,
    couponName = configuration.couponRule.couponName,
    couponPointsCost = configuration.couponRule.pointsCost,
    couponDiscountAmount = configuration.couponRule.discountAmount,
    couponMinimumSpendAmount = configuration.couponRule.minimumSpendAmount,
    checkInVisitsRequired = configuration.checkInRule.visitsRequired,
    checkInRewardPoints = configuration.checkInRule.rewardPoints,
    checkInRewardName = configuration.checkInRule.rewardName,
    purchaseFrequencyCount = configuration.purchaseFrequencyRule.purchaseCount,
    purchaseFrequencyWindowDays = configuration.purchaseFrequencyRule.windowDays,
    purchaseFrequencyRewardPoints = configuration.purchaseFrequencyRule.rewardPoints,
    purchaseFrequencyRewardName = configuration.purchaseFrequencyRule.rewardName,
    referralReferrerRewardPoints = configuration.referralRule.referrerRewardPoints,
    referralRefereeRewardPoints = configuration.referralRule.refereeRewardPoints,
    referralCodePrefix = configuration.referralRule.referralCodePrefix,
)
fun RewardEntity.toDomain() = Reward(id, storeId, name, description, pointsRequired, RewardType.valueOf(rewardType), expirationDate?.let(LocalDate::parse), usageLimit, activeStatus)
fun Reward.toEntity() = RewardEntity(
    id = id,
    storeId = storeId,
    name = name,
    description = description,
    pointsRequired = pointsRequired,
    rewardType = rewardType.name,
    expirationDate = expirationDate?.toString(),
    usageLimit = usageLimit,
    activeStatus = activeStatus,
)
fun CampaignEntity.toDomain(target: CampaignTargetEntity) = Campaign(
    id = id,
    storeId = storeId,
    name = name,
    description = description,
    startDate = LocalDate.parse(startDate),
    endDate = LocalDate.parse(endDate),
    promotionType = PromotionType.valueOf(promotionType),
    promotionValue = promotionValue,
    promoCode = promoCode,
    paymentFlowEnabled = paymentFlowEnabled,
    active = active,
    target = CampaignTarget(target.id, target.campaignId, CampaignSegment.valueOf(target.segment), target.description),
)

fun Campaign.toEntity() = CampaignEntity(
    id = id,
    storeId = storeId,
    name = name,
    description = description,
    startDate = startDate.toString(),
    endDate = endDate.toString(),
    promotionType = promotionType.name,
    promotionValue = promotionValue,
    promoCode = promoCode,
    paymentFlowEnabled = paymentFlowEnabled,
    active = active,
)
fun TransactionEntity.toDomain(items: List<TransactionItemEntity>) = Transaction(
    id = id,
    customerId = customerId,
    storeId = storeId,
    staffId = staffId,
    amount = amount,
    pointsEarned = pointsEarned,
    pointsRedeemed = pointsRedeemed,
    timestamp = LocalDateTime.parse(timestamp),
    metadata = metadata,
    countsAsVisit = countsAsVisit,
    items = items.map { TransactionItem(it.id, it.transactionId, it.name, it.quantity, it.unitPrice) },
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    customerId = customerId,
    storeId = storeId,
    staffId = staffId,
    amount = amount,
    pointsEarned = pointsEarned,
    pointsRedeemed = pointsRedeemed,
    timestamp = timestamp.toString(),
    metadata = metadata,
    countsAsVisit = countsAsVisit,
)
fun TransactionItem.toEntity() = TransactionItemEntity(id, transactionId, name, quantity, unitPrice)

fun CustomerCredentialEntity.toDomain() = CustomerCredential(
    customerId = customerId,
    loyaltyId = loyaltyId,
    method = CustomerCredentialMethod.valueOf(method),
    status = CustomerCredentialStatus.valueOf(status),
    referenceValue = referenceValue,
    updatedAt = LocalDateTime.parse(updatedAt),
)
fun PointsLedgerEntity.toDomain() = PointsLedger(
    id = id,
    customerId = customerId,
    transactionId = transactionId,
    delta = delta,
    reason = reason,
    createdAt = LocalDateTime.parse(createdAt),
)
