package com.vector.verevcodex.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
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
import com.vector.verevcodex.domain.model.scan.ScanMethod
import com.vector.verevcodex.presentation.common.state.UiState
import com.vector.verevcodex.presentation.scan.ScanMethodSheet

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
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        when (val snapshotState = state) {
            UiState.Loading -> {
                item {
                    DashboardStateCard(
                        title = stringResource(R.string.merchant_loading_dashboard_title),
                        subtitle = stringResource(R.string.merchant_loading_dashboard_subtitle),
                    )
                }
            }
            UiState.Empty -> {
                item {
                    DashboardStateCard(
                        title = stringResource(R.string.merchant_empty_dashboard_title),
                        subtitle = stringResource(R.string.merchant_empty_dashboard_subtitle),
                    )
                }
            }
            is UiState.Error -> {
                item {
                    DashboardStateCard(
                        title = stringResource(R.string.merchant_dashboard_error_title),
                        subtitle = snapshotState.message,
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
