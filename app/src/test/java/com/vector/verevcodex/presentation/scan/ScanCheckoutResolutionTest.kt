package com.vector.verevcodex.presentation.scan

import com.vector.verevcodex.domain.model.common.CouponBenefitType
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.domain.model.common.RewardCatalogType
import com.vector.verevcodex.domain.model.common.RewardType
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.loyalty.PointsProgramRule
import com.vector.verevcodex.domain.model.loyalty.ProgramRepeatType
import com.vector.verevcodex.domain.model.loyalty.ProgramRewardOutcome
import com.vector.verevcodex.domain.model.loyalty.ProgramRewardOutcomeType
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramConfigurationFactory
import com.vector.verevcodex.domain.model.loyalty.TierBenefitType
import com.vector.verevcodex.domain.model.loyalty.TierLevelRule
import com.vector.verevcodex.domain.model.loyalty.TierProgramRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ScanCheckoutResolutionTest {

    @Test
    fun `preview includes welcome bonus and projected tier reward`() {
        val customer = testCustomer(
            currentPoints = 90,
            totalSpent = 100.0,
            totalVisits = 0,
            tierLabel = "Bronze",
        )
        val pointsProgram = pointsProgram(
            pointsRule = PointsProgramRule(
                spendStepAmount = 10,
                pointsAwardedPerStep = 1,
                welcomeBonusPoints = 50,
                minimumRedeemPoints = 20,
            ),
        )
        val tierProgram = tierProgram(
            tierRule = TierProgramRule(
                levels = listOf(
                    TierLevelRule(
                        id = "bronze",
                        name = "Bronze",
                        threshold = 0,
                        benefitType = TierBenefitType.BONUS_PERCENT,
                        bonusPercent = 0,
                    ),
                    TierLevelRule(
                        id = "silver",
                        name = "Silver",
                        threshold = 100,
                        benefitType = TierBenefitType.BONUS_PERCENT,
                        bonusPercent = 5,
                        rewardOutcome = ProgramRewardOutcome(
                            type = ProgramRewardOutcomeType.DISCOUNT_COUPON,
                            rewardName = "Silver coupon",
                        ),
                    ),
                ),
            ),
        )

        val preview = computeScanCheckoutPreview(
            customer = customer,
            activePrograms = listOf(pointsProgram, tierProgram),
            availableCoupons = emptyList(),
            amountInput = "100",
            useBenefits = false,
            spendMode = ScanSpendMode.POINTS,
            pointsInput = "",
            selectedCouponId = null,
        )

        assertEquals(10, preview.basePoints)
        assertEquals(50, preview.welcomeBonusPoints)
        assertEquals(0, preview.tierBonusPoints)
        assertEquals(60, preview.totalEarnedPoints)
        assertEquals("Silver", preview.projectedTierLabel)
        assertTrue(preview.projectedBenefits.any { it.title == "Welcome bonus" })
        assertTrue(preview.projectedBenefits.any { it.detail.contains("Silver coupon") })
    }

    @Test
    fun `coupon mode applies discount and point cost`() {
        val customer = testCustomer(
            currentPoints = 200,
            totalSpent = 300.0,
            totalVisits = 4,
            tierLabel = "Silver",
        )
        val pointsProgram = pointsProgram()
        val tierProgram = tierProgram(
            tierRule = TierProgramRule(
                levels = listOf(
                    TierLevelRule(
                        id = "bronze",
                        name = "Bronze",
                        threshold = 0,
                        benefitType = TierBenefitType.BONUS_PERCENT,
                        bonusPercent = 0,
                    ),
                    TierLevelRule(
                        id = "silver",
                        name = "Silver",
                        threshold = 100,
                        benefitType = TierBenefitType.DISCOUNT_PERCENT,
                        bonusPercent = 10,
                    ),
                ),
            ),
        )
        val coupon = Reward(
            id = "coupon-1",
            storeId = "store-1",
            name = "20% Coupon",
            description = "",
            pointsRequired = 40,
            rewardType = RewardType.DISCOUNT_COUPON,
            imageUri = null,
            expirationDate = LocalDate.now().plusDays(10),
            usageLimit = 1,
            inventoryTracked = false,
            availableQuantity = null,
            activeStatus = true,
            catalogType = RewardCatalogType.COUPON,
            couponCode = "SAVE20",
            couponBenefitType = CouponBenefitType.DISCOUNT_PERCENT,
            couponDiscountPercent = 20.0,
        )

        val preview = computeScanCheckoutPreview(
            customer = customer,
            activePrograms = listOf(pointsProgram, tierProgram),
            availableCoupons = listOf(coupon),
            amountInput = "1000",
            useBenefits = true,
            spendMode = ScanSpendMode.COUPON,
            pointsInput = "",
            selectedCouponId = coupon.id,
        )

        assertEquals(10, preview.tierDiscountPercent)
        assertEquals(100.0, preview.tierDiscountAmount, 0.001)
        assertEquals(20, preview.couponDiscountPercent)
        assertEquals(180.0, preview.couponDiscountAmount, 0.001)
        assertEquals(720.0, preview.finalAmount, 0.001)
        assertEquals(40, preview.rewardPointsCost)
        assertEquals(167, preview.projectedPointsBalance)
    }

    private fun testCustomer(
        currentPoints: Int,
        totalSpent: Double,
        totalVisits: Int,
        tierLabel: String,
    ) = Customer(
        id = "customer-1",
        firstName = "John",
        lastName = "Doe",
        phoneNumber = "+374000000",
        email = "john@example.com",
        birthDate = LocalDate.parse("1990-01-01"),
        loyaltyId = "LOYAL-1",
        enrolledDate = LocalDate.parse("2026-01-01"),
        totalVisits = totalVisits,
        totalSpent = totalSpent,
        currentPoints = currentPoints,
        loyaltyTier = LoyaltyTier.BRONZE,
        loyaltyTierLabel = tierLabel,
        lastVisit = null,
        favoriteStoreId = null,
        gender = null,
    )

    private fun pointsProgram(pointsRule: PointsProgramRule = PointsProgramRule()) = RewardProgram(
        id = "points-program",
        storeId = "store-1",
        name = "Points",
        description = "",
        type = LoyaltyProgramType.POINTS,
        rulesSummary = "",
        active = true,
        autoScheduleEnabled = false,
        scheduleStartDate = null,
        scheduleEndDate = null,
        annualRepeatEnabled = false,
        repeatType = ProgramRepeatType.NONE,
        configuration = RewardProgramConfigurationFactory.defaultFor(
            type = LoyaltyProgramType.POINTS,
            active = true,
        ).copy(pointsRule = pointsRule),
    )

    private fun tierProgram(tierRule: TierProgramRule) = RewardProgram(
        id = "tier-program",
        storeId = "store-1",
        name = "Tier",
        description = "",
        type = LoyaltyProgramType.TIER,
        rulesSummary = "",
        active = true,
        autoScheduleEnabled = false,
        scheduleStartDate = null,
        scheduleEndDate = null,
        annualRepeatEnabled = false,
        repeatType = ProgramRepeatType.NONE,
        configuration = RewardProgramConfigurationFactory.defaultFor(
            type = LoyaltyProgramType.TIER,
            active = true,
        ).copy(tierRule = tierRule),
    )
}
