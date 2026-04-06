package com.vector.verevcodex.presentation.programs

import androidx.compose.ui.graphics.Color
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.loyalty.ProgramRepeatType
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.presentation.theme.VerevColors
import java.time.LocalDate
import java.time.MonthDay

internal enum class ProgramDisplayStatus {
    PAUSED,
    LIVE,
    SCHEDULED,
    COMPLETED,
}

internal data class ProgramStatusPresentation(
    val textRes: Int,
    val backgroundColor: Color,
    val contentColor: Color,
)

internal fun RewardProgram.displayStatus(date: LocalDate = LocalDate.now()): ProgramDisplayStatus {
    if (!active) return ProgramDisplayStatus.PAUSED
    if (!autoScheduleEnabled) return ProgramDisplayStatus.LIVE
    return when (repeatType) {
        ProgramRepeatType.WEEKDAYS,
        ProgramRepeatType.SEASONAL -> {
            if (isScheduledForDate(date)) ProgramDisplayStatus.LIVE else ProgramDisplayStatus.SCHEDULED
        }
        ProgramRepeatType.CUSTOM -> {
            val start = scheduleStartDate ?: return ProgramDisplayStatus.SCHEDULED
            val end = scheduleEndDate ?: return ProgramDisplayStatus.SCHEDULED
            when {
                isScheduledForDate(date) -> ProgramDisplayStatus.LIVE
                date.isBefore(start) -> ProgramDisplayStatus.SCHEDULED
                date.isAfter(end) -> ProgramDisplayStatus.COMPLETED
                else -> ProgramDisplayStatus.SCHEDULED
            }
        }
        ProgramRepeatType.NONE -> ProgramDisplayStatus.SCHEDULED
    }
}

internal fun RewardProgram.statusPresentation(date: LocalDate = LocalDate.now()): ProgramStatusPresentation =
    when (displayStatus(date)) {
        ProgramDisplayStatus.PAUSED -> ProgramStatusPresentation(
            textRes = R.string.merchant_programs_status_paused,
            backgroundColor = Color(0xFFF3F4F6),
            contentColor = VerevColors.Inactive,
        )
        ProgramDisplayStatus.LIVE -> ProgramStatusPresentation(
            textRes = R.string.merchant_programs_status_live,
            backgroundColor = VerevColors.Moss.copy(alpha = 0.14f),
            contentColor = VerevColors.Moss,
        )
        ProgramDisplayStatus.SCHEDULED -> ProgramStatusPresentation(
            textRes = R.string.merchant_programs_status_scheduled,
            backgroundColor = VerevColors.Gold.copy(alpha = 0.14f),
            contentColor = VerevColors.Forest,
        )
        ProgramDisplayStatus.COMPLETED -> ProgramStatusPresentation(
            textRes = R.string.merchant_programs_status_completed,
            backgroundColor = Color(0xFFF3F4F6),
            contentColor = VerevColors.Inactive,
        )
    }

internal fun RewardProgram.isScheduledForDate(date: LocalDate): Boolean {
    if (!autoScheduleEnabled) return true
    return when (repeatType) {
        ProgramRepeatType.WEEKDAYS -> repeatDaysOfWeek.contains(date.dayOfWeek.value)
        ProgramRepeatType.SEASONAL -> seasons.flatMap { it.months }.contains(date.monthValue)
        ProgramRepeatType.CUSTOM -> {
            val start = scheduleStartDate ?: return false
            val end = scheduleEndDate ?: return false
            annualWindowContains(start = start, end = end, date = date)
        }
        ProgramRepeatType.NONE -> false
    }
}

private fun annualWindowContains(
    start: LocalDate,
    end: LocalDate,
    date: LocalDate,
): Boolean {
    val startMonthDay = MonthDay.from(start)
    val endMonthDay = MonthDay.from(end)
    val dateMonthDay = MonthDay.from(date)
    val window = if (!endMonthDay.isBefore(startMonthDay)) {
        startMonthDay.atYear(date.year) to endMonthDay.atYear(date.year)
    } else if (!dateMonthDay.isBefore(startMonthDay)) {
        startMonthDay.atYear(date.year) to endMonthDay.atYear(date.year + 1)
    } else {
        startMonthDay.atYear(date.year - 1) to endMonthDay.atYear(date.year)
    }
    return !date.isBefore(window.first) && !date.isAfter(window.second)
}
