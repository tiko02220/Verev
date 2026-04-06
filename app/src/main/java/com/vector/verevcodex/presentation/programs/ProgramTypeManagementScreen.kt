package com.vector.verevcodex.presentation.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.presentation.merchant.common.MerchantConfirmationDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantErrorDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantSuccessDialog
import com.vector.verevcodex.presentation.navigation.ShellViewModel
import com.vector.verevcodex.presentation.theme.VerevColors
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun ProgramTypeManagementScreen(
    type: LoyaltyProgramType,
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    onNavigateToProgramsRoot: () -> Unit,
    onOpenRewardsCatalog: () -> Unit = {},
    onOpenProgramsCatalog: () -> Unit = onNavigateToProgramsRoot,
    openEditorOnLaunch: Boolean = false,
    viewModel: LoyaltyViewModel = hiltViewModel(),
    shellViewModel: ShellViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val shellState by shellViewModel.uiState.collectAsStateWithLifecycle()
    val spec = type.screenSpec()
    val programs = state.programs.filter { it.type == type }
    val showCreateSuccessDialog = state.messageRes == R.string.merchant_program_created_message
    val showUpdateSuccessDialog = state.messageRes == R.string.merchant_program_updated_message
    val showDeleteSuccessDialog = state.messageRes == R.string.merchant_program_deleted_message
    val canManagePrograms = shellState.currentUser?.permissions?.managePrograms == true
    var handledInitialCreate by rememberSaveable(type.name, openEditorOnLaunch) { mutableStateOf(false) }
    var pendingCreateFlowClose by rememberSaveable(type.name, openEditorOnLaunch) { mutableStateOf(false) }
    var pendingEditFlowClose by rememberSaveable(type.name) { mutableStateOf(false) }
    val activeEditorState = state.editorState
    LaunchedEffect(openEditorOnLaunch, handledInitialCreate) {
        if (openEditorOnLaunch && !handledInitialCreate && state.editorState == null && state.activeSubEditor == null) {
            handledInitialCreate = true
            viewModel.openCreateProgram(type)
        }
    }
    LaunchedEffect(showCreateSuccessDialog, pendingCreateFlowClose) {
        if (pendingCreateFlowClose && !showCreateSuccessDialog) {
            pendingCreateFlowClose = false
            viewModel.dismissEditor()
            onNavigateToProgramsRoot()
        }
    }

    val showExclusiveCreateEditor = openEditorOnLaunch && activeEditorState != null && !activeEditorState.isEditing
    if (state.activeSubEditor != null && activeEditorState != null) {
        when (state.activeSubEditor) {
            ProgramSubEditor.BASICS_EDIT -> BasicsEditScreen(
                editorState = activeEditorState,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onNameChange = viewModel::updateEditorName,
                onDescriptionChange = viewModel::updateEditorDescription,
                onSave = viewModel::applyProgramSubEditorChanges,
            )
            ProgramSubEditor.AUDIENCE_EDIT -> AudienceEditScreen(
                editorState = activeEditorState,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onTargetGenderChange = viewModel::updateEditorTargetGender,
                onAgeTargetingEnabledChange = viewModel::updateEditorAgeTargetingEnabled,
                onTargetAgeMinChange = viewModel::updateEditorTargetAgeMin,
                onTargetAgeMaxChange = viewModel::updateEditorTargetAgeMax,
                onOneTimePerCustomerChange = viewModel::updateEditorOneTimePerCustomer,
                onSave = viewModel::applyProgramSubEditorChanges,
            )
            ProgramSubEditor.AVAILABILITY_EDIT -> AvailabilityEditScreen(
                editorState = activeEditorState,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onActiveChanged = viewModel::updateEditorActive,
                onAutoScheduleEnabledChange = viewModel::updateEditorAutoScheduleEnabled,
                onScheduleStartDateChange = viewModel::updateEditorScheduleStartDate,
                onScheduleEndDateChange = viewModel::updateEditorScheduleEndDate,
                onRepeatTypeChange = viewModel::updateEditorRepeatType,
                onToggleRepeatDayOfWeek = viewModel::toggleEditorRepeatDayOfWeek,
                onToggleRepeatDayOfMonth = viewModel::toggleEditorRepeatDayOfMonth,
                onToggleRepeatMonth = viewModel::toggleEditorRepeatMonth,
                onToggleSeason = viewModel::toggleEditorSeason,
                onBenefitResetTypeChange = viewModel::updateEditorBenefitResetType,
                onBenefitResetCustomDaysChange = viewModel::updateEditorBenefitResetCustomDays,
                onSave = viewModel::applyProgramSubEditorChanges,
            )
            ProgramSubEditor.TIER_EDIT -> TierEditScreen(
                editorState = activeEditorState,
                availablePrograms = state.programs,
                availableRewards = state.rewards,
                currencyCode = state.currencyCode,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onTierThresholdBasisChange = viewModel::updateTierThresholdBasis,
                onTierNameChange = viewModel::updateTierLevelName,
                onTierThresholdChange = viewModel::updateTierLevelThreshold,
                onTierBenefitTypeChange = viewModel::updateTierBenefitType,
                onTierBonusPercentChange = viewModel::updateTierLevelBonusPercent,
                onTierPerkEnabledChange = viewModel::updateTierLevelPerkEnabled,
                onTierRewardTypeChange = viewModel::updateTierLevelRewardType,
                onTierRewardPointsChange = viewModel::updateTierLevelRewardPoints,
                onTierRewardRewardIdChange = viewModel::updateTierLevelRewardRewardId,
                onTierRewardProgramIdChange = viewModel::updateTierLevelRewardProgramId,
                onClearTierBenefit = viewModel::clearTierLevelBenefit,
                onAddTier = viewModel::addTierLevel,
                onRemoveTier = viewModel::removeTierLevel,
                onOpenRewardsCatalog = onOpenRewardsCatalog,
                onOpenProgramsCatalog = onOpenProgramsCatalog,
                onSave = viewModel::applyProgramSubEditorChanges,
            )
            ProgramSubEditor.EARN_RULES_EDIT -> EarnRulesEditScreen(
                editorState = activeEditorState,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onSpendStepAmountChange = viewModel::updatePointsSpendStepAmount,
                onPointsAwardedPerStepChange = viewModel::updatePointsAwardedPerStep,
                onPointsWelcomeBonusChange = viewModel::updatePointsWelcomeBonus,
                onPointsMinimumRedeemChange = viewModel::updatePointsMinimumRedeem,
                onSave = viewModel::applyProgramSubEditorChanges,
            )
            ProgramSubEditor.REWARD_EDIT -> RewardEditScreen(
                editorState = activeEditorState,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onCouponNameChange = viewModel::updateCouponName,
                onCouponPointsCostChange = viewModel::updateCouponPointsCost,
                onCouponDiscountAmountChange = viewModel::updateCouponDiscountAmount,
                onCouponMinimumSpendAmountChange = viewModel::updateCouponMinimumSpendAmount,
                onSave = viewModel::applyProgramSubEditorChanges,
            )
            ProgramSubEditor.CASHBACK_EDIT -> CashbackEditScreen(
                editorState = activeEditorState,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onCashbackPercentChange = viewModel::updateCashbackPercent,
                onCashbackMinimumSpendAmountChange = viewModel::updateCashbackMinimumSpendAmount,
                onSave = viewModel::applyProgramSubEditorChanges,
            )
            ProgramSubEditor.CHECKIN_EDIT -> CheckInEditScreen(
                editorState = activeEditorState,
                availableRewards = state.rewards,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onCheckInVisitsRequiredChange = viewModel::updateCheckInVisitsRequired,
                onOpenBenefitEditor = viewModel::openBenefitEditor,
                onSave = viewModel::applyProgramSubEditorChanges,
            )
            ProgramSubEditor.FREQUENCY_EDIT -> FrequencyEditScreen(
                editorState = activeEditorState,
                availablePrograms = state.programs,
                availableRewards = state.rewards,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onPurchaseFrequencyCountChange = viewModel::updatePurchaseFrequencyCount,
                onPurchaseFrequencyWindowDaysChange = viewModel::updatePurchaseFrequencyWindowDays,
                onRewardChoiceChange = viewModel::updatePurchaseFrequencyRewardChoice,
                onRewardPointsChange = viewModel::updatePurchaseFrequencyRewardPoints,
                onRewardIdChange = viewModel::updatePurchaseFrequencyRewardId,
                onProgramIdChange = viewModel::updatePurchaseFrequencyRewardProgramId,
                onOpenRewardsCatalog = onOpenRewardsCatalog,
                onOpenProgramsCatalog = onOpenProgramsCatalog,
                onSave = viewModel::applyProgramSubEditorChanges,
            )
            ProgramSubEditor.REFERRAL_EDIT -> ReferralEditScreen(
                editorState = activeEditorState,
                availableRewards = state.rewards,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onOpenBenefitEditor = viewModel::openBenefitEditor,
                onReferralCodePrefixChange = viewModel::updateReferralCodePrefix,
                onSave = viewModel::applyProgramSubEditorChanges,
            )
            ProgramSubEditor.BENEFIT_EDIT -> {
                val target = state.activeBenefitEditor
                val benefitState = target?.currentBenefitState(activeEditorState)
                if (target != null && benefitState != null) {
                    BenefitEditScreen(
                        target = target,
                        state = benefitState,
                        availablePrograms = state.programs,
                        availableRewards = state.rewards,
                        errorRes = target.currentError(state.editorFieldErrors, activeEditorState),
                        onBack = viewModel::closeProgramSubEditor,
                        onChoiceChange = viewModel::updateActiveBenefitChoice,
                        onPointsChange = viewModel::updateActiveBenefitPoints,
                        onRewardIdChange = viewModel::updateActiveBenefitRewardId,
                        onProgramIdChange = viewModel::updateActiveBenefitProgramId,
                        onOpenRewardsCatalog = onOpenRewardsCatalog,
                        onOpenProgramsCatalog = onOpenProgramsCatalog,
                        onClear = target.takeIf { it.optional }?.let { { viewModel.clearActiveBenefit() } },
                        onSave = viewModel::applyProgramSubEditorChanges,
                    )
                }
            }
            null -> Unit
        }
        return
    }

    if (showExclusiveCreateEditor) {
        ProgramEditorSheet(
            editorState = activeEditorState,
            selectedStoreName = state.selectedStoreName,
            availableStores = state.stores,
            availablePrograms = state.programs,
            availableRewards = state.rewards,
            fieldErrors = state.editorFieldErrors,
            isSubmitting = state.isSubmitting,
            currencyCode = state.currencyCode,
            onDismiss = {
                viewModel.dismissEditor()
                onBack()
            },
            onNameChange = viewModel::updateEditorName,
            onDescriptionChange = viewModel::updateEditorDescription,
            onApplyToAllBranchesChange = viewModel::updateEditorApplyToAllBranches,
            onToggleStoreTarget = viewModel::toggleEditorStoreTarget,
            onTypeChange = viewModel::updateEditorType,
            onActiveChanged = viewModel::updateEditorActive,
            onTargetGenderChange = viewModel::updateEditorTargetGender,
            onAgeTargetingEnabledChange = viewModel::updateEditorAgeTargetingEnabled,
            onTargetAgeMinChange = viewModel::updateEditorTargetAgeMin,
            onTargetAgeMaxChange = viewModel::updateEditorTargetAgeMax,
            onOneTimePerCustomerChange = viewModel::updateEditorOneTimePerCustomer,
            onAutoScheduleEnabledChange = viewModel::updateEditorAutoScheduleEnabled,
            onScheduleStartDateChange = viewModel::updateEditorScheduleStartDate,
            onScheduleEndDateChange = viewModel::updateEditorScheduleEndDate,
            onRepeatTypeChange = viewModel::updateEditorRepeatType,
            onToggleRepeatDayOfWeek = viewModel::toggleEditorRepeatDayOfWeek,
            onToggleRepeatDayOfMonth = viewModel::toggleEditorRepeatDayOfMonth,
            onToggleRepeatMonth = viewModel::toggleEditorRepeatMonth,
            onToggleSeason = viewModel::toggleEditorSeason,
            onBenefitResetTypeChange = viewModel::updateEditorBenefitResetType,
            onBenefitResetCustomDaysChange = viewModel::updateEditorBenefitResetCustomDays,
            onPointsSpendStepAmountChange = viewModel::updatePointsSpendStepAmount,
            onPointsAwardedPerStepChange = viewModel::updatePointsAwardedPerStep,
            onPointsWelcomeBonusChange = viewModel::updatePointsWelcomeBonus,
            onPointsMinimumRedeemChange = viewModel::updatePointsMinimumRedeem,
            onCashbackPercentChange = viewModel::updateCashbackPercent,
            onCashbackMinimumSpendAmountChange = viewModel::updateCashbackMinimumSpendAmount,
            onTierThresholdBasisChange = viewModel::updateTierThresholdBasis,
            onTierNameChange = viewModel::updateTierLevelName,
            onTierThresholdChange = viewModel::updateTierLevelThreshold,
            onTierBenefitTypeChange = viewModel::updateTierBenefitType,
            onTierBonusPercentChange = viewModel::updateTierLevelBonusPercent,
            onTierPerkEnabledChange = viewModel::updateTierLevelPerkEnabled,
            onTierRewardTypeChange = viewModel::updateTierLevelRewardType,
            onTierRewardLabelChange = viewModel::updateTierLevelRewardLabel,
            onTierRewardPointsChange = viewModel::updateTierLevelRewardPoints,
            onTierRewardRewardIdChange = viewModel::updateTierLevelRewardRewardId,
            onTierRewardProgramIdChange = viewModel::updateTierLevelRewardProgramId,
            onAddTier = viewModel::addTierLevel,
            onRemoveTier = viewModel::removeTierLevel,
            onCouponNameChange = viewModel::updateCouponName,
            onCouponPointsCostChange = viewModel::updateCouponPointsCost,
            onCouponDiscountAmountChange = viewModel::updateCouponDiscountAmount,
            onCouponMinimumSpendAmountChange = viewModel::updateCouponMinimumSpendAmount,
            onCheckInVisitsRequiredChange = viewModel::updateCheckInVisitsRequired,
            onPurchaseFrequencyCountChange = viewModel::updatePurchaseFrequencyCount,
            onPurchaseFrequencyWindowDaysChange = viewModel::updatePurchaseFrequencyWindowDays,
            onReferralCodePrefixChange = viewModel::updateReferralCodePrefix,
            onRewardOutcomeTypeChange = viewModel::updateRewardOutcomeType,
            onRewardOutcomeLabelChange = viewModel::updateRewardOutcomeLabel,
            onRewardOutcomePointsChange = viewModel::updateRewardOutcomePoints,
            onRewardOutcomeRewardIdChange = viewModel::updateRewardOutcomeRewardId,
            onRewardOutcomeProgramIdChange = viewModel::updateRewardOutcomeProgramId,
            onOpenSubEditor = viewModel::openActiveProgramSubEditor,
            onOpenTierBenefitEditor = viewModel::openTierBenefitEditor,
            onOpenBenefitEditor = viewModel::openBenefitEditor,
            onClearTierBenefit = viewModel::clearTierLevelBenefit,
            onOpenRewardsCatalog = onOpenRewardsCatalog,
            onOpenProgramsCatalog = onOpenProgramsCatalog,
            onApplyEditorValidationErrors = viewModel::applyEditorValidationErrors,
            onSave = viewModel::saveProgram,
            fullScreen = true,
        )
    }

    if (!showExclusiveCreateEditor) {
        state.editorState?.let { editor ->
        ProgramEditorSheet(
            editorState = editor,
            selectedStoreName = state.selectedStoreName,
            availableStores = state.stores,
            availablePrograms = state.programs,
            availableRewards = state.rewards,
            fieldErrors = state.editorFieldErrors,
            isSubmitting = state.isSubmitting,
            currencyCode = state.currencyCode,
            onDismiss = viewModel::dismissEditor,
            onNameChange = viewModel::updateEditorName,
            onDescriptionChange = viewModel::updateEditorDescription,
            onApplyToAllBranchesChange = viewModel::updateEditorApplyToAllBranches,
            onToggleStoreTarget = viewModel::toggleEditorStoreTarget,
            onTypeChange = viewModel::updateEditorType,
            onActiveChanged = viewModel::updateEditorActive,
            onTargetGenderChange = viewModel::updateEditorTargetGender,
            onAgeTargetingEnabledChange = viewModel::updateEditorAgeTargetingEnabled,
            onTargetAgeMinChange = viewModel::updateEditorTargetAgeMin,
            onTargetAgeMaxChange = viewModel::updateEditorTargetAgeMax,
            onOneTimePerCustomerChange = viewModel::updateEditorOneTimePerCustomer,
            onAutoScheduleEnabledChange = viewModel::updateEditorAutoScheduleEnabled,
            onScheduleStartDateChange = viewModel::updateEditorScheduleStartDate,
            onScheduleEndDateChange = viewModel::updateEditorScheduleEndDate,
            onRepeatTypeChange = viewModel::updateEditorRepeatType,
            onToggleRepeatDayOfWeek = viewModel::toggleEditorRepeatDayOfWeek,
            onToggleRepeatDayOfMonth = viewModel::toggleEditorRepeatDayOfMonth,
            onToggleRepeatMonth = viewModel::toggleEditorRepeatMonth,
            onToggleSeason = viewModel::toggleEditorSeason,
            onBenefitResetTypeChange = viewModel::updateEditorBenefitResetType,
            onBenefitResetCustomDaysChange = viewModel::updateEditorBenefitResetCustomDays,
            onPointsSpendStepAmountChange = viewModel::updatePointsSpendStepAmount,
            onPointsAwardedPerStepChange = viewModel::updatePointsAwardedPerStep,
            onPointsWelcomeBonusChange = viewModel::updatePointsWelcomeBonus,
            onPointsMinimumRedeemChange = viewModel::updatePointsMinimumRedeem,
            onCashbackPercentChange = viewModel::updateCashbackPercent,
            onCashbackMinimumSpendAmountChange = viewModel::updateCashbackMinimumSpendAmount,
            onTierThresholdBasisChange = viewModel::updateTierThresholdBasis,
            onTierNameChange = viewModel::updateTierLevelName,
            onTierThresholdChange = viewModel::updateTierLevelThreshold,
            onTierBenefitTypeChange = viewModel::updateTierBenefitType,
            onTierBonusPercentChange = viewModel::updateTierLevelBonusPercent,
            onTierPerkEnabledChange = viewModel::updateTierLevelPerkEnabled,
            onTierRewardTypeChange = viewModel::updateTierLevelRewardType,
            onTierRewardLabelChange = viewModel::updateTierLevelRewardLabel,
            onTierRewardPointsChange = viewModel::updateTierLevelRewardPoints,
            onTierRewardRewardIdChange = viewModel::updateTierLevelRewardRewardId,
            onTierRewardProgramIdChange = viewModel::updateTierLevelRewardProgramId,
            onAddTier = viewModel::addTierLevel,
            onRemoveTier = viewModel::removeTierLevel,
            onCouponNameChange = viewModel::updateCouponName,
            onCouponPointsCostChange = viewModel::updateCouponPointsCost,
            onCouponDiscountAmountChange = viewModel::updateCouponDiscountAmount,
            onCouponMinimumSpendAmountChange = viewModel::updateCouponMinimumSpendAmount,
            onCheckInVisitsRequiredChange = viewModel::updateCheckInVisitsRequired,
            onPurchaseFrequencyCountChange = viewModel::updatePurchaseFrequencyCount,
            onPurchaseFrequencyWindowDaysChange = viewModel::updatePurchaseFrequencyWindowDays,
            onReferralCodePrefixChange = viewModel::updateReferralCodePrefix,
            onRewardOutcomeTypeChange = viewModel::updateRewardOutcomeType,
            onRewardOutcomeLabelChange = viewModel::updateRewardOutcomeLabel,
            onRewardOutcomePointsChange = viewModel::updateRewardOutcomePoints,
            onRewardOutcomeRewardIdChange = viewModel::updateRewardOutcomeRewardId,
            onRewardOutcomeProgramIdChange = viewModel::updateRewardOutcomeProgramId,
            onOpenSubEditor = viewModel::openActiveProgramSubEditor,
            onOpenTierBenefitEditor = viewModel::openTierBenefitEditor,
            onOpenBenefitEditor = viewModel::openBenefitEditor,
            onClearTierBenefit = viewModel::clearTierLevelBenefit,
            onOpenRewardsCatalog = onOpenRewardsCatalog,
            onOpenProgramsCatalog = onOpenProgramsCatalog,
            onApplyEditorValidationErrors = viewModel::applyEditorValidationErrors,
            onSave = viewModel::saveProgram,
        )
        }
    }
    state.programToggleCandidate?.let { candidate ->
        if (candidate.autoScheduleWarning) {
            MerchantConfirmationDialog(
                title = stringResource(R.string.merchant_program_auto_schedule_override_title),
                message = stringResource(R.string.merchant_program_auto_schedule_override_message),
                confirmLabel = stringResource(
                    if (candidate.enabled) {
                        R.string.merchant_program_manual_enable_action
                    } else {
                        R.string.merchant_program_manual_disable_action
                    },
                ),
                dismissLabel = stringResource(R.string.auth_cancel),
                onDismiss = viewModel::dismissProgramToggleDialog,
                onConfirm = viewModel::confirmProgramToggle,
            )
        } else if (candidate.enabled) {
            ProgramEnableGuardrailDialog(
                program = candidate.program,
                selectedStoreName = state.selectedStoreName,
                snapshot = candidate.program.toOperationalSnapshot(
                    existingPrograms = state.programs,
                    campaigns = state.campaigns,
                    activeScanActions = state.activeScanActions,
                ),
                onDismiss = viewModel::dismissProgramToggleDialog,
                onConfirm = viewModel::confirmProgramToggle,
            )
        }
    }
    state.deleteCandidate?.let { program ->
        ProgramDeleteDialog(
            programName = program.name,
            isSubmitting = state.isSubmitting,
            onDismiss = viewModel::dismissDeleteDialog,
            onConfirm = viewModel::confirmDeleteProgram,
        )
    }

    if (!showExclusiveCreateEditor) {
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
                    top = contentPadding.calculateTopPadding() + 24.dp,
                    bottom = contentPadding.calculateBottomPadding() + 112.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    ProgramsBackHeader(
                        title = stringResource(spec.titleRes),
                        subtitle = stringResource(spec.subtitleRes),
                        onBack = onBack,
                        onAddProgram = { viewModel.openCreateProgram(type) },
                        showAddAction = canManagePrograms,
                        colors = type.gradient(),
                    )
                }
                if (programs.isEmpty()) {
                    item {
                        ProgramsModuleEmptyCard(
                            title = stringResource(spec.emptyTitleRes),
                            subtitle = stringResource(spec.emptySubtitleRes),
                            icon = type.icon(),
                        )
                    }
                } else {
                    items(programs.size) { index ->
                        val program = programs[index]
                        ProgramTypeProgramCard(
                            type = type,
                            program = program,
                            selectedStoreName = state.selectedStoreName,
                            snapshot = program.toOperationalSnapshot(
                                existingPrograms = state.programs,
                                campaigns = state.campaigns,
                                activeScanActions = state.activeScanActions,
                            ),
                            isBusy = state.busyProgramId == program.id,
                            onEdit = if (canManagePrograms) ({ viewModel.openEditProgram(program.id) }) else null,
                            onToggleEnabled = { enabled -> viewModel.requestProgramToggle(program.id, enabled) },
                            onDelete = if (canManagePrograms) ({ viewModel.requestDelete(program.id) }) else null,
                            canManagePrograms = canManagePrograms,
                        )
                    }
                }
            }
        }
    }
    state.formErrorRes?.let { errorRes ->
        MerchantErrorDialog(
            message = stringResource(errorRes),
            onDismiss = viewModel::clearMessage,
        )
    }
    if (showCreateSuccessDialog) {
        MerchantSuccessDialog(
            title = stringResource(R.string.merchant_program_created_message),
            message = stringResource(R.string.merchant_program_created_supporting),
            actionLabel = stringResource(R.string.merchant_program_success_ok),
            onDismiss = {
                if (openEditorOnLaunch) {
                    pendingCreateFlowClose = true
                } else {
                    pendingEditFlowClose = true
                }
                viewModel.clearMessage()
            },
        )
    }
    if (showUpdateSuccessDialog) {
        MerchantSuccessDialog(
            title = stringResource(R.string.merchant_program_updated_message),
            message = stringResource(R.string.merchant_program_updated_supporting),
            actionLabel = stringResource(R.string.merchant_program_success_ok),
            onDismiss = {
                pendingEditFlowClose = true
                viewModel.clearMessage()
            },
        )
    }
    if (showDeleteSuccessDialog) {
        MerchantSuccessDialog(
            title = stringResource(R.string.merchant_program_deleted_message),
            message = stringResource(R.string.merchant_program_deleted_supporting),
            actionLabel = stringResource(R.string.merchant_program_success_ok),
            onDismiss = viewModel::clearMessage,
        )
    }
    LaunchedEffect(showUpdateSuccessDialog, pendingEditFlowClose) {
        if (pendingEditFlowClose && !showUpdateSuccessDialog && !showCreateSuccessDialog) {
            pendingEditFlowClose = false
            viewModel.dismissEditor()
        }
    }
}

private fun ProgramBenefitEditorTarget.currentBenefitState(
    editorState: ProgramEditorState,
): ProgramRewardOutcomeEditorState? = when {
    tierLevelId != null -> editorState.tierLevels.firstOrNull { it.id == tierLevelId }?.rewardOutcome
    slot == ProgramRewardSlot.CHECK_IN -> editorState.checkInReward
    slot == ProgramRewardSlot.PURCHASE_FREQUENCY -> editorState.purchaseFrequencyReward
    slot == ProgramRewardSlot.REFERRAL_REFERRER -> editorState.referralReferrerReward
    slot == ProgramRewardSlot.REFERRAL_REFEREE -> editorState.referralRefereeReward
    else -> null
}

private fun ProgramBenefitEditorTarget.currentError(
    fieldErrors: Map<String, Int>,
    editorState: ProgramEditorState,
): Int? = when {
    tierLevelId != null -> {
        val index = editorState.tierLevels.indexOfFirst { it.id == tierLevelId }
        if (index >= 0) fieldErrors[tierLevelFieldKey(tierLevelId, index)] else null
    }
    slot == ProgramRewardSlot.CHECK_IN -> fieldErrors[PROGRAM_FIELD_CHECKIN_REWARD]
    slot == ProgramRewardSlot.PURCHASE_FREQUENCY -> fieldErrors[PROGRAM_FIELD_FREQUENCY_REWARD]
    slot == ProgramRewardSlot.REFERRAL_REFERRER -> fieldErrors[PROGRAM_FIELD_REFERRAL_REFERRER]
    slot == ProgramRewardSlot.REFERRAL_REFEREE -> fieldErrors[PROGRAM_FIELD_REFERRAL_REFEREE]
    else -> null
}

@Composable
fun BranchProgramsConfigScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    onOpenProgramType: (LoyaltyProgramType) -> Unit,
    viewModel: BranchProgramsConfigViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
                top = contentPadding.calculateTopPadding() + 24.dp,
                bottom = contentPadding.calculateBottomPadding() + 112.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                ProgramsBackHeader(
                    title = stringResource(R.string.merchant_branch_programs_title),
                    subtitle = if (state.storeName.isBlank()) {
                        stringResource(R.string.merchant_branch_programs_subtitle)
                    } else {
                        stringResource(R.string.merchant_branch_programs_store_subtitle, state.storeName)
                    },
                    onBack = onBack,
                    onAddProgram = {},
                    showAddAction = false,
                    colors = listOf(VerevColors.Forest, VerevColors.Moss),
                )
            }
            state.errorRes?.let { errorRes ->
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFF3F1),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        androidx.compose.material3.Text(
                            text = stringResource(errorRes),
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                            color = VerevColors.ErrorText,
                        )
                    }
                }
            }
            items@ for (card in state.cards) {
                item {
                    BranchProgramConfigCard(
                        card = card,
                        onOpen = {
                            viewModel.selectBranchForManagement()
                            onOpenProgramType(card.type)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgramsBackHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onAddProgram: () -> Unit,
    showAddAction: Boolean = true,
    colors: List<Color> = listOf(VerevColors.Forest, VerevColors.Moss),
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier
                .clickable(onClick = onBack)
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = VerevColors.Forest,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = stringResource(R.string.auth_back),
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
        }
        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(30.dp),
            shadowElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(colors), RoundedCornerShape(30.dp))
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = title,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = subtitle,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.82f),
                    )
                }
                if (showAddAction) {
                    Button(
                        onClick = onAddProgram,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = colors.last(),
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp),
                    ) {
                        androidx.compose.material3.Text(
                            stringResource(R.string.merchant_program_add_button),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ProgramsScreenHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    colors: List<Color> = listOf(VerevColors.Forest, VerevColors.Moss),
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier
                .clickable(onClick = onBack)
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = VerevColors.Forest,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = stringResource(R.string.auth_back),
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
        }
        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(30.dp),
            shadowElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(colors), RoundedCornerShape(30.dp))
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = title,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = subtitle,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.82f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BranchProgramConfigCard(
    card: BranchProgramCardUi,
    onOpen: () -> Unit,
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Surface(
                color = VerevColors.AppBackground,
                shape = RoundedCornerShape(18.dp),
            ) {
                Row(
                    modifier = Modifier
                        .size(52.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = card.type.icon(),
                        contentDescription = null,
                        tint = VerevColors.Forest,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                androidx.compose.material3.Text(
                    text = stringResource(card.type.screenSpec().titleRes),
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                androidx.compose.material3.Text(
                    text = stringResource(R.string.merchant_program_type_counts, card.totalCount, card.activeCount),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.64f),
                )
            }
            Button(
                onClick = onOpen,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerevColors.Forest,
                    contentColor = Color.White,
                ),
            ) {
                androidx.compose.material3.Text(stringResource(R.string.merchant_branch_programs_open_manager))
            }
        }
    }
}
