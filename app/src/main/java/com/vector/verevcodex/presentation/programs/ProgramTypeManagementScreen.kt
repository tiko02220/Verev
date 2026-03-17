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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.presentation.merchant.common.MerchantErrorDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantSuccessDialog
import com.vector.verevcodex.presentation.theme.VerevColors
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun ProgramTypeManagementScreen(
    type: LoyaltyProgramType,
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    viewModel: LoyaltyViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val spec = type.screenSpec()
    val programs = state.programs.filter { it.type == type }

    val activeEditorState = state.editorState
    if (state.activeSubEditor != null && activeEditorState != null) {
        when (state.activeSubEditor) {
            ProgramSubEditor.TIER_EDIT -> TierEditScreen(
                editorState = activeEditorState,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onSilverThresholdChange = viewModel::updateTierSilverThreshold,
                onGoldThresholdChange = viewModel::updateTierGoldThreshold,
                onVipThresholdChange = viewModel::updateTierVipThreshold,
                onTierBonusPercentChange = viewModel::updateTierBonusPercent,
                onSave = viewModel::saveProgram,
            )
            ProgramSubEditor.EARN_RULES_EDIT -> EarnRulesEditScreen(
                editorState = activeEditorState,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onSpendStepAmountChange = viewModel::updatePointsSpendStepAmount,
                onPointsAwardedPerStepChange = viewModel::updatePointsAwardedPerStep,
                onPointsWelcomeBonusChange = viewModel::updatePointsWelcomeBonus,
                onPointsMinimumRedeemChange = viewModel::updatePointsMinimumRedeem,
                onSave = viewModel::saveProgram,
            )
            ProgramSubEditor.REWARD_EDIT -> RewardEditScreen(
                editorState = activeEditorState,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onCouponNameChange = viewModel::updateCouponName,
                onCouponPointsCostChange = viewModel::updateCouponPointsCost,
                onCouponDiscountAmountChange = viewModel::updateCouponDiscountAmount,
                onCouponMinimumSpendAmountChange = viewModel::updateCouponMinimumSpendAmount,
                onSave = viewModel::saveProgram,
            )
            ProgramSubEditor.CASHBACK_EDIT -> CashbackEditScreen(
                editorState = activeEditorState,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onCashbackPercentChange = viewModel::updateCashbackPercent,
                onCashbackMinimumSpendAmountChange = viewModel::updateCashbackMinimumSpendAmount,
                onSave = viewModel::saveProgram,
            )
            ProgramSubEditor.CHECKIN_EDIT -> CheckInEditScreen(
                editorState = activeEditorState,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onCheckInVisitsRequiredChange = viewModel::updateCheckInVisitsRequired,
                onCheckInRewardPointsChange = viewModel::updateCheckInRewardPoints,
                onCheckInRewardNameChange = viewModel::updateCheckInRewardName,
                onSave = viewModel::saveProgram,
            )
            ProgramSubEditor.FREQUENCY_EDIT -> FrequencyEditScreen(
                editorState = activeEditorState,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onPurchaseFrequencyCountChange = viewModel::updatePurchaseFrequencyCount,
                onPurchaseFrequencyWindowDaysChange = viewModel::updatePurchaseFrequencyWindowDays,
                onPurchaseFrequencyRewardPointsChange = viewModel::updatePurchaseFrequencyRewardPoints,
                onPurchaseFrequencyRewardNameChange = viewModel::updatePurchaseFrequencyRewardName,
                onSave = viewModel::saveProgram,
            )
            ProgramSubEditor.REFERRAL_EDIT -> ReferralEditScreen(
                editorState = activeEditorState,
                fieldErrors = state.editorFieldErrors,
                onBack = viewModel::closeProgramSubEditor,
                onReferralReferrerRewardPointsChange = viewModel::updateReferralReferrerRewardPoints,
                onReferralRefereeRewardPointsChange = viewModel::updateReferralRefereeRewardPoints,
                onReferralCodePrefixChange = viewModel::updateReferralCodePrefix,
                onSave = viewModel::saveProgram,
            )
            null -> Unit
        }
        return
    }

    state.editorState?.let { editor ->
        ProgramEditorSheet(
            editorState = editor,
            fieldErrors = state.editorFieldErrors,
            isSubmitting = state.isSubmitting,
            onDismiss = viewModel::dismissEditor,
            onNameChange = viewModel::updateEditorName,
            onDescriptionChange = viewModel::updateEditorDescription,
            onTypeChange = viewModel::updateEditorType,
            onActiveChanged = viewModel::updateEditorActive,
            onPointsSpendStepAmountChange = viewModel::updatePointsSpendStepAmount,
            onPointsAwardedPerStepChange = viewModel::updatePointsAwardedPerStep,
            onPointsWelcomeBonusChange = viewModel::updatePointsWelcomeBonus,
            onPointsMinimumRedeemChange = viewModel::updatePointsMinimumRedeem,
            onCashbackPercentChange = viewModel::updateCashbackPercent,
            onCashbackMinimumSpendAmountChange = viewModel::updateCashbackMinimumSpendAmount,
            onTierSilverThresholdChange = viewModel::updateTierSilverThreshold,
            onTierGoldThresholdChange = viewModel::updateTierGoldThreshold,
            onTierVipThresholdChange = viewModel::updateTierVipThreshold,
            onTierBonusPercentChange = viewModel::updateTierBonusPercent,
            onCouponNameChange = viewModel::updateCouponName,
            onCouponPointsCostChange = viewModel::updateCouponPointsCost,
            onCouponDiscountAmountChange = viewModel::updateCouponDiscountAmount,
            onCouponMinimumSpendAmountChange = viewModel::updateCouponMinimumSpendAmount,
            onCheckInVisitsRequiredChange = viewModel::updateCheckInVisitsRequired,
            onCheckInRewardPointsChange = viewModel::updateCheckInRewardPoints,
            onCheckInRewardNameChange = viewModel::updateCheckInRewardName,
            onPurchaseFrequencyCountChange = viewModel::updatePurchaseFrequencyCount,
            onPurchaseFrequencyWindowDaysChange = viewModel::updatePurchaseFrequencyWindowDays,
            onPurchaseFrequencyRewardPointsChange = viewModel::updatePurchaseFrequencyRewardPoints,
            onPurchaseFrequencyRewardNameChange = viewModel::updatePurchaseFrequencyRewardName,
            onReferralReferrerRewardPointsChange = viewModel::updateReferralReferrerRewardPoints,
            onReferralRefereeRewardPointsChange = viewModel::updateReferralRefereeRewardPoints,
            onReferralCodePrefixChange = viewModel::updateReferralCodePrefix,
            onSave = viewModel::saveProgram,
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
                    colors = type.gradient(),
                )
            }
            item {
                ProgramTypeHeroCard(
                    type = type,
                    spec = spec,
                    storeName = state.selectedStoreName,
                    totalCount = programs.size,
                    activeCount = programs.count { it.active },
                )
            }
            item { ProgramTypeInsightCard(type = type, programs = programs, rewards = state.rewards) }
            item { ProgramTypeGuidanceCard(type = type) }
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
                        isBusy = state.busyProgramId == program.id,
                        onEdit = { viewModel.openEditProgram(program.id) },
                        quickActions = quickActions,
                        onToggleEnabled = { enabled -> viewModel.toggleProgramEnabled(program.id, enabled) },
                        onDelete = { viewModel.requestDelete(program.id) },
                    )
                }
            }
        }
    }
    state.messageRes?.let { messageRes ->
        MerchantSuccessDialog(
            message = stringResource(messageRes),
            onDismiss = viewModel::clearMessage,
        )
    }
    state.formErrorRes?.let { errorRes ->
        MerchantErrorDialog(
            message = stringResource(errorRes),
            onDismiss = viewModel::clearMessage,
        )
    }
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
