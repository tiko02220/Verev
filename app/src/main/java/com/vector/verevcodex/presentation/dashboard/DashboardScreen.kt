package com.vector.verevcodex.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.core.UiState
import com.vector.verevcodex.domain.model.ScanMethod
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard
import com.vector.verevcodex.presentation.scan.ScanMethodSheet
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun DashboardScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onOpenScan: (ScanMethod, Boolean) -> Unit = { _, _ -> },
    onOpenAddCustomer: () -> Unit = {},
    onOpenPromotions: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showScanMethodChooser by remember { mutableStateOf(false) }
    var rememberScanChoice by remember { mutableStateOf(false) }

    if (showScanMethodChooser) {
        ScanMethodSheet(
            rememberChoice = rememberScanChoice,
            onRememberChoiceChanged = { rememberScanChoice = it },
            onSelectMethod = { method ->
                showScanMethodChooser = false
                onOpenScan(method, rememberScanChoice)
                rememberScanChoice = false
            },
            onDismiss = {
                showScanMethodChooser = false
                rememberScanChoice = false
            },
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
        when (val snapshotState = state) {
            UiState.Loading -> {
                item {
                    MerchantEmptyStateCard(
                        title = stringResource(R.string.merchant_loading_dashboard_title),
                        subtitle = stringResource(R.string.merchant_loading_dashboard_subtitle),
                        icon = Icons.Default.Storefront,
                    )
                }
            }
            UiState.Empty -> {
                item {
                    MerchantEmptyStateCard(
                        title = stringResource(R.string.merchant_empty_dashboard_title),
                        subtitle = stringResource(R.string.merchant_empty_dashboard_subtitle),
                        icon = Icons.Default.Storefront,
                    )
                }
            }
            is UiState.Error -> {
                item {
                    MerchantEmptyStateCard(
                        title = stringResource(R.string.merchant_dashboard_error_title),
                        subtitle = snapshotState.message,
                        icon = Icons.Default.Storefront,
                    )
                }
            }
            is UiState.Success -> {
                val snapshot = snapshotState.data
                item { DashboardOverviewCard(snapshot) }
                item {
                    DashboardQuickActions(
                        onOpenScan = { showScanMethodChooser = true },
                        onOpenAddCustomer = onOpenAddCustomer,
                    )
                }
                item { DashboardPromotionCard(snapshot = snapshot, onOpenPromotions = onOpenPromotions) }
                item { DashboardTodayStats(snapshot) }
            }
        }
    }
}
