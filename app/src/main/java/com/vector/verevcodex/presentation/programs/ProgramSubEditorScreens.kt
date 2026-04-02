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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
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
import com.vector.verevcodex.domain.model.loyalty.ProgramBenefitResetType
import com.vector.verevcodex.domain.model.loyalty.ProgramRewardOutcomeType
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.TierBenefitType
import com.vector.verevcodex.domain.model.loyalty.TierThresholdBasis
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.theme.VerevColors

enum class ProgramSubEditor {
    BASICS_EDIT,
    AUDIENCE_EDIT,
    AVAILABILITY_EDIT,
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
internal fun BasicsEditScreen(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    ProgramSubEditorLayout(
        title = stringResource(R.string.merchant_program_basics_edit_action),
        subtitle = stringResource(R.string.merchant_program_editor_basics_subtitle),
        headerColors = listOf(VerevColors.Forest, VerevColors.Gold),
        onBack = onBack,
        onSave = onSave,
    ) {
        item {
            ProgramSectionCard(
                title = stringResource(R.string.merchant_program_editor_basics_title),
                subtitle = "",
            ) {
                MerchantFormField(
                    value = editorState.name,
                    onValueChange = onNameChange,
                    label = stringResource(R.string.merchant_program_form_name),
                    leadingIcon = Icons.Default.Loyalty,
                    isError = fieldErrors.containsKey(PROGRAM_FIELD_NAME),
                    errorText = fieldErrors[PROGRAM_FIELD_NAME]?.let { stringResource(it) },
                    supportingText = null,
                )
                MerchantFormField(
                    value = editorState.description,
                    onValueChange = onDescriptionChange,
                    label = stringResource(R.string.merchant_program_form_description),
                    leadingIcon = Icons.Default.Storefront,
                    isError = fieldErrors.containsKey(PROGRAM_FIELD_DESCRIPTION),
                    errorText = fieldErrors[PROGRAM_FIELD_DESCRIPTION]?.let { stringResource(it) },
                    supportingText = null,
                )
            }
        }
    }
}

@Composable
internal fun AudienceEditScreen(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onTargetGenderChange: (String) -> Unit,
    onAgeTargetingEnabledChange: (Boolean) -> Unit,
    onTargetAgeMinChange: (String) -> Unit,
    onTargetAgeMaxChange: (String) -> Unit,
    onOneTimePerCustomerChange: (Boolean) -> Unit,
    onSave: () -> Unit,
) {
    ProgramSubEditorLayout(
        title = stringResource(R.string.merchant_program_audience_edit_action),
        subtitle = stringResource(R.string.merchant_program_audience_subtitle),
        headerColors = listOf(VerevColors.Forest, VerevColors.Moss),
        onBack = onBack,
        onSave = onSave,
    ) {
        item {
            ProgramAudienceSection(
                editorState = editorState,
                fieldErrors = fieldErrors,
                onTargetGenderChange = onTargetGenderChange,
                onAgeTargetingEnabledChange = onAgeTargetingEnabledChange,
                onTargetAgeMinChange = onTargetAgeMinChange,
                onTargetAgeMaxChange = onTargetAgeMaxChange,
                onOneTimePerCustomerChange = onOneTimePerCustomerChange,
            )
        }
    }
}

@Composable
internal fun AvailabilityEditScreen(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onActiveChanged: (Boolean) -> Unit,
    onAutoScheduleEnabledChange: (Boolean) -> Unit,
    onScheduleStartDateChange: (String) -> Unit,
    onScheduleEndDateChange: (String) -> Unit,
    onAnnualRepeatEnabledChange: (Boolean) -> Unit,
    onBenefitResetTypeChange: (ProgramBenefitResetType) -> Unit,
    onBenefitResetCustomDaysChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    ProgramSubEditorLayout(
        title = stringResource(R.string.merchant_program_availability_edit_action),
        subtitle = stringResource(R.string.merchant_program_editor_schedule_subtitle),
        headerColors = listOf(VerevColors.Gold, VerevColors.Tan),
        onBack = onBack,
        onSave = onSave,
    ) {
        item {
            ProgramSectionCard(
                title = stringResource(R.string.merchant_program_editor_schedule_title),
                subtitle = "",
            ) {
                ProgramToggleRow(
                    title = stringResource(R.string.merchant_program_form_enabled),
                    subtitle = stringResource(R.string.merchant_program_enabled_toggle_subtitle),
                    checked = editorState.active,
                    onCheckedChange = onActiveChanged,
                )
                ProgramScheduleSection(
                    editorState = editorState,
                    fieldErrors = fieldErrors,
                    onAutoScheduleEnabledChange = onAutoScheduleEnabledChange,
                    onScheduleStartDateChange = onScheduleStartDateChange,
                    onScheduleEndDateChange = onScheduleEndDateChange,
                    onAnnualRepeatEnabledChange = onAnnualRepeatEnabledChange,
                    benefitResetType = editorState.benefitResetType,
                    benefitResetCustomDays = editorState.benefitResetCustomDays,
                    onBenefitResetTypeChange = onBenefitResetTypeChange,
                    onBenefitResetCustomDaysChange = onBenefitResetCustomDaysChange,
                )
            }
        }
    }
}

@Composable
internal fun TierEditScreen(
    editorState: ProgramEditorState,
    availablePrograms: List<RewardProgram>,
    availableRewards: List<Reward>,
    currencyCode: String,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onTierThresholdBasisChange: (TierThresholdBasis) -> Unit,
    onTierNameChange: (String, String) -> Unit,
    onTierThresholdChange: (String, String) -> Unit,
    onTierBenefitTypeChange: (String, TierBenefitType) -> Unit,
    onTierBonusPercentChange: (String, String) -> Unit,
    onTierRewardTypeChange: (String, ProgramRewardOutcomeType) -> Unit,
    onTierRewardPointsChange: (String, String) -> Unit,
    onTierRewardRewardIdChange: (String, String?) -> Unit,
    onTierRewardProgramIdChange: (String, String?) -> Unit,
    onClearTierBenefit: (String) -> Unit,
    onAddTier: () -> Unit,
    onRemoveTier: (String) -> Unit,
    onOpenRewardsCatalog: () -> Unit,
    onOpenProgramsCatalog: () -> Unit,
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
                tierThresholdBasis = editorState.tierThresholdBasis,
                tierLevels = editorState.tierLevels,
                availablePrograms = availablePrograms,
                availableRewards = availableRewards,
                currencyCode = currencyCode,
                fieldErrors = fieldErrors,
                onTierThresholdBasisChange = onTierThresholdBasisChange,
                onTierNameChange = onTierNameChange,
                onTierThresholdChange = onTierThresholdChange,
                onTierBenefitTypeChange = onTierBenefitTypeChange,
                onTierBonusPercentChange = onTierBonusPercentChange,
                onTierRewardTypeChange = onTierRewardTypeChange,
                onTierRewardPointsChange = onTierRewardPointsChange,
                onTierRewardRewardIdChange = onTierRewardRewardIdChange,
                onTierRewardProgramIdChange = onTierRewardProgramIdChange,
                onClearTierBenefit = onClearTierBenefit,
                onAddTier = onAddTier,
                onRemoveTier = onRemoveTier,
                onOpenRewardsCatalog = onOpenRewardsCatalog,
                onOpenProgramsCatalog = onOpenProgramsCatalog,
            )
        }
    }
}

@Composable
internal fun TierProgramLevelsEditor(
    tierThresholdBasis: TierThresholdBasis,
    tierLevels: List<TierLevelEditorState>,
    availablePrograms: List<RewardProgram>,
    availableRewards: List<Reward>,
    currencyCode: String,
    fieldErrors: Map<String, Int>,
    onTierThresholdBasisChange: (TierThresholdBasis) -> Unit,
    onTierNameChange: (String, String) -> Unit,
    onTierThresholdChange: (String, String) -> Unit,
    onTierBenefitTypeChange: (String, TierBenefitType) -> Unit,
    onTierBonusPercentChange: (String, String) -> Unit,
    onTierRewardTypeChange: (String, ProgramRewardOutcomeType) -> Unit,
    onTierRewardPointsChange: (String, String) -> Unit,
    onTierRewardRewardIdChange: (String, String?) -> Unit,
    onTierRewardProgramIdChange: (String, String?) -> Unit,
    onClearTierBenefit: (String) -> Unit,
    onAddTier: () -> Unit,
    onRemoveTier: (String) -> Unit,
    onOpenRewardsCatalog: () -> Unit,
    onOpenProgramsCatalog: () -> Unit,
) {
    Text(
        text = stringResource(R.string.merchant_program_tier_threshold_basis_title),
        style = MaterialTheme.typography.titleSmall,
        color = VerevColors.Forest,
        fontWeight = FontWeight.SemiBold,
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TierThresholdBasis.entries.forEach { basis ->
            MerchantFilterChip(
                text = stringResource(
                    if (basis == TierThresholdBasis.POINTS) {
                        R.string.merchant_program_tier_threshold_basis_points
                    } else {
                        R.string.merchant_program_tier_threshold_basis_spend
                    },
                ),
                selected = tierThresholdBasis == basis,
                onClick = { onTierThresholdBasisChange(basis) },
            )
        }
    }
    tierLevels.forEachIndexed { index, level ->
        val errorKey = tierLevelFieldKey(level.id, index)
        val errorText = fieldErrors[errorKey]?.let { stringResource(id = it) }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = level.name.ifBlank {
                    if (index == 0) {
                        stringResource(R.string.merchant_program_tier_entry_title)
                    } else {
                        stringResource(R.string.merchant_program_tier_level_title, index + 1)
                    }
                },
                style = MaterialTheme.typography.titleMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            TierNamePresetSelector(
                selectedName = level.name,
                onSelect = { onTierNameChange(level.id, it) },
            )
            MerchantFormField(
                value = level.name,
                onValueChange = { onTierNameChange(level.id, it) },
                label = stringResource(R.string.merchant_program_form_tier_name),
                leadingIcon = Icons.Default.Loyalty,
                supportingText = null,
                isError = errorText != null && level.name.isBlank(),
                errorText = errorText?.takeIf { level.name.isBlank() },
            )
            MerchantFormField(
                value = level.threshold,
                onValueChange = { onTierThresholdChange(level.id, it) },
                label = stringResource(
                    if (tierThresholdBasis == TierThresholdBasis.POINTS) {
                        R.string.merchant_program_form_tier_threshold_points
                    } else {
                        R.string.merchant_program_form_tier_threshold_spend
                    },
                ),
                leadingIcon = if (tierThresholdBasis == TierThresholdBasis.POINTS) Icons.Default.Star else Icons.Default.Payments,
                supportingText = stringResource(
                    if (tierThresholdBasis == TierThresholdBasis.POINTS) {
                        R.string.merchant_program_form_tier_threshold_points_supporting
                    } else {
                        R.string.merchant_program_form_tier_threshold_spend_supporting
                    },
                ),
                isError = errorText != null && level.threshold.isBlank(),
                errorText = errorText?.takeIf { level.threshold.isBlank() },
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TierBenefitType.entries.forEach { benefitType ->
                    MerchantFilterChip(
                        text = stringResource(
                            if (benefitType == TierBenefitType.BONUS_PERCENT) {
                                R.string.merchant_program_tier_benefit_bonus
                            } else {
                                R.string.merchant_program_tier_benefit_discount
                            },
                        ),
                        selected = level.benefitType == benefitType,
                        onClick = { onTierBenefitTypeChange(level.id, benefitType) },
                    )
                }
            }
            MerchantFormField(
                value = level.bonusPercent,
                onValueChange = { onTierBonusPercentChange(level.id, it) },
                label = stringResource(
                    if (level.benefitType == TierBenefitType.BONUS_PERCENT) {
                        R.string.merchant_program_form_tier_bonus_percent
                    } else {
                        R.string.merchant_program_form_tier_discount_percent
                    },
                ),
                leadingIcon = if (level.benefitType == TierBenefitType.BONUS_PERCENT) Icons.Default.Percent else Icons.Default.LocalOffer,
                supportingText = stringResource(
                    if (level.benefitType == TierBenefitType.BONUS_PERCENT) {
                        R.string.merchant_program_form_tier_bonus_percent_supporting
                    } else {
                        R.string.merchant_program_form_tier_discount_percent_supporting
                    },
                ),
                isError = errorText != null && level.bonusPercent.isBlank(),
                errorText = errorText?.takeIf { level.bonusPercent.isBlank() },
            )
            if (errorText != null && level.threshold.isNotBlank()) {
                InlineProgramError(errorRes = fieldErrors.getValue(errorKey))
            }
            Text(
                text = stringResource(R.string.merchant_program_tier_reward_title),
                style = MaterialTheme.typography.titleSmall,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            ProgramBenefitEditorFields(
                state = level.rewardOutcome,
                availablePrograms = availablePrograms,
                availableRewards = availableRewards,
                errorRes = fieldErrors[errorKey],
                pointsLabel = stringResource(R.string.merchant_program_reward_points_checkin_label),
                currencyCode = currencyCode,
                optional = true,
                onChoiceChange = { choice ->
                    onTierRewardTypeChange(
                        level.id,
                        when (choice) {
                            ProgramBenefitChoice.POINTS -> ProgramRewardOutcomeType.POINTS
                            ProgramBenefitChoice.REWARD_CATALOG -> ProgramRewardOutcomeType.FREE_PRODUCT
                            ProgramBenefitChoice.PROGRAM -> ProgramRewardOutcomeType.PROGRAM_POINTS
                        },
                    )
                },
                onPointsChange = { onTierRewardPointsChange(level.id, it) },
                onRewardIdChange = { onTierRewardRewardIdChange(level.id, it) },
                onProgramIdChange = { onTierRewardProgramIdChange(level.id, it) },
                onOpenRewardsCatalog = onOpenRewardsCatalog,
                onOpenProgramsCatalog = onOpenProgramsCatalog,
                onClear = { onClearTierBenefit(level.id) },
            )
            if (tierLevels.size > 2) {
                OutlinedButton(
                    onClick = { onRemoveTier(level.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VerevColors.ErrorText),
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null)
                    Text(
                        text = stringResource(R.string.merchant_program_remove_tier_action),
                        modifier = Modifier.padding(start = 10.dp, top = 4.dp, bottom = 4.dp),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
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
internal fun TierProgressExplainer() {
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
    availablePrograms: List<RewardProgram>,
    availableRewards: List<Reward>,
    fieldErrors: Map<String, Int>,
    onBack: () -> Unit,
    onPurchaseFrequencyCountChange: (String) -> Unit,
    onPurchaseFrequencyWindowDaysChange: (String) -> Unit,
    onRewardChoiceChange: (ProgramBenefitChoice) -> Unit,
    onRewardPointsChange: (String) -> Unit,
    onRewardIdChange: (String?) -> Unit,
    onProgramIdChange: (String?) -> Unit,
    onOpenRewardsCatalog: () -> Unit,
    onOpenProgramsCatalog: () -> Unit,
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
            ProgramSectionCard(
                title = stringResource(R.string.merchant_program_frequency_reward_title),
                subtitle = "",
            ) {
                ProgramBenefitEditorFields(
                    state = editorState.purchaseFrequencyReward,
                    availablePrograms = availablePrograms,
                    availableRewards = availableRewards,
                    errorRes = fieldErrors[PROGRAM_FIELD_FREQUENCY_REWARD],
                    pointsLabel = stringResource(R.string.merchant_program_reward_points_frequency_label),
                    optional = false,
                    onChoiceChange = onRewardChoiceChange,
                    onPointsChange = onRewardPointsChange,
                    onRewardIdChange = onRewardIdChange,
                    onProgramIdChange = onProgramIdChange,
                    onOpenRewardsCatalog = onOpenRewardsCatalog,
                    onOpenProgramsCatalog = onOpenProgramsCatalog,
                )
            }
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
    availablePrograms: List<RewardProgram>,
    availableRewards: List<Reward>,
    errorRes: Int?,
    onBack: () -> Unit,
    onChoiceChange: (ProgramBenefitChoice) -> Unit,
    onPointsChange: (String) -> Unit,
    onRewardIdChange: (String?) -> Unit,
    onProgramIdChange: (String?) -> Unit,
    onOpenRewardsCatalog: () -> Unit,
    onOpenProgramsCatalog: () -> Unit,
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
                availablePrograms = availablePrograms,
                availableRewards = availableRewards,
                errorRes = errorRes,
                pointsLabel = stringResource(target.pointsLabelRes),
                optional = target.optional,
                onChoiceChange = onChoiceChange,
                onPointsChange = onPointsChange,
                onRewardIdChange = onRewardIdChange,
                onProgramIdChange = onProgramIdChange,
                onOpenRewardsCatalog = onOpenRewardsCatalog,
                onOpenProgramsCatalog = onOpenProgramsCatalog,
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
                .navigationBarsPadding()
                .imePadding(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 24.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 112.dp,
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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerevColors.Gold,
                        contentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(vertical = 18.dp),
                ) {
                    Text(
                        text = stringResource(R.string.merchant_program_subeditor_save_action),
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
