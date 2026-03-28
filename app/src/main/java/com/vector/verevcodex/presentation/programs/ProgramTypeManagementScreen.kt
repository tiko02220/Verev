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
    openEditorOnLaunch: Boolean = false,
    viewModel: LoyaltyViewModel = hiltViewModel(),
    shellViewModel: ShellViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val shellState by shellViewModel.uiState.collectAsStateWithLifecycle()
    val spec = type.screenSpec()
    val programs = state.programs.filter { it.type == type }
    val showCreateSuccessDialog = state.messageRes == R.string.merchant_program_created_message
    val canManagePrograms = shellState.currentUser?.permissions?.managePrograms == true
    var handledInitialCreate by rememberSaveable(type.name, openEditorOnLaunch) { mutableStateOf(false) }

    LaunchedEffect(state.messageRes) {
        if (state.messageRes != null && !showCreateSuccessDialog) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(openEditorOnLaunch, handledInitialCreate) {
        if (openEditorOnLaunch && !handledInitialCreate && state.editorState == null && state.activeSubEditor == null) {
            handledInitialCreate = true
            viewModel.openCreateProgram(type)
        }
    }

    val activeEditorState = state.editorState
    val showExclusiveCreateEditor = openEditorOnLaunch && activeEditorState != null && !activeEditorState.isEditing
    if (state.activeSubEditor != null && activeEditorState != null) {
        when (state.activeSubEditor) {
            ProgramSubEditor.TIER_EDIT -> TierEditScreen(
                editorState = activeEditorState,
                availableRewards = state.rewards,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onTierNameChange = viewModel::updateTierLevelName,
                onTierThresholdChange = viewModel::updateTierLevelThreshold,
                onTierBonusPercentChange = viewModel::updateTierLevelBonusPercent,
                onOpenTierBenefitEditor = viewModel::openTierBenefitEditor,
                onClearTierBenefit = viewModel::clearTierLevelBenefit,
                onAddTier = viewModel::addTierLevel,
                onRemoveTier = viewModel::removeTierLevel,
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
                availableRewards = state.rewards,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onPurchaseFrequencyCountChange = viewModel::updatePurchaseFrequencyCount,
                onPurchaseFrequencyWindowDaysChange = viewModel::updatePurchaseFrequencyWindowDays,
                onOpenBenefitEditor = viewModel::openBenefitEditor,
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
                        availableRewards = state.rewards,
                        errorRes = target.currentError(state.editorFieldErrors, activeEditorState),
                        onBack = viewModel::closeProgramSubEditor,
                        onChoiceChange = viewModel::updateActiveBenefitChoice,
                        onPointsChange = viewModel::updateActiveBenefitPoints,
                        onRewardIdChange = viewModel::updateActiveBenefitRewardId,
                        onOpenRewardsCatalog = onOpenRewardsCatalog,
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
            availablePrograms = state.programs,
            availableRewards = state.rewards,
            fieldErrors = state.editorFieldErrors,
            isSubmitting = state.isSubmitting,
            onDismiss = {
                viewModel.dismissEditor()
                onBack()
            },
            onNameChange = viewModel::updateEditorName,
            onDescriptionChange = viewModel::updateEditorDescription,
            onTypeChange = viewModel::updateEditorType,
            onActiveChanged = viewModel::updateEditorActive,
            onAutoScheduleEnabledChange = viewModel::updateEditorAutoScheduleEnabled,
            onScheduleStartDateChange = viewModel::updateEditorScheduleStartDate,
            onScheduleEndDateChange = viewModel::updateEditorScheduleEndDate,
            onAnnualRepeatEnabledChange = viewModel::updateEditorAnnualRepeatEnabled,
            onPointsSpendStepAmountChange = viewModel::updatePointsSpendStepAmount,
            onPointsAwardedPerStepChange = viewModel::updatePointsAwardedPerStep,
            onPointsWelcomeBonusChange = viewModel::updatePointsWelcomeBonus,
            onPointsMinimumRedeemChange = viewModel::updatePointsMinimumRedeem,
            onCashbackPercentChange = viewModel::updateCashbackPercent,
            onCashbackMinimumSpendAmountChange = viewModel::updateCashbackMinimumSpendAmount,
            onTierNameChange = viewModel::updateTierLevelName,
            onTierThresholdChange = viewModel::updateTierLevelThreshold,
            onTierBonusPercentChange = viewModel::updateTierLevelBonusPercent,
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
            onSave = viewModel::saveProgram,
            fullScreen = true,
        )
        return
    }

    if (openEditorOnLaunch && showCreateSuccessDialog) {
        MerchantSuccessDialog(
            title = stringResource(R.string.merchant_program_created_message),
            message = stringResource(R.string.merchant_program_created_supporting),
            actionLabel = stringResource(R.string.merchant_program_success_ok),
            onDismiss = {
                viewModel.clearMessage()
                onNavigateToProgramsRoot()
            },
        )
        return
    }

    state.editorState?.let { editor ->
        ProgramEditorSheet(
            editorState = editor,
            selectedStoreName = state.selectedStoreName,
            availablePrograms = state.programs,
            availableRewards = state.rewards,
            fieldErrors = state.editorFieldErrors,
            isSubmitting = state.isSubmitting,
            onDismiss = viewModel::dismissEditor,
            onNameChange = viewModel::updateEditorName,
            onDescriptionChange = viewModel::updateEditorDescription,
            onTypeChange = viewModel::updateEditorType,
            onActiveChanged = viewModel::updateEditorActive,
            onAutoScheduleEnabledChange = viewModel::updateEditorAutoScheduleEnabled,
            onScheduleStartDateChange = viewModel::updateEditorScheduleStartDate,
            onScheduleEndDateChange = viewModel::updateEditorScheduleEndDate,
            onAnnualRepeatEnabledChange = viewModel::updateEditorAnnualRepeatEnabled,
            onPointsSpendStepAmountChange = viewModel::updatePointsSpendStepAmount,
            onPointsAwardedPerStepChange = viewModel::updatePointsAwardedPerStep,
            onPointsWelcomeBonusChange = viewModel::updatePointsWelcomeBonus,
            onPointsMinimumRedeemChange = viewModel::updatePointsMinimumRedeem,
            onCashbackPercentChange = viewModel::updateCashbackPercent,
            onCashbackMinimumSpendAmountChange = viewModel::updateCashbackMinimumSpendAmount,
            onTierNameChange = viewModel::updateTierLevelName,
            onTierThresholdChange = viewModel::updateTierLevelThreshold,
            onTierBonusPercentChange = viewModel::updateTierLevelBonusPercent,
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
            onSave = viewModel::saveProgram,
        )
    }
    state.enableCandidate?.let { program ->
        ProgramEnableGuardrailDialog(
            program = program,
            selectedStoreName = state.selectedStoreName,
            snapshot = program.toOperationalSnapshot(
                existingPrograms = state.programs,
                campaigns = state.campaigns,
                activeScanActions = state.activeScanActions,
            ),
            onDismiss = viewModel::dismissProgramEnableDialog,
            onConfirm = viewModel::confirmProgramEnable,
        )
    }
    state.deleteCandidate?.let { program ->
        ProgramDeleteDialog(
            programName = program.name,
            isSubmitting = state.isSubmitting,
            onDismiss = viewModel::dismissDeleteDialog,
            onConfirm = viewModel::confirmDeleteProgram,
        )
    }

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
            state.messageRes?.takeUnless { showCreateSuccessDialog }?.let { messageRes ->
                item {
                    ProgramsFeedbackBanner(message = stringResource(messageRes))
                }
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
                    val quickActions = buildProgramQuickActions(
                        type = type,
                        programId = program.id,
                        openSubEditor = viewModel::openProgramSubEditor,
                    )
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
                        quickActions = if (canManagePrograms) quickActions else emptyList(),
                        onToggleEnabled = { enabled -> viewModel.requestProgramToggle(program.id, enabled) },
                        onDelete = if (canManagePrograms) ({ viewModel.requestDelete(program.id) }) else null,
                        canManagePrograms = canManagePrograms,
                    )
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
                viewModel.clearMessage()
                onNavigateToProgramsRoot()
            },
        )
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

private fun buildProgramQuickActions(
    type: LoyaltyProgramType,
    programId: String,
    openSubEditor: (String, ProgramSubEditor) -> Unit,
): List<ProgramQuickActionUi> = when (type) {
    LoyaltyProgramType.POINTS -> listOf(
        ProgramQuickActionUi(R.string.merchant_program_rules_edit_action) {
            openSubEditor(programId, ProgramSubEditor.EARN_RULES_EDIT)
        },
        ProgramQuickActionUi(R.string.merchant_program_reward_edit_action) {
            openSubEditor(programId, ProgramSubEditor.REWARD_EDIT)
        },
    )
    LoyaltyProgramType.TIER -> listOf(
        ProgramQuickActionUi(R.string.merchant_program_tier_edit_action) {
            openSubEditor(programId, ProgramSubEditor.TIER_EDIT)
        },
    )
    LoyaltyProgramType.COUPON -> listOf(
        ProgramQuickActionUi(R.string.merchant_program_coupon_edit_action) {
            openSubEditor(programId, ProgramSubEditor.REWARD_EDIT)
        },
    )
    LoyaltyProgramType.DIGITAL_STAMP -> listOf(
        ProgramQuickActionUi(R.string.merchant_program_checkin_edit_action) {
            openSubEditor(programId, ProgramSubEditor.CHECKIN_EDIT)
        },
    )
    LoyaltyProgramType.PURCHASE_FREQUENCY -> listOf(
        ProgramQuickActionUi(R.string.merchant_program_frequency_edit_action) {
            openSubEditor(programId, ProgramSubEditor.FREQUENCY_EDIT)
        },
    )
    LoyaltyProgramType.REFERRAL -> listOf(
        ProgramQuickActionUi(R.string.merchant_program_referral_edit_action) {
            openSubEditor(programId, ProgramSubEditor.REFERRAL_EDIT)
        },
    )
    LoyaltyProgramType.HYBRID -> listOf(
        ProgramQuickActionUi(R.string.merchant_program_rules_edit_action) {
            openSubEditor(programId, ProgramSubEditor.EARN_RULES_EDIT)
        },
        ProgramQuickActionUi(R.string.merchant_program_cashback_edit_action) {
            openSubEditor(programId, ProgramSubEditor.CASHBACK_EDIT)
        },
        ProgramQuickActionUi(R.string.merchant_program_tier_edit_action) {
            openSubEditor(programId, ProgramSubEditor.TIER_EDIT)
        },
        ProgramQuickActionUi(R.string.merchant_program_reward_edit_action) {
            openSubEditor(programId, ProgramSubEditor.REWARD_EDIT)
        },
        ProgramQuickActionUi(R.string.merchant_program_checkin_edit_action) {
            openSubEditor(programId, ProgramSubEditor.CHECKIN_EDIT)
        },
        ProgramQuickActionUi(R.string.merchant_program_frequency_edit_action) {
            openSubEditor(programId, ProgramSubEditor.FREQUENCY_EDIT)
        },
        ProgramQuickActionUi(R.string.merchant_program_referral_edit_action) {
            openSubEditor(programId, ProgramSubEditor.REFERRAL_EDIT)
        },
    )
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
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = stringResource(R.string.auth_back),
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                )
            }
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

@Composable
internal fun ProgramsScreenHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    colors: List<Color> = listOf(VerevColors.Forest, VerevColors.Moss),
) {
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
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = stringResource(R.string.auth_back),
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                )
            }
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
