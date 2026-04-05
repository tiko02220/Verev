package com.vector.verevcodex.domain.model.loyalty

import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import java.time.LocalDate

data class RewardProgramDraft(
    val storeId: String,
    val name: String,
    val description: String,
    val type: LoyaltyProgramType,
    val rulesSummary: String,
    val active: Boolean,
    val autoScheduleEnabled: Boolean,
    val scheduleStartDate: LocalDate?,
    val scheduleEndDate: LocalDate?,
    val annualRepeatEnabled: Boolean,
    val repeatType: ProgramRepeatType = if (annualRepeatEnabled) ProgramRepeatType.CUSTOM else ProgramRepeatType.NONE,
    val repeatDaysOfWeek: List<Int> = emptyList(),
    val repeatDaysOfMonth: List<Int> = emptyList(),
    val repeatMonths: List<Int> = emptyList(),
    val seasons: List<ProgramSeason> = emptyList(),
    val configuration: RewardProgramConfiguration,
    val targetGender: String = "ALL",
    val targetAgeMin: Int? = null,
    val targetAgeMax: Int? = null,
    val oneTimePerCustomer: Boolean = false,
    val benefitResetPolicy: ProgramBenefitResetPolicy = ProgramBenefitResetPolicy(),
)
