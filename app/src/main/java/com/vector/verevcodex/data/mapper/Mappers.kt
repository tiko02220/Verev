package com.vector.verevcodex.data.mapper

import com.vector.verevcodex.data.db.entity.CampaignEntity
import com.vector.verevcodex.data.db.entity.CampaignTargetEntity
import com.vector.verevcodex.data.db.entity.CustomerCredentialEntity
import com.vector.verevcodex.data.db.entity.CustomerEntity
import com.vector.verevcodex.data.db.entity.OwnerEntity
import com.vector.verevcodex.data.db.entity.RewardEntity
import com.vector.verevcodex.data.db.entity.RewardProgramEntity
import com.vector.verevcodex.data.db.entity.StaffMemberEntity
import com.vector.verevcodex.data.db.entity.StoreEntity
import com.vector.verevcodex.data.db.entity.TransactionEntity
import com.vector.verevcodex.data.db.entity.TransactionItemEntity
import com.vector.verevcodex.domain.model.BusinessOwner
import com.vector.verevcodex.domain.model.Campaign
import com.vector.verevcodex.domain.model.CampaignSegment
import com.vector.verevcodex.domain.model.CampaignTarget
import com.vector.verevcodex.domain.model.Customer
import com.vector.verevcodex.domain.model.CustomerCredential
import com.vector.verevcodex.domain.model.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.CustomerCredentialStatus
import com.vector.verevcodex.domain.model.LoyaltyProgramType
import com.vector.verevcodex.domain.model.LoyaltyTier
import com.vector.verevcodex.domain.model.RewardProgramConfiguration
import com.vector.verevcodex.domain.model.RewardProgramConfigurationFactory
import com.vector.verevcodex.domain.model.RewardProgramScanAction
import com.vector.verevcodex.domain.model.Reward
import com.vector.verevcodex.domain.model.RewardProgram
import com.vector.verevcodex.domain.model.RewardType
import com.vector.verevcodex.domain.model.StaffMember
import com.vector.verevcodex.domain.model.StaffRole
import com.vector.verevcodex.domain.model.Store
import com.vector.verevcodex.domain.model.Transaction
import com.vector.verevcodex.domain.model.TransactionItem
import java.time.LocalDate
import java.time.LocalDateTime

fun OwnerEntity.toDomain() = BusinessOwner(id, firstName, lastName, email, phoneNumber)
fun StoreEntity.toDomain() = Store(id, ownerId, name, address, contactInfo, category, workingHours, logoUrl, primaryColor, secondaryColor, active)
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
fun RewardProgramEntity.toDomain(): RewardProgram {
    val programType = LoyaltyProgramType.valueOf(type)
    val configuration = if (active) {
        RewardProgramConfiguration(
            earningEnabled = earningEnabled,
            rewardRedemptionEnabled = rewardRedemptionEnabled,
            visitCheckInEnabled = visitCheckInEnabled,
            cashbackEnabled = cashbackEnabled,
            tierTrackingEnabled = tierTrackingEnabled,
            scanActions = buildSet {
                if (earningEnabled) add(RewardProgramScanAction.EARN_POINTS)
                if (rewardRedemptionEnabled) add(RewardProgramScanAction.REDEEM_REWARDS)
                if (visitCheckInEnabled) add(RewardProgramScanAction.CHECK_IN)
                if (cashbackEnabled) add(RewardProgramScanAction.APPLY_CASHBACK)
                if (tierTrackingEnabled) add(RewardProgramScanAction.TRACK_TIER_PROGRESS)
            },
        )
    } else {
        RewardProgramConfigurationFactory.defaultFor(programType, active = false)
    }
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
)
fun RewardEntity.toDomain() = Reward(id, storeId, name, description, pointsRequired, RewardType.valueOf(rewardType), expirationDate?.let(LocalDate::parse), usageLimit, activeStatus)
fun CampaignEntity.toDomain(target: CampaignTargetEntity) = Campaign(
    id = id,
    storeId = storeId,
    name = name,
    description = description,
    startDate = LocalDate.parse(startDate),
    endDate = LocalDate.parse(endDate),
    rewardMultiplier = rewardMultiplier,
    active = active,
    target = CampaignTarget(target.id, target.campaignId, CampaignSegment.valueOf(target.segment), target.description),
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
    items = items.map { TransactionItem(it.id, it.transactionId, it.name, it.quantity, it.unitPrice) },
)

fun Transaction.toEntity() = TransactionEntity(id, customerId, storeId, staffId, amount, pointsEarned, pointsRedeemed, timestamp.toString(), metadata)
fun TransactionItem.toEntity() = TransactionItemEntity(id, transactionId, name, quantity, unitPrice)

fun CustomerCredentialEntity.toDomain() = CustomerCredential(
    customerId = customerId,
    loyaltyId = loyaltyId,
    method = CustomerCredentialMethod.valueOf(method),
    status = CustomerCredentialStatus.valueOf(status),
    referenceValue = referenceValue,
    updatedAt = LocalDateTime.parse(updatedAt),
)
