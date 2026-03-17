package com.vector.verevcodex.presentation.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.RewardType
import com.vector.verevcodex.presentation.merchant.common.MerchantErrorDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.MerchantSuccessDialog
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import androidx.compose.foundation.shape.RoundedCornerShape
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun LoyaltyProgramManagementScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onOpenConfiguredPrograms: () -> Unit = {},
    onOpenProgramModules: () -> Unit = {},
    onOpenPointsRewards: () -> Unit = {},
    onOpenTieredLoyalty: () -> Unit = {},
    onOpenCouponsManager: () -> Unit = {},
    onOpenCheckinRewards: () -> Unit = {},
    onOpenPurchaseFrequency: () -> Unit = {},
    onOpenReferralRewards: () -> Unit = {},
    onOpenHybridPrograms: () -> Unit = {},
    viewModel: LoyaltyViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val visiblePrograms = state.programs

    fun openProgram(programType: com.vector.verevcodex.domain.model.common.LoyaltyProgramType) {
        when (programType) {
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.POINTS -> onOpenPointsRewards()
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.TIER -> onOpenTieredLoyalty()
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.COUPON -> onOpenCouponsManager()
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.DIGITAL_STAMP -> onOpenCheckinRewards()
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.PURCHASE_FREQUENCY -> onOpenPurchaseFrequency()
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.REFERRAL -> onOpenReferralRewards()
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.HYBRID -> onOpenHybridPrograms()
        }
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
                ProgramsHeader(
                    totalPrograms = visiblePrograms.size,
                    totalRewards = state.rewards.size,
                    storeName = state.selectedStoreName,
                    onAddProgram = onOpenProgramModules,
                )
            }
            if (visiblePrograms.isEmpty()) {
                item {
                    ProgramsModuleEmptyCard(
                        title = stringResource(R.string.merchant_programs_empty_title),
                        subtitle = stringResource(R.string.merchant_programs_empty_subtitle),
                        icon = Icons.Default.Loyalty,
                    )
                }
            } else {
                items(visiblePrograms.size) { index ->
                    val program = visiblePrograms[index]
                    ProgramListItem(
                        program = program,
                        isBusy = state.busyProgramId == program.id,
                        onEdit = { openProgram(program.type) },
                        onToggleEnabled = { enabled -> viewModel.toggleProgramEnabled(program.id, enabled) },
                        onDelete = { viewModel.requestDelete(program.id) },
                    )
                }
            }
            item {
                Button(
                    onClick = onOpenProgramModules,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerevColors.Gold,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = stringResource(R.string.merchant_program_add_button),
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.SemiBold,
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

@Composable
fun ProgramModulesScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    onOpenPointsRewards: () -> Unit = {},
    onOpenTieredLoyalty: () -> Unit = {},
    onOpenCouponsManager: () -> Unit = {},
    onOpenCheckinRewards: () -> Unit = {},
    onOpenPurchaseFrequency: () -> Unit = {},
    onOpenReferralRewards: () -> Unit = {},
    onOpenHybridPrograms: () -> Unit = {},
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
                top = contentPadding.calculateTopPadding() + 24.dp,
                bottom = contentPadding.calculateBottomPadding() + 112.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                ProgramsPlainHeader(
                    title = stringResource(R.string.merchant_program_modules_title),
                    subtitle = stringResource(R.string.merchant_programs_empty_subtitle),
                    onBack = onBack,
                )
            }
            item {
                ProgramModulesSection(
                    onOpenPointsRewards = onOpenPointsRewards,
                    onOpenTieredLoyalty = onOpenTieredLoyalty,
                    onOpenCouponsManager = onOpenCouponsManager,
                    onOpenCheckinRewards = onOpenCheckinRewards,
                    onOpenPurchaseFrequency = onOpenPurchaseFrequency,
                    onOpenReferralRewards = onOpenReferralRewards,
                    onOpenHybridPrograms = onOpenHybridPrograms,
                )
            }
        }
    }
}

@Composable
private fun ProgramsPlainHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .background(Color.Transparent, RoundedCornerShape(0.dp))
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.auth_back),
                    modifier = Modifier.padding(start = 6.dp),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                color = VerevColors.Forest.copy(alpha = 0.66f),
            )
        }
    }
}

@Composable
fun ConfiguredProgramsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    onOpenRewards: () -> Unit = {},
    onOpenCampaigns: () -> Unit = {},
    viewModel: LoyaltyViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
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
                ProgramsScreenHeader(
                    title = stringResource(R.string.merchant_programs_configured_title),
                    subtitle = stringResource(
                        R.string.merchant_programs_subtitle,
                        state.programs.size,
                        state.rewards.size,
                    ),
                    onBack = onBack,
                )
            }
            item {
                ProgramListSection(
                    programs = state.programs,
                    busyProgramId = state.busyProgramId,
                    onEdit = viewModel::openEditProgram,
                    onToggleEnabled = viewModel::toggleProgramEnabled,
                    onDelete = viewModel::requestDelete,
                )
            }
        }
    }
}

@Composable
fun RewardManagementScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: LoyaltyViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    state.rewardEditorState?.let { editor ->
        RewardEditorDialog(
            editorState = editor,
            fieldErrors = state.rewardEditorFieldErrors,
            isSubmitting = state.isSubmitting,
            onDismiss = viewModel::dismissRewardEditor,
            onNameChange = viewModel::updateRewardName,
            onDescriptionChange = viewModel::updateRewardDescription,
            onPointsRequiredChange = viewModel::updateRewardPointsRequired,
            onRewardTypeChange = viewModel::updateRewardType,
            onExpirationDateChange = viewModel::updateRewardExpirationDate,
            onUsageLimitChange = viewModel::updateRewardUsageLimit,
            onActiveStatusChange = viewModel::updateRewardActiveStatus,
            onSave = viewModel::saveReward,
        )
    }

    state.rewardDeleteCandidate?.let { reward ->
        AlertDialog(
            onDismissRequest = viewModel::dismissRewardDeleteDialog,
            title = { Text(text = stringResource(R.string.merchant_reward_delete_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.merchant_reward_delete_message,
                        reward.name,
                    ),
                )
            },
            confirmButton = {
                Button(onClick = viewModel::confirmDeleteReward, enabled = !state.isSubmitting) {
                    Text(text = stringResource(R.string.merchant_reward_delete_confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = viewModel::dismissRewardDeleteDialog, enabled = !state.isSubmitting) {
                    Text(text = stringResource(R.string.auth_cancel))
                }
            },
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
            ProgramsDetailHeader(
                title = stringResource(R.string.merchant_rewards_title),
                subtitle = stringResource(R.string.merchant_rewards_subtitle, state.rewards.size),
                onBack = onBack,
            )
        }
        item {
            Button(
                onClick = viewModel::openCreateReward,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerevColors.Forest,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = stringResource(R.string.merchant_reward_add_action),
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RewardMetricCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.merchant_rewards_active_label),
                    value = formatCompactCount(state.rewards.count { it.activeStatus }),
                )
                RewardMetricCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.merchant_rewards_expiring_label),
                    value = formatCompactCount(state.rewards.count { it.expirationDate != null }),
                )
            }
        }
        if (state.rewards.isEmpty()) {
            item {
                ProgramsEmptyStateCard(
                    title = stringResource(R.string.merchant_rewards_empty_title),
                    subtitle = stringResource(R.string.merchant_rewards_empty_subtitle),
                    icon = Icons.Default.CardGiftcard,
                )
            }
        }
        state.rewards.forEach { reward ->
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(28.dp),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                            androidx.compose.material3.Text(reward.name, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = VerevColors.Forest)
                            androidx.compose.material3.Text(reward.description, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.6f))
                        }
                        MerchantStatusPill(
                            text = stringResource(R.string.merchant_points_required_format, reward.pointsRequired),
                            backgroundColor = VerevColors.Gold.copy(alpha = 0.16f),
                            contentColor = VerevColors.Gold,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MerchantStatusPill(
                            text = reward.rewardType.displayName(),
                            backgroundColor = VerevColors.Forest.copy(alpha = 0.08f),
                            contentColor = VerevColors.Forest,
                        )
                        MerchantStatusPill(
                            text = if (reward.activeStatus) stringResource(R.string.merchant_program_active) else stringResource(R.string.merchant_program_disabled),
                            backgroundColor = if (reward.activeStatus) VerevColors.Moss.copy(alpha = 0.16f) else VerevColors.AppBackground,
                            contentColor = if (reward.activeStatus) VerevColors.Moss else VerevColors.Inactive,
                        )
                        reward.expirationDate?.let { expirationDate ->
                            MerchantStatusPill(
                                text = stringResource(R.string.merchant_rewards_expiry_format, expirationDate.toString()),
                                backgroundColor = VerevColors.Tan.copy(alpha = 0.12f),
                                contentColor = VerevColors.Tan,
                            )
                        }
                    }
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = VerevColors.Forest.copy(alpha = 0.42f),
                        )
                        androidx.compose.material3.Text(
                            text = stringResource(R.string.merchant_rewards_usage_limit_format, reward.usageLimit),
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.56f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.openEditReward(reward.id) },
                            modifier = Modifier.weight(1f),
                            enabled = state.busyRewardId != reward.id,
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            Text(stringResource(R.string.merchant_reward_edit_action))
                        }
                        OutlinedButton(
                            onClick = { viewModel.toggleRewardEnabled(reward.id, !reward.activeStatus) },
                            modifier = Modifier.weight(1f),
                            enabled = state.busyRewardId != reward.id,
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            Text(
                                stringResource(
                                    if (reward.activeStatus) {
                                        R.string.merchant_reward_disable_action
                                    } else {
                                        R.string.merchant_reward_enable_action
                                    }
                                )
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.requestDeleteReward(reward.id) },
                            modifier = Modifier.weight(1f),
                            enabled = state.busyRewardId != reward.id,
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            Text(stringResource(R.string.merchant_reward_delete_action))
                        }
                    }
                    }
                }
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

@Composable
private fun RewardEditorDialog(
    editorState: RewardEditorState,
    fieldErrors: Map<String, Int>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPointsRequiredChange: (String) -> Unit,
    onRewardTypeChange: (RewardType) -> Unit,
    onExpirationDateChange: (String) -> Unit,
    onUsageLimitChange: (String) -> Unit,
    onActiveStatusChange: (Boolean) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onSave, enabled = !isSubmitting) {
                Text(
                    text = stringResource(
                        if (editorState.rewardId == null) {
                            R.string.merchant_reward_add_action
                        } else {
                            R.string.merchant_save_changes
                        }
                    ),
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text(text = stringResource(R.string.auth_cancel))
            }
        },
        title = {
            Text(
                text = stringResource(
                    if (editorState.rewardId == null) {
                        R.string.merchant_reward_add_action
                    } else {
                        R.string.merchant_reward_edit_action
                    }
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MerchantFormField(
                    value = editorState.name,
                    onValueChange = onNameChange,
                    label = stringResource(R.string.merchant_reward_name_label),
                    leadingIcon = Icons.Default.CardGiftcard,
                    isError = fieldErrors.containsKey(REWARD_FIELD_NAME),
                    errorText = fieldErrors[REWARD_FIELD_NAME]?.let { stringResource(it) },
                )
                MerchantFormField(
                    value = editorState.description,
                    onValueChange = onDescriptionChange,
                    label = stringResource(R.string.merchant_reward_description_label),
                    leadingIcon = Icons.Default.Description,
                    singleLine = false,
                )
                MerchantFormField(
                    value = editorState.pointsRequired,
                    onValueChange = onPointsRequiredChange,
                    label = stringResource(R.string.merchant_reward_points_required_label),
                    leadingIcon = Icons.Default.CardGiftcard,
                    isError = fieldErrors.containsKey(REWARD_FIELD_POINTS),
                    errorText = fieldErrors[REWARD_FIELD_POINTS]?.let { stringResource(it) },
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.merchant_reward_type_label),
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.Medium,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RewardType.entries.take(2).forEach { type ->
                            RewardTypeChip(
                                type = type,
                                selected = editorState.rewardType == type,
                                onClick = { onRewardTypeChange(type) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RewardType.entries.drop(2).forEach { type ->
                            RewardTypeChip(
                                type = type,
                                selected = editorState.rewardType == type,
                                onClick = { onRewardTypeChange(type) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                MerchantFormField(
                    value = editorState.expirationDate,
                    onValueChange = onExpirationDateChange,
                    label = stringResource(R.string.merchant_reward_expiration_label),
                    leadingIcon = Icons.Default.Schedule,
                    supportingText = stringResource(R.string.merchant_reward_expiration_supporting),
                    isError = fieldErrors.containsKey(REWARD_FIELD_EXPIRY),
                    errorText = fieldErrors[REWARD_FIELD_EXPIRY]?.let { stringResource(it) },
                )
                MerchantFormField(
                    value = editorState.usageLimit,
                    onValueChange = onUsageLimitChange,
                    label = stringResource(R.string.merchant_reward_usage_limit_label),
                    leadingIcon = Icons.Default.Schedule,
                    isError = fieldErrors.containsKey(REWARD_FIELD_USAGE_LIMIT),
                    errorText = fieldErrors[REWARD_FIELD_USAGE_LIMIT]?.let { stringResource(it) },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.merchant_reward_active_toggle_title),
                            style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.merchant_reward_active_toggle_subtitle),
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.6f),
                        )
                    }
                    Switch(checked = editorState.activeStatus, onCheckedChange = onActiveStatusChange)
                }
            }
        },
    )
}

@Composable
private fun RewardTypeChip(
    type: RewardType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip(
        text = type.displayName(),
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun CampaignManagementScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: CampaignsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    var selectedFilter by rememberSaveable { mutableStateOf(CampaignFilter.ALL) }
    var selectedCampaignId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedCampaign = state.campaigns.firstOrNull { it.id == selectedCampaignId }

    if (selectedCampaign != null) {
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
                    CampaignDetailScreen(
                        campaign = selectedCampaign,
                        onBack = { selectedCampaignId = null },
                    )
                }
            }
        }
        return
    }

    val filteredCampaigns = state.campaigns.filter { it.matches(selectedFilter) }
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
            CampaignsHeader(
                storeName = state.selectedStoreName.ifBlank { stringResource(R.string.merchant_select_store) },
                onBack = onBack,
            )
        }
        item { CampaignStatsGrid(state) }
        item { CampaignFilterRow(selectedFilter = selectedFilter, onFilterSelected = { selectedFilter = it }) }
        filteredCampaigns.forEach { campaign ->
            item { CampaignCard(campaign = campaign, onOpen = { selectedCampaignId = campaign.id }) }
        }
        if (filteredCampaigns.isEmpty()) {
            item {
                ProgramsEmptyStateCard(
                    title = stringResource(R.string.merchant_campaigns_empty_filtered_title),
                    subtitle = stringResource(R.string.merchant_campaigns_empty_filtered_subtitle),
                    icon = Icons.Default.Campaign,
                )
            }
        }
    }
    }
}

@Composable
private fun RewardMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(
                            Color.White,
                            VerevColors.AppBackground,
                        ),
                    ),
                    shape = RoundedCornerShape(24.dp),
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            androidx.compose.material3.Text(
                text = label,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.56f),
            )
            androidx.compose.material3.Text(
                text = value,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ProgramsDetailHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.Top,
    ) {
        Surface(
            onClick = onBack,
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = VerevColors.Forest,
                )
                androidx.compose.material3.Text(
                    text = stringResource(R.string.auth_back),
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            androidx.compose.material3.Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            androidx.compose.material3.Text(
                text = subtitle,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.66f),
            )
        }
    }
}

@Composable
private fun ProgramsEmptyStateCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(30.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(
                            Color.White,
                            VerevColors.AppBackground,
                        ),
                    ),
                    shape = RoundedCornerShape(30.dp),
                )
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                color = VerevColors.AppBackground,
                shape = RoundedCornerShape(22.dp),
            ) {
                Row(
                    modifier = Modifier.size(64.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Icon(icon, contentDescription = null, tint = VerevColors.Forest)
                }
            }
            androidx.compose.material3.Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            androidx.compose.material3.Text(
                text = subtitle,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.64f),
            )
        }
    }
}
