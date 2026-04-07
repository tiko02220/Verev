package com.vector.verevcodex.presentation.programs

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.produceState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.CouponBenefitType
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.common.RewardCatalogType
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.presentation.merchant.common.MerchantBackHeader
import com.vector.verevcodex.presentation.merchant.common.MerchantConfirmationDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantErrorDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.MerchantInlineToggle
import com.vector.verevcodex.presentation.merchant.common.MerchantLoadingOverlay
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.navigation.ShellViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import com.vector.verevcodex.presentation.theme.VerevColors
import com.vector.verevcodex.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.net.URL
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp

private enum class ProgramsHomeTab(val routeValue: String) {
    PROGRAMS("programs"),
    REWARDS("rewards"),
    GIVEAWAY("giveaway");

    companion object {
        fun fromRouteValue(value: String?): ProgramsHomeTab =
            entries.firstOrNull { it.routeValue == value } ?: PROGRAMS
    }
}

@Composable
fun LoyaltyProgramManagementScreen(
    contentPadding: PaddingValues = PaddingValues(),
    startOnRewardsTab: Boolean = false,
    onOpenConfiguredPrograms: () -> Unit = {},
    onOpenProgramModules: () -> Unit = {},
    onOpenPointsRewards: () -> Unit = {},
    onOpenTieredLoyalty: () -> Unit = {},
    onOpenCheckinRewards: () -> Unit = {},
    onOpenPurchaseFrequency: () -> Unit = {},
    onOpenReferralRewards: () -> Unit = {},
    viewModel: LoyaltyViewModel = hiltViewModel(),
    shellViewModel: ShellViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    val activity = context as? MainActivity
    val shellState by shellViewModel.uiState.collectAsStateWithLifecycle()
    val visiblePrograms = state.programs
    val canManagePrograms = shellState.currentUser?.permissions?.managePrograms == true
    var selectedTabRoute by rememberSaveable(startOnRewardsTab) {
        mutableStateOf(
            if (startOnRewardsTab) ProgramsHomeTab.REWARDS.routeValue
            else ProgramsHomeTab.PROGRAMS.routeValue,
        )
    }
    val selectedTab = ProgramsHomeTab.fromRouteValue(selectedTabRoute)
    val rewardImagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            viewModel.updateRewardImageUri(it.toString())
        }
    }

    LaunchedEffect(state.messageRes) {
        if (state.messageRes != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearMessage()
        }
    }

    fun openProgram(programType: com.vector.verevcodex.domain.model.common.LoyaltyProgramType) {
        when (programType) {
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.POINTS -> onOpenPointsRewards()
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.TIER -> onOpenTieredLoyalty()
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.DIGITAL_STAMP -> onOpenCheckinRewards()
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.PURCHASE_FREQUENCY -> onOpenPurchaseFrequency()
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.REFERRAL -> onOpenReferralRewards()
        }
    }

    state.rewardEditorState?.let { editor ->
        RewardCatalogEditorScreen(
            editorState = editor,
            fieldErrors = state.rewardEditorFieldErrors,
            isSubmitting = state.isSubmitting,
            contentPadding = contentPadding,
            onDismiss = {
                viewModel.dismissRewardEditor()
                selectedTabRoute = ProgramsHomeTab.REWARDS.routeValue
            },
            onNameChange = viewModel::updateRewardName,
            onDescriptionChange = viewModel::updateRewardDescription,
            availableRewards = state.rewards.filter { it.id != editor.rewardId && it.catalogType == RewardCatalogType.REWARD },
            onCatalogTypeChange = viewModel::updateRewardCatalogType,
            onImageUriChange = viewModel::updateRewardImageUri,
            onPickImage = {
                activity?.suppressRelockForTransientSystemUi()
                rewardImagePickerLauncher.launch(arrayOf("image/*"))
            },
            onExpirationEnabledChange = viewModel::updateRewardExpirationEnabled,
            onExpirationDateChange = viewModel::updateRewardExpirationDate,
            onAvailableQuantityChange = viewModel::updateRewardAvailableQuantity,
            onCouponCodeChange = viewModel::updateCouponCode,
            onCouponBenefitTypeChange = viewModel::updateCouponBenefitType,
            onCouponDiscountPercentChange = viewModel::updateCouponDiscountPercent,
            onCouponBonusPointsChange = viewModel::updateCouponBonusPoints,
            onCouponRewardSelected = viewModel::updateCouponRewardSelection,
            onSave = viewModel::saveReward,
        )
        state.formErrorRes?.let { errorRes ->
            MerchantErrorDialog(
                message = stringResource(errorRes),
                onDismiss = viewModel::clearMessage,
            )
        }
        return
    }

    state.giveawayEditorState?.let { editor ->
        GiveawayEditorScreen(
            editorState = editor,
            fieldErrors = state.giveawayEditorFieldErrors,
            isSubmitting = state.isSubmitting,
            contentPadding = contentPadding,
            availableTierNames = state.programs
                .filter { it.configuration.tierTrackingEnabled }
                .flatMap { it.configuration.tierRule.sortedLevels.map { level -> level.name } }
                .distinct(),
            customers = state.customers,
            onDismiss = {
                viewModel.dismissGiveawayEditor()
                selectedTabRoute = ProgramsHomeTab.GIVEAWAY.routeValue
            },
            onNameChange = viewModel::updateGiveawayName,
            onDescriptionChange = viewModel::updateGiveawayDescription,
            onGiveawayTypeChange = viewModel::updateGiveawayType,
            onBonusPointsChange = viewModel::updateGiveawayBonusPoints,
            onDiscountPercentChange = viewModel::updateGiveawayDiscountPercent,
            onCouponBenefitTypeChange = viewModel::updateGiveawayCouponBenefitType,
            onCouponDiscountPercentChange = viewModel::updateGiveawayCouponDiscountPercent,
            onCouponBonusPointsChange = viewModel::updateGiveawayCouponBonusPoints,
            onCouponAvailableQuantityChange = viewModel::updateGiveawayCouponAvailableQuantity,
            onSendModeChange = viewModel::updateGiveawaySendMode,
            onScheduledDateChange = viewModel::updateGiveawayScheduledDate,
            onExpirationEnabledChange = viewModel::updateGiveawayExpirationEnabled,
            onExpirationDateChange = viewModel::updateGiveawayExpirationDate,
            onAudienceAllChange = viewModel::updateGiveawayAudienceAll,
            onAudienceGenderChange = viewModel::updateGiveawayAudienceGender,
            onAudienceAgeMinChange = viewModel::updateGiveawayAudienceAgeMin,
            onAudienceAgeMaxChange = viewModel::updateGiveawayAudienceAgeMax,
            onAudienceTierChange = viewModel::updateGiveawayAudienceTier,
            onCouponCodeChange = viewModel::updateGiveawayCouponCode,
            onSave = viewModel::saveGiveaway,
        )
        state.formErrorRes?.let { errorRes ->
            MerchantErrorDialog(
                message = stringResource(errorRes),
                onDismiss = viewModel::clearMessage,
            )
        }
        return
    }

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
            onOpenRewardsCatalog = { selectedTabRoute = ProgramsHomeTab.REWARDS.routeValue },
            onApplyEditorValidationErrors = viewModel::applyEditorValidationErrors,
            onSave = viewModel::saveProgram,
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
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 24.dp,
                bottom = contentPadding.calculateBottomPadding() + 112.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.merchant_programs_title),
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.merchant_programs_rewards_subtitle),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = VerevColors.Forest.copy(alpha = 0.66f),
                    )
                }
            }
            item {
                ProgramsHomeTabSelector(
                    selectedTab = selectedTab,
                    programCount = visiblePrograms.size,
                    rewardCount = state.rewards.size,
                    giveawayCount = state.campaigns.size,
                    onTabSelected = { selectedTabRoute = it.routeValue },
                )
            }
            state.messageRes?.let { messageRes ->
                item {
                    ProgramsFeedbackBanner(message = stringResource(messageRes))
                }
            }
            when {
                state.isLoading -> {
                    item {
                        ProgramsListSkeleton(canManagePrograms = canManagePrograms)
                    }
                }
                selectedTab == ProgramsHomeTab.PROGRAMS -> {
                    if (visiblePrograms.isEmpty()) {
                        item {
                            ProgramsModuleEmptyCard(
                                title = stringResource(R.string.merchant_programs_empty_title),
                                subtitle = stringResource(R.string.merchant_programs_empty_subtitle),
                                icon = Icons.Default.Loyalty,
                            )
                        }
                    } else {
                        items(visiblePrograms, key = { it.id }) { program ->
                            ProgramListItem(
                                program = program,
                                isBusy = state.busyProgramId == program.id,
                                onEdit = if (canManagePrograms) ({ openProgram(program.type) }) else null,
                                onToggleEnabled = { enabled -> viewModel.requestProgramToggle(program.id, enabled) },
                                onDelete = if (canManagePrograms) ({ viewModel.requestDelete(program.id) }) else null,
                                canManagePrograms = canManagePrograms,
                            )
                        }
                    }
                    if (canManagePrograms) {
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
                ProgramsHomeTab.REWARDS == selectedTab -> {
                    rewardsCatalogItems(
                        state = state,
                        onCreateReward = {
                            selectedTabRoute = ProgramsHomeTab.REWARDS.routeValue
                            viewModel.openCreateReward()
                        },
                        onEditReward = viewModel::openEditReward,
                        onRefreshReward = viewModel::openCreateReward,
                        onToggleRewardEnabled = viewModel::toggleRewardEnabled,
                        onDeleteReward = viewModel::requestDeleteReward,
                    )
                }
                ProgramsHomeTab.GIVEAWAY == selectedTab -> {
                    giveawayItems(
                        state = state,
                        onCreateGiveaway = {
                            selectedTabRoute = ProgramsHomeTab.GIVEAWAY.routeValue
                            viewModel.openCreateGiveaway()
                        },
                        onEditGiveaway = viewModel::openEditGiveaway,
                        onRefreshGiveaway = viewModel::openCreateGiveaway,
                        onToggleGiveawayEnabled = viewModel::toggleGiveawayEnabled,
                        onDeleteGiveaway = viewModel::requestDeleteGiveaway,
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
    state.giveawayDeleteCandidate?.let { campaign ->
        MerchantConfirmationDialog(
            title = stringResource(R.string.merchant_giveaway_delete_title),
            message = stringResource(R.string.merchant_giveaway_delete_message, campaign.name),
            confirmLabel = stringResource(R.string.merchant_delete_action),
            dismissLabel = stringResource(R.string.auth_cancel),
            onDismiss = viewModel::dismissGiveawayDeleteDialog,
            onConfirm = viewModel::confirmDeleteGiveaway,
        )
    }
}

private fun LazyListScope.rewardsCatalogItems(
    state: LoyaltyUiState,
    onCreateReward: () -> Unit,
    onEditReward: (String) -> Unit,
    onRefreshReward: (com.vector.verevcodex.domain.model.loyalty.Reward) -> Unit,
    onToggleRewardEnabled: (String, Boolean) -> Unit,
    onDeleteReward: (String) -> Unit,
) {
    val activeItems = state.rewards.filterNot(::isEndedReward)
    val endedItems = state.rewards.filter(::isEndedReward)
    if (state.rewards.isNotEmpty()) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.merchant_rewards_active_section_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.merchant_rewards_active_section_subtitle, activeItems.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.62f),
                )
            }
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
    } else if (activeItems.isEmpty()) {
        item {
            ProgramsEmptyStateCard(
                title = stringResource(R.string.merchant_rewards_empty_title),
                subtitle = stringResource(R.string.merchant_rewards_empty_subtitle),
                icon = Icons.Default.CardGiftcard,
            )
        }
    } else {
        items(activeItems, key = { it.id }) { reward ->
            RewardCatalogListItem(
                reward = reward,
                busyRewardId = state.busyRewardId,
                onEditReward = onEditReward,
                onRefreshReward = onRefreshReward,
                onToggleRewardEnabled = onToggleRewardEnabled,
                onDeleteReward = onDeleteReward,
            )
        }
    }
    item {
        Button(
            onClick = onCreateReward,
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
                text = stringResource(R.string.merchant_reward_add_action),
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
    if (endedItems.isNotEmpty()) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.merchant_rewards_ended_section_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.merchant_rewards_ended_section_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.62f),
                )
            }
        }
        items(endedItems, key = { "ended_${it.id}" }) { reward ->
            RewardCatalogListItem(
                reward = reward,
                busyRewardId = state.busyRewardId,
                onEditReward = onEditReward,
                onRefreshReward = onRefreshReward,
                onToggleRewardEnabled = onToggleRewardEnabled,
                onDeleteReward = onDeleteReward,
            )
        }
    }
}

private fun LazyListScope.giveawayItems(
    state: LoyaltyUiState,
    onCreateGiveaway: () -> Unit,
    onEditGiveaway: (String) -> Unit,
    onRefreshGiveaway: (com.vector.verevcodex.domain.model.promotions.Campaign) -> Unit,
    onToggleGiveawayEnabled: (String, Boolean) -> Unit,
    onDeleteGiveaway: (String) -> Unit,
) {
    if (state.campaigns.isEmpty()) {
        item {
            ProgramsEmptyStateCard(
                title = stringResource(R.string.merchant_giveaway_empty_title),
                subtitle = stringResource(R.string.merchant_giveaway_empty_subtitle),
                icon = Icons.Default.Campaign,
            )
        }
    } else {
        items(state.campaigns, key = { it.id }) { campaign ->
            GiveawayListItem(
                campaign = campaign,
                isBusy = state.busyCampaignId == campaign.id,
                onEdit = onEditGiveaway,
                onRefresh = onRefreshGiveaway,
                onToggleEnabled = onToggleGiveawayEnabled,
                onDelete = onDeleteGiveaway,
            )
        }
    }
    item {
        Button(
            onClick = onCreateGiveaway,
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
                text = stringResource(R.string.merchant_giveaway_add_action),
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun GiveawayListItem(
    campaign: com.vector.verevcodex.domain.model.promotions.Campaign,
    isBusy: Boolean,
    onEdit: (String) -> Unit,
    onRefresh: (com.vector.verevcodex.domain.model.promotions.Campaign) -> Unit,
    onToggleEnabled: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
) {
    val actionSent = giveawayHasBeenSent(campaign)
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = campaign.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = giveawayOutcomeSummary(campaign),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VerevColors.Forest.copy(alpha = 0.72f),
                    )
                }
                MerchantStatusPill(
                    text = giveawayStatusLabel(campaign),
                    backgroundColor = when {
                        giveawayIsExpired(campaign) -> VerevColors.Forest.copy(alpha = 0.08f)
                        actionSent -> VerevColors.Moss.copy(alpha = 0.18f)
                        !campaign.active -> VerevColors.Forest.copy(alpha = 0.08f)
                        giveawaySendDate(campaign).isAfter(LocalDate.now()) -> VerevColors.Gold.copy(alpha = 0.18f)
                        else -> VerevColors.Forest.copy(alpha = 0.08f)
                    },
                    contentColor = VerevColors.Forest,
                )
            }
            Text(
                text = campaign.description,
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.62f),
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                giveawayMetaRow(
                    label = stringResource(R.string.merchant_giveaway_send_label),
                    value = if (campaign.sendMode == com.vector.verevcodex.domain.model.promotions.GiveawaySendMode.IMMEDIATE) {
                        stringResource(R.string.merchant_giveaway_send_immediately)
                    } else {
                        stringResource(R.string.merchant_giveaway_send_scheduled_value, (campaign.scheduledDate ?: campaign.startDate).toString())
                    },
                )
                giveawayMetaRow(
                    label = stringResource(R.string.merchant_giveaway_audience_label),
                    value = giveawayAudienceSummary(campaign),
                )
                if (campaign.expirationEnabled) {
                    giveawayMetaRow(
                        label = stringResource(R.string.merchant_giveaway_expiration_title),
                        value = campaign.expirationDate?.toString().orEmpty(),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = { onEdit(campaign.id) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(stringResource(R.string.merchant_edit_action))
                }
                OutlinedButton(
                    onClick = { onRefresh(campaign) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(stringResource(R.string.merchant_reward_refresh_action))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { onDelete(campaign.id) }) {
                    Text(stringResource(R.string.merchant_delete_action), color = VerevColors.Forest)
                }
                if (actionSent || giveawayIsExpired(campaign)) {
                    Text(
                        text = if (giveawayIsExpired(campaign)) "Expired" else "Action sent",
                        style = MaterialTheme.typography.labelMedium,
                        color = VerevColors.Forest.copy(alpha = 0.72f),
                        fontWeight = FontWeight.Medium,
                    )
                } else {
                    MerchantInlineToggle(
                        checked = campaign.active,
                        onCheckedChange = { if (!isBusy) onToggleEnabled(campaign.id, it) },
                        enabled = !isBusy,
                    )
                }
            }
        }
    }
}

@Composable
private fun giveawayMetaRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = VerevColors.Forest.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest,
        )
    }
}

@Composable
private fun GiveawayEditorScreen(
    editorState: GiveawayEditorState,
    fieldErrors: Map<String, Int>,
    isSubmitting: Boolean,
    contentPadding: PaddingValues,
    availableTierNames: List<String>,
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onGiveawayTypeChange: (com.vector.verevcodex.domain.model.promotions.GiveawayType) -> Unit,
    onBonusPointsChange: (String) -> Unit,
    onDiscountPercentChange: (String) -> Unit,
    onCouponBenefitTypeChange: (CouponBenefitType) -> Unit,
    onCouponDiscountPercentChange: (String) -> Unit,
    onCouponBonusPointsChange: (String) -> Unit,
    onCouponAvailableQuantityChange: (String) -> Unit,
    onSendModeChange: (com.vector.verevcodex.domain.model.promotions.GiveawaySendMode) -> Unit,
    onScheduledDateChange: (String) -> Unit,
    onExpirationEnabledChange: (Boolean) -> Unit,
    onExpirationDateChange: (String) -> Unit,
    onAudienceAllChange: (Boolean) -> Unit,
    onAudienceGenderChange: (String) -> Unit,
    onAudienceAgeMinChange: (String) -> Unit,
    onAudienceAgeMaxChange: (String) -> Unit,
    onAudienceTierChange: (String?) -> Unit,
    onCouponCodeChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    val context = LocalContext.current
    var showScheduledDatePicker by remember { mutableStateOf(false) }
    var showExpirationDatePicker by remember { mutableStateOf(false) }
    val affectedCustomerCount = remember(editorState, customers) {
        calculateGiveawayAudienceCount(editorState, customers)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = VerevColors.AppBackground,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = contentPadding.calculateTopPadding() + 24.dp,
                    bottom = contentPadding.calculateBottomPadding() + 112.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    MerchantBackHeader(
                        title = stringResource(
                            if (editorState.campaignId == null) R.string.merchant_giveaway_editor_title_create
                            else R.string.merchant_giveaway_editor_title_edit,
                        ),
                        subtitle = stringResource(R.string.merchant_giveaway_editor_subtitle),
                        onBack = onDismiss,
                    )
                }
                item {
                    ProgramSectionCard(
                        title = stringResource(R.string.merchant_giveaway_basics_title),
                        subtitle = stringResource(R.string.merchant_giveaway_basics_subtitle),
                    ) {
                        MerchantFormField(
                            value = editorState.name,
                            onValueChange = onNameChange,
                            label = stringResource(R.string.merchant_reward_name_label),
                            leadingIcon = Icons.Default.Campaign,
                            isError = fieldErrors.containsKey(GIVEAWAY_FIELD_NAME),
                            errorText = fieldErrors[GIVEAWAY_FIELD_NAME]?.let { stringResource(it) },
                        )
                        MerchantFormField(
                            value = editorState.description,
                            onValueChange = onDescriptionChange,
                            label = stringResource(R.string.merchant_reward_description_label),
                            leadingIcon = Icons.Default.Description,
                            singleLine = false,
                            isError = fieldErrors.containsKey(GIVEAWAY_FIELD_DESCRIPTION),
                            errorText = fieldErrors[GIVEAWAY_FIELD_DESCRIPTION]?.let { stringResource(it) },
                        )
                    }
                }
                item {
                    ProgramSectionCard(
                        title = stringResource(R.string.merchant_giveaway_type_title),
                        subtitle = stringResource(R.string.merchant_giveaway_type_subtitle),
                    ) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(
                                com.vector.verevcodex.domain.model.promotions.GiveawayType.BONUS_POINTS to R.string.merchant_giveaway_type_bonus_points,
                                com.vector.verevcodex.domain.model.promotions.GiveawayType.DISCOUNT_PERCENT to R.string.merchant_giveaway_type_discount,
                                com.vector.verevcodex.domain.model.promotions.GiveawayType.COUPON to R.string.merchant_giveaway_type_coupon,
                            ).forEach { (type, labelRes) ->
                                MerchantFilterChip(
                                    text = stringResource(labelRes),
                                    selected = editorState.giveawayType == type,
                                    onClick = { onGiveawayTypeChange(type) },
                                )
                            }
                        }
                        when (editorState.giveawayType) {
                            com.vector.verevcodex.domain.model.promotions.GiveawayType.BONUS_POINTS -> MerchantFormField(
                                value = editorState.bonusPointsAmount,
                                onValueChange = onBonusPointsChange,
                                label = stringResource(R.string.merchant_giveaway_bonus_points_label),
                                leadingIcon = Icons.Default.Stars,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                                ),
                                isError = fieldErrors.containsKey(GIVEAWAY_FIELD_BONUS_POINTS),
                                errorText = fieldErrors[GIVEAWAY_FIELD_BONUS_POINTS]?.let { stringResource(it) },
                            )
                            com.vector.verevcodex.domain.model.promotions.GiveawayType.DISCOUNT_PERCENT -> MerchantFormField(
                                value = editorState.discountPercent,
                                onValueChange = onDiscountPercentChange,
                                label = stringResource(R.string.merchant_giveaway_discount_percent_label),
                                leadingIcon = Icons.Default.Percent,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                                ),
                                isError = fieldErrors.containsKey(GIVEAWAY_FIELD_DISCOUNT_PERCENT),
                                errorText = fieldErrors[GIVEAWAY_FIELD_DISCOUNT_PERCENT]?.let { stringResource(it) },
                            )
                            com.vector.verevcodex.domain.model.promotions.GiveawayType.COUPON -> {
                                Text(
                                    text = stringResource(R.string.merchant_giveaway_coupon_setup_note),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VerevColors.Forest.copy(alpha = 0.58f),
                                )
                                MerchantFormField(
                                    value = editorState.couponCode,
                                    onValueChange = onCouponCodeChange,
                                    label = stringResource(R.string.merchant_giveaway_coupon_code_label),
                                    leadingIcon = Icons.Default.Sell,
                                    isError = fieldErrors.containsKey(GIVEAWAY_FIELD_COUPON_CODE),
                                    errorText = fieldErrors[GIVEAWAY_FIELD_COUPON_CODE]?.let { stringResource(it) },
                                )
                                Text(
                                    text = stringResource(R.string.merchant_giveaway_coupon_code_supporting),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VerevColors.Forest.copy(alpha = 0.58f),
                                )
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    listOf(
                                        CouponBenefitType.DISCOUNT_PERCENT to R.string.merchant_coupon_benefit_discount,
                                        CouponBenefitType.BONUS_POINTS to R.string.merchant_coupon_benefit_bonus_points,
                                    ).forEach { (type, labelRes) ->
                                        MerchantFilterChip(
                                            text = stringResource(labelRes),
                                            selected = editorState.couponBenefitType == type,
                                            onClick = { onCouponBenefitTypeChange(type) },
                                        )
                                    }
                                }
                                when (editorState.couponBenefitType) {
                                    CouponBenefitType.DISCOUNT_PERCENT -> MerchantFormField(
                                        value = editorState.couponDiscountPercent,
                                        onValueChange = onCouponDiscountPercentChange,
                                        label = stringResource(R.string.merchant_coupon_discount_percent_label),
                                        leadingIcon = Icons.Default.Percent,
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                                        ),
                                        isError = fieldErrors.containsKey(GIVEAWAY_FIELD_COUPON_DISCOUNT_PERCENT),
                                        errorText = fieldErrors[GIVEAWAY_FIELD_COUPON_DISCOUNT_PERCENT]?.let { stringResource(it) },
                                    )
                                    CouponBenefitType.BONUS_POINTS -> MerchantFormField(
                                        value = editorState.couponBonusPoints,
                                        onValueChange = onCouponBonusPointsChange,
                                        label = stringResource(R.string.merchant_coupon_bonus_points_label),
                                        leadingIcon = Icons.Default.Stars,
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                                        ),
                                        isError = fieldErrors.containsKey(GIVEAWAY_FIELD_COUPON_BONUS_POINTS),
                                        errorText = fieldErrors[GIVEAWAY_FIELD_COUPON_BONUS_POINTS]?.let { stringResource(it) },
                                    )
                                    CouponBenefitType.REWARD -> Unit
                                }
                                MerchantFormField(
                                    value = editorState.couponAvailableQuantity,
                                    onValueChange = onCouponAvailableQuantityChange,
                                    label = stringResource(R.string.merchant_reward_available_count_label),
                                    leadingIcon = Icons.Default.Inventory2,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                                    ),
                                    isError = fieldErrors.containsKey(GIVEAWAY_FIELD_COUPON_QUANTITY),
                                    errorText = fieldErrors[GIVEAWAY_FIELD_COUPON_QUANTITY]?.let { stringResource(it) },
                                )
                            }
                            com.vector.verevcodex.domain.model.promotions.GiveawayType.REWARD -> Unit
                        }
                    }
                }
                item {
                    ProgramSectionCard(
                        title = stringResource(R.string.merchant_giveaway_schedule_title),
                        subtitle = stringResource(R.string.merchant_giveaway_schedule_subtitle),
                    ) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(
                                com.vector.verevcodex.domain.model.promotions.GiveawaySendMode.IMMEDIATE to R.string.merchant_giveaway_send_immediately,
                                com.vector.verevcodex.domain.model.promotions.GiveawaySendMode.SCHEDULED to R.string.merchant_giveaway_send_schedule,
                            ).forEach { (mode, labelRes) ->
                                MerchantFilterChip(
                                    text = stringResource(labelRes),
                                    selected = editorState.sendMode == mode,
                                    onClick = { onSendModeChange(mode) },
                                )
                            }
                        }
                        if (editorState.sendMode == com.vector.verevcodex.domain.model.promotions.GiveawaySendMode.SCHEDULED) {
                            RewardDateSelectionField(
                                value = editorState.scheduledDate,
                                label = stringResource(R.string.merchant_giveaway_send_date_label),
                                supportingText = stringResource(R.string.merchant_giveaway_send_date_supporting),
                                onClick = { showScheduledDatePicker = true },
                                isError = fieldErrors.containsKey(GIVEAWAY_FIELD_SCHEDULED_DATE),
                                errorText = fieldErrors[GIVEAWAY_FIELD_SCHEDULED_DATE]?.let { stringResource(it) },
                            )
                        }
                    }
                }
                item {
                    ProgramSectionCard(
                        title = stringResource(R.string.merchant_giveaway_expiration_title),
                        subtitle = stringResource(R.string.merchant_giveaway_expiration_subtitle),
                    ) {
                        GiveawayTogglePanel(
                            title = stringResource(R.string.merchant_reward_expiration_toggle_title),
                            subtitle = stringResource(R.string.merchant_giveaway_expiration_supporting),
                            checked = editorState.expirationEnabled,
                            onCheckedChange = onExpirationEnabledChange,
                        )
                        if (editorState.expirationEnabled) {
                            RewardDateSelectionField(
                                value = editorState.expirationDate,
                                label = stringResource(R.string.merchant_reward_expiration_label),
                                supportingText = stringResource(R.string.merchant_giveaway_expiration_supporting),
                                onClick = { showExpirationDatePicker = true },
                                isError = fieldErrors.containsKey(GIVEAWAY_FIELD_EXPIRATION_DATE),
                                errorText = fieldErrors[GIVEAWAY_FIELD_EXPIRATION_DATE]?.let { stringResource(it) },
                            )
                        }
                    }
                }
                item {
                    ProgramSectionCard(
                        title = stringResource(R.string.merchant_giveaway_audience_title),
                        subtitle = stringResource(R.string.merchant_giveaway_audience_subtitle),
                    ) {
                        GiveawayTogglePanel(
                            title = stringResource(R.string.merchant_giveaway_audience_all_title),
                            subtitle = stringResource(R.string.merchant_giveaway_audience_all_subtitle),
                            checked = editorState.audienceAll,
                            onCheckedChange = onAudienceAllChange,
                        )
                        CompactGiveawayAudienceCountRow(count = affectedCustomerCount)
                        if (!editorState.audienceAll) {
                            Surface(
                                color = VerevColors.AppBackground,
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, VerevColors.Forest.copy(alpha = 0.10f)),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.merchant_giveaway_gender_title),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = VerevColors.Forest,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        listOf("ALL", "FEMALE", "MALE").forEach { gender ->
                                            MerchantFilterChip(
                                                text = stringResource(
                                                    when (gender) {
                                                        "FEMALE" -> R.string.merchant_add_customer_gender_female
                                                        "MALE" -> R.string.merchant_add_customer_gender_male
                                                        else -> R.string.merchant_target_all_customers
                                                    },
                                                ),
                                                selected = editorState.audienceGender.equals(gender, ignoreCase = true),
                                                onClick = { onAudienceGenderChange(gender) },
                                            )
                                        }
                                    }
                                    Text(
                                        text = stringResource(R.string.merchant_program_age_range_title),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = VerevColors.Forest,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    MerchantFormField(
                                        value = editorState.audienceAgeMin,
                                        onValueChange = onAudienceAgeMinChange,
                                        label = stringResource(R.string.merchant_program_age_min_label),
                                        leadingIcon = Icons.Default.Add,
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                                        ),
                                    )
                                    MerchantFormField(
                                        value = editorState.audienceAgeMax,
                                        onValueChange = onAudienceAgeMaxChange,
                                        label = stringResource(R.string.merchant_program_age_max_label),
                                        leadingIcon = Icons.Default.Remove,
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                                        ),
                                        isError = fieldErrors.containsKey(GIVEAWAY_FIELD_AGE_RANGE),
                                        errorText = fieldErrors[GIVEAWAY_FIELD_AGE_RANGE]?.let { stringResource(it) },
                                    )
                                    if (availableTierNames.isNotEmpty()) {
                                        Text(
                                            text = stringResource(R.string.merchant_giveaway_tier_title),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = VerevColors.Forest,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            MerchantFilterChip(
                                                text = stringResource(R.string.merchant_giveaway_tier_any),
                                                selected = editorState.audienceTierName == null,
                                                onClick = { onAudienceTierChange(null) },
                                            )
                                            availableTierNames.forEach { tierName ->
                                                MerchantFilterChip(
                                                    text = tierName,
                                                    selected = editorState.audienceTierName == tierName,
                                                    onClick = { onAudienceTierChange(tierName) },
                                                )
                                            }
                                        }
                                    }
                                    fieldErrors[GIVEAWAY_FIELD_AUDIENCE]?.let { resId ->
                                        Text(
                                            text = stringResource(resId),
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Button(
                        onClick = onSave,
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(vertical = 18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerevColors.Gold,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = stringResource(
                                if (editorState.campaignId == null) R.string.merchant_giveaway_add_action else R.string.merchant_save_changes,
                            ),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
        if (isSubmitting) {
            MerchantLoadingOverlay(
                isVisible = true,
                title = stringResource(
                    if (editorState.campaignId == null) R.string.merchant_giveaway_add_action else R.string.merchant_save_changes,
                ),
                subtitle = stringResource(R.string.merchant_giveaway_loading_subtitle),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
    if (showScheduledDatePicker) {
        RewardExpirationDatePickerDialog(
            context = context,
            currentValue = editorState.scheduledDate,
            onDismiss = { showScheduledDatePicker = false },
            onDateSelected = {
                onScheduledDateChange(it.toString())
                showScheduledDatePicker = false
            },
        )
    }
    if (showExpirationDatePicker) {
        RewardExpirationDatePickerDialog(
            context = context,
            currentValue = editorState.expirationDate,
            onDismiss = { showExpirationDatePicker = false },
            onDateSelected = {
                onExpirationDateChange(it.toString())
                showExpirationDatePicker = false
            },
        )
    }
}

@Composable
private fun CompactGiveawayAudienceCountRow(count: Int) {
    Surface(
        color = VerevColors.Forest.copy(alpha = 0.04f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, VerevColors.Forest.copy(alpha = 0.06f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.merchant_giveaway_audience_count_title),
                    color = VerevColors.Forest,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.merchant_giveaway_audience_count_subtitle),
                    color = VerevColors.Forest.copy(alpha = 0.58f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = VerevColors.Gold.copy(alpha = 0.16f),
            ) {
                Text(
                    text = formatCompactCount(count),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = VerevColors.Gold,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun GiveawayTogglePanel(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        color = VerevColors.AppBackground,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, VerevColors.Forest.copy(alpha = 0.10f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.width(12.dp))
                MerchantInlineToggle(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.62f),
            )
        }
    }
}

private fun calculateGiveawayAudienceCount(
    editorState: GiveawayEditorState,
    customers: List<Customer>,
): Int = customers.count { customer -> matchesGiveawayAudience(editorState, customer) }

private fun matchesGiveawayAudience(
    editorState: GiveawayEditorState,
    customer: Customer,
): Boolean {
    if (editorState.audienceAll) return true
    when (editorState.audienceGender.uppercase()) {
        "FEMALE" -> if (customer.gender?.name != "FEMALE") return false
        "MALE" -> if (customer.gender?.name != "MALE") return false
    }
    val age = customer.birthDate?.let { java.time.Period.between(it, LocalDate.now()).years }
    editorState.audienceAgeMin.toIntOrNull()?.let { min -> if (age == null || age < min) return false }
    editorState.audienceAgeMax.toIntOrNull()?.let { max -> if (age == null || age > max) return false }
    editorState.audienceTierName?.takeIf { it.isNotBlank() }?.let { tierName ->
        if (!customer.loyaltyTierLabel.equals(tierName, ignoreCase = true)) return false
    }
    return true
}

private fun giveawayStatusLabel(campaign: com.vector.verevcodex.domain.model.promotions.Campaign): String = when {
    giveawayIsExpired(campaign) -> "Expired"
    giveawayHasBeenSent(campaign) && campaign.giveawayType == com.vector.verevcodex.domain.model.promotions.GiveawayType.BONUS_POINTS -> "Completed"
    giveawayHasBeenSent(campaign) -> "Sent"
    !campaign.active -> "Disabled"
    giveawaySendDate(campaign).isAfter(LocalDate.now()) -> "Scheduled"
    else -> "Ready"
}

private fun giveawayIsExpired(campaign: com.vector.verevcodex.domain.model.promotions.Campaign): Boolean =
    campaign.expirationEnabled && (campaign.expirationDate?.isBefore(LocalDate.now()) == true)

private fun giveawaySendDate(campaign: com.vector.verevcodex.domain.model.promotions.Campaign): LocalDate =
    campaign.scheduledDate ?: campaign.startDate

private fun giveawayHasBeenSent(campaign: com.vector.verevcodex.domain.model.promotions.Campaign): Boolean =
    giveawaySendDate(campaign).isBefore(LocalDate.now()) ||
        giveawaySendDate(campaign).isEqual(LocalDate.now())

private fun giveawayOutcomeSummary(campaign: com.vector.verevcodex.domain.model.promotions.Campaign): String = when (campaign.giveawayType) {
    com.vector.verevcodex.domain.model.promotions.GiveawayType.BONUS_POINTS -> "${campaign.bonusPointsAmount ?: 0} bonus points"
    com.vector.verevcodex.domain.model.promotions.GiveawayType.DISCOUNT_PERCENT -> "${campaign.discountPercent?.let { if (it.rem(1.0) == 0.0) it.toInt().toString() else it.toString() } ?: "0"}% discount"
    com.vector.verevcodex.domain.model.promotions.GiveawayType.COUPON -> campaign.rewardName.ifBlank { "Coupon" }
    com.vector.verevcodex.domain.model.promotions.GiveawayType.REWARD -> campaign.rewardName.ifBlank { "Reward" }
    null -> campaign.name
}

private fun giveawayAudienceSummary(campaign: com.vector.verevcodex.domain.model.promotions.Campaign): String {
    if (campaign.audienceAll) return "All customers"
    val parts = mutableListOf<String>()
    when (campaign.audienceGender.uppercase()) {
        "FEMALE" -> parts += "Women"
        "MALE" -> parts += "Men"
    }
    when {
        campaign.audienceAgeMin != null && campaign.audienceAgeMax != null -> parts += "Ages ${campaign.audienceAgeMin}-${campaign.audienceAgeMax}"
        campaign.audienceAgeMin != null -> parts += "Age ${campaign.audienceAgeMin}+"
        campaign.audienceAgeMax != null -> parts += "Up to ${campaign.audienceAgeMax}"
    }
    campaign.audienceTierName?.takeIf { it.isNotBlank() }?.let { parts += "$it tier" }
    return parts.joinToString(" • ").ifBlank { "Filtered customers" }
}

@Composable
fun ProgramModulesScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    onOpenPointsRewards: () -> Unit = {},
    onOpenTieredLoyalty: () -> Unit = {},
    onOpenCheckinRewards: () -> Unit = {},
    onOpenPurchaseFrequency: () -> Unit = {},
    onOpenReferralRewards: () -> Unit = {},
) {
    var infoType by rememberSaveable { mutableStateOf<LoyaltyProgramType?>(null) }

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
                    onOpenCheckinRewards = onOpenCheckinRewards,
                    onOpenPurchaseFrequency = onOpenPurchaseFrequency,
                    onOpenReferralRewards = onOpenReferralRewards,
                    onOpenProgramInfo = { infoType = it },
                )
            }
        }
    }

    infoType?.let { type ->
        ProgramTypeInfoSheet(
            type = type,
            onDismiss = { infoType = null },
        )
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
                .clickable(onClick = onBack)
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = VerevColors.Forest,
            )
            Text(
                text = stringResource(R.string.auth_back),
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
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
    shellViewModel: ShellViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val rewardImagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.updateRewardImageUri(it.toString()) }
    }
    val shellState by shellViewModel.uiState.collectAsStateWithLifecycle()
    val canManagePrograms = shellState.currentUser?.permissions?.managePrograms == true
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
            onApplyEditorValidationErrors = viewModel::applyEditorValidationErrors,
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
                    onEdit = { if (canManagePrograms) viewModel.openEditProgram(it) },
                    onToggleEnabled = viewModel::requestProgramToggle,
                    onDelete = viewModel::requestDelete,
                    canManagePrograms = canManagePrograms,
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
    val context = LocalContext.current
    val activity = context as? MainActivity
    val rewardImagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            viewModel.updateRewardImageUri(it.toString())
        }
    }

    LaunchedEffect(state.messageRes) {
        if (state.messageRes != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearMessage()
        }
    }

    state.rewardEditorState?.let { editor ->
        RewardCatalogEditorScreen(
            editorState = editor,
            fieldErrors = state.rewardEditorFieldErrors,
            isSubmitting = state.isSubmitting,
            contentPadding = contentPadding,
            onDismiss = viewModel::dismissRewardEditor,
            onNameChange = viewModel::updateRewardName,
            onDescriptionChange = viewModel::updateRewardDescription,
            availableRewards = state.rewards.filter { it.id != editor.rewardId && it.catalogType == RewardCatalogType.REWARD },
            onCatalogTypeChange = viewModel::updateRewardCatalogType,
            onImageUriChange = viewModel::updateRewardImageUri,
            onPickImage = {
                activity?.suppressRelockForTransientSystemUi()
                rewardImagePickerLauncher.launch(arrayOf("image/*"))
            },
            onExpirationEnabledChange = viewModel::updateRewardExpirationEnabled,
            onExpirationDateChange = viewModel::updateRewardExpirationDate,
            onAvailableQuantityChange = viewModel::updateRewardAvailableQuantity,
            onCouponCodeChange = viewModel::updateCouponCode,
            onCouponBenefitTypeChange = viewModel::updateCouponBenefitType,
            onCouponDiscountPercentChange = viewModel::updateCouponDiscountPercent,
            onCouponBonusPointsChange = viewModel::updateCouponBonusPoints,
            onCouponRewardSelected = viewModel::updateCouponRewardSelection,
            onSave = viewModel::saveReward,
        )
        state.formErrorRes?.let { errorRes ->
            MerchantErrorDialog(
                message = stringResource(errorRes),
                onDismiss = viewModel::clearMessage,
            )
        }
        return
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
            state.messageRes?.let { messageRes ->
                item {
                    ProgramsFeedbackBanner(message = stringResource(messageRes))
                }
            }
            if (state.isLoading) {
                item {
                    ProgramsListSkeleton(canManagePrograms = true)
                }
            } else {
                rewardsCatalogItems(
                    state = state,
                    onCreateReward = viewModel::openCreateReward,
                    onEditReward = viewModel::openEditReward,
                    onRefreshReward = viewModel::openCreateReward,
                    onToggleRewardEnabled = viewModel::toggleRewardEnabled,
                    onDeleteReward = viewModel::requestDeleteReward,
                )
            }
        }
    }
    state.formErrorRes?.let { errorRes ->
        MerchantErrorDialog(
            message = stringResource(errorRes),
            onDismiss = viewModel::clearMessage,
        )
    }
}

@Composable
private fun ProgramsHomeTabSelector(
    selectedTab: ProgramsHomeTab,
    programCount: Int,
    rewardCount: Int,
    giveawayCount: Int,
    onTabSelected: (ProgramsHomeTab) -> Unit,
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ProgramsHomeTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                val icon = when (tab) {
                    ProgramsHomeTab.PROGRAMS -> Icons.Default.Loyalty
                    ProgramsHomeTab.REWARDS -> Icons.Default.CardGiftcard
                    ProgramsHomeTab.GIVEAWAY -> Icons.Default.Campaign
                }
                val title = stringResource(
                    when (tab) {
                        ProgramsHomeTab.PROGRAMS -> R.string.merchant_programs_tab_title_short
                        ProgramsHomeTab.REWARDS -> R.string.merchant_rewards_tab_title_short
                        ProgramsHomeTab.GIVEAWAY -> R.string.merchant_giveaway_tab_title
                    },
                )
                val count = when (tab) {
                    ProgramsHomeTab.PROGRAMS -> programCount
                    ProgramsHomeTab.REWARDS -> rewardCount
                    ProgramsHomeTab.GIVEAWAY -> giveawayCount
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (selected) VerevColors.Gold.copy(alpha = 0.12f) else Color.Transparent,
                            shape = RoundedCornerShape(14.dp),
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onTabSelected(tab) }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (selected) VerevColors.Forest else VerevColors.Forest.copy(alpha = 0.48f),
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = title,
                            color = if (selected) VerevColors.Forest else VerevColors.Forest.copy(alpha = 0.62f),
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (selected) VerevColors.Gold else VerevColors.Forest.copy(alpha = 0.08f),
                    ) {
                        Text(
                            text = count.toString(),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp),
                            color = if (selected) Color.White else VerevColors.Forest.copy(alpha = 0.58f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (selected) VerevColors.Gold else Color.Transparent,
                                shape = RoundedCornerShape(999.dp),
                            )
                            .padding(vertical = 1.5.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardCatalogListItem(
    reward: com.vector.verevcodex.domain.model.loyalty.Reward,
    busyRewardId: String?,
    onEditReward: (String) -> Unit,
    onRefreshReward: (com.vector.verevcodex.domain.model.loyalty.Reward) -> Unit,
    onToggleRewardEnabled: (String, Boolean) -> Unit,
    onDeleteReward: (String) -> Unit,
) {
    val isBusy = busyRewardId == reward.id
    val isEnded = isEndedReward(reward)
    val density = LocalDensity.current
    val revealWidthPx = with(density) { 104.dp.toPx() }
    val defaultShape = RoundedCornerShape(20.dp)
    var swipeOffset by remember(reward.id) { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var dragStartOffset by remember(reward.id) { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var isDragging by remember(reward.id) { mutableStateOf(false) }
    val animatedOffset by animateFloatAsState(
        targetValue = swipeOffset,
        animationSpec = if (isDragging) tween(durationMillis = 0) else tween(durationMillis = 180),
        label = "rewardSwipeOffset",
    )
    val swipeProgress = (-animatedOffset / revealWidthPx).coerceIn(0f, 1f)
    val swipeAwareShape = RoundedCornerShape(
        topStart = 20.dp,
        bottomStart = 20.dp,
        topEnd = lerp(20.dp, 0.dp, swipeProgress),
        bottomEnd = lerp(20.dp, 0.dp, swipeProgress),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent, defaultShape),
    ) {
        Row(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFFEF4444), defaultShape)
                .padding(end = 24.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                onClick = { onDeleteReward(reward.id) },
                enabled = animatedOffset <= -(revealWidthPx * 0.8f) && !isBusy,
                color = Color.Transparent,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.merchant_reward_delete_action),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.toInt(), 0) }
                .pointerInput(reward.id, isBusy) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            if (!isBusy) {
                                isDragging = true
                                dragStartOffset = swipeOffset
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            if (isBusy) return@detectHorizontalDragGestures
                            swipeOffset = (swipeOffset + dragAmount).coerceIn(-revealWidthPx, 0f)
                        },
                        onDragEnd = {
                            isDragging = false
                            val wasOpen = dragStartOffset <= -(revealWidthPx * 0.85f)
                            val settleThreshold = if (wasOpen) 0.72f else 0.18f
                            swipeOffset = if (swipeOffset <= -(revealWidthPx * settleThreshold)) -revealWidthPx else 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            val wasOpen = dragStartOffset <= -(revealWidthPx * 0.85f)
                            val settleThreshold = if (wasOpen) 0.72f else 0.18f
                            swipeOffset = if (swipeOffset <= -(revealWidthPx * settleThreshold)) -revealWidthPx else 0f
                        },
                    )
                },
            shape = swipeAwareShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    RewardThumbnail(
                        imageUri = reward.imageUri,
                        modifier = Modifier.size(56.dp),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    reward.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = VerevColors.Forest,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                if (reward.description.isNotBlank()) {
                                    Text(
                                        reward.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VerevColors.Forest.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            MerchantStatusPill(
                                text = if (reward.activeStatus) {
                                    stringResource(R.string.merchant_program_active)
                                } else {
                                    stringResource(R.string.merchant_program_disabled)
                                },
                                backgroundColor = if (reward.activeStatus) {
                                    VerevColors.Moss.copy(alpha = 0.14f)
                                } else {
                                    VerevColors.AppBackground
                                },
                                contentColor = if (reward.activeStatus) VerevColors.Moss else VerevColors.Inactive,
                            )
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            MerchantStatusPill(
                                text = stringResource(
                                    if (reward.catalogType == RewardCatalogType.COUPON) {
                                        R.string.merchant_reward_status_coupon
                                    } else {
                                        R.string.merchant_reward_status_reward
                                    },
                                ),
                                backgroundColor = VerevColors.Forest.copy(alpha = 0.08f),
                                contentColor = VerevColors.Forest,
                            )
                            MerchantStatusPill(
                                text = stringResource(
                                    R.string.merchant_reward_available_count_format,
                                    reward.availableQuantity ?: 0,
                                ),
                                backgroundColor = VerevColors.Gold.copy(alpha = 0.12f),
                                contentColor = VerevColors.Gold,
                            )
                            reward.expirationDate?.let {
                                MerchantStatusPill(
                                    text = stringResource(R.string.merchant_reward_expiration_short_format, it.toString()),
                                    backgroundColor = VerevColors.AppBackground,
                                    contentColor = VerevColors.Forest.copy(alpha = 0.7f),
                                )
                            }
                            if (isEnded) {
                                MerchantStatusPill(
                                    text = stringResource(R.string.merchant_reward_status_ended),
                                    backgroundColor = Color(0xFFFEF2F2),
                                    contentColor = Color(0xFFB91C1C),
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = { onEditReward(reward.id) },
                        enabled = !isBusy,
                        color = VerevColors.Forest.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = VerevColors.Forest,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = stringResource(R.string.merchant_program_configure_action),
                                color = VerevColors.Forest,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                    if (isEnded) {
                        Surface(
                            onClick = { onRefreshReward(reward) },
                            enabled = !isBusy,
                            color = VerevColors.Gold.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = VerevColors.Gold,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = stringResource(R.string.merchant_reward_refresh_action),
                                    color = VerevColors.Gold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    } else {
                        MerchantInlineToggle(
                            checked = reward.activeStatus,
                            enabled = !isBusy,
                            onCheckedChange = { onToggleRewardEnabled(reward.id, it) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardCatalogEditorScreen(
    editorState: RewardEditorState,
    fieldErrors: Map<String, Int>,
    isSubmitting: Boolean,
    contentPadding: PaddingValues,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    availableRewards: List<com.vector.verevcodex.domain.model.loyalty.Reward>,
    onCatalogTypeChange: (RewardCatalogType) -> Unit,
    onImageUriChange: (String) -> Unit,
    onPickImage: () -> Unit,
    onExpirationEnabledChange: (Boolean) -> Unit,
    onExpirationDateChange: (String) -> Unit,
    onAvailableQuantityChange: (String) -> Unit,
    onCouponCodeChange: (String) -> Unit,
    onCouponBenefitTypeChange: (CouponBenefitType) -> Unit,
    onCouponDiscountPercentChange: (String) -> Unit,
    onCouponBonusPointsChange: (String) -> Unit,
    onCouponRewardSelected: (String?, String) -> Unit,
    onSave: () -> Unit,
) {
    val context = LocalContext.current
    var showExpirationDatePicker by remember { mutableStateOf(false) }
    var showCouponRewardPicker by remember { mutableStateOf(false) }
    var showNoRewardsDialog by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
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
                MerchantBackHeader(
                    title = stringResource(
                        when {
                            editorState.rewardId == null && editorState.catalogType == RewardCatalogType.COUPON ->
                                R.string.merchant_reward_editor_title_coupon_create
                            editorState.rewardId == null ->
                                R.string.merchant_reward_editor_title_reward_create
                            editorState.catalogType == RewardCatalogType.COUPON ->
                                R.string.merchant_reward_editor_title_coupon_edit
                            else ->
                                R.string.merchant_reward_editor_title_reward_edit
                        },
                    ),
                    subtitle = stringResource(R.string.merchant_rewards_editor_subtitle_compact),
                    onBack = onDismiss,
                )
            }
            item {
                ProgramSectionCard(
                    title = stringResource(R.string.merchant_reward_catalog_type_title),
                    subtitle = stringResource(R.string.merchant_reward_catalog_type_subtitle),
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        RewardCatalogType.entries.forEach { type ->
                            val selected = editorState.catalogType == type
                            Surface(
                                onClick = { onCatalogTypeChange(type) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (selected) VerevColors.Gold.copy(alpha = 0.16f) else VerevColors.AppBackground,
                                border = BorderStroke(1.dp, if (selected) VerevColors.Gold.copy(alpha = 0.28f) else VerevColors.Forest.copy(alpha = 0.10f)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = if (type == RewardCatalogType.REWARD) Icons.Default.CardGiftcard else Icons.Default.Sell,
                                        contentDescription = null,
                                        tint = if (selected) VerevColors.Gold else VerevColors.Forest.copy(alpha = 0.74f),
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = stringResource(
                                            if (type == RewardCatalogType.REWARD) R.string.merchant_reward_catalog_type_reward
                                            else R.string.merchant_reward_catalog_type_coupon,
                                        ),
                                        color = VerevColors.Forest,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                ProgramSectionCard(
                    title = stringResource(R.string.merchant_reward_expiration_section_title),
                    subtitle = "",
                ) {
                    Surface(
                        color = VerevColors.AppBackground,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, VerevColors.Forest.copy(alpha = 0.10f)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = VerevColors.Gold.copy(alpha = 0.16f),
                            ) {
                                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = VerevColors.Gold,
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.merchant_reward_expiration_toggle_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = VerevColors.Forest,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = stringResource(R.string.merchant_reward_expiration_toggle_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VerevColors.Forest.copy(alpha = 0.62f),
                                )
                            }
                            Column(
                                horizontalAlignment = androidx.compose.ui.Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                MerchantInlineToggle(
                                    checked = editorState.expirationEnabled,
                                    onCheckedChange = onExpirationEnabledChange,
                                )
                                Text(
                                    text = stringResource(
                                        if (editorState.expirationEnabled) R.string.merchant_toggle_state_on
                                        else R.string.merchant_toggle_state_off,
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (editorState.expirationEnabled) VerevColors.Gold else VerevColors.Forest.copy(alpha = 0.44f),
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    if (editorState.expirationEnabled) {
                        RewardDateSelectionField(
                            value = editorState.expirationDate,
                            label = stringResource(R.string.merchant_reward_expiration_label),
                            supportingText = stringResource(R.string.merchant_reward_expiration_supporting),
                            onClick = { showExpirationDatePicker = true },
                            isError = fieldErrors.containsKey(REWARD_FIELD_EXPIRY),
                            errorText = fieldErrors[REWARD_FIELD_EXPIRY]?.let { stringResource(it) },
                        )
                    }
                }
            }
            item {
                ProgramSectionCard(
                    title = stringResource(R.string.merchant_rewards_editor_basics_title),
                    subtitle = "",
                ) {
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
                    if (editorState.catalogType == RewardCatalogType.REWARD) {
                        RewardImagePickerCard(
                            imageUri = editorState.imageUri,
                            onPickImage = onPickImage,
                            onClearImage = { onImageUriChange("") },
                        )
                    }
                }
            }
            if (editorState.catalogType == RewardCatalogType.COUPON) {
            item {
                ProgramSectionCard(
                    title = stringResource(R.string.merchant_coupon_benefit_title),
                    subtitle = stringResource(R.string.merchant_coupon_benefit_subtitle),
                ) {
                        MerchantFormField(
                            value = editorState.couponCode,
                            onValueChange = onCouponCodeChange,
                            label = stringResource(R.string.merchant_coupon_code_label),
                            leadingIcon = Icons.Default.Sell,
                            isError = fieldErrors.containsKey(REWARD_FIELD_COUPON_CODE),
                            errorText = fieldErrors[REWARD_FIELD_COUPON_CODE]?.let { stringResource(it) },
                        )
                        Text(
                            text = stringResource(R.string.merchant_coupon_code_supporting),
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.58f),
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            listOf(
                                CouponBenefitType.DISCOUNT_PERCENT to Icons.Default.Percent,
                                CouponBenefitType.BONUS_POINTS to Icons.Default.Stars,
                                CouponBenefitType.REWARD to Icons.Default.CardGiftcard,
                            ).forEach { (type, _) ->
                                MerchantFilterChip(
                                    text = stringResource(
                                        when (type) {
                                            CouponBenefitType.DISCOUNT_PERCENT -> R.string.merchant_coupon_benefit_discount
                                            CouponBenefitType.BONUS_POINTS -> R.string.merchant_coupon_benefit_bonus_points
                                            CouponBenefitType.REWARD -> R.string.merchant_coupon_benefit_reward
                                        },
                                    ),
                                    selected = editorState.couponBenefitType == type,
                                    onClick = { onCouponBenefitTypeChange(type) },
                                )
                            }
                        }
                        when (editorState.couponBenefitType) {
                            CouponBenefitType.DISCOUNT_PERCENT -> MerchantFormField(
                                value = editorState.couponDiscountPercent,
                                onValueChange = onCouponDiscountPercentChange,
                                label = stringResource(R.string.merchant_coupon_discount_percent_label),
                                leadingIcon = Icons.Default.Percent,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                                ),
                                isError = fieldErrors.containsKey(REWARD_FIELD_COUPON_DISCOUNT_PERCENT),
                                errorText = fieldErrors[REWARD_FIELD_COUPON_DISCOUNT_PERCENT]?.let { stringResource(it) },
                            )
                            CouponBenefitType.BONUS_POINTS -> MerchantFormField(
                                value = editorState.couponBonusPoints,
                                onValueChange = onCouponBonusPointsChange,
                                label = stringResource(R.string.merchant_coupon_bonus_points_label),
                                leadingIcon = Icons.Default.Stars,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                                ),
                                isError = fieldErrors.containsKey(REWARD_FIELD_COUPON_BONUS_POINTS),
                                errorText = fieldErrors[REWARD_FIELD_COUPON_BONUS_POINTS]?.let { stringResource(it) },
                            )
                            CouponBenefitType.REWARD -> RewardSelectionField(
                                value = editorState.couponRewardName,
                                label = stringResource(R.string.merchant_coupon_reward_label),
                                supportingText = fieldErrors[REWARD_FIELD_COUPON_REWARD]?.let { stringResource(it) }
                                    ?: stringResource(R.string.merchant_coupon_reward_pick_action),
                                icon = Icons.Default.CardGiftcard,
                                onClick = {
                                    if (availableRewards.isEmpty()) showNoRewardsDialog = true else showCouponRewardPicker = true
                                },
                                isError = fieldErrors.containsKey(REWARD_FIELD_COUPON_REWARD),
                                errorText = fieldErrors[REWARD_FIELD_COUPON_REWARD]?.let { stringResource(it) },
                            )
                        }
                    }
                }
            }
            item {
                ProgramSectionCard(
                    title = stringResource(R.string.merchant_reward_inventory_section_title),
                    subtitle = "",
                ) {
                    MerchantFormField(
                        value = editorState.availableQuantity,
                        onValueChange = onAvailableQuantityChange,
                        label = stringResource(R.string.merchant_reward_available_count_label),
                        leadingIcon = Icons.Default.Inventory2,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        ),
                        isError = fieldErrors.containsKey(REWARD_FIELD_AVAILABLE_QUANTITY),
                        errorText = fieldErrors[REWARD_FIELD_AVAILABLE_QUANTITY]?.let { stringResource(it) },
                    )
                    RewardInventoryHintRow()
                }
            }
            item {
                Button(
                    onClick = onSave,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerevColors.Gold,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        text = stringResource(
                            if (editorState.rewardId == null) R.string.merchant_reward_add_action else R.string.merchant_save_changes,
                        ),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            }
        }
        if (isSubmitting) {
            MerchantLoadingOverlay(
                isVisible = true,
                title = stringResource(
                    if (editorState.rewardId == null) R.string.merchant_reward_add_action
                    else R.string.merchant_save_changes,
                ),
                subtitle = stringResource(R.string.merchant_reward_loading_subtitle),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
    if (showExpirationDatePicker) {
        RewardExpirationDatePickerDialog(
            context = context,
            currentValue = editorState.expirationDate,
            onDismiss = { showExpirationDatePicker = false },
            onDateSelected = {
                onExpirationDateChange(it.toString())
                showExpirationDatePicker = false
            },
        )
    }
    if (showCouponRewardPicker) {
        AlertDialog(
            onDismissRequest = { showCouponRewardPicker = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCouponRewardPicker = false }) {
                    Text(stringResource(R.string.auth_cancel))
                }
            },
            title = { Text(stringResource(R.string.merchant_coupon_reward_label)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableRewards.forEach { reward ->
                        Surface(
                            onClick = {
                                onCouponRewardSelected(reward.id, reward.name)
                                showCouponRewardPicker = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = VerevColors.AppBackground,
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(reward.name, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
                                Text(reward.description, color = VerevColors.Forest.copy(alpha = 0.62f), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
        )
    }
    if (showNoRewardsDialog) {
        MerchantErrorDialog(
            message = stringResource(R.string.merchant_coupon_reward_empty_message),
            onDismiss = { showNoRewardsDialog = false },
        )
    }
}

private fun isEndedReward(reward: com.vector.verevcodex.domain.model.loyalty.Reward): Boolean {
    val expired = reward.expirationDate?.isBefore(LocalDate.now()) == true
    val outOfStock = (reward.availableQuantity ?: 0) <= 0
    return !reward.activeStatus || expired || outOfStock
}

@Composable
private fun RewardExpirationDatePickerDialog(
    context: android.content.Context,
    currentValue: String,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val initialDate = remember(currentValue) {
        runCatching { LocalDate.parse(currentValue.takeIf { it.isNotBlank() } ?: LocalDate.now().toString()) }
            .getOrElse { LocalDate.now() }
    }
    DisposableEffect(context, initialDate) {
        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth,
        )
        dialog.setOnDismissListener { onDismiss() }
        dialog.show()
        onDispose {
            dialog.setOnDismissListener(null)
            dialog.dismiss()
        }
    }
}

@Composable
private fun RewardDateSelectionField(
    value: String,
    label: String,
    supportingText: String,
    onClick: () -> Unit,
    isError: Boolean,
    errorText: String?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(
                1.dp,
                if (isError) colorResource(R.color.error_red) else colorResource(R.color.text_hint).copy(alpha = 0.18f),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (isError) colorResource(R.color.error_red) else VerevColors.Gold,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = VerevColors.Forest.copy(alpha = 0.56f),
                    )
                    Text(
                        text = value.ifBlank { stringResource(R.string.merchant_reward_expiration_pick_action) },
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (value.isBlank()) VerevColors.Forest.copy(alpha = 0.62f) else VerevColors.Forest,
                        fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.Medium,
                    )
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = VerevColors.Forest.copy(alpha = 0.38f),
                )
            }
        }
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(R.color.error_red),
            )
        } else {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun RewardSelectionField(
    value: String,
    label: String,
    supportingText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isError: Boolean,
    errorText: String?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(
                1.dp,
                if (isError) colorResource(R.color.error_red) else colorResource(R.color.text_hint).copy(alpha = 0.18f),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isError) colorResource(R.color.error_red) else VerevColors.Gold,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = VerevColors.Forest.copy(alpha = 0.56f),
                    )
                    Text(
                        text = value.ifBlank { supportingText },
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (value.isBlank()) VerevColors.Forest.copy(alpha = 0.62f) else VerevColors.Forest,
                        fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.Medium,
                    )
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = VerevColors.Forest.copy(alpha = 0.38f),
                )
            }
        }
        if (errorText != null) {
            Text(
                text = errorText,
                color = colorResource(R.color.error_red),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun RewardImagePickerCard(
    imageUri: String,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = VerevColors.AppBackground,
        border = BorderStroke(1.dp, VerevColors.Forest.copy(alpha = 0.10f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.Top,
        ) {
            RewardThumbnail(
                imageUri = imageUri.takeIf { it.isNotBlank() },
                modifier = Modifier.size(72.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.merchant_reward_image_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (imageUri.isBlank()) {
                        stringResource(R.string.merchant_reward_image_helper_empty)
                    } else {
                        stringResource(R.string.merchant_reward_image_helper_ready)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.62f),
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onPickImage,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Text(
                            text = stringResource(R.string.merchant_reward_image_pick_action),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    if (imageUri.isNotBlank()) {
                        TextButton(
                            onClick = onClearImage,
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                        ) {
                            Text(stringResource(R.string.merchant_reward_image_remove_action))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardInventoryHintRow() {
    Text(
        text = stringResource(R.string.merchant_reward_inventory_hint),
        style = MaterialTheme.typography.bodySmall,
        color = VerevColors.Forest.copy(alpha = 0.6f),
    )
}

@Composable
private fun RewardThumbnail(
    imageUri: String?,
    modifier: Modifier = Modifier.size(72.dp),
) {
    val bitmap = rememberRewardImageBitmap(imageUri)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = VerevColors.AppBackground,
        border = BorderStroke(1.dp, VerevColors.Forest.copy(alpha = 0.08f)),
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = VerevColors.Forest.copy(alpha = 0.32f),
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun rememberRewardImageBitmap(imageUri: String?): ImageBitmap? {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = null, imageUri) {
        value = if (imageUri.isNullOrBlank()) {
            null
        } else {
            runCatching {
                withContext(Dispatchers.IO) {
                    val uri = Uri.parse(imageUri)
                    when (uri.scheme?.lowercase()) {
                        "http", "https" -> URL(imageUri).openStream().use { input ->
                            BitmapFactory.decodeStream(input)?.asImageBitmap()
                        }
                        else -> context.contentResolver.openInputStream(uri)?.use { input ->
                            BitmapFactory.decodeStream(input)?.asImageBitmap()
                        }
                    }
                }
            }.getOrNull()
        }
    }.value
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
