package com.vector.verevcodex.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val storeId: String,
    val staffId: String,
    val amount: Double,
    val pointsEarned: Int,
    val pointsRedeemed: Int,
    val timestamp: String,
    val metadata: String,
)

@Entity(tableName = "transaction_items")
data class TransactionItemEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
)

@Entity(tableName = "reward_programs")
data class RewardProgramEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val name: String,
    val description: String,
    val type: String,
    val rulesSummary: String,
    val active: Boolean,
    val earningEnabled: Boolean,
    val rewardRedemptionEnabled: Boolean,
    val visitCheckInEnabled: Boolean,
    val cashbackEnabled: Boolean,
    val tierTrackingEnabled: Boolean,
)

@Entity(tableName = "rewards")
data class RewardEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val name: String,
    val description: String,
    val pointsRequired: Int,
    val rewardType: String,
    val expirationDate: String?,
    val usageLimit: Int,
    val activeStatus: Boolean,
)

@Entity(tableName = "points_ledger")
data class PointsLedgerEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val transactionId: String?,
    val delta: Int,
    val reason: String,
    val createdAt: String,
)

@Entity(tableName = "campaigns")
data class CampaignEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val name: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val rewardMultiplier: Double,
    val active: Boolean,
)

@Entity(tableName = "campaign_targets")
data class CampaignTargetEntity(
    @PrimaryKey val id: String,
    val campaignId: String,
    val segment: String,
    val description: String,
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val title: String,
    val message: String,
    val type: String,
    val createdAt: String,
)
