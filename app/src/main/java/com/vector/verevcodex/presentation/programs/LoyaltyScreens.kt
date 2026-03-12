package com.vector.verevcodex.presentation.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard
import com.vector.verevcodex.presentation.merchant.common.MerchantPageHeader
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun LoyaltyProgramManagementScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onOpenRewards: () -> Unit = {},
    onOpenCampaigns: () -> Unit = {},
    onOpenPointsRewards: () -> Unit = {},
    onOpenTieredLoyalty: () -> Unit = {},
    onOpenCouponsManager: () -> Unit = {},
    onOpenCheckinRewards: () -> Unit = {},
    onOpenPurchaseFrequency: () -> Unit = {},
    onOpenReferralRewards: () -> Unit = {},
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
            ProgramsHeader(
                totalPrograms = state.programs.size,
                totalRewards = state.rewards.size,
                storeName = state.selectedStoreName,
                onAddProgram = viewModel::openCreateProgram,
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
                        color = androidx.compose.ui.graphics.Color(0xFFD94B4B),
                    )
                }
            }
        }
        item { ProgramsOverviewCard(programs = state.programs, rewards = state.rewards, campaigns = state.campaigns) }
        item { ProgramTemplateSection(onCreateProgram = viewModel::openCreateProgram) }
        item {
            ProgramModulesSection(
                onOpenPointsRewards = onOpenPointsRewards,
                onOpenTieredLoyalty = onOpenTieredLoyalty,
                onOpenCouponsManager = onOpenCouponsManager,
                onOpenCheckinRewards = onOpenCheckinRewards,
                onOpenPurchaseFrequency = onOpenPurchaseFrequency,
                onOpenReferralRewards = onOpenReferralRewards,
            )
        }
        item { ProgramActionRow(onOpenRewards = onOpenRewards, onOpenCampaigns = onOpenCampaigns) }
        item {
            ProgramListSection(
                programs = state.programs,
                busyProgramId = state.busyProgramId,
                onEdit = viewModel::openEditProgram,
                onToggleEnabled = viewModel::toggleProgramEnabled,
                onDelete = viewModel::requestDelete,
            )
        }
        if (state.rewards.isNotEmpty()) item { RewardsPreviewSection(rewards = state.rewards) }
        if (state.campaigns.isNotEmpty()) item { CampaignPreviewSection(campaigns = state.campaigns) }
    }
}

@Composable
fun RewardManagementScreen(
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: LoyaltyViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
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
            MerchantPageHeader(
                title = stringResource(R.string.merchant_rewards_title),
                subtitle = stringResource(R.string.merchant_rewards_subtitle, state.rewards.size),
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MerchantPrimaryCard(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp)) {
                    androidx.compose.material3.Text(
                        text = stringResource(R.string.merchant_rewards_active_label),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.56f),
                    )
                    androidx.compose.material3.Text(
                        text = formatCompactCount(state.rewards.count { it.activeStatus }),
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                        color = VerevColors.Forest,
                    )
                }
                MerchantPrimaryCard(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp)) {
                    androidx.compose.material3.Text(
                        text = stringResource(R.string.merchant_rewards_expiring_label),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.56f),
                    )
                    androidx.compose.material3.Text(
                        text = formatCompactCount(state.rewards.count { it.expirationDate != null }),
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                        color = VerevColors.Forest,
                    )
                }
            }
        }
        if (state.rewards.isEmpty()) {
            item {
                MerchantEmptyStateCard(
                    title = stringResource(R.string.merchant_rewards_empty_title),
                    subtitle = stringResource(R.string.merchant_rewards_empty_subtitle),
                    icon = Icons.Default.CardGiftcard,
                )
            }
        }
        state.rewards.forEach { reward ->
            item {
                MerchantPrimaryCard {
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
                }
            }
        }
    }
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
                CampaignDetailScreen(
                    campaign = selectedCampaign,
                    onBack = { selectedCampaignId = null },
                )
            }
        }
        return
    }

    val filteredCampaigns = state.campaigns.filter { it.matches(selectedFilter) }
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
                MerchantEmptyStateCard(
                    title = stringResource(R.string.merchant_campaigns_empty_filtered_title),
                    subtitle = stringResource(R.string.merchant_campaigns_empty_filtered_subtitle),
                    icon = Icons.Default.Campaign,
                )
            }
        }
    }
}
