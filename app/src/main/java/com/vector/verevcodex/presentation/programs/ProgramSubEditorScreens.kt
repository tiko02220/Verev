package com.vector.verevcodex.presentation.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.loyalty.ProgramRewardOutcomeType
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.theme.VerevColors

enum class ProgramSubEditor {
    TIER_EDIT,
    EARN_RULES_EDIT,
    REWARD_EDIT,
    CASHBACK_EDIT,
    CHECKIN_EDIT,
    FREQUENCY_EDIT,
    REFERRAL_EDIT,
    BENEFIT_EDIT,
}

@Composable
internal fun TierEditScreen(
    editorState: ProgramEditorState,
    availableRewards: List<Reward>,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onTierNameChange: (String, String) -> Unit,
    onTierThresholdChange: (String, String) -> Unit,
    onTierBonusPercentChange: (String, String) -> Unit,
    onOpenTierBenefitEditor: (String) -> Unit,
    onClearTierBenefit: (String) -> Unit,
    onAddTier: () -> Unit,
    onRemoveTier: (String) -> Unit,
    onSave: () -> Unit,
) {
    ProgramSubEditorLayout(
        title = stringResource(R.string.merchant_program_tier_edit_action),
        subtitle = stringResource(R.string.merchant_program_editor_tier_setup_subtitle),
        headerColors = LoyaltyProgramType.TIER.gradient(),
        onBack = onBack,
        onSave = onSave,
    ) {
        item {
            TierProgramLevelsEditor(
                tierLevels = editorState.tierLevels,
                availableRewards = availableRewards,
                fieldErrors = fieldErrors,
                onTierNameChange = onTierNameChange,
                onTierThresholdChange = onTierThresholdChange,
                onTierBonusPercentChange = onTierBonusPercentChange,
                onOpenTierBenefitEditor = onOpenTierBenefitEditor,
                onClearTierBenefit = onClearTierBenefit,
                onAddTier = onAddTier,
                onRemoveTier = onRemoveTier,
            )
        }
    }
}

@Composable
internal fun TierProgramLevelsEditor(
    tierLevels: List<TierLevelEditorState>,
    availableRewards: List<Reward>,
    fieldErrors: Map<String, Int>,
    onTierNameChange: (String, String) -> Unit,
    onTierThresholdChange: (String, String) -> Unit,
    onTierBonusPercentChange: (String, String) -> Unit,
    onOpenTierBenefitEditor: (String) -> Unit,
    onClearTierBenefit: (String) -> Unit,
    onAddTier: () -> Unit,
    onRemoveTier: (String) -> Unit,
) {
    tierLevels.forEachIndexed { index, level ->
        val errorKey = tierLevelFieldKey(level.id, index)
        val errorText = fieldErrors[errorKey]?.let { stringResource(id = it) }
        val showBonusField = level.bonusPercent.isNotBlank()
        ProgramSectionCard(
            title = if (index == 0) {
                stringResource(R.string.merchant_program_tier_entry_title)
            } else {
                stringResource(R.string.merchant_program_tier_level_title, index + 1)
            },
            subtitle = "",
        ) {
            TierNamePresetSelector(
                selectedName = level.name,
                onSelect = { onTierNameChange(level.id, it) },
            )
            MerchantFormField(
                value = level.name,
                onValueChange = { onTierNameChange(level.id, it) },
                label = stringResource(R.string.merchant_program_form_tier_name),
                leadingIcon = Icons.Default.Percent,
                supportingText = null,
                isError = errorText != null && level.name.isBlank(),
                errorText = errorText?.takeIf { level.name.isBlank() },
            )
            MerchantFormField(
                value = level.threshold,
                onValueChange = { onTierThresholdChange(level.id, it) },
                label = stringResource(R.string.merchant_program_form_tier_threshold),
                leadingIcon = Icons.Default.Percent,
                supportingText = stringResource(R.string.merchant_program_form_tier_threshold_supporting),
                isError = errorText != null && level.threshold.isBlank(),
                errorText = errorText?.takeIf { level.threshold.isBlank() },
            )
            MerchantFormField(
                value = level.bonusPercent,
                onValueChange = { onTierBonusPercentChange(level.id, it) },
                label = stringResource(R.string.merchant_program_form_tier_bonus_percent),
                leadingIcon = Icons.Default.Percent,
                supportingText = stringResource(R.string.merchant_program_form_tier_bonus_percent_supporting),
                isError = errorText != null && level.bonusPercent.isBlank(),
                errorText = errorText?.takeIf { level.bonusPercent.isBlank() },
            )
            if (errorText != null && level.threshold.isNotBlank()) {
                InlineProgramError(errorRes = fieldErrors.getValue(errorKey))
            }
            ProgramBenefitSummarySection(
                state = level.rewardOutcome,
                availableRewards = availableRewards,
                errorRes = fieldErrors[errorKey],
                title = stringResource(R.string.merchant_program_tier_reward_title),
                optional = true,
                onOpenEditor = { onOpenTierBenefitEditor(level.id) },
                onClear = { onClearTierBenefit(level.id) },
            )
            Button(
                onClick = { onRemoveTier(level.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFF3F1),
                    contentColor = VerevColors.ErrorText,
                ),
            ) {
                androidx.compose.material3.Icon(Icons.Default.DeleteOutline, contentDescription = null)
                Text(
                    text = stringResource(R.string.merchant_program_remove_tier_action),
                    modifier = Modifier.padding(start = 10.dp, top = 4.dp, bottom = 4.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
    Button(
        onClick = onAddTier,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = VerevColors.Gold.copy(alpha = 0.16f),
            contentColor = VerevColors.Forest,
        ),
    ) {
        androidx.compose.material3.Icon(Icons.Default.Add, contentDescription = null)
        Text(
            text = stringResource(R.string.merchant_program_add_tier_action),
            modifier = Modifier.padding(start = 10.dp, top = 4.dp, bottom = 4.dp),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun TierProgressExplainer() {
    Surface(
        color = VerevColors.Gold.copy(alpha = 0.10f),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.merchant_program_tier_progress_title),
                style = MaterialTheme.typography.titleSmall,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.merchant_program_tier_progress_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.72f),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TierNamePresetSelector(
    selectedName: String,
    onSelect: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DefaultTierNames.forEach { name ->
            MerchantFilterChip(
                text = name,
                selected = selectedName.equals(name, ignoreCase = true),
                onClick = { onSelect(name) },
            )
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
        subtitle = stringResource(R.string.merchant_program_editor_points_setup_subtitle),
        headerColors = LoyaltyProgramType.POINTS.gradient(),
        onBack = onBack,
        onSave = onSave,
    ) {
        item {
            MerchantFormField(
                value = editorState.pointsSpendStepAmount,
                onValueChange = onSpendStepAmountChange,
                label = stringResource(R.string.merchant_program_form_points_step_amount),
                leadingIcon = Icons.Default.Payments,
                supportingText = null,
                isError = fieldErrors.containsKey(PROGRAM_FIELD_POINTS_STEP),
                errorText = fieldErrors[PROGRAM_FIELD_POINTS_STEP]?.let { stringResource(id = it) },
            )
        }
        item {
            MerchantFormField(
                value = editorState.pointsAwardedPerStep,
                onValueChange = onPointsAwardedPerStepChange,
                label = stringResource(R.string.merchant_program_form_points_awarded),
                leadingIcon = Icons.Default.Add,
                supportingText = null,
                isError = fieldErrors.containsKey(PROGRAM_FIELD_POINTS_AWARDED),
                errorText = fieldErrors[PROGRAM_FIELD_POINTS_AWARDED]?.let { stringResource(id = it) },
            )
        }
        item {
            MerchantFormField(
                value = editorState.pointsWelcomeBonus,
                onValueChange = onPointsWelcomeBonusChange,
                label = stringResource(R.string.merchant_program_form_points_welcome_bonus),
                leadingIcon = Icons.Default.Add,
                supportingText = null,
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
                supportingText = null,
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
        subtitle = stringResource(R.string.merchant_program_editor_coupon_setup_subtitle),
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
                supportingText = null,
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
                supportingText = null,
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
                supportingText = null,
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
                supportingText = null,
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
        subtitle = stringResource(R.string.merchant_program_editor_cashback_setup_subtitle),
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
                supportingText = null,
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
                supportingText = null,
                isError = false,
                errorText = null,
            )
        }
    }
}

@Composable
internal fun CheckInEditScreen(
    editorState: ProgramEditorState,
    availableRewards: List<Reward>,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onCheckInVisitsRequiredChange: (String) -> Unit,
    onOpenBenefitEditor: (ProgramRewardSlot) -> Unit,
    onSave: () -> Unit,
) {
    ProgramSubEditorLayout(
        title = stringResource(R.string.merchant_program_checkin_edit_action),
        subtitle = stringResource(R.string.merchant_program_editor_checkin_setup_subtitle),
        headerColors = LoyaltyProgramType.DIGITAL_STAMP.gradient(),
        onBack = onBack,
        onSave = onSave,
    ) {
        item {
            MerchantFormField(
                value = editorState.checkInVisitsRequired,
                onValueChange = onCheckInVisitsRequiredChange,
                label = stringResource(R.string.merchant_program_form_checkin_visits),
                leadingIcon = Icons.Default.Percent,
                supportingText = null,
                isError = false,
                errorText = null,
            )
        }
        item {
            ProgramBenefitSummarySection(
                title = stringResource(R.string.merchant_program_checkin_reward_title),
                state = editorState.checkInReward,
                availableRewards = availableRewards,
                errorRes = fieldErrors[PROGRAM_FIELD_CHECKIN_REWARD],
                optional = false,
                onOpenEditor = { onOpenBenefitEditor(ProgramRewardSlot.CHECK_IN) },
            )
        }
    }
}

@Composable
internal fun FrequencyEditScreen(
    editorState: ProgramEditorState,
    availableRewards: List<Reward>,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onPurchaseFrequencyCountChange: (String) -> Unit,
    onPurchaseFrequencyWindowDaysChange: (String) -> Unit,
    onOpenBenefitEditor: (ProgramRewardSlot) -> Unit,
    onSave: () -> Unit,
) {
    ProgramSubEditorLayout(
        title = stringResource(R.string.merchant_program_frequency_edit_action),
        subtitle = stringResource(R.string.merchant_program_editor_frequency_setup_subtitle),
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
                supportingText = null,
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
                supportingText = null,
                isError = false,
                errorText = null,
            )
        }
        item {
            ProgramBenefitSummarySection(
                title = stringResource(R.string.merchant_program_frequency_reward_title),
                state = editorState.purchaseFrequencyReward,
                availableRewards = availableRewards,
                errorRes = fieldErrors[PROGRAM_FIELD_FREQUENCY_REWARD],
                optional = false,
                onOpenEditor = { onOpenBenefitEditor(ProgramRewardSlot.PURCHASE_FREQUENCY) },
            )
        }
    }
}

@Composable
internal fun ReferralEditScreen(
    editorState: ProgramEditorState,
    availableRewards: List<Reward>,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onOpenBenefitEditor: (ProgramRewardSlot) -> Unit,
    onReferralCodePrefixChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    ProgramSubEditorLayout(
        title = stringResource(R.string.merchant_program_referral_edit_action),
        subtitle = stringResource(R.string.merchant_program_editor_referral_setup_subtitle),
        headerColors = LoyaltyProgramType.REFERRAL.gradient(),
        onBack = onBack,
        onSave = onSave,
    ) {
        item {
            ProgramBenefitSummarySection(
                title = stringResource(R.string.merchant_program_referral_existing_reward_title),
                state = editorState.referralReferrerReward,
                availableRewards = availableRewards,
                errorRes = fieldErrors[PROGRAM_FIELD_REFERRAL_REFERRER],
                optional = false,
                onOpenEditor = { onOpenBenefitEditor(ProgramRewardSlot.REFERRAL_REFERRER) },
            )
        }
        item {
            ProgramBenefitSummarySection(
                title = stringResource(R.string.merchant_program_referral_new_reward_title),
                state = editorState.referralRefereeReward,
                availableRewards = availableRewards,
                errorRes = fieldErrors[PROGRAM_FIELD_REFERRAL_REFEREE],
                optional = false,
                onOpenEditor = { onOpenBenefitEditor(ProgramRewardSlot.REFERRAL_REFEREE) },
            )
        }
        item {
            MerchantFormField(
                value = editorState.referralCodePrefix,
                onValueChange = onReferralCodePrefixChange,
                label = stringResource(R.string.merchant_program_form_referral_prefix),
                leadingIcon = Icons.Default.Groups,
                supportingText = null,
                isError = fieldErrors.containsKey(PROGRAM_FIELD_REFERRAL_PREFIX),
                errorText = fieldErrors[PROGRAM_FIELD_REFERRAL_PREFIX]?.let { stringResource(id = it) },
            )
        }
    }
}

@Composable
internal fun BenefitEditScreen(
    target: ProgramBenefitEditorTarget,
    state: ProgramRewardOutcomeEditorState,
    availableRewards: List<Reward>,
    errorRes: Int?,
    onBack: () -> Unit,
    onChoiceChange: (ProgramBenefitChoice) -> Unit,
    onPointsChange: (String) -> Unit,
    onRewardIdChange: (String?) -> Unit,
    onOpenRewardsCatalog: () -> Unit,
    onClear: (() -> Unit)?,
    onSave: () -> Unit,
) {
    ProgramSubEditorLayout(
        title = stringResource(target.titleRes),
        subtitle = stringResource(target.subtitleRes),
        headerColors = listOf(VerevColors.Forest, VerevColors.Gold),
        onBack = onBack,
        onSave = onSave,
    ) {
        item {
            ProgramBenefitEditorFields(
                state = state,
                availableRewards = availableRewards,
                errorRes = errorRes,
                pointsLabel = stringResource(target.pointsLabelRes),
                optional = target.optional,
                onChoiceChange = onChoiceChange,
                onPointsChange = onPointsChange,
                onRewardIdChange = onRewardIdChange,
                onOpenRewardsCatalog = onOpenRewardsCatalog,
                onClear = onClear,
            )
        }
    }
}

@Composable
private fun ProgramSubEditorLayout(
    title: String,
    subtitle: String,
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
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 24.dp,
                bottom = 112.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                CompactSubEditorHeader(
                    title = title,
                    subtitle = subtitle,
                    onBack = onBack,
                    accent = headerColors.firstOrNull() ?: VerevColors.Forest,
                )
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
                    Text(
                        text = stringResource(R.string.merchant_program_subeditor_save_action),
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactSubEditorHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    accent: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBack),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = VerevColors.Forest,
            )
            Text(
                text = stringResource(R.string.auth_back),
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
        }
        Surface(
            color = Color.White,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            tonalElevation = 0.dp,
            shadowElevation = 3.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = accent.copy(alpha = 0.78f),
                )
            }
        }
    }
}

@Composable
internal fun InlineProgramError(errorRes: Int) {
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
