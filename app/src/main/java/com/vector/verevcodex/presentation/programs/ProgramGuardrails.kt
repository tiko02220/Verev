package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramConfiguration
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.model.promotions.Campaign
import java.time.LocalDate
import java.time.MonthDay

internal data class ProgramOperationalSnapshot(
    val scanActions: Set<RewardProgramScanAction>,
    val overlapWarnings: List<ProgramOverlapWarning>,
    val inactiveReasons: List<ProgramInactiveReason>,
    val affectsScansNow: Boolean,
)

internal sealed interface ProgramOverlapWarning {
    data class ProgramConflict(
        val programName: String,
        val sharedActions: Set<RewardProgramScanAction>,
    ) : ProgramOverlapWarning

    data class CampaignConflict(
        val campaignName: String,
    ) : ProgramOverlapWarning
}

internal sealed interface ProgramInactiveReason {
    data object Disabled : ProgramInactiveReason

    data class StartsLater(val startDate: LocalDate) : ProgramInactiveReason

    data class Ended(val endDate: LocalDate) : ProgramInactiveReason

    data object ReferralOnly : ProgramInactiveReason

    data object NoScanActions : ProgramInactiveReason

    data object NoActiveScanCoverage : ProgramInactiveReason
}

internal fun ProgramEditorState.toOperationalSnapshot(
    existingPrograms: List<RewardProgram>,
    campaigns: List<Campaign>,
    activeScanActions: List<RewardProgramScanAction>,
    today: LocalDate = LocalDate.now(),
): ProgramOperationalSnapshot {
    val configuration = toConfiguration(
        availablePrograms = existingPrograms,
        availableRewards = emptyList(),
    )
    val scanActions = configuration.scanActions
    val effectiveWindow = effectiveWindow(
        active = active,
        autoScheduleEnabled = autoScheduleEnabled,
        scheduleStartDate = scheduleStartDate.toLocalDateOrNull(),
        scheduleEndDate = scheduleEndDate.toLocalDateOrNull(),
        annualRepeatEnabled = annualRepeatEnabled,
        today = today,
    )
    return buildProgramOperationalSnapshot(
        programId = programId,
        type = type,
        configuration = configuration,
        active = active,
        autoScheduleEnabled = autoScheduleEnabled,
        scheduleStartDate = scheduleStartDate.toLocalDateOrNull(),
        scheduleEndDate = scheduleEndDate.toLocalDateOrNull(),
        annualRepeatEnabled = annualRepeatEnabled,
        effectiveWindow = effectiveWindow,
        existingPrograms = existingPrograms,
        campaigns = campaigns,
        activeScanActions = activeScanActions,
        today = today,
    )
}

internal fun RewardProgram.toOperationalSnapshot(
    existingPrograms: List<RewardProgram>,
    campaigns: List<Campaign>,
    activeScanActions: List<RewardProgramScanAction>,
    today: LocalDate = LocalDate.now(),
): ProgramOperationalSnapshot {
    val effectiveWindow = effectiveWindow(
        active = active,
        autoScheduleEnabled = autoScheduleEnabled,
        scheduleStartDate = scheduleStartDate,
        scheduleEndDate = scheduleEndDate,
        annualRepeatEnabled = annualRepeatEnabled,
        today = today,
    )
    return buildProgramOperationalSnapshot(
        programId = id,
        type = type,
        configuration = configuration,
        active = active,
        autoScheduleEnabled = autoScheduleEnabled,
        scheduleStartDate = scheduleStartDate,
        scheduleEndDate = scheduleEndDate,
        annualRepeatEnabled = annualRepeatEnabled,
        effectiveWindow = effectiveWindow,
        existingPrograms = existingPrograms,
        campaigns = campaigns,
        activeScanActions = activeScanActions,
        today = today,
    )
}

private fun buildProgramOperationalSnapshot(
    programId: String?,
    type: com.vector.verevcodex.domain.model.common.LoyaltyProgramType,
    configuration: RewardProgramConfiguration,
    active: Boolean,
    autoScheduleEnabled: Boolean,
    scheduleStartDate: LocalDate?,
    scheduleEndDate: LocalDate?,
    annualRepeatEnabled: Boolean,
    effectiveWindow: ProgramEffectiveWindow?,
    existingPrograms: List<RewardProgram>,
    campaigns: List<Campaign>,
    activeScanActions: List<RewardProgramScanAction>,
    today: LocalDate,
): ProgramOperationalSnapshot {
    val scanActions = configuration.scanActions
    val overlapWarnings = buildList {
        if (effectiveWindow != null) {
            existingPrograms
                .asSequence()
                .filter { it.id != programId }
                .mapNotNull { other ->
                    if (!windowsOverlap(effectiveWindow, other.toEffectiveWindow(today))) return@mapNotNull null
                    val sharedActions = sharedProgramActions(
                        leftType = type,
                        leftActions = scanActions,
                        rightType = other.type,
                        rightActions = other.configuration.scanActions,
                    )
                    if (sharedActions.isEmpty()) {
                        null
                    } else {
                        ProgramOverlapWarning.ProgramConflict(
                            programName = other.name,
                            sharedActions = sharedActions,
                        )
                    }
                }
                .forEach(::add)

            campaigns
                .asSequence()
                .filter { it.id != programId }
                .filter { windowsOverlap(effectiveWindow, ProgramEffectiveWindow(it.startDate, it.endDate, annualRepeatEnabled = false)) }
                .map { ProgramOverlapWarning.CampaignConflict(it.name) }
                .forEach(::add)
        }
    }
    val activeCoverage = activeScanActions.toSet()
    val inactiveReasons = buildList {
        when {
            !active -> add(ProgramInactiveReason.Disabled)
            autoScheduleEnabled && scheduleStartDate != null && scheduleStartDate.isAfter(today) ->
                add(ProgramInactiveReason.StartsLater(scheduleStartDate))
            autoScheduleEnabled && annualRepeatEnabled && scheduleStartDate != null && scheduleEndDate != null &&
                (effectiveWindow?.contains(today) != true) -> {
                nextAnnualWindowStart(scheduleStartDate, scheduleEndDate, today)?.let { add(ProgramInactiveReason.StartsLater(it)) }
            }
            autoScheduleEnabled && scheduleStartDate != null && scheduleEndDate != null &&
                scheduleEndDate.isBefore(today) ->
                add(ProgramInactiveReason.Ended(scheduleEndDate))
        }
        when {
            scanActions.isEmpty() && configuration.referralEnabled -> add(ProgramInactiveReason.ReferralOnly)
            scanActions.isEmpty() -> add(ProgramInactiveReason.NoScanActions)
            active && (effectiveWindow?.contains(today) == true) && activeCoverage.intersect(scanActions).isEmpty() ->
                add(ProgramInactiveReason.NoActiveScanCoverage)
        }
    }
    val affectsScansNow = active &&
        inactiveReasons.isEmpty() &&
        effectiveWindow?.contains(today) == true &&
        scanActions.isNotEmpty() &&
        activeCoverage.intersect(scanActions).isNotEmpty()
    return ProgramOperationalSnapshot(
        scanActions = scanActions,
        overlapWarnings = overlapWarnings,
        inactiveReasons = inactiveReasons,
        affectsScansNow = affectsScansNow,
    )
}

private data class ProgramEffectiveWindow(
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val annualRepeatEnabled: Boolean,
) {
    fun contains(date: LocalDate): Boolean {
        if (endDate == null) return !startDate.isAfter(date)
        return if (annualRepeatEnabled) {
            annualWindowContains(startDate, endDate, date)
        } else {
            !startDate.isAfter(date) && !endDate.isBefore(date)
        }
    }
}

private fun effectiveWindow(
    active: Boolean,
    autoScheduleEnabled: Boolean,
    scheduleStartDate: LocalDate?,
    scheduleEndDate: LocalDate?,
    annualRepeatEnabled: Boolean,
    today: LocalDate,
): ProgramEffectiveWindow? {
    if (!active) return null
    if (!autoScheduleEnabled) return ProgramEffectiveWindow(startDate = today, endDate = null, annualRepeatEnabled = false)
    if (scheduleStartDate == null || scheduleEndDate == null || scheduleEndDate.isBefore(scheduleStartDate)) return null
    return ProgramEffectiveWindow(
        startDate = scheduleStartDate,
        endDate = scheduleEndDate,
        annualRepeatEnabled = annualRepeatEnabled,
    )
}

private fun RewardProgram.toEffectiveWindow(today: LocalDate): ProgramEffectiveWindow? = effectiveWindow(
    active = active,
    autoScheduleEnabled = autoScheduleEnabled,
    scheduleStartDate = scheduleStartDate,
    scheduleEndDate = scheduleEndDate,
    annualRepeatEnabled = annualRepeatEnabled,
    today = today,
)

private fun windowsOverlap(
    left: ProgramEffectiveWindow,
    right: ProgramEffectiveWindow?,
): Boolean {
    if (right == null) return false
    if (left.annualRepeatEnabled || right.annualRepeatEnabled) {
        val leftWindows = comparisonWindows(left, right)
        val rightWindows = comparisonWindows(right, left)
        return leftWindows.any { leftWindow ->
            rightWindows.any { rightWindow -> simpleWindowsOverlap(leftWindow, rightWindow) }
        }
    }
    return simpleWindowsOverlap(left, right)
}

private fun simpleWindowsOverlap(
    left: ProgramEffectiveWindow,
    right: ProgramEffectiveWindow,
): Boolean {
    val leftEnd = left.endDate ?: LocalDate.MAX
    val rightEnd = right.endDate ?: LocalDate.MAX
    return !left.startDate.isAfter(rightEnd) && !right.startDate.isAfter(leftEnd)
}

private fun comparisonWindows(
    window: ProgramEffectiveWindow,
    other: ProgramEffectiveWindow,
): List<ProgramEffectiveWindow> {
    if (!window.annualRepeatEnabled || window.endDate == null) return listOf(window)
    if (other.endDate == null) return listOf(projectAnnualWindow(window, other.startDate.year))
    val years = (other.startDate.year - 1..other.endDate.year + 1).toList()
    return years.map { projectAnnualWindow(window, it) }
}

private fun projectAnnualWindow(
    window: ProgramEffectiveWindow,
    year: Int,
): ProgramEffectiveWindow {
    val startMonthDay = MonthDay.from(window.startDate)
    val endMonthDay = MonthDay.from(window.endDate!!)
    val startDate = startMonthDay.atYear(year)
    val endDate = if (!endMonthDay.isBefore(startMonthDay)) {
        endMonthDay.atYear(year)
    } else {
        endMonthDay.atYear(year + 1)
    }
    return ProgramEffectiveWindow(
        startDate = startDate,
        endDate = endDate,
        annualRepeatEnabled = false,
    )
}

private fun annualWindowContains(start: LocalDate, end: LocalDate, date: LocalDate): Boolean {
    val projectedWindow = if (!MonthDay.from(end).isBefore(MonthDay.from(start))) {
        ProgramEffectiveWindow(
            startDate = MonthDay.from(start).atYear(date.year),
            endDate = MonthDay.from(end).atYear(date.year),
            annualRepeatEnabled = false,
        )
    } else if (!MonthDay.from(date).isBefore(MonthDay.from(start))) {
        ProgramEffectiveWindow(
            startDate = MonthDay.from(start).atYear(date.year),
            endDate = MonthDay.from(end).atYear(date.year + 1),
            annualRepeatEnabled = false,
        )
    } else {
        ProgramEffectiveWindow(
            startDate = MonthDay.from(start).atYear(date.year - 1),
            endDate = MonthDay.from(end).atYear(date.year),
            annualRepeatEnabled = false,
        )
    }
    return simpleWindowsOverlap(
        projectedWindow,
        ProgramEffectiveWindow(date, date, annualRepeatEnabled = false),
    )
}

private fun nextAnnualWindowStart(start: LocalDate, end: LocalDate, today: LocalDate): LocalDate? {
    val startMonthDay = MonthDay.from(start)
    val endMonthDay = MonthDay.from(end)
    val thisYearStart = startMonthDay.atYear(today.year)
    val thisYearEnd = if (!endMonthDay.isBefore(startMonthDay)) endMonthDay.atYear(today.year) else endMonthDay.atYear(today.year + 1)
    return if (today.isBefore(thisYearStart)) {
        thisYearStart
    } else if (today.isAfter(thisYearEnd)) {
        startMonthDay.atYear(today.year + 1)
    } else {
        null
    }
}

private fun sharedProgramActions(
    leftType: com.vector.verevcodex.domain.model.common.LoyaltyProgramType,
    leftActions: Set<RewardProgramScanAction>,
    rightType: com.vector.verevcodex.domain.model.common.LoyaltyProgramType,
    rightActions: Set<RewardProgramScanAction>,
): Set<RewardProgramScanAction> {
    val directOverlap = leftActions.intersect(rightActions)
    if (directOverlap.isNotEmpty()) return directOverlap
    return if (leftActions.isEmpty() && rightActions.isEmpty() && leftType == rightType) {
        setOf(RewardProgramScanAction.EARN_POINTS)
    } else {
        emptySet()
    }
}

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(trim()) }.getOrNull()
