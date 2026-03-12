package com.vector.verevcodex.presentation.programs

import androidx.annotation.StringRes
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType

internal data class ProgramScreenSpec(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    @StringRes val emptyTitleRes: Int,
    @StringRes val emptySubtitleRes: Int,
)

internal fun LoyaltyProgramType.screenSpec(): ProgramScreenSpec = when (this) {
    LoyaltyProgramType.POINTS -> ProgramScreenSpec(
        titleRes = R.string.merchant_points_rewards_title,
        subtitleRes = R.string.merchant_points_rewards_subtitle,
        emptyTitleRes = R.string.merchant_points_rewards_empty_title,
        emptySubtitleRes = R.string.merchant_points_rewards_empty_subtitle,
    )
    LoyaltyProgramType.TIER -> ProgramScreenSpec(
        titleRes = R.string.merchant_tiered_loyalty_title,
        subtitleRes = R.string.merchant_tiered_loyalty_subtitle,
        emptyTitleRes = R.string.merchant_tiered_loyalty_empty_title,
        emptySubtitleRes = R.string.merchant_tiered_loyalty_empty_subtitle,
    )
    LoyaltyProgramType.COUPON -> ProgramScreenSpec(
        titleRes = R.string.merchant_coupons_manager_title,
        subtitleRes = R.string.merchant_coupons_manager_subtitle,
        emptyTitleRes = R.string.merchant_coupons_manager_empty_title,
        emptySubtitleRes = R.string.merchant_coupons_manager_empty_subtitle,
    )
    LoyaltyProgramType.DIGITAL_STAMP -> ProgramScreenSpec(
        titleRes = R.string.merchant_checkin_rewards_title,
        subtitleRes = R.string.merchant_checkin_rewards_subtitle,
        emptyTitleRes = R.string.merchant_checkin_rewards_empty_title,
        emptySubtitleRes = R.string.merchant_checkin_rewards_empty_subtitle,
    )
    LoyaltyProgramType.PURCHASE_FREQUENCY -> ProgramScreenSpec(
        titleRes = R.string.merchant_purchase_frequency_title,
        subtitleRes = R.string.merchant_purchase_frequency_subtitle,
        emptyTitleRes = R.string.merchant_purchase_frequency_empty_title,
        emptySubtitleRes = R.string.merchant_purchase_frequency_empty_subtitle,
    )
    LoyaltyProgramType.REFERRAL -> ProgramScreenSpec(
        titleRes = R.string.merchant_referral_rewards_title,
        subtitleRes = R.string.merchant_referral_rewards_subtitle,
        emptyTitleRes = R.string.merchant_referral_rewards_empty_title,
        emptySubtitleRes = R.string.merchant_referral_rewards_empty_subtitle,
    )
    LoyaltyProgramType.HYBRID -> ProgramScreenSpec(
        titleRes = R.string.merchant_hybrid_programs_title,
        subtitleRes = R.string.merchant_hybrid_programs_subtitle,
        emptyTitleRes = R.string.merchant_hybrid_programs_empty_title,
        emptySubtitleRes = R.string.merchant_hybrid_programs_empty_subtitle,
    )
}
