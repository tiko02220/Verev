package com.vector.verevcodex.presentation.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
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
    onOpenStaffAnalytics: () -> Unit = {},
    onOpenReports: () -> Unit = {},
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedRange by rememberSaveable { mutableStateOf(AnalyticsRange.WEEK) }

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
        item { AnalyticsHeader(onOpenReports = onOpenReports, onOpenStaffAnalytics = onOpenStaffAnalytics) }
        item { AnalyticsRangeFilters(selectedRange = selectedRange, onRangeSelected = { selectedRange = it }) }
        state.businessAnalytics?.let { analytics ->
            item { AnalyticsHeroCards(analytics) }
            item { AnalyticsRevenueTrendSection(analytics) }
            item { AnalyticsCustomerGrowthSection(analytics) }
            item { AnalyticsPerformanceSection(analytics) }
            item { AnalyticsInsightSection(analytics, state.staffAnalytics, onOpenStaffAnalytics = onOpenStaffAnalytics) }
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
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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
            AnalyticsHeader(onOpenReports = {}, onOpenStaffAnalytics = {})
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
            state.staffAnalytics.forEach { staff ->
                item {
                    com.vector.verevcodex.presentation.merchant.common.MerchantMenuRow(
                        title = stringResource(R.string.merchant_staff_performance_title),
                        subtitle = stringResource(
                            R.string.merchant_staff_performance_subtitle,
                            com.vector.verevcodex.presentation.merchant.common.formatCompactCount(staff.transactionsProcessed),
                            com.vector.verevcodex.presentation.merchant.common.formatCompactCurrency(staff.revenueHandled),
                        ),
                        icon = Icons.Default.Person,
                        onClick = {},
                        trailing = {
                            androidx.compose.material3.Text(
                                text = com.vector.verevcodex.presentation.merchant.common.formatCompactCurrency(staff.averageTransactionValue),
                                color = com.vector.verevcodex.presentation.theme.VerevColors.Moss,
                            )
                        },
                    )
                }
            }
        }
    }
}
