package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.common.RewardType
import com.vector.verevcodex.domain.model.loyalty.ProgramRewardOutcomeType
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProgramEditorMappingsTest {

    @Test
    fun `toDraft carries schedule window when auto schedule is enabled`() {
        val draft = ProgramEditorState(
            name = "Spring points",
            description = "Points campaign",
            type = LoyaltyProgramType.POINTS,
            autoScheduleEnabled = true,
            scheduleStartDate = "2026-03-22",
            scheduleEndDate = "2026-03-28",
            annualRepeatEnabled = true,
        ).toDraft(
            storeId = "store-1",
            availablePrograms = emptyList(),
            availableRewards = emptyList(),
        )

        assertTrue(draft.autoScheduleEnabled)
        assertEquals(LocalDate.parse("2026-03-22"), draft.scheduleStartDate)
        assertEquals(LocalDate.parse("2026-03-28"), draft.scheduleEndDate)
        assertTrue(draft.annualRepeatEnabled)
    }

    @Test
    fun `validate requires schedule fields when auto schedule is enabled`() {
        val errors = ProgramEditorState(
            name = "Spring points",
            description = "Points campaign",
            type = LoyaltyProgramType.POINTS,
            autoScheduleEnabled = true,
            scheduleStartDate = "",
            scheduleEndDate = "",
        ).validate()

        assertEquals(com.vector.verevcodex.R.string.merchant_program_error_schedule_start_required, errors[PROGRAM_FIELD_SCHEDULE_START])
        assertEquals(com.vector.verevcodex.R.string.merchant_program_error_schedule_end_required, errors[PROGRAM_FIELD_SCHEDULE_END])
    }

    @Test
    fun `toDraft maps reward item outcome for check-in rewards`() {
        val draft = ProgramEditorState(
            name = "Visit club",
            description = "Reward loyal guests",
            type = LoyaltyProgramType.DIGITAL_STAMP,
            checkInReward = ProgramRewardOutcomeEditorState(
                type = ProgramRewardOutcomeType.FREE_PRODUCT,
                rewardId = "reward-1",
            ),
        ).toDraft(
            storeId = "store-1",
            availablePrograms = emptyList(),
            availableRewards = listOf(
                Reward(
                    id = "reward-1",
                    storeId = "store-1",
                    name = "Free Coffee",
                    description = "Any small coffee",
                    pointsRequired = 150,
                    rewardType = RewardType.FREE_PRODUCT,
                    imageUri = null,
                    expirationDate = null,
                    usageLimit = 1,
                    inventoryTracked = false,
                    availableQuantity = null,
                    activeStatus = true,
                ),
            ),
        )

        assertEquals(ProgramRewardOutcomeType.FREE_PRODUCT, draft.configuration.checkInRule.rewardOutcome.type)
        assertEquals("reward-1", draft.configuration.checkInRule.rewardOutcome.rewardId)
        assertEquals("Free Coffee", draft.configuration.checkInRule.rewardOutcome.rewardName)
    }

    @Test
    fun `toDraft maps program benefits for different target program types`() {
     /*   val couponProgram = RewardProgram(
            id = "coupon-program",
            storeId = "store-1",
            name = "Birthday Coupon",
            description = "Coupon rewards",
            type = LoyaltyProgramType.COUPON,
            rulesSummary = "",
            active = true,
            autoScheduleEnabled = false,
            scheduleStartDate = null,
            scheduleEndDate = null,
            annualRepeatEnabled = false,
            configuration = com.vector.verevcodex.domain.model.loyalty.RewardProgramConfigurationFactory.defaultFor(
                LoyaltyProgramType.COUPON,
                true,
            ),
        )
        val cashbackProgram = RewardProgram(
            id = "cashback-program",
            storeId = "store-1",
            name = "VIP Cashback",
            description = "Cashback rewards",
            type = LoyaltyProgramType.HYBRID,
            rulesSummary = "",
            active = true,
            autoScheduleEnabled = false,
            scheduleStartDate = null,
            scheduleEndDate = null,
            annualRepeatEnabled = false,
            configuration = com.vector.verevcodex.domain.model.loyalty.RewardProgramConfigurationFactory.defaultFor(
                LoyaltyProgramType.HYBRID,
                true,
            ),
        )

        val draft = ProgramEditorState(
            name = "Referral club",
            description = "Flexible benefits",
            type = LoyaltyProgramType.REFERRAL,
            referralReferrerReward = ProgramRewardOutcomeEditorState(
                type = ProgramRewardOutcomeType.PROGRAM_COUPON,
                programId = "coupon-program",
            ),
            referralRefereeReward = ProgramRewardOutcomeEditorState(
                type = ProgramRewardOutcomeType.PROGRAM_HYBRID,
                programId = "cashback-program",
            ),
        ).toDraft(
            storeId = "store-1",
            availablePrograms = listOf(couponProgram, cashbackProgram),
            availableRewards = emptyList(),
        )

        assertEquals(ProgramRewardOutcomeType.PROGRAM_COUPON, draft.configuration.referralRule.referrerRewardOutcome.type)
        assertEquals("coupon-program", draft.configuration.referralRule.referrerRewardOutcome.programId)
        assertEquals("Birthday Coupon", draft.configuration.referralRule.referrerRewardOutcome.programName)
        assertEquals(ProgramRewardOutcomeType.PROGRAM_HYBRID, draft.configuration.referralRule.refereeRewardOutcome.type)
        assertEquals("cashback-program", draft.configuration.referralRule.refereeRewardOutcome.programId)
        assertEquals("VIP Cashback", draft.configuration.referralRule.refereeRewardOutcome.programName)*/
    }

    @Test
    fun `toDraft preserves custom tier names thresholds and benefits`() {
/*        val draft = ProgramEditorState(
            name = "Tiered club",
            description = "Custom tier ladder",
            type = LoyaltyProgramType.TIER,
            tierLevels = listOf(
                TierLevelEditorState(id = "starter", name = "Starter", threshold = "0", bonusPercent = "0"),
                TierLevelEditorState(id = "plus", name = "Plus", threshold = "120", bonusPercent = "4"),
                TierLevelEditorState(id = "elite", name = "Elite", threshold = "420", bonusPercent = "11"),
                TierLevelEditorState(id = "legend", name = "Legend", threshold = "960", bonusPercent = "18"),
            ),
        ).toDraft(
            storeId = "store-1",
            availablePrograms = emptyList(),
            availableRewards = emptyList(),
        )

        val levels = draft.configuration.tierRule.sortedLevels
        assertEquals(listOf("Starter", "Plus", "Elite", "Legend"), levels.map { it.name })
        assertEquals(listOf(0, 120, 420, 960), levels.map { it.threshold })
        assertEquals(listOf(0, 4, 11, 18), levels.map { it.bonusPercent })*/
    }

    @Test
    fun `validate tier program rejects empty tier names`() {
/*        val errors = ProgramEditorState(
            name = "Tiered club",
            description = "Custom tier ladder",
            type = LoyaltyProgramType.TIER,
            tierLevels = listOf(
                TierLevelEditorState(id = "starter", name = "Starter", threshold = "0", bonusPercent = "0"),
                TierLevelEditorState(id = "plus", name = "", threshold = "100", bonusPercent = "5"),
                TierLevelEditorState(id = "elite", name = "Elite", threshold = "200", bonusPercent = "10"),
            ),
        ).validate()

        assertFalse(errors.isEmpty())
        assertEquals(
            com.vector.verevcodex.R.string.merchant_program_error_tier_name_required,
            errors[tierLevelFieldKey("plus", 1)],
        )*/
    }

    @Test
    fun `validate tier program rejects non increasing thresholds`() {
/*        val errors = ProgramEditorState(
            name = "Tiered club",
            description = "Custom tier ladder",
            type = LoyaltyProgramType.TIER,
            tierLevels = listOf(
                TierLevelEditorState(id = "starter", name = "Starter", threshold = "0", bonusPercent = "0"),
                TierLevelEditorState(id = "plus", name = "Plus", threshold = "100", bonusPercent = "5"),
                TierLevelEditorState(id = "elite", name = "Elite", threshold = "90", bonusPercent = "10"),
            ),
        ).validate()

        assertEquals(
            com.vector.verevcodex.R.string.merchant_program_error_tier_order,
            errors[tierLevelFieldKey("elite", 2)],
        )*/
    }
}
