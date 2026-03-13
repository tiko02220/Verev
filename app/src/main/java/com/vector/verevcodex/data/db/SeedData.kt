package com.vector.verevcodex.data.db

import com.vector.verevcodex.data.db.entity.loyalty.CampaignEntity
import com.vector.verevcodex.data.db.entity.loyalty.CampaignTargetEntity
import com.vector.verevcodex.data.db.entity.settings.BillingInvoiceEntity
import com.vector.verevcodex.data.db.entity.settings.BranchConfigurationEntity
import com.vector.verevcodex.data.db.entity.settings.BrandingSettingsEntity
import com.vector.verevcodex.data.db.entity.customer.CustomerBusinessRelationEntity
import com.vector.verevcodex.data.db.entity.customer.CustomerCredentialEntity
import com.vector.verevcodex.data.db.entity.customer.CustomerEntity
import com.vector.verevcodex.data.db.entity.loyalty.NotificationEntity
import com.vector.verevcodex.data.db.entity.business.OwnerEntity
import com.vector.verevcodex.data.db.entity.loyalty.PointsLedgerEntity
import com.vector.verevcodex.data.db.entity.loyalty.RewardEntity
import com.vector.verevcodex.data.db.entity.loyalty.RewardProgramEntity
import com.vector.verevcodex.data.db.entity.settings.SavedPaymentMethodEntity
import com.vector.verevcodex.data.db.entity.business.StaffMemberEntity
import com.vector.verevcodex.data.db.entity.business.StoreEntity
import com.vector.verevcodex.data.db.entity.settings.SubscriptionPlanEntity
import com.vector.verevcodex.data.db.entity.loyalty.TransactionEntity
import com.vector.verevcodex.data.db.entity.loyalty.TransactionItemEntity
import com.vector.verevcodex.data.db.entity.auth.AuthAccountEntity
import java.time.LocalDate
import java.time.LocalDateTime

object SeedData {
    val owner = OwnerEntity(
        id = "11111111-1111-1111-1111-111111111111",
        firstName = "Test",
        lastName = "Owner",
        email = "owner@verevcrm.local",
        phoneNumber = "+37499000000",
    )

    val stores = listOf(
        StoreEntity("22222222-2222-2222-2222-222222222221", owner.id, "Bean & Bloom", "12 Abovyan St", "+37410111222", "Coffee Shop", "08:00 - 22:00", "", "#9C6B3F", "#F3E7D8", true),
        StoreEntity("22222222-2222-2222-2222-222222222222", owner.id, "Luna Salon", "44 Komitas Ave", "+37410444555", "Salon", "10:00 - 20:00", "", "#1C3D5A", "#D9EEF7", true),
        StoreEntity("22222222-2222-2222-2222-222222222223", owner.id, "North Retail", "7 Northern Ave", "+37410666777", "Retail", "09:00 - 21:00", "", "#275D38", "#E5F3E8", false),
    )

    val staff = listOf(
        StaffMemberEntity("33333333-3333-3333-3333-333333333331", stores[0].id, "Aram", "Hakobyan", "aram@bean.local", "+37493000001", "OWNER", true, "All stores, all permissions"),
        StaffMemberEntity("33333333-3333-3333-3333-333333333332", stores[0].id, "Mariam", "Petrosyan", "mariam@bean.local", "+37493000002", "STORE_MANAGER", true, "Store analytics, campaigns, staff"),
        StaffMemberEntity("33333333-3333-3333-3333-333333333333", stores[0].id, "Narek", "Sahakyan", "narek@bean.local", "+37493000003", "CASHIER", true, "Scan cards, create transactions"),
        StaffMemberEntity("33333333-3333-3333-3333-333333333334", stores[1].id, "Lilit", "Avetisyan", "lilit@luna.local", "+37493000004", "STORE_MANAGER", true, "Programs, rewards, customers"),
    )

    val customers = listOf(
        CustomerEntity("44444444-4444-4444-4444-444444444441", "Anna", "Sargsyan", "+37491112233", "anna@example.com", "VRV-04AABBCC", LocalDate.now().minusMonths(6).toString(), 18, 142000.0, 380, "GOLD", LocalDateTime.now().minusDays(2).toString(), stores[0].id),
        CustomerEntity("44444444-4444-4444-4444-444444444442", "David", "Mkrtchyan", "+37493111223", "david@example.com", "VRV-04DD0011", LocalDate.now().minusMonths(2).toString(), 7, 56000.0, 120, "SILVER", LocalDateTime.now().minusDays(4).toString(), stores[0].id),
        CustomerEntity("44444444-4444-4444-4444-444444444443", "Sona", "Karapetyan", "+37494123456", "sona@example.com", "VRV-04EE2299", LocalDate.now().minusMonths(4).toString(), 10, 88000.0, 65, "BRONZE", LocalDateTime.now().minusDays(8).toString(), stores[1].id),
    )

    val customerRelations = listOf(
        CustomerBusinessRelationEntity("55555555-5555-5555-5555-555555555551", customers[0].id, stores[0].id, LocalDateTime.now().minusMonths(6).toString(), "Prefers oat milk"),
        CustomerBusinessRelationEntity("55555555-5555-5555-5555-555555555552", customers[1].id, stores[0].id, LocalDateTime.now().minusMonths(2).toString(), "Lunch rush customer"),
        CustomerBusinessRelationEntity("55555555-5555-5555-5555-555555555553", customers[2].id, stores[1].id, LocalDateTime.now().minusMonths(4).toString(), "Hair color membership"),
    )

    val customerCredentials = listOf(
        CustomerCredentialEntity("cred-1-barcode", customers[0].id, customers[0].loyaltyId, "BARCODE_IMAGE", "LINKED", customers[0].loyaltyId, LocalDateTime.now().minusMonths(6).toString()),
        CustomerCredentialEntity("cred-1-wallet", customers[0].id, customers[0].loyaltyId, "GOOGLE_WALLET", "AVAILABLE", null, LocalDateTime.now().minusMonths(6).toString()),
        CustomerCredentialEntity("cred-1-nfc", customers[0].id, customers[0].loyaltyId, "NFC_CARD", "LINKED", customers[0].loyaltyId, LocalDateTime.now().minusMonths(6).toString()),
        CustomerCredentialEntity("cred-2-barcode", customers[1].id, customers[1].loyaltyId, "BARCODE_IMAGE", "LINKED", customers[1].loyaltyId, LocalDateTime.now().minusMonths(2).toString()),
        CustomerCredentialEntity("cred-2-wallet", customers[1].id, customers[1].loyaltyId, "GOOGLE_WALLET", "AVAILABLE", null, LocalDateTime.now().minusMonths(2).toString()),
        CustomerCredentialEntity("cred-2-nfc", customers[1].id, customers[1].loyaltyId, "NFC_CARD", "AVAILABLE", null, LocalDateTime.now().minusMonths(2).toString()),
        CustomerCredentialEntity("cred-3-barcode", customers[2].id, customers[2].loyaltyId, "BARCODE_IMAGE", "LINKED", customers[2].loyaltyId, LocalDateTime.now().minusMonths(4).toString()),
        CustomerCredentialEntity("cred-3-wallet", customers[2].id, customers[2].loyaltyId, "GOOGLE_WALLET", "AVAILABLE", null, LocalDateTime.now().minusMonths(4).toString()),
        CustomerCredentialEntity("cred-3-nfc", customers[2].id, customers[2].loyaltyId, "NFC_CARD", "AVAILABLE", null, LocalDateTime.now().minusMonths(4).toString()),
    )

    val programs = listOf(
        RewardProgramEntity(
            id = "66666666-6666-6666-6666-666666666661",
            storeId = stores[0].id,
            name = "Bean Points",
            description = "1 point per 100 AMD spent",
            type = "POINTS",
            rulesSummary = "1 point per 100 AMD, redeem from 50 points",
            active = true,
            earningEnabled = true,
            rewardRedemptionEnabled = true,
            visitCheckInEnabled = false,
            cashbackEnabled = false,
            tierTrackingEnabled = false,
            couponEnabled = false,
            purchaseFrequencyEnabled = false,
            referralEnabled = false,
            pointsSpendStepAmount = 100,
            pointsAwardedPerStep = 1,
            pointsWelcomeBonus = 25,
            pointsMinimumRedeem = 50,
            cashbackPercent = 0.0,
            cashbackMinimumSpendAmount = 0.0,
            tierSilverThreshold = 250,
            tierGoldThreshold = 500,
            tierVipThreshold = 1000,
            tierBonusPercent = 10,
            couponName = "",
            couponPointsCost = 0,
            couponDiscountAmount = 0.0,
            couponMinimumSpendAmount = 0.0,
            checkInVisitsRequired = 5,
            checkInRewardPoints = 25,
            checkInRewardName = "Check-in reward",
            purchaseFrequencyCount = 5,
            purchaseFrequencyWindowDays = 30,
            purchaseFrequencyRewardPoints = 50,
            purchaseFrequencyRewardName = "Repeat purchase reward",
            referralReferrerRewardPoints = 50,
            referralRefereeRewardPoints = 25,
            referralCodePrefix = "BEAN",
        ),
        RewardProgramEntity(
            id = "66666666-6666-6666-6666-666666666662",
            storeId = stores[1].id,
            name = "Luna Tier Club",
            description = "Tiered salon perks for repeat clients",
            type = "TIER",
            rulesSummary = "Silver 250, Gold 500, VIP 1000",
            active = true,
            earningEnabled = true,
            rewardRedemptionEnabled = false,
            visitCheckInEnabled = false,
            cashbackEnabled = false,
            tierTrackingEnabled = true,
            couponEnabled = false,
            purchaseFrequencyEnabled = false,
            referralEnabled = false,
            pointsSpendStepAmount = 100,
            pointsAwardedPerStep = 1,
            pointsWelcomeBonus = 0,
            pointsMinimumRedeem = 100,
            cashbackPercent = 0.0,
            cashbackMinimumSpendAmount = 0.0,
            tierSilverThreshold = 250,
            tierGoldThreshold = 500,
            tierVipThreshold = 1000,
            tierBonusPercent = 15,
            couponName = "",
            couponPointsCost = 0,
            couponDiscountAmount = 0.0,
            couponMinimumSpendAmount = 0.0,
            checkInVisitsRequired = 5,
            checkInRewardPoints = 20,
            checkInRewardName = "Visit bonus",
            purchaseFrequencyCount = 4,
            purchaseFrequencyWindowDays = 30,
            purchaseFrequencyRewardPoints = 40,
            purchaseFrequencyRewardName = "Priority treatment bonus",
            referralReferrerRewardPoints = 60,
            referralRefereeRewardPoints = 30,
            referralCodePrefix = "LUNA",
        ),
        RewardProgramEntity(
            id = "66666666-6666-6666-6666-666666666663",
            storeId = stores[0].id,
            name = "Weekend Check-in",
            description = "Visit rewards for weekend regulars",
            type = "DIGITAL_STAMP",
            rulesSummary = "Reward after 8 weekend visits",
            active = true,
            earningEnabled = false,
            rewardRedemptionEnabled = false,
            visitCheckInEnabled = true,
            cashbackEnabled = false,
            tierTrackingEnabled = false,
            couponEnabled = false,
            purchaseFrequencyEnabled = false,
            referralEnabled = false,
            pointsSpendStepAmount = 100,
            pointsAwardedPerStep = 1,
            pointsWelcomeBonus = 0,
            pointsMinimumRedeem = 50,
            cashbackPercent = 0.0,
            cashbackMinimumSpendAmount = 0.0,
            tierSilverThreshold = 250,
            tierGoldThreshold = 500,
            tierVipThreshold = 1000,
            tierBonusPercent = 10,
            couponName = "",
            couponPointsCost = 0,
            couponDiscountAmount = 0.0,
            couponMinimumSpendAmount = 0.0,
            checkInVisitsRequired = 8,
            checkInRewardPoints = 40,
            checkInRewardName = "Free pastry reward",
            purchaseFrequencyCount = 5,
            purchaseFrequencyWindowDays = 30,
            purchaseFrequencyRewardPoints = 50,
            purchaseFrequencyRewardName = "Repeat purchase reward",
            referralReferrerRewardPoints = 50,
            referralRefereeRewardPoints = 25,
            referralCodePrefix = "STAMP",
        ),
        RewardProgramEntity(
            id = "66666666-6666-6666-6666-666666666664",
            storeId = stores[0].id,
            name = "Bean Coupon Drop",
            description = "Points coupons for frequent cafe buyers",
            type = "COUPON",
            rulesSummary = "Coffee coupon for 120 points",
            active = true,
            earningEnabled = false,
            rewardRedemptionEnabled = true,
            visitCheckInEnabled = false,
            cashbackEnabled = false,
            tierTrackingEnabled = false,
            couponEnabled = true,
            purchaseFrequencyEnabled = false,
            referralEnabled = false,
            pointsSpendStepAmount = 100,
            pointsAwardedPerStep = 1,
            pointsWelcomeBonus = 0,
            pointsMinimumRedeem = 120,
            cashbackPercent = 0.0,
            cashbackMinimumSpendAmount = 0.0,
            tierSilverThreshold = 250,
            tierGoldThreshold = 500,
            tierVipThreshold = 1000,
            tierBonusPercent = 10,
            couponName = "Free coffee coupon",
            couponPointsCost = 120,
            couponDiscountAmount = 1500.0,
            couponMinimumSpendAmount = 4500.0,
            checkInVisitsRequired = 5,
            checkInRewardPoints = 25,
            checkInRewardName = "Check-in reward",
            purchaseFrequencyCount = 5,
            purchaseFrequencyWindowDays = 30,
            purchaseFrequencyRewardPoints = 50,
            purchaseFrequencyRewardName = "Repeat purchase reward",
            referralReferrerRewardPoints = 50,
            referralRefereeRewardPoints = 25,
            referralCodePrefix = "COFFEE",
        ),
        RewardProgramEntity(
            id = "66666666-6666-6666-6666-666666666665",
            storeId = stores[1].id,
            name = "Salon Repeat Visits",
            description = "Reward repeat purchase frequency",
            type = "PURCHASE_FREQUENCY",
            rulesSummary = "5 bookings in 30 days unlock bonus",
            active = true,
            earningEnabled = true,
            rewardRedemptionEnabled = false,
            visitCheckInEnabled = false,
            cashbackEnabled = false,
            tierTrackingEnabled = false,
            couponEnabled = false,
            purchaseFrequencyEnabled = true,
            referralEnabled = false,
            pointsSpendStepAmount = 100,
            pointsAwardedPerStep = 1,
            pointsWelcomeBonus = 0,
            pointsMinimumRedeem = 100,
            cashbackPercent = 0.0,
            cashbackMinimumSpendAmount = 0.0,
            tierSilverThreshold = 250,
            tierGoldThreshold = 500,
            tierVipThreshold = 1000,
            tierBonusPercent = 10,
            couponName = "",
            couponPointsCost = 0,
            couponDiscountAmount = 0.0,
            couponMinimumSpendAmount = 0.0,
            checkInVisitsRequired = 5,
            checkInRewardPoints = 25,
            checkInRewardName = "Check-in reward",
            purchaseFrequencyCount = 5,
            purchaseFrequencyWindowDays = 30,
            purchaseFrequencyRewardPoints = 60,
            purchaseFrequencyRewardName = "Free treatment booster",
            referralReferrerRewardPoints = 50,
            referralRefereeRewardPoints = 25,
            referralCodePrefix = "REPEAT",
        ),
        RewardProgramEntity(
            id = "66666666-6666-6666-6666-666666666666",
            storeId = stores[0].id,
            name = "Friends Bring Friends",
            description = "Referral rewards for both customers",
            type = "REFERRAL",
            rulesSummary = "60 pts referrer / 30 pts referee",
            active = true,
            earningEnabled = false,
            rewardRedemptionEnabled = false,
            visitCheckInEnabled = false,
            cashbackEnabled = false,
            tierTrackingEnabled = false,
            couponEnabled = false,
            purchaseFrequencyEnabled = false,
            referralEnabled = true,
            pointsSpendStepAmount = 100,
            pointsAwardedPerStep = 1,
            pointsWelcomeBonus = 0,
            pointsMinimumRedeem = 100,
            cashbackPercent = 0.0,
            cashbackMinimumSpendAmount = 0.0,
            tierSilverThreshold = 250,
            tierGoldThreshold = 500,
            tierVipThreshold = 1000,
            tierBonusPercent = 10,
            couponName = "",
            couponPointsCost = 0,
            couponDiscountAmount = 0.0,
            couponMinimumSpendAmount = 0.0,
            checkInVisitsRequired = 5,
            checkInRewardPoints = 25,
            checkInRewardName = "Check-in reward",
            purchaseFrequencyCount = 5,
            purchaseFrequencyWindowDays = 30,
            purchaseFrequencyRewardPoints = 50,
            purchaseFrequencyRewardName = "Repeat purchase reward",
            referralReferrerRewardPoints = 60,
            referralRefereeRewardPoints = 30,
            referralCodePrefix = "FRIEND",
        ),
    )

    val rewards = listOf(
        RewardEntity("77777777-7777-7777-7777-777777777771", stores[0].id, "Free Cappuccino", "Any medium size coffee", 150, "FREE_PRODUCT", LocalDate.now().plusMonths(1).toString(), 100, true),
        RewardEntity("77777777-7777-7777-7777-777777777772", stores[0].id, "15% Pastry Discount", "Discount coupon", 90, "DISCOUNT_COUPON", LocalDate.now().plusWeeks(2).toString(), 300, true),
        RewardEntity("77777777-7777-7777-7777-777777777773", stores[1].id, "Free Hair Mask", "Gift item with premium service", 180, "GIFT_ITEM", LocalDate.now().plusMonths(2).toString(), 40, true),
    )

    val campaigns = listOf(
        CampaignEntity(
            "88888888-8888-8888-8888-888888888881",
            stores[0].id,
            "Double Points Weekend",
            "Weekend multiplier for coffee purchases",
            LocalDate.now().minusDays(3).toString(),
            LocalDate.now().plusDays(12).toString(),
            "POINTS_MULTIPLIER",
            2.0,
            "WEEKEND2X",
            false,
            true,
        ),
        CampaignEntity(
            "88888888-8888-8888-8888-888888888882",
            stores[1].id,
            "VIP Birthday Glow",
            "Birthday bonus for VIP salon members",
            LocalDate.now().minusDays(10).toString(),
            LocalDate.now().plusDays(20).toString(),
            "BONUS_POINTS",
            120.0,
            "BIRTHDAYVIP",
            false,
            true,
        ),
        CampaignEntity(
            "88888888-8888-8888-8888-888888888883",
            stores[0].id,
            "Lunch Combo Discount",
            "Flat discount at checkout for lunch combos",
            LocalDate.now().minusDays(1).toString(),
            LocalDate.now().plusDays(14).toString(),
            "FIXED_DISCOUNT",
            1500.0,
            "LUNCH1500",
            true,
            true,
        ),
        CampaignEntity(
            "88888888-8888-8888-8888-888888888884",
            stores[1].id,
            "Color Service Flash Sale",
            "Percentage discount on color services this week",
            LocalDate.now().plusDays(2).toString(),
            LocalDate.now().plusDays(9).toString(),
            "PERCENT_DISCOUNT",
            12.0,
            "COLOR12",
            true,
            true,
        ),
    )

    val campaignTargets = listOf(
        CampaignTargetEntity("99999999-9999-9999-9999-999999999991", campaigns[0].id, "FREQUENT_VISITORS", "Customers with 5+ visits in 30 days"),
        CampaignTargetEntity("99999999-9999-9999-9999-999999999992", campaigns[1].id, "TIER_MEMBERS", "Gold and VIP members with birthdays this month"),
        CampaignTargetEntity("99999999-9999-9999-9999-999999999993", campaigns[2].id, "HIGH_SPENDERS", "Customers with high lunchtime spend"),
        CampaignTargetEntity("99999999-9999-9999-9999-999999999994", campaigns[3].id, "INACTIVE_CUSTOMERS", "Customers returning after inactive period"),
    )

    val transactions = listOf(
        TransactionEntity("aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1", customers[0].id, stores[0].id, staff[2].id, 12500.0, 125, 0, LocalDateTime.now().minusHours(5).toString(), "Latte + pastry", true),
        TransactionEntity("aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2", customers[1].id, stores[0].id, staff[2].id, 8400.0, 84, 20, LocalDateTime.now().minusDays(1).toString(), "Flat white + beans", true),
        TransactionEntity("aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3", customers[2].id, stores[1].id, staff[3].id, 25500.0, 90, 0, LocalDateTime.now().minusDays(2).toString(), "Hair color service", true),
    )

    val transactionItems = listOf(
        TransactionItemEntity("item-1", transactions[0].id, "Latte", 2, 3500.0),
        TransactionItemEntity("item-2", transactions[0].id, "Croissant", 1, 5500.0),
        TransactionItemEntity("item-3", transactions[1].id, "Flat White", 1, 2400.0),
        TransactionItemEntity("item-4", transactions[1].id, "Coffee Beans", 1, 6000.0),
        TransactionItemEntity("item-5", transactions[2].id, "Hair Coloring", 1, 25500.0),
    )

    val ledger = listOf(
        PointsLedgerEntity("ledger-1", customers[0].id, transactions[0].id, 125, "Purchase points", LocalDateTime.now().minusHours(5).toString()),
        PointsLedgerEntity("ledger-2", customers[1].id, transactions[1].id, 64, "Purchase points after redemption", LocalDateTime.now().minusDays(1).toString()),
        PointsLedgerEntity("ledger-3", customers[2].id, transactions[2].id, 90, "Service points", LocalDateTime.now().minusDays(2).toString()),
    )

    val notifications = listOf(
        NotificationEntity("notify-1", stores[0].id, "Campaign live", "Double Points Weekend is active now.", "CAMPAIGN", LocalDateTime.now().minusHours(1).toString()),
        NotificationEntity("notify-2", stores[1].id, "Reward stock", "Free Hair Mask reward inventory is low.", "SYSTEM", LocalDateTime.now().minusDays(1).toString()),
    )

    val authAccounts = listOf(
        AuthAccountEntity("auth-owner-1", owner.id, "Test Owner", "owner@verevcrm.local", owner.phoneNumber, "", "12345678", "OWNER", true),
        AuthAccountEntity("auth-manager-1", staff[1].id, "Mariam Petrosyan", "manager@gmail.com", staff[1].phoneNumber, "", "12345678", "STORE_MANAGER", true),
        AuthAccountEntity("auth-staff-1", staff[2].id, "Narek Sahakyan", "staff@gmail.com", staff[2].phoneNumber, "", "12345678", "STAFF", true),
    )

    val brandingSettings = listOf(
        BrandingSettingsEntity(stores[0].id, "golden_hour", "LIGHT", "#BB8A52"),
        BrandingSettingsEntity(stores[1].id, "forest_mark", "AUTO", "#FFBA00"),
        BrandingSettingsEntity(stores[2].id, "espresso", "LIGHT", "#FFBA00"),
    )

    val subscriptionPlans = listOf(
        SubscriptionPlanEntity("plan-owner-1", owner.id, "Premium Plan", 99.0, "USD", LocalDate.now().plusMonths(1).toString(), true),
    )

    val paymentMethods = emptyList<SavedPaymentMethodEntity>()

    val billingInvoices = listOf(
        BillingInvoiceEntity("inv_2026_02", owner.id, "Premium plan", "February 2026", 99.0, "USD", "PAID", LocalDate.now().minusMonths(1).toString()),
        BillingInvoiceEntity("inv_2026_01", owner.id, "Premium plan", "January 2026", 99.0, "USD", "PAID", LocalDate.now().minusMonths(2).toString()),
        BillingInvoiceEntity("inv_2025_12", owner.id, "Premium plan", "December 2025", 99.0, "USD", "PAID", LocalDate.now().minusMonths(3).toString()),
    )

    val branchConfigurations = listOf(
        BranchConfigurationEntity(stores[0].id, true, true, true, false, true, "Thanks for visiting Bean & Bloom."),
        BranchConfigurationEntity(stores[1].id, true, true, true, true, true, "Luna Salon loyalty rewards are valid for 30 days."),
        BranchConfigurationEntity(stores[2].id, false, true, true, true, true, "North Retail branch currently in maintenance mode."),
    )
}
