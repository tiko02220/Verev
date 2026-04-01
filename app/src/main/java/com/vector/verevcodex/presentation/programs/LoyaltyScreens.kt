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
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.presentation.merchant.common.MerchantBackHeader
import com.vector.verevcodex.presentation.merchant.common.MerchantErrorDialog
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
    REWARDS("rewards");

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
    onOpenCouponsManager: () -> Unit = {},
    onOpenCheckinRewards: () -> Unit = {},
    onOpenPurchaseFrequency: () -> Unit = {},
    onOpenReferralRewards: () -> Unit = {},
    onOpenHybridPrograms: () -> Unit = {},
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
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.COUPON -> onOpenCouponsManager()
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.DIGITAL_STAMP -> onOpenCheckinRewards()
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.PURCHASE_FREQUENCY -> onOpenPurchaseFrequency()
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.REFERRAL -> onOpenReferralRewards()
            com.vector.verevcodex.domain.model.common.LoyaltyProgramType.HYBRID -> onOpenHybridPrograms()
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
            onImageUriChange = viewModel::updateRewardImageUri,
            onPickImage = {
                activity?.suppressRelockForTransientSystemUi()
                rewardImagePickerLauncher.launch(arrayOf("image/*"))
            },
            onExpirationEnabledChange = viewModel::updateRewardExpirationEnabled,
            onExpirationDateChange = viewModel::updateRewardExpirationDate,
            onAvailableQuantityChange = viewModel::updateRewardAvailableQuantity,
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

    state.editorState?.let { editor ->
        ProgramEditorSheet(
            editorState = editor,
            selectedStoreName = state.selectedStoreName,
            availableStores = state.stores,
            availablePrograms = state.programs,
            availableRewards = state.rewards,
            fieldErrors = state.editorFieldErrors,
            isSubmitting = state.isSubmitting,
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
            onAnnualRepeatEnabledChange = viewModel::updateEditorAnnualRepeatEnabled,
            onBenefitResetTypeChange = viewModel::updateEditorBenefitResetType,
            onBenefitResetCustomDaysChange = viewModel::updateEditorBenefitResetCustomDays,
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
                start = 20.dp,
                end = 20.dp,
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
                                onToggleEnabled = { enabled -> viewModel.toggleProgramEnabled(program.id, enabled) },
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
                        onToggleRewardEnabled = viewModel::toggleRewardEnabled,
                        onDeleteReward = viewModel::requestDeleteReward,
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
}

private fun LazyListScope.rewardsCatalogItems(
    state: LoyaltyUiState,
    onCreateReward: () -> Unit,
    onEditReward: (String) -> Unit,
    onToggleRewardEnabled: (String, Boolean) -> Unit,
    onDeleteReward: (String) -> Unit,
) {
    if (state.rewards.isNotEmpty()) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.merchant_rewards_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.merchant_rewards_subtitle, state.rewards.size),
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
    } else {
        items(state.rewards, key = { it.id }) { reward ->
            RewardCatalogListItem(
                reward = reward,
                busyRewardId = state.busyRewardId,
                onEditReward = onEditReward,
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
                    onOpenCouponsManager = onOpenCouponsManager,
                    onOpenCheckinRewards = onOpenCheckinRewards,
                    onOpenPurchaseFrequency = onOpenPurchaseFrequency,
                    onOpenReferralRewards = onOpenReferralRewards,
                    onOpenHybridPrograms = onOpenHybridPrograms,
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
            onAnnualRepeatEnabledChange = viewModel::updateEditorAnnualRepeatEnabled,
            onBenefitResetTypeChange = viewModel::updateEditorBenefitResetType,
            onBenefitResetCustomDaysChange = viewModel::updateEditorBenefitResetCustomDays,
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
                    onToggleEnabled = viewModel::toggleProgramEnabled,
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
            onImageUriChange = viewModel::updateRewardImageUri,
            onPickImage = {
                activity?.suppressRelockForTransientSystemUi()
                rewardImagePickerLauncher.launch(arrayOf("image/*"))
            },
            onExpirationEnabledChange = viewModel::updateRewardExpirationEnabled,
            onExpirationDateChange = viewModel::updateRewardExpirationDate,
            onAvailableQuantityChange = viewModel::updateRewardAvailableQuantity,
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
            rewardsCatalogItems(
                state = state,
                onCreateReward = viewModel::openCreateReward,
                onEditReward = viewModel::openEditReward,
                onToggleRewardEnabled = viewModel::toggleRewardEnabled,
                onDeleteReward = viewModel::requestDeleteReward,
            )
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
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProgramsHomeTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                val icon = when (tab) {
                    ProgramsHomeTab.PROGRAMS -> Icons.Default.Loyalty
                    ProgramsHomeTab.REWARDS -> Icons.Default.CardGiftcard
                }
                val title = stringResource(
                    when (tab) {
                        ProgramsHomeTab.PROGRAMS -> R.string.merchant_programs_count_label
                        ProgramsHomeTab.REWARDS -> R.string.merchant_rewards_title
                    },
                )
                val count = when (tab) {
                    ProgramsHomeTab.PROGRAMS -> programCount
                    ProgramsHomeTab.REWARDS -> rewardCount
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onTabSelected(tab) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = formatCompactCount(count),
                            color = if (selected) VerevColors.Gold else VerevColors.Forest.copy(alpha = 0.38f),
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
    onToggleRewardEnabled: (String, Boolean) -> Unit,
    onDeleteReward: (String) -> Unit,
) {
    val isBusy = busyRewardId == reward.id
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

@Composable
private fun RewardCatalogEditorScreen(
    editorState: RewardEditorState,
    fieldErrors: Map<String, Int>,
    isSubmitting: Boolean,
    contentPadding: PaddingValues,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onImageUriChange: (String) -> Unit,
    onPickImage: () -> Unit,
    onExpirationEnabledChange: (Boolean) -> Unit,
    onExpirationDateChange: (String) -> Unit,
    onAvailableQuantityChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    val context = LocalContext.current
    var showExpirationDatePicker by remember { mutableStateOf(false) }
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
                        if (editorState.rewardId == null) R.string.merchant_reward_add_action
                        else R.string.merchant_reward_edit_action,
                    ),
                    subtitle = stringResource(R.string.merchant_rewards_editor_subtitle_compact),
                    onBack = onDismiss,
                )
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
                    RewardImagePickerCard(
                        imageUri = editorState.imageUri,
                        onPickImage = onPickImage,
                        onClearImage = { onImageUriChange("") },
                    )
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
                            if (editorState.rewardId == null) R.string.merchant_reward_add_action
                            else R.string.merchant_save_changes,
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
