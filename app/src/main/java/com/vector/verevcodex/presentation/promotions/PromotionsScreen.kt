package com.vector.verevcodex.presentation.promotions

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.MainActivity
import com.vector.verevcodex.presentation.theme.VerevColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatCompactCurrency
import com.vector.verevcodex.presentation.merchant.common.MerchantErrorDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantLoadingOverlay
import com.vector.verevcodex.presentation.merchant.common.MerchantSuccessDialog

@Composable
fun PromotionsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: PromotionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? MainActivity
    val fieldErrors = state.editorFieldErrors.mapValues { (_, value) -> stringResource(value) }
    val selectedPromotion = state.promotions.firstOrNull { it.id == state.selectedPromotionId }
    val paymentPromotion = state.promotions.firstOrNull { it.id == state.paymentPromotionId }
    val filteredPromotions = state.promotions.filter { it.matchesPromotionFilter(state.selectedFilter) }
    val performanceByPromotion = remember(state.promotions, state.transactions) {
        state.promotions.associate { promotion ->
            promotion.id to promotion.performanceSummary(state.transactions)
        }
    }
    val selectedPromotionPerformance = selectedPromotion?.let { performanceByPromotion[it.id] ?: PromotionPerformanceSummary() }
    val selectedPromotionWeeklyPerformance = selectedPromotion?.weeklyPerformance(state.transactions).orEmpty()
    val selectedPromotionRecentRedemptions = selectedPromotion?.recentRedemptions(
        transactions = state.transactions,
        customers = state.customers,
    ).orEmpty()
    val totalPromotionCustomers = remember(state.promotions, state.transactions) {
        state.promotions
            .flatMap { promotion -> state.transactions.filter(promotion::matchesTransaction).map { it.customerId } }
            .distinct()
            .size
    }
    val averageRate = remember(state.promotions, performanceByPromotion) {
        val activePromotions = state.promotions.filter { it.promotionStatus() == PromotionStatus.ACTIVE }
        if (activePromotions.isEmpty()) {
            0.0
        } else {
            activePromotions.map { promotion -> performanceByPromotion[promotion.id]?.redemptionRate ?: 0.0 }.average()
        }
    }
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            viewModel.updateImageUri(uri.toString())
        }
    }

    state.editorState?.let { editor ->
        PromotionEditorDialog(
            editorState = editor,
            fieldErrors = fieldErrors,
            isSubmitting = state.isSubmitting,
            paymentMethods = state.paymentMethods,
            selectedStoreId = state.selectedStoreId.orEmpty(),
            onDismiss = viewModel::dismissEditor,
            onPickImage = {
                activity?.suppressRelockForTransientSystemUi()
                photoPickerLauncher.launch(arrayOf("image/*"))
            },
            onNameChange = viewModel::updateName,
            onDescriptionChange = viewModel::updateDescription,
            onStartDateChange = viewModel::updateStartDate,
            onEndDateChange = viewModel::updateEndDate,
            onTypeChange = viewModel::updatePromotionType,
            onValueChange = viewModel::updatePromotionValue,
            onMinimumPurchaseAmountChange = viewModel::updateMinimumPurchaseAmount,
            onUsageLimitChange = viewModel::updateUsageLimit,
            onVisibilityChange = viewModel::updateVisibility,
            onBoostLevelChange = viewModel::updateBoostLevel,
            onTargetSegmentChange = viewModel::updateTargetSegment,
            onSave = viewModel::savePromotion,
        )
    }

    state.deleteCandidate?.let { promotion ->
        PromotionDeleteDialog(
            promotionName = promotion.name,
            isSubmitting = state.isSubmitting,
            onDismiss = viewModel::dismissDeleteDialog,
            onConfirm = viewModel::confirmDeletePromotion,
        )
    }

    paymentPromotion?.let { promotion ->
        NetworkPromotionPaymentDialog(
            promotion = promotion,
            paymentMethods = state.paymentMethods,
            onDismiss = viewModel::dismissNetworkPromotionPayment,
            onConfirm = viewModel::publishNetworkPaymentConfirmed,
        )
    }

    val promotionsStats = PromotionSummaryStats(
        activeCount = state.promotions.count { it.active },
        revenueLabel = formatCompactCurrency(state.promotions.sumOf { promotion ->
            performanceByPromotion[promotion.id]?.revenue ?: 0.0
        }),
        customerCountLabel = formatCompactCount(totalPromotionCustomers),
        averageRateLabel = formatPromotionRate(averageRate),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VerevColors.AppBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VerevColors.AppBackground),
        ) {
            if (selectedPromotion == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(VerevColors.ForestDeep, VerevColors.Forest, VerevColors.Moss)))
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    PromotionsHeader(
                        storeName = state.selectedStoreName.ifBlank { stringResource(com.vector.verevcodex.R.string.merchant_select_store) },
                        stats = promotionsStats,
                        onBack = onBack,
                    )
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = if (selectedPromotion == null) {
                    androidx.compose.foundation.shape.RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                } else {
                    androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                },
                color = VerevColors.AppBackground,
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (selectedPromotion != null) Modifier.statusBarsPadding() else Modifier)
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(
                        start = if (selectedPromotion == null) 16.dp else 0.dp,
                        end = if (selectedPromotion == null) 16.dp else 0.dp,
                        top = if (selectedPromotion == null) 18.dp else 0.dp,
                        bottom = contentPadding.calculateBottomPadding() + if (selectedPromotion == null) 96.dp else 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(if (selectedPromotion == null) 18.dp else 14.dp),
                ) {
                    if (selectedPromotion != null) {
                        item {
                            PromotionDetailCard(
                                promotion = selectedPromotion,
                                performance = selectedPromotionPerformance ?: PromotionPerformanceSummary(),
                                weeklyPerformance = selectedPromotionWeeklyPerformance,
                                recentRedemptions = selectedPromotionRecentRedemptions,
                                onOpenPayment = { viewModel.openNetworkPromotionPayment(selectedPromotion.id) },
                                onEdit = { viewModel.openEditPromotion(selectedPromotion.id) },
                                onDelete = { viewModel.requestDelete(selectedPromotion.id) },
                                onBack = viewModel::closePromotionDetail,
                            )
                        }
                        return@LazyColumn
                    }

                    item { PromotionsFilterRow(selectedFilter = state.selectedFilter, onSelected = viewModel::selectFilter) }
                    item { PromotionsCreateButton(onCreate = { viewModel.openCreatePromotion() }) }

                    if (filteredPromotions.isEmpty()) {
                        item { PromotionsEmptyState() }
                    } else {
                        items(filteredPromotions, key = { it.id }) { promotion ->
                            PromotionListCard(
                                promotion = promotion,
                                performance = performanceByPromotion[promotion.id] ?: PromotionPerformanceSummary(),
                                onOpen = { viewModel.openPromotionDetail(promotion.id) },
                            )
                        }
                    }
                }
            }
        }
        MerchantLoadingOverlay(
            isVisible = state.isSubmitting,
            title = stringResource(com.vector.verevcodex.R.string.merchant_loader_promotion_title),
            subtitle = stringResource(com.vector.verevcodex.R.string.merchant_loader_promotion_subtitle),
        )
        state.messageRes?.let { messageRes ->
            MerchantSuccessDialog(
                message = stringResource(messageRes),
                onDismiss = viewModel::dismissFeedback,
            )
        }
        state.errorRes?.let { errorRes ->
            MerchantErrorDialog(
                message = stringResource(errorRes),
                onDismiss = viewModel::dismissFeedback,
            )
        }
    }
}
