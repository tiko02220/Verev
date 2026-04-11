package com.vector.verevcodex.presentation.scan

import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.loyalty.ProgramRepeatType
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramConfigurationFactory
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.presentation.programs.ProgramInactiveReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ScanProgramAvailabilityTest {

    @Test
    fun `future scheduled program is not treated as live scan coverage`() {
        val today = LocalDate.parse("2026-04-11")
        val tomorrow = today.plusDays(1)
        val program = testProgram(
            autoScheduleEnabled = true,
            scheduleStartDate = tomorrow,
            scheduleEndDate = tomorrow.plusDays(5),
        )

        val availability = resolveScanProgramAvailability(
            programs = listOf(program),
            remoteActions = emptyList(),
            today = today,
        )

        assertTrue(availability.livePrograms.isEmpty())
        assertEquals(
            ProgramInactiveReason.StartsLater(tomorrow),
            availability.primaryInactiveReason,
        )
    }

    @Test
    fun `live program stays available when backend exposes active scan action`() {
        val program = testProgram(autoScheduleEnabled = false)

        val availability = resolveScanProgramAvailability(
            programs = listOf(program),
            remoteActions = listOf(RewardProgramScanAction.EARN_POINTS),
            today = LocalDate.parse("2026-04-11"),
        )

        assertEquals(listOf(program), availability.livePrograms)
        assertNull(availability.primaryInactiveReason)
    }

    @Test
    fun `future scheduled program does not expose scan actions even when backend returns stale actions`() {
        val today = LocalDate.parse("2026-04-11")
        val tomorrow = today.plusDays(1)
        val program = testProgram(
            autoScheduleEnabled = true,
            scheduleStartDate = tomorrow,
            scheduleEndDate = tomorrow.plusDays(5),
        )

        val availability = resolveScanProgramAvailability(
            programs = listOf(program),
            remoteActions = listOf(RewardProgramScanAction.EARN_POINTS, RewardProgramScanAction.REDEEM_REWARDS),
            today = today,
        )

        val actions = resolveScanAvailableActions(
            livePrograms = availability.livePrograms,
            remoteActions = listOf(RewardProgramScanAction.EARN_POINTS, RewardProgramScanAction.REDEEM_REWARDS),
        )

        assertTrue(availability.livePrograms.isEmpty())
        assertTrue(actions.isEmpty())
        assertEquals(
            ProgramInactiveReason.StartsLater(tomorrow),
            availability.primaryInactiveReason,
        )
    }

    private fun testProgram(
        autoScheduleEnabled: Boolean,
        scheduleStartDate: LocalDate? = null,
        scheduleEndDate: LocalDate? = null,
    ): RewardProgram = RewardProgram(
        id = "program-1",
        storeId = "store-1",
        name = "Points Club",
        description = "Earn and redeem points",
        type = LoyaltyProgramType.POINTS,
        rulesSummary = "",
        active = true,
        autoScheduleEnabled = autoScheduleEnabled,
        scheduleStartDate = scheduleStartDate,
        scheduleEndDate = scheduleEndDate,
        annualRepeatEnabled = false,
        repeatType = ProgramRepeatType.NONE,
        configuration = RewardProgramConfigurationFactory.defaultFor(
            type = LoyaltyProgramType.POINTS,
            active = true,
        ),
    )
}
