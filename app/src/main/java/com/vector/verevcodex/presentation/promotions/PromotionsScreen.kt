package com.vector.verevcodex.presentation.promotions

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.theme.VerevColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

@Composable
fun PromotionsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: PromotionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fieldErrors = state.editorFieldErrors.mapValues { (_, value) -> stringResource(value) }
    val selectedPromotion = state.promotions.firstOrNull { it.id == state.selectedPromotionId }
    val paymentPromotion = state.promotions.firstOrNull { it.id == state.paymentPromotionId }
    val filteredPromotions = state.promotions.filter { it.matchesPromotionFilter(state.selectedFilter) }

    state.editorState?.let { editor ->
        PromotionEditorDialog(
            editorState = editor,
            fieldErrors = fieldErrors,
            isSubmitting = state.isSubmitting,
            onDismiss = viewModel::dismissEditor,
            onNameChange = viewModel::updateName,
            onDescriptionChange = viewModel::updateDescription,
            onStartDateChange = viewModel::updateStartDate,
            onEndDateChange = viewModel::updateEndDate,
            onTypeChange = viewModel::updatePromotionType,
            onValueChange = viewModel::updatePromotionValue,
            onCodeChange = viewModel::updatePromoCode,
            onPaymentFlowEnabledChange = viewModel::updatePaymentFlowEnabled,
            onActiveChange = viewModel::updateActive,
            onTargetSegmentChange = viewModel::updateTargetSegment,
            onTargetDescriptionChange = viewModel::updateTargetDescription,
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
            onDismiss = viewModel::dismissNetworkPromotionPayment,
            onConfirm = viewModel::publishNetworkPaymentConfirmed,
        )
    }

    val promotionsStats = PromotionSummaryStats(
        activeCount = state.promotions.count { it.active },
        totalRevenue = state.promotions.count { it.paymentFlowEnabled }.toDouble(),
        customersReached = if (state.promotions.isEmpty()) 0 else state.promotions.map { it.promotionValue }.average().toInt(),
        averageRedemption = state.promotions.count { it.active }.toFloat(),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VerevColors.AppBackground),
    ) {
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
                onBack = if (selectedPromotion != null) viewModel::closePromotionDetail else onBack,
                onAddPromotion = { viewModel.openCreatePromotion() },
            )
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            color = VerevColors.AppBackground,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 18.dp,
                    bottom = contentPadding.calculateBottomPadding() + 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
        state.messageRes?.let { messageRes ->
            item {
                MerchantPrimaryCard {
                    Text(
                        text = stringResource(messageRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VerevColors.Forest,
                    )
                }
            }
        }
        state.errorRes?.let { errorRes ->
            item {
                MerchantPrimaryCard {
                    Text(
                        text = stringResource(errorRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color(0xFFD94B4B),
                    )
                }
            }
        }
                if (selectedPromotion != null) {
                    item {
                        PromotionDetailCard(
                            promotion = selectedPromotion,
                            onOpenPayment = { viewModel.openNetworkPromotionPayment(selectedPromotion.id) },
                            onEdit = { viewModel.openEditPromotion(selectedPromotion.id) },
                            onDelete = { viewModel.requestDelete(selectedPromotion.id) },
                        )
                    }
                    return@LazyColumn
                }

                item { PromotionTemplateRow(onCreate = viewModel::openCreatePromotion) }
                item { PromotionsFilterRow(selectedFilter = state.selectedFilter, onSelected = viewModel::selectFilter) }

                if (filteredPromotions.isEmpty()) {
                    item { PromotionsEmptyState() }
                } else {
                    items(filteredPromotions, key = { it.id }) { promotion ->
                        PromotionListCard(
                            promotion = promotion,
                            isBusy = state.busyPromotionId == promotion.id,
                            onOpen = { viewModel.openPromotionDetail(promotion.id) },
                            onToggle = { enabled -> viewModel.togglePromotionEnabled(promotion.id, enabled) },
                            onEdit = { viewModel.openEditPromotion(promotion.id) },
                            onDelete = { viewModel.requestDelete(promotion.id) },
                        )
                    }
                }
            }
        }
    }
}
