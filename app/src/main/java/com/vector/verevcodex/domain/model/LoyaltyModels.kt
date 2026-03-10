package com.vector.verevcodex.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class TransactionItem(
    override val id: String,
    val transactionId: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
) : Identifiable

data class Transaction(
    override val id: String,
    val customerId: String,
    val storeId: String,
    val staffId: String,
    val amount: Double,
    val pointsEarned: Int,
    val pointsRedeemed: Int,
    val timestamp: LocalDateTime,
    val metadata: String,
    val items: List<TransactionItem> = emptyList(),
) : Identifiable

data class RewardProgram(
    override val id: String,
    val storeId: String,
    val name: String,
    val description: String,
    val type: LoyaltyProgramType,
    val rulesSummary: String,
    val active: Boolean,
) : Identifiable

data class Reward(
    override val id: String,
    val storeId: String,
    val name: String,
    val description: String,
    val pointsRequired: Int,
    val rewardType: RewardType,
    val expirationDate: LocalDate?,
    val usageLimit: Int,
    val activeStatus: Boolean,
) : Identifiable

data class PointsLedger(
    override val id: String,
    val customerId: String,
    val transactionId: String?,
    val delta: Int,
    val reason: String,
    val createdAt: LocalDateTime,
) : Identifiable

data class CampaignTarget(
    override val id: String,
    val campaignId: String,
    val segment: CampaignSegment,
    val description: String,
) : Identifiable

data class Campaign(
    override val id: String,
    val storeId: String,
    val name: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val rewardMultiplier: Double,
    val active: Boolean,
    val target: CampaignTarget,
) : Identifiable

data class Notification(
    override val id: String,
    val storeId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val createdAt: LocalDateTime,
) : Identifiable
