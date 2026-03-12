package com.vector.verevcodex.presentation.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.theme.VerevColors

enum class ProgramSubEditor {
    TIER_EDIT,
    EARN_RULES_EDIT,
    REWARD_EDIT,
    CASHBACK_EDIT,
    CHECKIN_EDIT,
    FREQUENCY_EDIT,
    REFERRAL_EDIT,
}

@Composable
internal fun TierEditScreen(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onSilverThresholdChange: (String) -> Unit,
    onGoldThresholdChange: (String) -> Unit,
    onVipThresholdChange: (String) -> Unit,
    onTierBonusPercentChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    ProgramSubEditorLayout(
        title = stringResource(R.string.merchant_program_tier_edit_action),
        subtitle = stringResource(R.string.merchant_program_editor_thresholds_subtitle),
        iconTitle = stringResource(R.string.merchant_program_section_tier),
        icon = Icons.Default.AutoGraph,
        headerColors = LoyaltyProgramType.TIER.gradient(),
        onBack = onBack,
        onSave = onSave,
    ) {
        item {
            MerchantFormField(
                value = editorState.tierSilverThreshold,
                onValueChange = onSilverThresholdChange,
                label = stringResource(R.string.merchant_program_form_tier_silver),
                leadingIcon = Icons.Default.AutoGraph,
                supportingText = stringResource(R.string.merchant_program_form_tier_silver_supporting),
                isError = fieldErrors.containsKey(PROGRAM_FIELD_TIER_SILVER),
                errorText = fieldErrors[PROGRAM_FIELD_TIER_SILVER]?.let { stringResource(id = it) },
            )
        }
        item {
            MerchantFormField(
                value = editorState.tierGoldThreshold,
                onValueChange = onGoldThresholdChange,
                label = stringResource(R.string.merchant_program_form_tier_gold),
                leadingIcon = Icons.Default.AutoGraph,
                supportingText = stringResource(R.string.merchant_program_form_tier_gold_supporting),
                isError = fieldErrors.containsKey(PROGRAM_FIELD_TIER_GOLD),
                errorText = fieldErrors[PROGRAM_FIELD_TIER_GOLD]?.let { stringResource(id = it) },
            )
        }
        item {
            MerchantFormField(
                value = editorState.tierVipThreshold,
                onValueChange = onVipThresholdChange,
                label = stringResource(R.string.merchant_program_form_tier_vip),
                leadingIcon = Icons.Default.AutoGraph,
                supportingText = stringResource(R.string.merchant_program_form_tier_vip_supporting),
                isError = fieldErrors.containsKey(PROGRAM_FIELD_TIER_VIP),
                errorText = fieldErrors[PROGRAM_FIELD_TIER_VIP]?.let { stringResource(id = it) },
            )
        }
        item {
            MerchantFormField(
                value = editorState.tierBonusPercent,
                onValueChange = onTierBonusPercentChange,
                label = stringResource(R.string.merchant_program_form_tier_bonus_percent),
                leadingIcon = Icons.Default.AutoGraph,
                supportingText = stringResource(R.string.merchant_program_form_tier_bonus_percent_supporting),
                isError = false,
                errorText = null,
            )
        }
        fieldErrors[PROGRAM_FIELD_TIER_GOLD]?.takeIf { it == R.string.merchant_program_error_tier_order }?.let { errorRes ->
            item { InlineProgramError(errorRes = errorRes) }
        }
        fieldErrors[PROGRAM_FIELD_TIER_VIP]?.takeIf { it == R.string.merchant_program_error_tier_order }?.let { errorRes ->
            item { InlineProgramError(errorRes = errorRes) }
        }
    }
}

@Composable
internal fun EarnRulesEditScreen(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onSpendStepAmountChange: (String) -> Unit,
    onPointsAwardedPerStepChange: (String) -> Unit,
    onPointsWelcomeBonusChange: (String) -> Unit,
    onPointsMinimumRedeemChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    ProgramSubEditorLayout(
        title = stringResource(R.string.merchant_program_rules_edit_action),
        subtitle = stringResource(R.string.merchant_program_module_points_subtitle),
        iconTitle = stringResource(R.string.merchant_program_feature_earn_points),
        icon = Icons.Default.Loyalty,
        headerColors = LoyaltyProgramType.POINTS.gradient(),
        onBack = onBack,
        onSave = onSave,
    ) {
        item {
            MerchantFormField(
                value = editorState.pointsSpendStepAmount,
                onValueChange = onSpendStepAmountChange,
                label = stringResource(R.string.merchant_program_form_points_step_amount),
                leadingIcon = Icons.Default.Loyalty,
                supportingText = stringResource(R.string.merchant_program_form_points_step_amount_supporting),
                isError = fieldErrors.containsKey(PROGRAM_FIELD_POINTS_STEP),
                errorText = fieldErrors[PROGRAM_FIELD_POINTS_STEP]?.let { stringResource(id = it) },
            )
        }
        item {
            MerchantFormField(
                value = editorState.pointsAwardedPerStep,
                onValueChange = onPointsAwardedPerStepChange,
                label = stringResource(R.string.merchant_program_form_points_awarded),
                leadingIcon = Icons.Default.Loyalty,
                supportingText = stringResource(R.string.merchant_program_form_points_awarded_supporting),
                isError = fieldErrors.containsKey(PROGRAM_FIELD_POINTS_AWARDED),
                errorText = fieldErrors[PROGRAM_FIELD_POINTS_AWARDED]?.let { stringResource(id = it) },
            )
        }
        item {
            MerchantFormField(
                value = editorState.pointsWelcomeBonus,
                onValueChange = onPointsWelcomeBonusChange,
                label = stringResource(R.string.merchant_program_form_points_welcome_bonus),
                leadingIcon = Icons.Default.Loyalty,
                supportingText = stringResource(R.string.merchant_program_form_points_welcome_bonus_supporting),
                isError = false,
                errorText = null,
            )
        }
        item {
            MerchantFormField(
                value = editorState.pointsMinimumRedeem,
                onValueChange = onPointsMinimumRedeemChange,
                label = stringResource(R.string.merchant_program_form_points_minimum_redeem),
                leadingIcon = Icons.Default.CardGiftcard,
                supportingText = stringResource(R.string.merchant_program_form_points_minimum_redeem_supporting),
                isError = fieldErrors.containsKey(PROGRAM_FIELD_POINTS_REDEEM),
                errorText = fieldErrors[PROGRAM_FIELD_POINTS_REDEEM]?.let { stringResource(id = it) },
            )
        }
    }
}

@Composable
internal fun RewardEditScreen(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onCouponNameChange: (String) -> Unit,
    onCouponPointsCostChange: (String) -> Unit,
    onCouponDiscountAmountChange: (String) -> Unit,
    onCouponMinimumSpendAmountChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    ProgramSubEditorLayout(
        title = stringResource(R.string.merchant_program_reward_edit_action),
        subtitle = stringResource(R.string.merchant_program_editor_reward_subtitle),
        iconTitle = stringResource(R.string.merchant_program_editor_reward_title),
        icon = Icons.Default.CardGiftcard,
        headerColors = LoyaltyProgramType.COUPON.gradient(),
        onBack = onBack,
        onSave = onSave,
    ) {
        item {
            MerchantFormField(
                value = editorState.couponName,
                onValueChange = onCouponNameChange,
                label = stringResource(R.string.merchant_program_form_coupon_name),
                leadingIcon = Icons.Default.CardGiftcard,
                supportingText = stringResource(R.string.merchant_program_form_coupon_name_supporting),
                isError = fieldErrors.containsKey(PROGRAM_FIELD_COUPON_NAME),
                errorText = fieldErrors[PROGRAM_FIELD_COUPON_NAME]?.let { stringResource(id = it) },
            )
        }
        item {
            MerchantFormField(
                value = editorState.couponPointsCost,
                onValueChange = onCouponPointsCostChange,
                label = stringResource(R.string.merchant_program_form_coupon_points_cost),
                leadingIcon = Icons.Default.CardGiftcard,
                supportingText = stringResource(R.string.merchant_program_form_coupon_points_cost_supporting),
                isError = fieldErrors.containsKey(PROGRAM_FIELD_COUPON_POINTS),
                errorText = fieldErrors[PROGRAM_FIELD_COUPON_POINTS]?.let { stringResource(id = it) },
            )
        }
        item {
            MerchantFormField(
                value = editorState.couponDiscountAmount,
                onValueChange = onCouponDiscountAmountChange,
                label = stringResource(R.string.merchant_program_form_coupon_discount_amount),
                leadingIcon = Icons.Default.CardGiftcard,
                supportingText = stringResource(R.string.merchant_program_form_coupon_discount_amount_supporting),
                isError = fieldErrors.containsKey(PROGRAM_FIELD_COUPON_DISCOUNT),
                errorText = fieldErrors[PROGRAM_FIELD_COUPON_DISCOUNT]?.let { stringResource(id = it) },
            )
        }
        item {
            MerchantFormField(
                value = editorState.couponMinimumSpendAmount,
                onValueChange = onCouponMinimumSpendAmountChange,
                label = stringResource(R.string.merchant_program_form_coupon_minimum_spend),
                leadingIcon = Icons.Default.CardGiftcard,
                supportingText = stringResource(R.string.merchant_program_form_coupon_minimum_spend_supporting),
                isError = false,
                errorText = null,
            )
        }
    }
}

@Composable
internal fun CashbackEditScreen(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onCashbackPercentChange: (String) -> Unit,
    onCashbackMinimumSpendAmountChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    ProgramSubEditorLayout(
        title = stringResource(R.string.merchant_program_cashback_edit_action),
        subtitle = stringResource(R.string.merchant_program_editor_cashback_subtitle),
        iconTitle = stringResource(R.string.merchant_program_section_cashback),
        icon = Icons.Default.Payments,
        headerColors = LoyaltyProgramType.HYBRID.gradient(),
        onBack = onBack,
        onSave = onSave,
    ) {
        item {
            MerchantFormField(
                value = editorState.cashbackPercent,
                onValueChange = onCashbackPercentChange,
                label = stringResource(R.string.merchant_program_form_cashback_percent),
                leadingIcon = Icons.Default.Payments,
                supportingText = stringResource(R.string.merchant_program_form_cashback_percent_supporting),
                isError = fieldErrors.containsKey(PROGRAM_FIELD_CASHBACK_PERCENT),
                errorText = fieldErrors[PROGRAM_FIELD_CASHBACK_PERCENT]?.let { stringResource(id = it) },
            )
        }
        item {
            MerchantFormField(
                value = editorState.cashbackMinimumSpendAmount,
                onValueChange = onCashbackMinimumSpendAmountChange,
                label = stringResource(R.string.merchant_program_form_cashback_minimum_spend),
                leadingIcon = Icons.Default.Payments,
                supportingText = stringResource(R.string.merchant_program_form_cashback_minimum_spend_supporting),
                isError = false,
                errorText = null,
            )
        }
    }
}

@Composable
internal fun CheckInEditScreen(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onCheckInVisitsRequiredChange: (String) -> Unit,
    onCheckInRewardPointsChange: (String) -> Unit,
    onCheckInRewardNameChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    ProgramSubEditorLayout(
        title = stringResource(R.string.merchant_program_checkin_edit_action),
        subtitle = stringResource(R.string.merchant_program_editor_visit_goal_subtitle),
        iconTitle = stringResource(R.string.merchant_program_module_checkin_subtitle),
        icon = Icons.Default.Repeat,
        headerColors = LoyaltyProgramType.DIGITAL_STAMP.gradient(),
        onBack = onBack,
        onSave = onSave,
    ) {
        item {
            MerchantFormField(
                value = editorState.checkInVisitsRequired,
                onValueChange = onCheckInVisitsRequiredChange,
                label = stringResource(R.string.merchant_program_form_checkin_visits),
                leadingIcon = Icons.Default.Repeat,
                supportingText = stringResource(R.string.merchant_program_form_checkin_visits_supporting),
                isError = false,
                errorText = null,
            )
        }
        item {
            MerchantFormField(
                value = editorState.checkInRewardPoints,
                onValueChange = onCheckInRewardPointsChange,
                label = stringResource(R.string.merchant_program_form_checkin_reward_points),
                leadingIcon = Icons.Default.CardGiftcard,
                supportingText = stringResource(R.string.merchant_program_form_checkin_reward_points_supporting),
                isError = false,
                errorText = null,
            )
        }
        item {
            MerchantFormField(
                value = editorState.checkInRewardName,
                onValueChange = onCheckInRewardNameChange,
                label = stringResource(R.string.merchant_program_form_checkin_reward_name),
                leadingIcon = Icons.Default.Sell,
                supportingText = stringResource(R.string.merchant_program_form_checkin_reward_name_supporting),
                isError = false,
                errorText = null,
            )
        }
    }
}

@Composable
internal fun FrequencyEditScreen(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onPurchaseFrequencyCountChange: (String) -> Unit,
    onPurchaseFrequencyWindowDaysChange: (String) -> Unit,
    onPurchaseFrequencyRewardPointsChange: (String) -> Unit,
    onPurchaseFrequencyRewardNameChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    ProgramSubEditorLayout(
        title = stringResource(R.string.merchant_program_frequency_edit_action),
        subtitle = stringResource(R.string.merchant_program_editor_trigger_rules_subtitle),
        iconTitle = stringResource(R.string.merchant_program_module_frequency_subtitle),
        icon = Icons.Default.Repeat,
        headerColors = LoyaltyProgramType.PURCHASE_FREQUENCY.gradient(),
        onBack = onBack,
        onSave = onSave,
    ) {
        item {
            MerchantFormField(
                value = editorState.purchaseFrequencyCount,
                onValueChange = onPurchaseFrequencyCountChange,
                label = stringResource(R.string.merchant_program_form_frequency_count),
                leadingIcon = Icons.Default.Repeat,
                supportingText = stringResource(R.string.merchant_program_form_frequency_count_supporting),
                isError = false,
                errorText = null,
            )
        }
        item {
            MerchantFormField(
                value = editorState.purchaseFrequencyWindowDays,
                onValueChange = onPurchaseFrequencyWindowDaysChange,
                label = stringResource(R.string.merchant_program_form_frequency_window_days),
                leadingIcon = Icons.Default.Repeat,
                supportingText = stringResource(R.string.merchant_program_form_frequency_window_days_supporting),
                isError = false,
                errorText = null,
            )
        }
        item {
            MerchantFormField(
                value = editorState.purchaseFrequencyRewardPoints,
                onValueChange = onPurchaseFrequencyRewardPointsChange,
                label = stringResource(R.string.merchant_program_form_frequency_reward_points),
                leadingIcon = Icons.Default.CardGiftcard,
                supportingText = stringResource(R.string.merchant_program_form_frequency_reward_points_supporting),
                isError = false,
                errorText = null,
            )
        }
        item {
            MerchantFormField(
                value = editorState.purchaseFrequencyRewardName,
                onValueChange = onPurchaseFrequencyRewardNameChange,
                label = stringResource(R.string.merchant_program_form_frequency_reward_name),
                leadingIcon = Icons.Default.Sell,
                supportingText = stringResource(R.string.merchant_program_form_frequency_reward_name_supporting),
                isError = false,
                errorText = null,
            )
        }
    }
}

@Composable
internal fun ReferralEditScreen(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onReferralReferrerRewardPointsChange: (String) -> Unit,
    onReferralRefereeRewardPointsChange: (String) -> Unit,
    onReferralCodePrefixChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    ProgramSubEditorLayout(
        title = stringResource(R.string.merchant_program_referral_edit_action),
        subtitle = stringResource(R.string.merchant_program_editor_referral_rewards_subtitle),
        iconTitle = stringResource(R.string.merchant_program_module_referral_subtitle),
        icon = Icons.Default.Groups,
        headerColors = LoyaltyProgramType.REFERRAL.gradient(),
        onBack = onBack,
        onSave = onSave,
    ) {
        item {
            MerchantFormField(
                value = editorState.referralReferrerRewardPoints,
                onValueChange = onReferralReferrerRewardPointsChange,
                label = stringResource(R.string.merchant_program_form_referral_referrer_points),
                leadingIcon = Icons.Default.Groups,
                supportingText = stringResource(R.string.merchant_program_form_referral_referrer_points_supporting),
                isError = false,
                errorText = null,
            )
        }
        item {
            MerchantFormField(
                value = editorState.referralRefereeRewardPoints,
                onValueChange = onReferralRefereeRewardPointsChange,
                label = stringResource(R.string.merchant_program_form_referral_referee_points),
                leadingIcon = Icons.Default.Groups,
                supportingText = stringResource(R.string.merchant_program_form_referral_referee_points_supporting),
                isError = false,
                errorText = null,
            )
        }
        item {
            MerchantFormField(
                value = editorState.referralCodePrefix,
                onValueChange = onReferralCodePrefixChange,
                label = stringResource(R.string.merchant_program_form_referral_prefix),
                leadingIcon = Icons.Default.Sell,
                supportingText = stringResource(R.string.merchant_program_form_referral_prefix_supporting),
                isError = fieldErrors.containsKey(PROGRAM_FIELD_REFERRAL_PREFIX),
                errorText = fieldErrors[PROGRAM_FIELD_REFERRAL_PREFIX]?.let { stringResource(id = it) },
            )
        }
    }
}

@Composable
private fun ProgramSubEditorLayout(
    title: String,
    subtitle: String,
    iconTitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    headerColors: List<Color>,
    onBack: () -> Unit,
    onSave: () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = VerevColors.AppBackground,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                ProgramsScreenHeader(
                    title = title,
                    subtitle = subtitle,
                    onBack = onBack,
                    colors = headerColors,
                )
            }
            item {
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                    color = Color.White,
                    tonalElevation = 0.dp,
                    shadowElevation = 4.dp,
                ) {
                    Text(
                        text = iconTitle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            content()
            item {
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerevColors.Forest,
                        contentColor = Color.White,
                    ),
                ) {
                    androidx.compose.material3.Icon(icon, contentDescription = null)
                    Text(
                        text = stringResource(R.string.merchant_program_subeditor_save_action),
                        modifier = Modifier.padding(start = 10.dp, top = 4.dp, bottom = 4.dp),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineProgramError(errorRes: Int) {
    Surface(
        color = Color(0xFFFFF3F1),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    ) {
        Text(
            text = stringResource(errorRes),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.ErrorText,
        )
    }
}
