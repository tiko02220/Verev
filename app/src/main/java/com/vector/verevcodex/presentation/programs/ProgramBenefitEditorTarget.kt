package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.R

data class ProgramBenefitEditorTarget(
    val tierLevelId: String? = null,
    val slot: ProgramRewardSlot? = null,
    val titleRes: Int,
    val subtitleRes: Int,
    val pointsLabelRes: Int,
    val optional: Boolean,
) {
    companion object {
        fun tier(levelId: String) = ProgramBenefitEditorTarget(
            tierLevelId = levelId,
            titleRes = R.string.merchant_program_tier_reward_title,
            subtitleRes = R.string.merchant_program_benefit_editor_tier_subtitle,
            pointsLabelRes = R.string.merchant_program_reward_points_tier_label,
            optional = true,
        )

        fun slot(slot: ProgramRewardSlot) = when (slot) {
            ProgramRewardSlot.CHECK_IN -> ProgramBenefitEditorTarget(
                slot = slot,
                titleRes = R.string.merchant_program_checkin_reward_title,
                subtitleRes = R.string.merchant_program_benefit_editor_checkin_subtitle,
                pointsLabelRes = R.string.merchant_program_reward_points_checkin_label,
                optional = false,
            )
            ProgramRewardSlot.PURCHASE_FREQUENCY -> ProgramBenefitEditorTarget(
                slot = slot,
                titleRes = R.string.merchant_program_frequency_reward_title,
                subtitleRes = R.string.merchant_program_benefit_editor_frequency_subtitle,
                pointsLabelRes = R.string.merchant_program_reward_points_frequency_label,
                optional = false,
            )
            ProgramRewardSlot.REFERRAL_REFERRER -> ProgramBenefitEditorTarget(
                slot = slot,
                titleRes = R.string.merchant_program_referral_existing_reward_title,
                subtitleRes = R.string.merchant_program_benefit_editor_referrer_subtitle,
                pointsLabelRes = R.string.merchant_program_reward_points_referrer_label,
                optional = false,
            )
            ProgramRewardSlot.REFERRAL_REFEREE -> ProgramBenefitEditorTarget(
                slot = slot,
                titleRes = R.string.merchant_program_referral_new_reward_title,
                subtitleRes = R.string.merchant_program_benefit_editor_referee_subtitle,
                pointsLabelRes = R.string.merchant_program_reward_points_referee_label,
                optional = false,
            )
        }
    }
}
