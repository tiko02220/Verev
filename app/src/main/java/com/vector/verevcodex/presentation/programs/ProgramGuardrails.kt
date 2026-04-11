package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramConfiguration
import com.vector.verevcodex.domain.model.loyalty.ProgramRepeatType
import com.vector.verevcodex.domain.model.loyalty.ProgramSeason
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.model.promotions.Campaign
import java.time.LocalDate

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
        repeatType = repeatType,
        repeatDaysOfWeek = repeatDaysOfWeek,
        repeatDaysOfMonth = repeatDaysOfMonth,
        repeatMonths = repeatMonths,
        seasons = seasons,
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
        repeatType = repeatType,
        repeatDaysOfWeek = repeatDaysOfWeek,
        repeatDaysOfMonth = repeatDaysOfMonth,
        repeatMonths = repeatMonths,
        seasons = seasons,
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
        repeatType = repeatType,
        repeatDaysOfWeek = repeatDaysOfWeek,
        repeatDaysOfMonth = repeatDaysOfMonth,
        repeatMonths = repeatMonths,
        seasons = seasons,
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
        repeatType = repeatType,
        repeatDaysOfWeek = repeatDaysOfWeek,
        repeatDaysOfMonth = repeatDaysOfMonth,
        repeatMonths = repeatMonths,
        seasons = seasons,
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
    repeatType: ProgramRepeatType,
    repeatDaysOfWeek: List<Int>,
    repeatDaysOfMonth: List<Int>,
    repeatMonths: List<Int>,
    seasons: List<ProgramSeason>,
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
                .filter { windowsOverlap(effectiveWindow, ProgramEffectiveWindow(it.startDate, it.endDate, ProgramRepeatType.NONE)) }
                .map { ProgramOverlapWarning.CampaignConflict(it.name) }
                .forEach(::add)
        }
    }
    val activeCoverage = activeScanActions.toSet()
    val inactiveReasons = buildList {
        when {
            !active -> add(ProgramInactiveReason.Disabled)
            autoScheduleEnabled &&
                repeatType != ProgramRepeatType.CUSTOM &&
                scheduleStartDate != null &&
                scheduleStartDate.isAfter(today) ->
                add(ProgramInactiveReason.StartsLater(scheduleStartDate))
            autoScheduleEnabled &&
                repeatType != ProgramRepeatType.CUSTOM &&
                scheduleEndDate != null &&
                scheduleEndDate.isBefore(today) ->
                add(ProgramInactiveReason.Ended(scheduleEndDate))
            autoScheduleEnabled &&
                repeatType == ProgramRepeatType.CUSTOM &&
                scheduleStartDate != null &&
                scheduleEndDate != null &&
                effectiveWindow?.contains(today) != true &&
                currentCustomOccurrence(scheduleStartDate, scheduleEndDate, today).first.isAfter(today) ->
                add(ProgramInactiveReason.StartsLater(currentCustomOccurrence(scheduleStartDate, scheduleEndDate, today).first))
            autoScheduleEnabled &&
                repeatType == ProgramRepeatType.CUSTOM &&
                scheduleStartDate != null &&
                scheduleEndDate != null &&
                effectiveWindow?.contains(today) != true &&
                currentCustomOccurrence(scheduleStartDate, scheduleEndDate, today).second.isBefore(today) ->
                add(ProgramInactiveReason.Ended(currentCustomOccurrence(scheduleStartDate, scheduleEndDate, today).second))
            autoScheduleEnabled && effectiveWindow?.contains(today) != true ->
                add(ProgramInactiveReason.NoActiveScanCoverage)
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
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val repeatType: ProgramRepeatType,
    val repeatDaysOfWeek: List<Int> = emptyList(),
    val repeatDaysOfMonth: List<Int> = emptyList(),
    val repeatMonths: List<Int> = emptyList(),
    val seasons: List<ProgramSeason> = emptyList(),
) {
    fun contains(date: LocalDate): Boolean {
        return when (repeatType) {
            ProgramRepeatType.WEEKDAYS -> repeatDaysOfWeek.contains(date.dayOfWeek.value)
            ProgramRepeatType.SEASONAL -> seasons.flatMap { it.months }.contains(date.monthValue)
            ProgramRepeatType.CUSTOM -> if (startDate != null && endDate != null) {
                annualWindowContains(startDate, endDate, date)
            } else {
                false
            }
            ProgramRepeatType.NONE -> when {
                startDate == null && endDate == null -> true
                startDate != null && endDate != null -> !date.isBefore(startDate) && !date.isAfter(endDate)
                startDate != null -> !date.isBefore(startDate)
                else -> !date.isAfter(endDate)
            }
        }
    }
}

private fun effectiveWindow(
    active: Boolean,
    autoScheduleEnabled: Boolean,
    scheduleStartDate: LocalDate?,
    scheduleEndDate: LocalDate?,
    repeatType: ProgramRepeatType,
    repeatDaysOfWeek: List<Int>,
    repeatDaysOfMonth: List<Int>,
    repeatMonths: List<Int>,
    seasons: List<ProgramSeason>,
    today: LocalDate,
): ProgramEffectiveWindow? {
    if (!active) return null
    if (!autoScheduleEnabled) return ProgramEffectiveWindow(startDate = today, endDate = null, repeatType = ProgramRepeatType.NONE)
    return when (repeatType) {
        ProgramRepeatType.WEEKDAYS -> ProgramEffectiveWindow(
            startDate = null,
            endDate = null,
            repeatType = repeatType,
            repeatDaysOfWeek = repeatDaysOfWeek,
            seasons = seasons,
        )
        ProgramRepeatType.SEASONAL -> ProgramEffectiveWindow(
            startDate = null,
            endDate = null,
            repeatType = repeatType,
            repeatDaysOfWeek = repeatDaysOfWeek,
            seasons = seasons,
        )
        ProgramRepeatType.CUSTOM -> {
            if (scheduleStartDate == null || scheduleEndDate == null || scheduleEndDate.isBefore(scheduleStartDate)) return null
            ProgramEffectiveWindow(
                startDate = scheduleStartDate,
                endDate = scheduleEndDate,
                repeatType = repeatType,
                repeatDaysOfWeek = repeatDaysOfWeek,
                seasons = seasons,
            )
        }
        ProgramRepeatType.NONE -> null
    }
}

private fun RewardProgram.toEffectiveWindow(today: LocalDate): ProgramEffectiveWindow? = effectiveWindow(
    active = active,
    autoScheduleEnabled = autoScheduleEnabled,
    scheduleStartDate = scheduleStartDate,
    scheduleEndDate = scheduleEndDate,
    repeatType = repeatType,
    repeatDaysOfWeek = repeatDaysOfWeek,
    repeatDaysOfMonth = repeatDaysOfMonth,
    repeatMonths = repeatMonths,
    seasons = seasons,
    today = today,
)

private fun windowsOverlap(
    left: ProgramEffectiveWindow,
    right: ProgramEffectiveWindow?,
): Boolean {
    if (right == null) return false
    return simpleWindowsOverlap(left, right)
}

private fun simpleWindowsOverlap(
    left: ProgramEffectiveWindow,
    right: ProgramEffectiveWindow,
): Boolean {
    if (left.repeatType != ProgramRepeatType.CUSTOM || right.repeatType != ProgramRepeatType.CUSTOM) return true
    val leftStart = left.startDate ?: return true
    val rightStart = right.startDate ?: return true
    val leftEnd = left.endDate ?: return true
    val rightEnd = right.endDate ?: return true
    return !leftStart.isAfter(rightEnd) && !rightStart.isAfter(leftEnd)
}

private fun annualWindowContains(start: LocalDate, end: LocalDate, date: LocalDate): Boolean {
    val startMonthDay = java.time.MonthDay.from(start)
    val endMonthDay = java.time.MonthDay.from(end)
    val dateMonthDay = java.time.MonthDay.from(date)
    val projectedWindow = if (!endMonthDay.isBefore(startMonthDay)) {
        startMonthDay.atYear(date.year) to endMonthDay.atYear(date.year)
    } else if (!dateMonthDay.isBefore(startMonthDay)) {
        startMonthDay.atYear(date.year) to endMonthDay.atYear(date.year + 1)
    } else {
        startMonthDay.atYear(date.year - 1) to endMonthDay.atYear(date.year)
    }
    return !date.isBefore(projectedWindow.first) && !date.isAfter(projectedWindow.second)
}

private fun currentCustomOccurrence(start: LocalDate, end: LocalDate, date: LocalDate): Pair<LocalDate, LocalDate> {
    val startMonthDay = java.time.MonthDay.from(start)
    val endMonthDay = java.time.MonthDay.from(end)
    val dateMonthDay = java.time.MonthDay.from(date)
    return if (!endMonthDay.isBefore(startMonthDay)) {
        startMonthDay.atYear(date.year) to endMonthDay.atYear(date.year)
    } else if (!dateMonthDay.isBefore(startMonthDay)) {
        startMonthDay.atYear(date.year) to endMonthDay.atYear(date.year + 1)
    } else {
        startMonthDay.atYear(date.year - 1) to endMonthDay.atYear(date.year)
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
