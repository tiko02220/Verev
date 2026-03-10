package com.vector.verevcodex.domain.model

interface Identifiable {
    val id: String
}

enum class StaffRole { OWNER, STORE_MANAGER, CASHIER, STAFF }
enum class LoyaltyProgramType { POINTS, CASHBACK, DIGITAL_STAMP, TIER, HYBRID }
enum class RewardType { FREE_PRODUCT, DISCOUNT_COUPON, GIFT_ITEM, SPECIAL_PROMOTION }
enum class LoyaltyTier { BRONZE, SILVER, GOLD, VIP }
enum class CampaignSegment { FREQUENT_VISITORS, HIGH_SPENDERS, TIER_MEMBERS, INACTIVE_CUSTOMERS, HIGH_VALUE_CUSTOMERS }
enum class NotificationType { CAMPAIGN, REWARD, SYSTEM }
