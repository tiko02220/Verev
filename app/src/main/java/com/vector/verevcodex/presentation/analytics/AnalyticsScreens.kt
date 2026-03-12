package com.vector.verevcodex.presentation.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard

@Composable
fun AnalyticsDashboardScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onOpenCustomerAnalytics: () -> Unit = {},
    onOpenRevenueAnalytics: () -> Unit = {},
    onOpenPromotionAnalytics: () -> Unit = {},
    onOpenProgramAnalytics: () -> Unit = {},
    onOpenStaffAnalytics: () -> Unit = {},
    onOpenReports: () -> Unit = {},
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            AnalyticsOverviewHeader(
                onOpenReports = onOpenReports,
                onOpenStaffAnalytics = onOpenStaffAnalytics,
            )
        }
        item {
            AnalyticsRangeSelector(
                selectedRange = state.selectedRange,
                onRangeSelected = viewModel::updateRange,
            )
        }
        state.businessAnalytics?.let { analytics ->
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
            MerchantEmptyStateCard(
                title = stringResource(R.string.merchant_analytics_empty_title),
                subtitle = stringResource(R.string.merchant_analytics_empty_subtitle),
                icon = Icons.Default.QueryStats,
            )
        }
    }
}

@Composable
fun StaffAnalyticsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: StaffAnalyticsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            AnalyticsDetailHeader(
                title = stringResource(R.string.merchant_staff_analytics_title),
                subtitle = stringResource(R.string.merchant_staff_analytics_detail_subtitle),
                onBack = onBack,
            )
        }
        if (state.staffAnalytics.isEmpty()) {
            item {
                MerchantEmptyStateCard(
                    title = stringResource(R.string.merchant_staff_analytics_empty_title),
                    subtitle = stringResource(R.string.merchant_staff_analytics_empty_subtitle),
                    icon = Icons.Default.Person,
                )
            }
        } else {
            item { StaffAnalyticsLeaderboard(state.staffAnalytics) }
        }
    }
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
    body: @Composable (T) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            AnalyticsDetailHeader(
                title = title,
                subtitle = subtitle,
                onBack = onBack,
            )
        }
        item {
            AnalyticsRangeSelector(
                selectedRange = selectedRange,
                onRangeSelected = onRangeSelected,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        analytics?.let { item { body(it) } } ?: item {
            MerchantEmptyStateCard(
                title = stringResource(R.string.merchant_analytics_empty_title),
                subtitle = stringResource(R.string.merchant_analytics_empty_subtitle),
                icon = emptyIcon,
            )
        }
    }
}
