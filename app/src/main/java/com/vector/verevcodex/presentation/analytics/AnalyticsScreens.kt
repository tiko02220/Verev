package com.vector.verevcodex.presentation.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.reports.ReportsViewModel
import com.vector.verevcodex.presentation.theme.VerevColors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun AnalyticsDashboardScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onOpenCustomerAnalytics: () -> Unit = {},
    onOpenRevenueAnalytics: () -> Unit = {},
    onOpenPromotionAnalytics: () -> Unit = {},
    onOpenProgramAnalytics: () -> Unit = {},
    onOpenStaffAnalytics: () -> Unit = {},
    viewModel: AnalyticsViewModel = hiltViewModel(),
    reportsViewModel: ReportsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val reportsState = reportsViewModel.uiState.collectAsStateWithLifecycle().value
    var showExportSheet by rememberSaveable { mutableStateOf(false) }
    var showAutoReportSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(reportsState.latestExport, reportsState.isExporting, showExportSheet) {
        if (showExportSheet && !reportsState.isExporting && reportsState.latestExport != null) {
            showExportSheet = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 14.dp,
            bottom = contentPadding.calculateBottomPadding() + 84.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            AnalyticsOverviewHeader(
                onOpenReports = {
                    reportsViewModel.clearError()
                    reportsViewModel.clearLatestExport()
                    showExportSheet = true
                },
                onOpenSettings = { showAutoReportSheet = true },
            )
        }
        item {
            AnalyticsRangeSelector(
                selectedRange = state.selectedRange,
                onRangeSelected = viewModel::updateRange,
            )
        }
        if (state.isLoading) {
            item { AnalyticsLoadingStateCard() }
        } else state.businessAnalytics?.let { analytics ->
            item { AnalyticsOverviewHero(analytics) }
            item { AnalyticsOverviewMetricStrip(analytics) }
            item { AnalyticsDualTrendCard(analytics, onOpenRevenueAnalytics) }
            item { AnalyticsCustomerGrowthCard(analytics, onOpenCustomerAnalytics) }
            item {
                AnalyticsOverviewActionGrid(
                    analytics = analytics,
                    staffAnalytics = state.staffAnalytics,
                    onOpenRevenueAnalytics = onOpenRevenueAnalytics,
                    onOpenCustomerAnalytics = onOpenCustomerAnalytics,
                    onOpenPromotionAnalytics = onOpenPromotionAnalytics,
                    onOpenProgramAnalytics = onOpenProgramAnalytics,
                    onOpenStaffAnalytics = onOpenStaffAnalytics,
                )
            }
        } ?: item {
            AnalyticsEmptyStateCard(
                title = stringResource(R.string.merchant_analytics_empty_title),
                subtitle = stringResource(R.string.merchant_analytics_empty_subtitle),
                icon = Icons.Default.QueryStats,
            )
        }
    }

    if (showExportSheet) {
        AnalyticsExportSheet(
            uiState = reportsState,
            selectedRange = state.selectedRange,
            onDismiss = { showExportSheet = false },
            onExport = reportsViewModel::export,
            onClearError = reportsViewModel::clearError,
        )
    }

    if (showAutoReportSheet) {
        AnalyticsAutoReportSettingsSheet(
            currentSettings = reportsState.autoSettings,
            onDismiss = { showAutoReportSheet = false },
            onSave = reportsViewModel::updateAutoReportSettings,
        )
    }
}

@Composable
fun StaffAnalyticsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: StaffAnalyticsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    AnalyticsDetailScreenContainer(
        title = stringResource(R.string.merchant_staff_analytics_title),
        subtitle = stringResource(R.string.merchant_staff_analytics_detail_subtitle),
        contentPadding = contentPadding,
        onBack = onBack,
        selectedRange = state.selectedRange,
        onRangeSelected = viewModel::updateRange,
        emptyIcon = Icons.Default.Person,
        isLoading = state.isLoading,
        body = { StaffAnalyticsLeaderboard(it) },
        analytics = state.staffAnalytics.takeIf { it.isNotEmpty() },
    )
}

@Composable
fun CustomerAnalyticsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: CustomerAnalyticsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    AnalyticsDetailScreenContainer(
        title = stringResource(R.string.merchant_analytics_customers_detail_title),
        subtitle = stringResource(R.string.merchant_analytics_customers_detail_subtitle),
        contentPadding = contentPadding,
        onBack = onBack,
        selectedRange = state.selectedRange,
        onRangeSelected = viewModel::updateRange,
        emptyIcon = Icons.Default.Groups,
        isLoading = state.isLoading,
        body = { CustomerAnalyticsDetailContent(it) },
        analytics = state.analytics,
    )
}

@Composable
fun RevenueAnalyticsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: RevenueAnalyticsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    AnalyticsDetailScreenContainer(
        title = stringResource(R.string.merchant_analytics_revenue_detail_title),
        subtitle = stringResource(R.string.merchant_analytics_revenue_detail_subtitle),
        contentPadding = contentPadding,
        onBack = onBack,
        selectedRange = state.selectedRange,
        onRangeSelected = viewModel::updateRange,
        emptyIcon = Icons.Default.Payments,
        isLoading = state.isLoading,
        body = { RevenueAnalyticsDetailContent(it) },
        analytics = state.analytics,
    )
}

@Composable
fun PromotionAnalyticsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: PromotionAnalyticsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    AnalyticsDetailScreenContainer(
        title = stringResource(R.string.merchant_analytics_promotions_detail_title),
        subtitle = stringResource(R.string.merchant_analytics_promotions_detail_subtitle),
        contentPadding = contentPadding,
        onBack = onBack,
        selectedRange = state.selectedRange,
        onRangeSelected = viewModel::updateRange,
        emptyIcon = Icons.Default.Campaign,
        isLoading = state.isLoading,
        body = { PromotionAnalyticsDetailContent(it) },
        analytics = state.analytics,
    )
}

@Composable
fun ProgramAnalyticsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: ProgramAnalyticsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    AnalyticsDetailScreenContainer(
        title = stringResource(R.string.merchant_analytics_programs_detail_title),
        subtitle = stringResource(R.string.merchant_analytics_programs_detail_subtitle),
        contentPadding = contentPadding,
        onBack = onBack,
        selectedRange = state.selectedRange,
        onRangeSelected = viewModel::updateRange,
        emptyIcon = Icons.Default.CardGiftcard,
        isLoading = state.isLoading,
        body = { ProgramAnalyticsDetailContent(it) },
        analytics = state.analytics,
    )
}

@Composable
private fun <T> AnalyticsDetailScreenContainer(
    title: String,
    subtitle: String,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    selectedRange: com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange,
    onRangeSelected: (com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange) -> Unit,
    emptyIcon: androidx.compose.ui.graphics.vector.ImageVector,
    analytics: T?,
    isLoading: Boolean,
    body: @Composable (T) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AnalyticsDetailHeader(
            title = title,
            subtitle = subtitle,
            onBack = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(Brush.horizontalGradient(listOf(VerevColors.Forest, VerevColors.Moss)))
                .statusBarsPadding()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = contentPadding.calculateTopPadding() + 8.dp,
                    bottom = 18.dp,
                ),
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 84.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                AnalyticsRangeSelector(
                    selectedRange = selectedRange,
                    onRangeSelected = onRangeSelected,
                )
            }
            if (isLoading) {
                item { AnalyticsLoadingStateCard() }
            } else analytics?.let { item { body(it) } } ?: item {
                AnalyticsEmptyStateCard(
                    title = stringResource(R.string.merchant_analytics_empty_title),
                    subtitle = stringResource(R.string.merchant_analytics_empty_subtitle),
                    icon = emptyIcon,
                )
            }
        }
    }
}
