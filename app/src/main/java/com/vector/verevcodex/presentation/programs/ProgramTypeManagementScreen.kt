package com.vector.verevcodex.presentation.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Sell
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard
import com.vector.verevcodex.presentation.merchant.common.MerchantBackHeader
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.theme.VerevColors

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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 24.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ProgramsBackHeader(
                title = stringResource(spec.titleRes),
                subtitle = stringResource(spec.subtitleRes),
                onBack = onBack,
                onAddProgram = { viewModel.openCreateProgram(type) },
            )
        }
        state.messageRes?.let { messageRes ->
            item {
                MerchantPrimaryCard {
                    androidx.compose.material3.Text(
                        text = stringResource(messageRes),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = VerevColors.Forest,
                    )
                }
            }
        }
        state.formErrorRes?.let { errorRes ->
            item {
                MerchantPrimaryCard {
                    androidx.compose.material3.Text(
                        text = stringResource(errorRes),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = VerevColors.ErrorText,
                    )
                }
            }
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
                MerchantEmptyStateCard(
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
                    isBusy = state.busyProgramId == program.id,
                    onEdit = { viewModel.openEditProgram(program.id) },
                    onToggleEnabled = { enabled -> viewModel.toggleProgramEnabled(program.id, enabled) },
                    onDelete = { viewModel.requestDelete(program.id) },
                )
            }
        }
    }
}

@Composable
fun BranchProgramsConfigScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    onOpenProgramType: (LoyaltyProgramType) -> Unit,
    viewModel: BranchProgramsConfigViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 24.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
            )
        }
        state.errorRes?.let { errorRes ->
            item {
                MerchantPrimaryCard {
                    androidx.compose.material3.Text(
                        text = stringResource(errorRes),
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

@Composable
private fun ProgramsBackHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onAddProgram: () -> Unit,
    showAddAction: Boolean = true,
) {
    MerchantBackHeader(
        title = title,
        subtitle = subtitle,
        onBack = onBack,
        modifier = Modifier.fillMaxWidth(),
    )
    if (showAddAction) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            androidx.compose.material3.Button(onClick = onAddProgram) {
                androidx.compose.material3.Text(stringResource(R.string.merchant_program_add_button))
            }
        }
    }
}

@Composable
private fun BranchProgramConfigCard(
    card: BranchProgramCardUi,
    onOpen: () -> Unit,
) {
    MerchantPrimaryCard {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            androidx.compose.material3.Icon(
                imageVector = card.type.icon(),
                contentDescription = null,
                tint = VerevColors.Forest,
            )
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                androidx.compose.material3.Text(
                    text = stringResource(card.type.screenSpec().titleRes),
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                )
                androidx.compose.material3.Text(
                    text = stringResource(R.string.merchant_program_type_counts, card.totalCount, card.activeCount),
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.64f),
                )
            }
            androidx.compose.material3.Button(onClick = onOpen) {
                androidx.compose.material3.Text(stringResource(R.string.merchant_branch_programs_open_manager))
            }
        }
    }
}
