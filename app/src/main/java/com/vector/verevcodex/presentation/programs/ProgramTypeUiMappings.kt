package com.vector.verevcodex.presentation.programs

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Sell
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.presentation.theme.VerevColors

internal fun LoyaltyProgramType.icon(): ImageVector = when (this) {
    LoyaltyProgramType.POINTS -> Icons.Default.Loyalty
    LoyaltyProgramType.CASHBACK -> Icons.Default.Payments
    LoyaltyProgramType.DIGITAL_STAMP -> Icons.Default.CheckCircle
    LoyaltyProgramType.TIER -> Icons.Default.AutoGraph
    LoyaltyProgramType.COUPON -> Icons.Default.Sell
    LoyaltyProgramType.PURCHASE_FREQUENCY -> Icons.Default.Repeat
    LoyaltyProgramType.REFERRAL -> Icons.Default.GroupAdd
    LoyaltyProgramType.HYBRID -> Icons.Default.Campaign
}

internal fun LoyaltyProgramType.gradient(): List<Color> = when (this) {
    LoyaltyProgramType.POINTS -> listOf(VerevColors.Gold, VerevColors.Tan)
    LoyaltyProgramType.CASHBACK -> listOf(VerevColors.Moss, VerevColors.Forest)
    LoyaltyProgramType.DIGITAL_STAMP -> listOf(Color(0xFF7A9CC6), Color(0xFF466B8F))
    LoyaltyProgramType.TIER -> listOf(Color(0xFFB97E4B), Color(0xFF8B5A2B))
    LoyaltyProgramType.COUPON -> listOf(Color(0xFFF59E0B), Color(0xFFD97706))
    LoyaltyProgramType.PURCHASE_FREQUENCY -> listOf(Color(0xFF5B8DEF), Color(0xFF315EBD))
    LoyaltyProgramType.REFERRAL -> listOf(Color(0xFF22A06B), Color(0xFF0F7A4A))
    LoyaltyProgramType.HYBRID -> listOf(VerevColors.Gold, VerevColors.Forest)
}

@StringRes
internal fun LoyaltyProgramType.templateSubtitleRes(): Int = when (this) {
    LoyaltyProgramType.POINTS -> R.string.merchant_program_module_points_subtitle
    LoyaltyProgramType.TIER -> R.string.merchant_program_module_tier_subtitle
    LoyaltyProgramType.COUPON -> R.string.merchant_program_module_coupons_subtitle
    LoyaltyProgramType.DIGITAL_STAMP -> R.string.merchant_program_module_checkin_subtitle
    LoyaltyProgramType.PURCHASE_FREQUENCY -> R.string.merchant_program_module_frequency_subtitle
    LoyaltyProgramType.REFERRAL -> R.string.merchant_program_module_referral_subtitle
    LoyaltyProgramType.CASHBACK -> R.string.merchant_program_section_cashback
    LoyaltyProgramType.HYBRID -> R.string.merchant_programs_subtitle
}

@StringRes
internal fun LoyaltyProgramType.summaryTitleRes(): Int = when (this) {
    LoyaltyProgramType.POINTS -> R.string.merchant_program_section_points
    LoyaltyProgramType.CASHBACK -> R.string.merchant_program_section_cashback
    LoyaltyProgramType.DIGITAL_STAMP -> R.string.merchant_program_section_check_in
    LoyaltyProgramType.TIER -> R.string.merchant_program_section_tier
    LoyaltyProgramType.COUPON -> R.string.merchant_program_section_coupon
    LoyaltyProgramType.PURCHASE_FREQUENCY -> R.string.merchant_program_section_purchase_frequency
    LoyaltyProgramType.REFERRAL -> R.string.merchant_program_section_referral
    LoyaltyProgramType.HYBRID -> R.string.merchant_programs_title
}
