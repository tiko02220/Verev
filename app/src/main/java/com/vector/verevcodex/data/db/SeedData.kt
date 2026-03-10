package com.vector.verevcodex.data.db

import com.vector.verevcodex.data.db.entity.CampaignEntity
import com.vector.verevcodex.data.db.entity.CampaignTargetEntity
import com.vector.verevcodex.data.db.entity.CustomerBusinessRelationEntity
import com.vector.verevcodex.data.db.entity.CustomerEntity
import com.vector.verevcodex.data.db.entity.NotificationEntity
import com.vector.verevcodex.data.db.entity.OwnerEntity
import com.vector.verevcodex.data.db.entity.PointsLedgerEntity
import com.vector.verevcodex.data.db.entity.RewardEntity
import com.vector.verevcodex.data.db.entity.RewardProgramEntity
import com.vector.verevcodex.data.db.entity.StaffMemberEntity
import com.vector.verevcodex.data.db.entity.StoreEntity
import com.vector.verevcodex.data.db.entity.TransactionEntity
import com.vector.verevcodex.data.db.entity.TransactionItemEntity
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
        CustomerEntity("44444444-4444-4444-4444-444444444441", "Anna", "Sargsyan", "+37491112233", "anna@example.com", "04AABBCC", LocalDate.now().minusMonths(6).toString(), 18, 142000.0, 380, "GOLD", LocalDateTime.now().minusDays(2).toString(), stores[0].id),
        CustomerEntity("44444444-4444-4444-4444-444444444442", "David", "Mkrtchyan", "+37493111223", "david@example.com", "04DD0011", LocalDate.now().minusMonths(2).toString(), 7, 56000.0, 120, "SILVER", LocalDateTime.now().minusDays(4).toString(), stores[0].id),
        CustomerEntity("44444444-4444-4444-4444-444444444443", "Sona", "Karapetyan", "+37494123456", "sona@example.com", "04EE2299", LocalDate.now().minusMonths(4).toString(), 10, 88000.0, 65, "BRONZE", LocalDateTime.now().minusDays(8).toString(), stores[1].id),
    )

    val customerRelations = listOf(
        CustomerBusinessRelationEntity("55555555-5555-5555-5555-555555555551", customers[0].id, stores[0].id, LocalDateTime.now().minusMonths(6).toString(), "Prefers oat milk"),
        CustomerBusinessRelationEntity("55555555-5555-5555-5555-555555555552", customers[1].id, stores[0].id, LocalDateTime.now().minusMonths(2).toString(), "Lunch rush customer"),
        CustomerBusinessRelationEntity("55555555-5555-5555-5555-555555555553", customers[2].id, stores[1].id, LocalDateTime.now().minusMonths(4).toString(), "Hair color membership"),
    )

    val programs = listOf(
        RewardProgramEntity("66666666-6666-6666-6666-666666666661", stores[0].id, "Bean Points", "1 point per 100 AMD", "POINTS", "1 point per 100 AMD, Gold bonus after 300 points", true),
        RewardProgramEntity("66666666-6666-6666-6666-666666666662", stores[1].id, "Luna Tier Club", "Tiered salon perks", "TIER", "Bronze/Silver/Gold/VIP with visit thresholds", true),
        RewardProgramEntity("66666666-6666-6666-6666-666666666663", stores[0].id, "Weekend Stamp", "Stamp every weekend visit", "DIGITAL_STAMP", "8 stamps unlock free pastry", true),
    )

    val rewards = listOf(
        RewardEntity("77777777-7777-7777-7777-777777777771", stores[0].id, "Free Cappuccino", "Any medium size coffee", 150, "FREE_PRODUCT", LocalDate.now().plusMonths(1).toString(), 100, true),
        RewardEntity("77777777-7777-7777-7777-777777777772", stores[0].id, "15% Pastry Discount", "Discount coupon", 90, "DISCOUNT_COUPON", LocalDate.now().plusWeeks(2).toString(), 300, true),
        RewardEntity("77777777-7777-7777-7777-777777777773", stores[1].id, "Free Hair Mask", "Gift item with premium service", 180, "GIFT_ITEM", LocalDate.now().plusMonths(2).toString(), 40, true),
    )

    val campaigns = listOf(
        CampaignEntity("88888888-8888-8888-8888-888888888881", stores[0].id, "Double Points Weekend", "Weekend multiplier for coffee purchases", LocalDate.now().minusDays(3).toString(), LocalDate.now().plusDays(12).toString(), 2.0, true),
        CampaignEntity("88888888-8888-8888-8888-888888888882", stores[1].id, "VIP Birthday Glow", "Birthday bonus for VIP salon members", LocalDate.now().minusDays(10).toString(), LocalDate.now().plusDays(20).toString(), 1.5, true),
    )

    val campaignTargets = listOf(
        CampaignTargetEntity("99999999-9999-9999-9999-999999999991", campaigns[0].id, "FREQUENT_VISITORS", "Customers with 5+ visits in 30 days"),
        CampaignTargetEntity("99999999-9999-9999-9999-999999999992", campaigns[1].id, "TIER_MEMBERS", "Gold and VIP members with birthdays this month"),
    )

    val transactions = listOf(
        TransactionEntity("aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1", customers[0].id, stores[0].id, staff[2].id, 12500.0, 125, 0, LocalDateTime.now().minusHours(5).toString(), "Latte + pastry"),
        TransactionEntity("aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2", customers[1].id, stores[0].id, staff[2].id, 8400.0, 84, 20, LocalDateTime.now().minusDays(1).toString(), "Flat white + beans"),
        TransactionEntity("aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3", customers[2].id, stores[1].id, staff[3].id, 25500.0, 90, 0, LocalDateTime.now().minusDays(2).toString(), "Hair color service"),
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
        AuthAccountEntity("auth-owner-1", owner.id, "Test Owner", "owner@verevcrm.local", owner.phoneNumber, "12345678", "OWNER", true),
        AuthAccountEntity("auth-manager-1", staff[1].id, "Mariam Petrosyan", "manager@gmail.com", staff[1].phoneNumber, "12345678", "STORE_MANAGER", true),
        AuthAccountEntity("auth-staff-1", staff[2].id, "Narek Sahakyan", "staff@gmail.com", staff[2].phoneNumber, "12345678", "STAFF", true),
    )
}
