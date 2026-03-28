package com.vector.verevcodex.presentation.promotions

import com.vector.verevcodex.domain.model.common.CampaignSegment
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.promotions.Campaign
import java.time.LocalDate
import java.time.MonthDay

internal data class PromotionOperationalSnapshot(
    val overlapWarnings: List<PromotionOverlapWarning>,
)

internal sealed interface PromotionOverlapWarning {
    data class CampaignConflict(
        val campaignName: String,
    ) : PromotionOverlapWarning

    data class LoyaltyStackingConflict(
        val programName: String,
    ) : PromotionOverlapWarning
}

internal fun PromotionEditorState.toOperationalSnapshot(
    storeId: String,
    existingPromotions: List<Campaign>,
    existingPrograms: List<RewardProgram>,
): PromotionOperationalSnapshot {
    val preview = toPreviewCampaign(storeId)
    return PromotionOperationalSnapshot(
        overlapWarnings = buildList {
            existingPromotions
                .asSequence()
                .filter { it.id != promotionId }
                .filter { campaignsOverlap(preview, it) }
                .filter { audienceLikelyOverlaps(preview.target.segment, it.target.segment) }
                .map { PromotionOverlapWarning.CampaignConflict(it.name) }
                .forEach(::add)

            existingPrograms
                .asSequence()
                .filter { it.active }
                .filter { programOverlapsCampaign(it, preview) }
                .map { PromotionOverlapWarning.LoyaltyStackingConflict(it.name) }
                .forEach(::add)
        },
    )
}

private fun campaignsOverlap(left: Campaign, right: Campaign): Boolean =
    !left.startDate.isAfter(right.endDate) && !right.startDate.isAfter(left.endDate)

private fun audienceLikelyOverlaps(left: CampaignSegment, right: CampaignSegment): Boolean =
    left == CampaignSegment.ALL_CUSTOMERS ||
        right == CampaignSegment.ALL_CUSTOMERS ||
        left == right

private fun programOverlapsCampaign(program: RewardProgram, campaign: Campaign, today: LocalDate = LocalDate.now()): Boolean {
    val programStartDate = program.scheduleStartDate
    val programEndDate = program.scheduleEndDate
    val start = when {
        !program.active -> return false
        program.autoScheduleEnabled && programStartDate != null -> programStartDate
        else -> today
    } ?: return false
    val end = if (program.autoScheduleEnabled && programStartDate != null && programEndDate != null) {
        programEndDate
    } else {
        null
    }
    if (program.annualRepeatEnabled && program.autoScheduleEnabled && programStartDate != null && programEndDate != null) {
        val years = (campaign.startDate.year - 1..campaign.endDate.year + 1)
        return years.any { year ->
            val occurrence = projectAnnualWindow(programStartDate, programEndDate, year)
            !occurrence.first.isAfter(campaign.endDate) && !campaign.startDate.isAfter(occurrence.second)
        }
    }
    val programEnd = end ?: LocalDate.MAX
    return !start.isAfter(campaign.endDate) && !campaign.startDate.isAfter(programEnd)
}

private fun projectAnnualWindow(start: LocalDate, end: LocalDate, year: Int): Pair<LocalDate, LocalDate> {
    val startMonthDay = MonthDay.from(start)
    val endMonthDay = MonthDay.from(end)
    val occurrenceStart = startMonthDay.atYear(year)
    val occurrenceEnd = if (!endMonthDay.isBefore(startMonthDay)) endMonthDay.atYear(year) else endMonthDay.atYear(year + 1)
    return occurrenceStart to occurrenceEnd
}
