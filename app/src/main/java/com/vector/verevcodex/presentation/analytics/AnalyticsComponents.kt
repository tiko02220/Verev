@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.vector.verevcodex.presentation.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.analytics.AnalyticsPoint
import com.vector.verevcodex.domain.model.analytics.AnalyticsSegment
import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.model.analytics.BusinessAnalytics
import com.vector.verevcodex.domain.model.analytics.RevenueAnalyticsDrillDown
import com.vector.verevcodex.domain.model.analytics.StaffAnalytics
import com.vector.verevcodex.domain.model.customer.CustomerAnalyticsDrillDown
import com.vector.verevcodex.domain.model.loyalty.ProgramAnalyticsDrillDown
import com.vector.verevcodex.domain.model.promotions.PromotionAnalyticsDrillDown
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantInteractiveCard
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.merchant.common.MerchantSectionTitle
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatCompactCurrency
import com.vector.verevcodex.presentation.merchant.common.formatPercent
import com.vector.verevcodex.presentation.theme.VerevColors
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.floor
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AnalyticsOverviewHeader(
    onOpenReports: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.merchant_analytics_title),
                fontSize = 24.sp,
                lineHeight = 28.sp,
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(R.string.merchant_analytics_subtitle),
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = VerevColors.Forest.copy(alpha = 0.68f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnalyticsHeaderActionButton(
                icon = Icons.Default.Download,
                tint = VerevColors.Moss,
                onClick = onOpenReports,
            )
            AnalyticsHeaderActionButton(
                icon = Icons.Default.Settings,
                tint = VerevColors.Forest,
                onClick = onOpenSettings,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AnalyticsRangeSelector(
    selectedRange: AnalyticsTimeRange,
    onRangeSelected: (AnalyticsTimeRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(AnalyticsTimeRange.entries.size) { index ->
            val range = AnalyticsTimeRange.entries[index]
            MerchantFilterChip(
                modifier = Modifier.shadow(
                    elevation = if (selectedRange == range) 8.dp else 4.dp,
                    shape = RoundedCornerShape(100.dp),
                    ambientColor = Color.Black.copy(alpha = 0.06f),
                    spotColor = Color.Black.copy(alpha = 0.06f),
                ),
                text = stringResource(range.labelRes()),
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
            )
        }
    }
}

@Composable
internal fun AnalyticsOverviewHero(analytics: BusinessAnalytics) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnalyticsGradientMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.merchant_metric_revenue),
                value = formatCompactCurrency(analytics.totalRevenue),
                supporting = stringResource(
                    R.string.merchant_analytics_metric_growth_value,
                    formatPercent(analytics.revenueGrowthRate),
                ),
                icon = Icons.Default.Payments,
                gradient = listOf(VerevColors.Gold, VerevColors.Tan),
            )
            AnalyticsGradientMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.merchant_metric_customers),
                value = formatCompactCount(analytics.totalCustomers),
                supporting = stringResource(
                    R.string.merchant_analytics_metric_growth_value,
                    formatPercent(analytics.customerGrowthRate),
                ),
                icon = Icons.Default.Groups,
                gradient = listOf(VerevColors.Moss, VerevColors.Forest),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnalyticsNeutralMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.merchant_metric_average_purchase),
                value = formatCompactCurrency(analytics.averagePurchaseValue),
                supporting = stringResource(R.string.merchant_analytics_metric_avg_purchase_supporting),
                icon = Icons.Default.Assessment,
                accent = VerevColors.Moss,
            )
            AnalyticsNeutralMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.merchant_analytics_promotion_roi_title),
                value = formatPercent(analytics.averagePromotionRoi),
                supporting = stringResource(R.string.merchant_analytics_active_promotions_value, analytics.activePromotions),
                icon = Icons.Default.CardGiftcard,
                accent = VerevColors.Gold,
            )
        }
    }
}

@Composable
internal fun AnalyticsOverviewMetricStrip(analytics: BusinessAnalytics) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AnalyticsHighlightChip(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.merchant_metric_retention),
            value = formatPercent(analytics.retentionRate),
        )
        AnalyticsHighlightChip(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.merchant_metric_visits_today),
            value = formatCompactCount(analytics.visitsToday),
        )
        AnalyticsHighlightChip(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.merchant_analytics_top_promotion_short),
            value = analytics.topPromotionName.ifBlank { stringResource(R.string.merchant_analytics_none_label) },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AnalyticsOverviewActionGrid(
    analytics: BusinessAnalytics,
    staffAnalytics: List<StaffAnalytics>,
    onOpenRevenueAnalytics: () -> Unit,
    onOpenCustomerAnalytics: () -> Unit,
    onOpenPromotionAnalytics: () -> Unit,
    onOpenProgramAnalytics: () -> Unit,
    onOpenStaffAnalytics: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_analytics_insights_section))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AnalyticsInsightCard(
                title = stringResource(R.string.merchant_analytics_promotions_detail_title),
                subtitle = stringResource(R.string.merchant_analytics_drilldown_promotion_blurb),
                icon = Icons.Default.Campaign,
                iconBackground = Brush.linearGradient(listOf(VerevColors.Gold.copy(alpha = 0.18f), VerevColors.Tan.copy(alpha = 0.14f))),
                iconTint = VerevColors.Gold,
                stats = listOf(
                    AnalyticsInsightStat(
                        stringResource(R.string.merchant_analytics_total_revenue_short),
                        formatCompactCurrency(analytics.totalRevenue),
                    ),
                    AnalyticsInsightStat(
                        stringResource(R.string.merchant_analytics_average_roi_title),
                        formatPercent(analytics.averagePromotionRoi),
                    ),
                    AnalyticsInsightStat(
                        stringResource(R.string.merchant_analytics_top_short),
                        analytics.topPromotionName.ifBlank { stringResource(R.string.merchant_analytics_none_label) },
                    ),
                ),
                onClick = onOpenPromotionAnalytics,
            )
            AnalyticsInsightCard(
                title = stringResource(R.string.merchant_analytics_customers_detail_title),
                subtitle = stringResource(R.string.merchant_analytics_drilldown_customer_blurb),
                icon = Icons.Default.Groups,
                iconBackground = Brush.linearGradient(listOf(VerevColors.Moss.copy(alpha = 0.18f), VerevColors.Forest.copy(alpha = 0.14f))),
                iconTint = VerevColors.Moss,
                stats = listOf(
                    AnalyticsInsightStat(
                        stringResource(R.string.merchant_metric_retention),
                        formatPercent(analytics.retentionRate),
                    ),
                    AnalyticsInsightStat(
                        stringResource(R.string.merchant_metric_customers),
                        formatCompactCount(analytics.totalCustomers),
                    ),
                    AnalyticsInsightStat(
                        stringResource(R.string.merchant_analytics_top_short),
                        analytics.topCustomerName.ifBlank { stringResource(R.string.merchant_analytics_no_top_customer) },
                    ),
                ),
                onClick = onOpenCustomerAnalytics,
            )
            AnalyticsInsightCard(
                title = stringResource(R.string.merchant_staff_analytics_title),
                subtitle = stringResource(R.string.merchant_analytics_drilldown_staff_blurb),
                icon = Icons.Default.WorkspacePremium,
                iconBackground = Brush.linearGradient(listOf(VerevColors.Tan.copy(alpha = 0.18f), VerevColors.Gold.copy(alpha = 0.12f))),
                iconTint = VerevColors.Tan,
                stats = listOf(
                    AnalyticsInsightStat(
                        stringResource(R.string.merchant_analytics_active_staff_short),
                        formatCompactCount(staffAnalytics.size),
                    ),
                    AnalyticsInsightStat(
                        stringResource(R.string.merchant_analytics_avg_sales_short),
                        formatCompactCurrency(staffAnalytics.firstOrNull()?.averageTransactionValue ?: 0.0),
                    ),
                    AnalyticsInsightStat(
                        stringResource(R.string.merchant_analytics_top_short),
                        formatCompactCurrency(staffAnalytics.firstOrNull()?.revenueHandled ?: 0.0),
                    ),
                ),
                onClick = onOpenStaffAnalytics,
            )
            AnalyticsInsightCard(
                title = stringResource(R.string.merchant_analytics_programs_detail_title),
                subtitle = stringResource(R.string.merchant_analytics_drilldown_program_blurb),
                icon = Icons.Default.QueryStats,
                iconBackground = Brush.linearGradient(listOf(VerevColors.Gold.copy(alpha = 0.18f), VerevColors.Tan.copy(alpha = 0.14f))),
                iconTint = VerevColors.Gold,
                stats = listOf(
                    AnalyticsInsightStat(
                        stringResource(R.string.merchant_metric_revenue),
                        formatCompactCurrency(analytics.totalRevenue),
                    ),
                    AnalyticsInsightStat(
                        stringResource(R.string.merchant_metric_customers),
                        formatCompactCount(analytics.totalCustomers),
                    ),
                    AnalyticsInsightStat(
                        stringResource(R.string.merchant_metric_retention),
                        formatPercent(analytics.retentionRate),
                    ),
                ),
                onClick = onOpenProgramAnalytics,
            )
        }
    }
}

@Composable
internal fun AnalyticsDualTrendCard(
    analytics: BusinessAnalytics,
    onOpenRevenueAnalytics: () -> Unit,
    chartAnimationEpoch: Int,
) {
    AnalyticsOverviewSurface {
        AnalyticsCardHeader(
            title = stringResource(R.string.merchant_analytics_revenue_chart_title),
            subtitle = stringResource(R.string.merchant_analytics_revenue_chart_subtitle),
            actionText = stringResource(R.string.merchant_analytics_view_details),
            onClick = onOpenRevenueAnalytics,
        )
        AnalyticsHeadlineRow(
            primaryTitle = stringResource(R.string.merchant_metric_revenue),
            primaryValue = formatCompactCurrency(analytics.totalRevenue),
            secondaryTitle = stringResource(R.string.merchant_analytics_transaction_count_title),
            secondaryValue = formatCompactCount(analytics.visitsInRange),
        )
        AnalyticsAreaChartFromPoints(
            points = analytics.revenueTrend,
            lineColor = VerevColors.Gold,
            animationEpoch = chartAnimationEpoch,
            valueFormatter = { formatCompactCurrency(it.toDouble()) },
        )
        AnalyticsMiniLegend(
            leftLabel = stringResource(R.string.merchant_analytics_revenue_legend),
            leftColor = VerevColors.Gold,
            rightLabel = stringResource(R.string.merchant_analytics_visit_legend),
            rightColor = VerevColors.Moss,
        )
        AnalyticsBarChartFromPoints(
            points = analytics.visitTrend,
            accent = VerevColors.Moss,
            animationEpoch = chartAnimationEpoch,
            valueFormatter = { formatCompactCount(it.toInt()) },
        )
    }
}

@Composable
internal fun AnalyticsCustomerGrowthCard(
    analytics: BusinessAnalytics,
    onOpenCustomerAnalytics: () -> Unit,
    chartAnimationEpoch: Int,
) {
    AnalyticsOverviewSurface {
        AnalyticsCardHeader(
            title = stringResource(R.string.merchant_analytics_customer_growth_title),
            subtitle = stringResource(R.string.merchant_analytics_customer_growth_subtitle),
            actionText = stringResource(R.string.merchant_analytics_view_details),
            onClick = onOpenCustomerAnalytics,
        )
        AnalyticsHeadlineRow(
            primaryTitle = stringResource(R.string.merchant_metric_customers),
            primaryValue = formatCompactCount(analytics.totalCustomers),
            secondaryTitle = stringResource(R.string.merchant_analytics_segment_new),
            secondaryValue = formatCompactCount(analytics.newCustomers),
        )
        AnalyticsGroupedBarChartFromPoints(
            primaryPoints = analytics.newCustomerTrend,
            secondaryPoints = analytics.returningCustomerTrend,
            primaryAccent = VerevColors.Gold,
            secondaryAccent = VerevColors.Moss,
            animationEpoch = chartAnimationEpoch,
            valueFormatter = { formatCompactCount(it.toInt()) },
        )
        AnalyticsMiniLegend(
            leftLabel = stringResource(R.string.merchant_analytics_new_label),
            leftColor = VerevColors.Gold,
            rightLabel = stringResource(R.string.merchant_analytics_returning_label),
            rightColor = VerevColors.Moss,
        )
    }
}

@Composable
internal fun AnalyticsTopPerformanceCard(
    analytics: BusinessAnalytics,
    staffAnalytics: List<StaffAnalytics>,
    onOpenStaffAnalytics: () -> Unit,
) {
    MerchantPrimaryCard(contentPadding = PaddingValues(20.dp)) {
        AnalyticsCardHeader(
            title = stringResource(R.string.merchant_analytics_insights_section),
            subtitle = stringResource(R.string.merchant_analytics_top_performance_subtitle),
            actionText = stringResource(R.string.merchant_analytics_view_details),
            onClick = onOpenStaffAnalytics,
        )
        AnalyticsHeadlineRow(
            primaryTitle = stringResource(R.string.merchant_insight_top_customer),
            primaryValue = analytics.topCustomerName.ifBlank { stringResource(R.string.merchant_analytics_no_top_customer) },
            secondaryTitle = stringResource(R.string.merchant_analytics_top_promotion),
            secondaryValue = analytics.topPromotionName.ifBlank { stringResource(R.string.merchant_analytics_none_label) },
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (staffAnalytics.isEmpty()) {
            AnalyticsEmptyStateCard(
                title = stringResource(R.string.merchant_staff_analytics_empty_title),
                subtitle = stringResource(R.string.merchant_staff_analytics_empty_subtitle),
                icon = Icons.Default.Person,
            )
        } else {
            staffAnalytics.take(3).forEachIndexed { index, staff ->
                AnalyticsRankCard(
                    rank = index + 1,
                    title = staff.staffName.ifBlank { stringResource(R.string.merchant_staff_rank_title, index + 1) },
                    subtitle = stringResource(
                        R.string.merchant_staff_performance_subtitle,
                        formatCompactCount(staff.transactionsProcessed),
                        formatCompactCurrency(staff.revenueHandled),
                    ),
                    value = formatCompactCurrency(staff.averageTransactionValue),
                    icon = Icons.Default.Person,
                    onClick = onOpenStaffAnalytics,
                )
            }
        }
    }
}

@Composable
internal fun AnalyticsEmptyStateCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
) {
    MerchantPrimaryCard(contentPadding = PaddingValues(24.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                VerevColors.Forest.copy(alpha = 0.12f),
                                VerevColors.Moss.copy(alpha = 0.08f),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VerevColors.Forest,
                    modifier = Modifier.size(28.dp),
                )
            }
            Text(
                text = title,
                color = VerevColors.Forest,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                color = VerevColors.Forest.copy(alpha = 0.62f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
internal fun AnalyticsLoadingStateCard() {
    MerchantPrimaryCard(contentPadding = PaddingValues(24.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                VerevColors.Gold.copy(alpha = 0.18f),
                                VerevColors.Moss.copy(alpha = 0.12f),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.QueryStats,
                    contentDescription = null,
                    tint = VerevColors.Forest,
                    modifier = Modifier.size(30.dp),
                )
            }
            Text(
                text = stringResource(R.string.merchant_analytics_loading_title),
                color = VerevColors.Forest,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.merchant_analytics_loading_subtitle),
                color = VerevColors.Forest.copy(alpha = 0.62f),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == 1) 12.dp else 10.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == 1) VerevColors.Gold else VerevColors.Forest.copy(alpha = 0.18f),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
internal fun AnalyticsDetailHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.14f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.auth_back),
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .offset(x = (-1).dp)
                    .graphicsLayer { rotationZ = 180f },
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color.White.copy(alpha = 0.74f),
            )
        }
    }
}

@Composable
internal fun CustomerAnalyticsDetailContent(
    analytics: CustomerAnalyticsDrillDown,
    chartAnimationEpoch: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AnalyticsDetailHeroCard(
            title = stringResource(R.string.merchant_analytics_customers_detail_title),
            value = formatCompactCount(analytics.totalCustomers),
            supporting = stringResource(
                R.string.merchant_analytics_customer_story,
                formatCompactCount(analytics.retainedCustomers),
                formatCompactCount(analytics.inactiveCustomers),
            ),
            accent = listOf(VerevColors.Moss, VerevColors.Forest),
            statLeftTitle = stringResource(R.string.merchant_analytics_segment_high_value),
            statLeftValue = formatCompactCount(analytics.highValueCustomers),
            statRightTitle = stringResource(R.string.merchant_analytics_average_lifetime_value_title),
            statRightValue = formatCompactCurrency(analytics.averageLifetimeValue),
        )
        AnalyticsDetailMetricGrid(
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_segment_new), formatCompactCount(analytics.newCustomers), VerevColors.Gold, Icons.Default.Person),
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_segment_returning), formatCompactCount(analytics.returningCustomers), VerevColors.Moss, Icons.Default.TrendingUp),
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_segment_high_value), formatCompactCount(analytics.highValueCustomers), VerevColors.ForestBright, Icons.Default.WorkspacePremium),
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_segment_inactive), formatCompactCount(analytics.inactiveCustomers), VerevColors.Inactive, Icons.Default.QueryStats),
        )
        AnalyticsComparisonCard(
            title = stringResource(R.string.merchant_analytics_customer_growth_title),
            subtitle = stringResource(R.string.merchant_analytics_customer_growth_subtitle),
            primaryPoints = analytics.newCustomerTrend,
            secondaryPoints = analytics.returningCustomerTrend,
            primaryAccent = VerevColors.Gold,
            secondaryAccent = VerevColors.Moss,
            legendLeftLabel = stringResource(R.string.merchant_analytics_new_label),
            legendRightLabel = stringResource(R.string.merchant_analytics_returning_label),
            valueFormatter = { formatCompactCount(it.toInt()) },
            animationEpoch = chartAnimationEpoch,
        )
        AnalyticsComparisonCard(
            title = stringResource(R.string.merchant_metric_retention),
            subtitle = stringResource(R.string.merchant_analytics_retention_mix_subtitle),
            points = analytics.retentionTrend,
            accent = VerevColors.Gold,
            animationEpoch = chartAnimationEpoch,
        )
        if (analytics.hasTierAnalytics && analytics.tierBreakdown.isNotEmpty()) {
            AnalyticsDonutSegmentCard(
                title = stringResource(R.string.merchant_analytics_customer_tier_breakdown),
                subtitle = stringResource(R.string.merchant_analytics_customer_segments_title),
                segments = analytics.tierBreakdown,
                accent = VerevColors.Forest,
            )
        }
        AnalyticsSegmentBlock(
            title = stringResource(R.string.merchant_analytics_customer_segments_title),
            segments = analytics.segmentBreakdown,
            accent = VerevColors.Gold,
            rich = true,
        )
        AnalyticsTopCustomerShowcase(
            customers = analytics.topCustomers,
            showTier = analytics.hasTierAnalytics,
        )
    }
}

@Composable
internal fun RevenueAnalyticsDetailContent(
    analytics: RevenueAnalyticsDrillDown,
    chartAnimationEpoch: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AnalyticsDetailHeroCard(
            title = stringResource(R.string.merchant_analytics_total_revenue),
            value = formatCompactCurrency(analytics.totalRevenue),
            supporting = stringResource(R.string.merchant_analytics_revenue_story, formatPercent(analytics.revenueGrowthRate)),
            accent = listOf(VerevColors.Gold, VerevColors.Tan),
            statLeftTitle = stringResource(R.string.merchant_analytics_today_revenue),
            statLeftValue = formatCompactCurrency(analytics.todayRevenue),
            statRightTitle = stringResource(R.string.merchant_metric_average_purchase),
            statRightValue = formatCompactCurrency(analytics.averageOrderValue),
        )
        AnalyticsDetailMetricGrid(
            AnalyticsDetailMetric(stringResource(R.string.merchant_metric_average_purchase), formatCompactCurrency(analytics.averageOrderValue), VerevColors.Gold, Icons.Default.Assessment),
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_today_revenue), formatCompactCurrency(analytics.todayRevenue), VerevColors.Moss, Icons.Default.Payments),
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_transaction_count_subtitle), formatCompactCount(analytics.transactionCount), VerevColors.ForestBright, Icons.Default.QueryStats),
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_redemption_short), formatCompactCurrency(analytics.redeemedPointsValue), VerevColors.Tan, Icons.Default.CardGiftcard),
        )
        AnalyticsComparisonCard(
            title = stringResource(R.string.merchant_analytics_revenue_chart_title),
            subtitle = stringResource(R.string.merchant_analytics_revenue_chart_subtitle),
            points = analytics.revenueTrend,
            accent = VerevColors.Gold,
            legendLeftLabel = stringResource(R.string.merchant_metric_revenue),
            valueFormatter = { formatCompactCurrency(it.toDouble()) },
            animationEpoch = chartAnimationEpoch,
        )
        AnalyticsComparisonCard(
            title = stringResource(R.string.merchant_analytics_hourly_revenue_title),
            subtitle = stringResource(R.string.merchant_analytics_time_bucket_subtitle),
            points = analytics.timeBucketTrend,
            accent = VerevColors.ForestBright,
            legendLeftLabel = stringResource(R.string.merchant_metric_revenue),
            valueFormatter = { formatCompactCurrency(it.toDouble()) },
            animationEpoch = chartAnimationEpoch,
        )
        AnalyticsSegmentBlock(
            title = stringResource(R.string.merchant_analytics_revenue_sources_title),
            segments = analytics.sourceBreakdown,
            accent = VerevColors.Moss,
            rich = true,
        )
    }
}

@Composable
internal fun PromotionAnalyticsDetailContent(analytics: PromotionAnalyticsDrillDown) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AnalyticsDetailHeroCard(
            title = stringResource(R.string.merchant_promotions_title),
            value = formatCompactCount(analytics.totalPromotions),
            supporting = stringResource(R.string.merchant_analytics_promotion_story, formatPercent(analytics.averageRoiScore)),
            accent = listOf(VerevColors.Gold, VerevColors.Moss),
            statLeftTitle = stringResource(R.string.merchant_analytics_active_promotions),
            statLeftValue = formatCompactCount(analytics.activePromotions),
            statRightTitle = stringResource(R.string.merchant_analytics_average_roi_title),
            statRightValue = formatPercent(analytics.averageRoiScore),
        )
        AnalyticsDetailMetricGrid(
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_active_promotions), formatCompactCount(analytics.activePromotions), VerevColors.Gold, Icons.Default.LocalOffer),
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_average_roi_title), formatPercent(analytics.averageRoiScore), VerevColors.Moss, Icons.Default.TrendingUp),
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_scheduled_promotions_subtitle), formatCompactCount(analytics.scheduledPromotions), VerevColors.ForestBright, Icons.Default.QueryStats),
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_status_inactive), formatCompactCount(analytics.expiredPromotions), VerevColors.Inactive, Icons.Default.Campaign),
        )
        AnalyticsComparisonCard(
            title = stringResource(R.string.merchant_analytics_top_promotions),
            subtitle = stringResource(R.string.merchant_analytics_top_promotions_subtitle),
            points = analytics.topPromotions.toRevenueImpactPoints(),
            accent = VerevColors.Gold,
            legendLeftLabel = stringResource(R.string.merchant_metric_revenue),
            valueFormatter = { formatCompactCurrency(it.toDouble()) },
        )
        AnalyticsDonutSegmentCard(
            title = stringResource(R.string.merchant_analytics_promotion_types),
            subtitle = stringResource(R.string.merchant_analytics_promotion_types_subtitle),
            segments = analytics.typeBreakdown,
            accent = VerevColors.Gold,
        )
        AnalyticsSegmentBlock(
            title = stringResource(R.string.merchant_analytics_promotion_status_title),
            segments = analytics.statusBreakdown,
            accent = VerevColors.ForestBright,
            rich = true,
        )
        MerchantPrimaryCard {
            MerchantSectionTitle(text = stringResource(R.string.merchant_analytics_top_promotions))
            analytics.topPromotions.forEachIndexed { index, promotion ->
                AnalyticsPromotionPerformanceCard(rank = index + 1, promotion = promotion)
            }
        }
    }
}

@Composable
internal fun ProgramAnalyticsDetailContent(analytics: ProgramAnalyticsDrillDown) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AnalyticsDetailHeroCard(
            title = stringResource(R.string.merchant_analytics_programs_detail_title),
            value = formatCompactCount(analytics.totalPrograms),
            supporting = stringResource(
                R.string.merchant_analytics_program_story,
                formatPercent(analytics.memberParticipationRate),
                formatPercent(analytics.redemptionEfficiency),
            ),
            accent = listOf(VerevColors.ForestBright, VerevColors.Forest),
            statLeftTitle = stringResource(R.string.merchant_analytics_active_programs),
            statLeftValue = formatCompactCount(analytics.activePrograms),
            statRightTitle = stringResource(R.string.merchant_analytics_redemption_efficiency_title),
            statRightValue = formatPercent(analytics.redemptionEfficiency),
        )
        AnalyticsDetailMetricGrid(
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_active_programs), formatCompactCount(analytics.activePrograms), VerevColors.Gold, Icons.Default.CardGiftcard),
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_redemption_efficiency_title), formatPercent(analytics.redemptionEfficiency), VerevColors.Moss, Icons.Default.TrendingUp),
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_member_participation_title), formatPercent(analytics.memberParticipationRate), VerevColors.ForestBright, Icons.Default.Groups),
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_program_types_label), formatCompactCount(analytics.typeBreakdown.sumOf { it.value }), VerevColors.Tan, Icons.Default.QueryStats),
        )
        AnalyticsComparisonCard(
            title = stringResource(R.string.merchant_analytics_top_programs),
            subtitle = stringResource(R.string.merchant_analytics_top_programs_subtitle),
            points = analytics.topPrograms.toProgramMemberPoints(),
            accent = VerevColors.Forest,
            legendLeftLabel = stringResource(R.string.merchant_analytics_member_participation_title),
        )
        AnalyticsDonutSegmentCard(
            title = stringResource(R.string.merchant_analytics_program_types),
            subtitle = stringResource(R.string.merchant_analytics_program_types_subtitle),
            segments = analytics.typeBreakdown,
            accent = VerevColors.Forest,
        )
        AnalyticsSegmentBlock(
            title = stringResource(R.string.merchant_analytics_program_reward_usage_title),
            segments = analytics.rewardUsageBreakdown,
            accent = VerevColors.Gold,
            rich = true,
        )
        AnalyticsSegmentBlock(
            title = stringResource(R.string.merchant_analytics_program_actions),
            segments = analytics.scanActionBreakdown,
            accent = VerevColors.ForestBright,
            rich = true,
        )
        MerchantPrimaryCard {
            MerchantSectionTitle(text = stringResource(R.string.merchant_analytics_top_programs))
            analytics.topPrograms.forEachIndexed { index, program ->
                AnalyticsProgramPerformanceCard(
                    rank = index + 1,
                    program = program,
                )
            }
        }
    }
}

@Composable
internal fun StaffAnalyticsLeaderboard(
    staffAnalytics: List<StaffAnalytics>,
    chartAnimationEpoch: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AnalyticsDetailHeroCard(
            title = stringResource(R.string.merchant_staff_analytics_title),
            value = formatCompactCount(staffAnalytics.sumOf { it.transactionsProcessed }),
            supporting = stringResource(
                R.string.merchant_staff_analytics_story,
                formatCompactCurrency(staffAnalytics.firstOrNull()?.revenueHandled ?: 0.0),
            ),
            accent = listOf(VerevColors.Tan, VerevColors.Gold),
            statLeftTitle = stringResource(R.string.merchant_analytics_active_staff_short),
            statLeftValue = formatCompactCount(staffAnalytics.size),
            statRightTitle = stringResource(R.string.merchant_analytics_avg_sales_short),
            statRightValue = formatCompactCurrency(staffAnalytics.firstOrNull()?.averageTransactionValue ?: 0.0),
        )
        AnalyticsDetailMetricGrid(
            AnalyticsDetailMetric(stringResource(R.string.merchant_metric_revenue), formatCompactCurrency(staffAnalytics.sumOf { it.revenueHandled }), VerevColors.Gold, Icons.Default.Payments),
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_redemption_short), formatCompactCount(staffAnalytics.sumOf { it.rewardsRedeemed }), VerevColors.Moss, Icons.Default.CardGiftcard),
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_active_staff_short), formatCompactCount(staffAnalytics.size), VerevColors.ForestBright, Icons.Default.Groups),
            AnalyticsDetailMetric(stringResource(R.string.merchant_analytics_avg_sales_short), formatCompactCurrency(staffAnalytics.firstOrNull()?.averageTransactionValue ?: 0.0), VerevColors.Tan, Icons.Default.TrendingUp),
        )
        AnalyticsComparisonCard(
            title = stringResource(R.string.merchant_staff_analytics_revenue_chart_title),
            subtitle = stringResource(R.string.merchant_staff_analytics_revenue_chart_subtitle),
            points = staffAnalytics.toStaffRevenuePoints(),
            accent = VerevColors.Tan,
            legendLeftLabel = stringResource(R.string.merchant_metric_revenue),
            valueFormatter = { formatCompactCurrency(it.toDouble()) },
            animationEpoch = chartAnimationEpoch,
        )
        MerchantPrimaryCard {
            MerchantSectionTitle(text = stringResource(R.string.merchant_staff_analytics_ranking_title))
            staffAnalytics.forEachIndexed { index, staff ->
                AnalyticsStaffPerformanceCard(
                    rank = index + 1,
                    staff = staff,
                )
            }
        }
    }
}

@Composable
private fun AnalyticsHeroLabel(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(VerevColors.White.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text = text, color = VerevColors.White.copy(alpha = 0.82f), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun AnalyticsHeroMetricPill(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(VerevColors.White.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = VerevColors.White, modifier = Modifier.size(16.dp))
        Column {
            Text(text = title, color = VerevColors.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
            Text(text = value, color = VerevColors.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AnalyticsKpiCard(
    title: String,
    value: String,
    supporting: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    MerchantPrimaryCard(
        modifier = modifier.shadow(
            elevation = 7.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = Color.Black.copy(alpha = 0.05f),
            spotColor = Color.Black.copy(alpha = 0.05f),
        ),
        contentPadding = PaddingValues(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest.copy(alpha = 0.72f))
        }
        Text(text = value, fontSize = 26.sp, lineHeight = 28.sp, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
        Text(text = supporting, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.56f))
    }
}

@Composable
private fun AnalyticsHeaderActionButton(
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            )
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun AnalyticsCardHeader(
    title: String,
    subtitle: String,
    actionText: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = VerevColors.Forest.copy(alpha = 0.58f),
            )
        }
        if (actionText != null && onClick != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(VerevColors.Moss.copy(alpha = 0.12f))
                    .clickable(onClick = onClick)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
            ) {
                Text(
                    text = actionText,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    color = VerevColors.Moss,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun AnalyticsGradientMetricCard(
    title: String,
    value: String,
    supporting: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradient))
            .padding(horizontal = 15.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 12.sp,
                lineHeight = 14.sp,
            )
        }
        Text(
            text = value,
            color = Color.White,
            fontSize = 28.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = supporting,
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )
    }
}

@Composable
private fun AnalyticsNeutralMetricCard(
    title: String,
    value: String,
    supporting: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 7.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.06f),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(horizontal = 15.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Text(
                text = title,
                color = VerevColors.Forest.copy(alpha = 0.62f),
                fontSize = 12.sp,
                lineHeight = 14.sp,
            )
        }
        Text(
            text = value,
            color = VerevColors.Forest,
            fontSize = 28.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = supporting,
            color = accent,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )
    }
}

@Composable
private fun AnalyticsHighlightChip(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            color = VerevColors.Forest.copy(alpha = 0.56f),
        )
        Text(
            text = value,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

private data class AnalyticsInsightStat(
    val label: String,
    val value: String,
)

@Composable
private fun AnalyticsOverviewSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color.Black.copy(alpha = 0.07f),
                spotColor = Color.Black.copy(alpha = 0.07f),
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
private fun AnalyticsInsightCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBackground: Brush,
    iconTint: Color,
    stats: List<AnalyticsInsightStat>,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 7.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.06f),
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = VerevColors.Forest.copy(alpha = 0.58f),
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = VerevColors.Forest.copy(alpha = 0.32f),
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            stats.take(3).forEach { stat ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VerevColors.SurfaceSoft)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stat.label,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = VerevColors.Forest.copy(alpha = 0.50f),
                    )
                    Text(
                        text = stat.value,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsHeadlineRow(
    primaryTitle: String,
    primaryValue: String,
    secondaryTitle: String,
    secondaryValue: String,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = primaryTitle, fontSize = 12.sp, lineHeight = 14.sp, color = VerevColors.Forest.copy(alpha = 0.56f))
            Text(text = primaryValue, fontSize = 28.sp, lineHeight = 30.sp, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = secondaryTitle, fontSize = 12.sp, lineHeight = 14.sp, color = VerevColors.Forest.copy(alpha = 0.56f))
            Text(text = secondaryValue, fontSize = 28.sp, lineHeight = 30.sp, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AnalyticsHeadlineDetailCard(
    title: String,
    value: String,
    supporting: String,
) {
    MerchantPrimaryCard(contentPadding = PaddingValues(22.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest.copy(alpha = 0.7f))
        Text(text = value, style = MaterialTheme.typography.displaySmall, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
        Text(text = supporting, style = MaterialTheme.typography.bodyLarge, color = VerevColors.Forest.copy(alpha = 0.62f))
    }
}

@Composable
private fun AnalyticsDetailHeroCard(
    title: String,
    value: String,
    supporting: String,
    accent: List<Color>,
    statLeftTitle: String,
    statLeftValue: String,
    statRightTitle: String,
    statRightValue: String,
) {
    Column(
        modifier = Modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            )
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(accent))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = supporting,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.82f),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnalyticsHeroMetricPill(
                icon = Icons.Default.QueryStats,
                title = statLeftTitle,
                value = statLeftValue,
            )
            AnalyticsHeroMetricPill(
                icon = Icons.Default.TrendingUp,
                title = statRightTitle,
                value = statRightValue,
            )
        }
    }
}

@Composable
private fun AnalyticsMetricBlock(title: String, value: String, accent: Color) {
    MerchantPrimaryCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp),
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(accent),
        )
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest.copy(alpha = 0.66f))
        Text(text = value, style = MaterialTheme.typography.headlineMedium, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AnalyticsKpiStrip(
    firstTitle: String,
    firstValue: String,
    secondTitle: String,
    secondValue: String,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AnalyticsHighlightChip(
            modifier = Modifier.weight(1f),
            title = firstTitle,
            value = firstValue,
        )
        AnalyticsHighlightChip(
            modifier = Modifier.weight(1f),
            title = secondTitle,
            value = secondValue,
        )
    }
}

@Composable
private fun AnalyticsComparisonCard(
    title: String,
    subtitle: String,
    points: List<AnalyticsPoint>,
    accent: Color,
    secondaryAccent: Color = VerevColors.ForestBright,
    legendLeftLabel: String? = null,
    legendRightLabel: String? = null,
    valueFormatter: (Float) -> String = { formatCompactCount(it.toInt()) },
    animationEpoch: Int = 0,
) {
    MerchantPrimaryCard(
        modifier = Modifier.shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = Color.Black.copy(alpha = 0.06f),
            spotColor = Color.Black.copy(alpha = 0.06f),
        ),
    ) {
        AnalyticsCardHeader(
            title = title,
            subtitle = subtitle,
        )
        AnalyticsAreaChartFromPoints(
            points = points,
            lineColor = accent,
            animationEpoch = animationEpoch,
            valueFormatter = valueFormatter,
        )
        if (legendLeftLabel != null) {
            AnalyticsMiniLegend(
                leftLabel = legendLeftLabel,
                leftColor = accent,
                rightLabel = legendRightLabel,
                rightColor = secondaryAccent,
            )
        }
    }
}

@Composable
private fun AnalyticsComparisonCard(
    title: String,
    subtitle: String,
    primaryPoints: List<AnalyticsPoint>,
    secondaryPoints: List<AnalyticsPoint>,
    primaryAccent: Color,
    secondaryAccent: Color,
    legendLeftLabel: String,
    legendRightLabel: String,
    valueFormatter: (Float) -> String = { formatCompactCount(it.toInt()) },
    animationEpoch: Int = 0,
) {
    MerchantPrimaryCard(
        modifier = Modifier.shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = Color.Black.copy(alpha = 0.06f),
            spotColor = Color.Black.copy(alpha = 0.06f),
        ),
    ) {
        AnalyticsCardHeader(
            title = title,
            subtitle = subtitle,
        )
        AnalyticsGroupedBarChartFromPoints(
            primaryPoints = primaryPoints,
            secondaryPoints = secondaryPoints,
            primaryAccent = primaryAccent,
            secondaryAccent = secondaryAccent,
            animationEpoch = animationEpoch,
            valueFormatter = valueFormatter,
        )
        AnalyticsMiniLegend(
            leftLabel = legendLeftLabel,
            leftColor = primaryAccent,
            rightLabel = legendRightLabel,
            rightColor = secondaryAccent,
        )
    }
}

@Composable
private fun AnalyticsSegmentBlock(
    title: String,
    segments: List<AnalyticsSegment>,
    accent: Color,
    rich: Boolean = false,
) {
    MerchantPrimaryCard(
        modifier = Modifier.shadow(
            elevation = 7.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = Color.Black.copy(alpha = 0.05f),
            spotColor = Color.Black.copy(alpha = 0.05f),
        ),
    ) {
        MerchantSectionTitle(text = title)
        segments.forEach { segment ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (rich) VerevColors.SurfaceSoft else Color.Transparent)
                    .padding(horizontal = if (rich) 14.dp else 0.dp, vertical = if (rich) 12.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.9f)),
                )
                Text(
                    text = segment.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = VerevColors.Forest,
                )
                Text(
                    text = formatCompactCount(segment.value),
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private data class AnalyticsDetailMetric(
    val label: String,
    val value: String,
    val accent: Color,
    val icon: ImageVector,
)

@Composable
private fun AnalyticsDetailMetricGrid(vararg metrics: AnalyticsDetailMetric) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 2,
    ) {
        metrics.forEach { metric ->
            AnalyticsKpiCard(
                modifier = Modifier.fillMaxWidth(0.48f),
                title = metric.label,
                value = metric.value,
                supporting = stringResource(R.string.merchant_analytics_detail_metric_supporting),
                icon = metric.icon,
                accent = metric.accent,
            )
        }
    }
}

@Composable
private fun AnalyticsDonutSegmentCard(
    title: String,
    subtitle: String,
    segments: List<AnalyticsSegment>,
    accent: Color,
) {
    MerchantPrimaryCard(
        modifier = Modifier.shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = Color.Black.copy(alpha = 0.06f),
            spotColor = Color.Black.copy(alpha = 0.06f),
        ),
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            color = VerevColors.Forest,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = subtitle,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = VerevColors.Forest.copy(alpha = 0.58f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnalyticsDonutChart(
                modifier = Modifier.weight(1f),
                segments = segments,
                accent = accent,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                segments.forEach { segment ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(accent.copy(alpha = 0.85f)),
                            )
                            Text(
                                text = segment.label,
                                fontSize = 13.sp,
                                lineHeight = 16.sp,
                                color = VerevColors.Forest,
                            )
                        }
                        Text(
                            text = formatCompactCount(segment.value),
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsDonutChart(
    segments: List<AnalyticsSegment>,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val total = max(segments.sumOf { it.value }, 1)
    Canvas(
        modifier = modifier
            .height(168.dp)
            .fillMaxWidth(),
    ) {
        val strokeWidth = 22.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = androidx.compose.ui.geometry.Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f,
        )
        var startAngle = -90f
        segments.forEachIndexed { index, segment ->
            val sweep = (segment.value.toFloat() / total.toFloat()) * 360f
            drawArc(
                color = when (index % 4) {
                    0 -> accent
                    1 -> VerevColors.Gold
                    2 -> VerevColors.Tan
                    else -> VerevColors.Moss
                },
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            startAngle += sweep + 3f
        }
    }
}

@Composable
private fun AnalyticsTopCustomerShowcase(
    customers: List<com.vector.verevcodex.domain.model.customer.TopCustomerAnalytics>,
    showTier: Boolean,
) {
    MerchantPrimaryCard(
        modifier = Modifier.shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = Color.Black.copy(alpha = 0.06f),
            spotColor = Color.Black.copy(alpha = 0.06f),
        ),
    ) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_analytics_top_customers))
        customers.forEachIndexed { index, customer ->
            AnalyticsCustomerRankCard(
                rank = index + 1,
                title = customer.customerName,
                subtitle = if (showTier) {
                    stringResource(
                        R.string.merchant_analytics_top_customer_subtitle,
                        customer.loyaltyTier.displayName(),
                        formatCompactCount(customer.totalVisits),
                    )
                } else {
                    stringResource(
                        R.string.merchant_analytics_top_customer_visits_subtitle,
                        formatCompactCount(customer.totalVisits),
                    )
                },
                value = formatCompactCurrency(customer.totalSpent),
            )
        }
    }
}

@Composable
private fun AnalyticsRankCard(
    rank: Int,
    title: String,
    subtitle: String,
    value: String,
    icon: ImageVector,
    onClick: (() -> Unit)? = null,
) {
    val content: @Composable () -> Unit = {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(VerevColors.Forest.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = rank.toString(), color = VerevColors.Forest, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(VerevColors.Gold.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = VerevColors.Gold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.58f))
            }
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
        }
    }
    if (onClick != null) {
        MerchantInteractiveCard(
            onClick = onClick,
            modifier = Modifier.shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f),
            ),
            contentPadding = PaddingValues(16.dp),
        ) { content() }
    } else {
        MerchantPrimaryCard(
            modifier = Modifier.shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f),
            ),
            contentPadding = PaddingValues(16.dp),
        ) { content() }
    }
}

@Composable
private fun AnalyticsCustomerRankCard(
    rank: Int,
    title: String,
    subtitle: String,
    value: String,
) {
    MerchantPrimaryCard(
        modifier = Modifier.shadow(
            elevation = 6.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = Color.Black.copy(alpha = 0.05f),
            spotColor = Color.Black.copy(alpha = 0.05f),
        ),
        contentPadding = PaddingValues(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VerevColors.Moss.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = rank.toString(), color = VerevColors.Forest, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.58f),
                        )
                    }
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun AnalyticsPromotionPerformanceCard(
    rank: Int,
    promotion: com.vector.verevcodex.domain.model.promotions.PromotionPerformance,
) {
    MerchantPrimaryCard(
        modifier = Modifier.shadow(
            elevation = 6.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = Color.Black.copy(alpha = 0.05f),
            spotColor = Color.Black.copy(alpha = 0.05f),
        ),
        contentPadding = PaddingValues(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VerevColors.Gold.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = rank.toString(), color = VerevColors.Forest, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = promotion.name, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = stringResource(
                                R.string.merchant_analytics_promotion_rank_subtitle,
                                promotion.type.name.analyticsHumanize(),
                                if (promotion.active) stringResource(R.string.merchant_analytics_status_active) else stringResource(R.string.merchant_analytics_status_inactive),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.58f),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (promotion.paymentFlowEnabled) VerevColors.Moss.copy(alpha = 0.14f) else VerevColors.SurfaceSoft)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = if (promotion.paymentFlowEnabled) stringResource(R.string.merchant_promotions_metric_payment) else stringResource(R.string.merchant_analytics_view_details),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (promotion.paymentFlowEnabled) VerevColors.Moss else VerevColors.Forest.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnalyticsInlineStat(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.merchant_analytics_average_roi_title),
                    value = formatPercent(promotion.roiScore),
                )
                AnalyticsInlineStat(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.merchant_metric_revenue),
                    value = formatCompactCurrency(promotion.revenueImpact),
                )
                AnalyticsInlineStat(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.merchant_analytics_redemption_short),
                    value = formatCompactCount(promotion.estimatedUsageCount),
                )
            }
        }
    }
}

@Composable
private fun AnalyticsProgramPerformanceCard(
    rank: Int,
    program: com.vector.verevcodex.domain.model.loyalty.ProgramPerformance,
) {
    MerchantPrimaryCard(
        modifier = Modifier.shadow(
            elevation = 6.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = Color.Black.copy(alpha = 0.05f),
            spotColor = Color.Black.copy(alpha = 0.05f),
        ),
        contentPadding = PaddingValues(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VerevColors.Forest.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = rank.toString(), color = VerevColors.Forest, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = program.name, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = stringResource(
                                R.string.merchant_analytics_program_rank_subtitle,
                                program.type.name.analyticsHumanize(),
                                formatCompactCount(program.memberCount),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.58f),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (program.active) VerevColors.Gold.copy(alpha = 0.18f) else VerevColors.SurfaceSoft)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = if (program.active) stringResource(R.string.merchant_analytics_status_active) else stringResource(R.string.merchant_analytics_status_inactive),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (program.active) VerevColors.Forest else VerevColors.Forest.copy(alpha = 0.56f),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnalyticsInlineStat(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.merchant_analytics_redemption_efficiency_title),
                    value = formatPercent(program.redemptionRate),
                )
                AnalyticsInlineStat(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.merchant_analytics_member_participation_title),
                    value = formatCompactCount(program.memberCount),
                )
                AnalyticsInlineStat(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.merchant_analytics_program_actions),
                    value = formatCompactCount(program.scanActionsEnabled),
                )
            }
        }
    }
}

@Composable
private fun AnalyticsStaffPerformanceCard(
    rank: Int,
    staff: StaffAnalytics,
) {
    MerchantPrimaryCard(
        modifier = Modifier.shadow(
            elevation = 6.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = Color.Black.copy(alpha = 0.05f),
            spotColor = Color.Black.copy(alpha = 0.05f),
        ),
        contentPadding = PaddingValues(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VerevColors.Tan.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = rank.toString(), color = VerevColors.Forest, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(
                            text = staff.staffName.ifBlank { stringResource(R.string.merchant_staff_rank_title, rank) },
                            style = MaterialTheme.typography.titleMedium,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(
                                R.string.merchant_staff_analytics_rank_subtitle,
                                formatCompactCount(staff.customersServed),
                                formatCompactCount(staff.rewardsRedeemed),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.58f),
                        )
                    }
                }
                Text(
                    text = formatCompactCurrency(staff.revenueHandled),
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(VerevColors.SurfaceSoft)
                    .padding(12.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AnalyticsInlineStat(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.merchant_analytics_avg_sales_short),
                        value = formatCompactCurrency(staff.averageTransactionValue),
                    )
                    AnalyticsInlineStat(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.merchant_analytics_redemption_short),
                        value = formatCompactCount(staff.rewardsRedeemed),
                    )
                    AnalyticsInlineStat(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.merchant_analytics_transaction_count_subtitle),
                        value = formatCompactCount(staff.transactionsProcessed),
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsInlineStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VerevColors.Forest.copy(alpha = 0.5f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun AnalyticsMiniLegend(
    leftLabel: String,
    leftColor: Color,
    rightLabel: String? = null,
    rightColor: Color = leftColor,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AnalyticsLegendDot(color = leftColor, label = leftLabel)
        if (rightLabel != null) {
            AnalyticsLegendDot(color = rightColor, label = rightLabel)
        }
    }
}

@Composable
private fun AnalyticsLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.6f))
    }
}

@Composable
internal fun AnalyticsAreaChartFromPoints(
    points: List<AnalyticsPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 180.dp,
    animationEpoch: Int = 0,
    valueFormatter: (Float) -> String = { formatCompactCount(it.toInt()) },
) {
    val chartPoints = points.map { AnalyticsChartPoint(label = it.label, primary = it.value) }
    val chartScale = remember(chartPoints) {
        analyticsChartScale(chartPoints.maxOfOrNull { it.primary } ?: 0f)
    }
    val maxValue = chartScale.maxValue
    val yAxisValues = remember(chartScale, valueFormatter) {
        chartScale.axisValues.map(valueFormatter)
    }
    var revealProgress by remember(animationEpoch) { mutableStateOf(0f) }
    val animatedRevealProgress by animateFloatAsState(
        targetValue = revealProgress,
        animationSpec = tween(durationMillis = 850),
        label = "analyticsAreaReveal",
    )
    LaunchedEffect(animationEpoch, points) {
        revealProgress = 0f
        delay(40)
        revealProgress = 1f
    }
    AnalyticsChartFrame(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier.width(52.dp).height(chartHeight),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End,
                ) {
                    yAxisValues.forEach { value ->
                        Text(
                            text = value,
                            style = MaterialTheme.typography.labelSmall,
                            color = VerevColors.Forest.copy(alpha = 0.52f),
                            maxLines = 1,
                        )
                    }
                }
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(chartHeight),
                ) {
                    if (chartPoints.size < 2) return@Canvas
                    val bucketWidth = size.width / chartPoints.size.toFloat().coerceAtLeast(1f)
                    val plotTop = analyticsPlotTop()
                    val plotBottom = analyticsPlotBottom()
                    val plotHeight = plotBottom - plotTop
                    drawAnalyticsGrid(columnCount = chartPoints.size)
                    val linePath = Path()
                    val fillPath = Path()
                    chartPoints.forEachIndexed { index, point ->
                        val x = (bucketWidth * index) + (bucketWidth / 2f)
                        val y = plotBottom - ((point.primary / maxValue) * plotHeight * animatedRevealProgress)
                        if (index == 0) {
                            linePath.moveTo(x, y)
                            fillPath.moveTo(x, plotBottom)
                            fillPath.lineTo(x, y)
                        } else {
                            linePath.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }
                    fillPath.lineTo(size.width, plotBottom)
                    fillPath.close()
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            listOf(
                                lineColor.copy(alpha = 0.24f * animatedRevealProgress.coerceIn(0f, 1f)),
                                Color.Transparent,
                            ),
                        ),
                    )
                    drawPath(
                        path = linePath,
                        color = lineColor.copy(alpha = 0.45f + (0.55f * animatedRevealProgress.coerceIn(0f, 1f))),
                        style = Stroke(width = 6f, cap = StrokeCap.Round),
                    )
                    chartPoints.forEachIndexed { index, point ->
                        val x = (bucketWidth * index) + (bucketWidth / 2f)
                        val y = plotBottom - ((point.primary / maxValue) * plotHeight * animatedRevealProgress)
                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y),
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Spacer(modifier = Modifier.width(52.dp))
                Row(modifier = Modifier.weight(1f)) {
                    chartPoints.forEach {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = it.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = VerevColors.Forest.copy(alpha = 0.62f),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AnalyticsBarChartFromPoints(
    points: List<AnalyticsPoint>,
    accent: Color,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 140.dp,
    animationEpoch: Int = 0,
    valueFormatter: (Float) -> String = { formatCompactCount(it.toInt()) },
) {
    val chartScale = remember(points) {
        analyticsChartScale(points.maxOfOrNull { it.value } ?: 0f)
    }
    val maxValue = chartScale.maxValue
    val yAxisValues = remember(chartScale, valueFormatter) {
        chartScale.axisValues.map(valueFormatter)
    }
    var revealProgress by remember(animationEpoch) { mutableStateOf(0f) }
    val animatedRevealProgress by animateFloatAsState(
        targetValue = revealProgress,
        animationSpec = tween(durationMillis = 700),
        label = "analyticsBarReveal",
    )
    LaunchedEffect(animationEpoch, points) {
        revealProgress = 0f
        delay(40)
        revealProgress = 1f
    }
    AnalyticsChartFrame(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier.width(52.dp).height(chartHeight),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End,
                ) {
                    yAxisValues.forEach { value ->
                        Text(
                            text = value,
                            style = MaterialTheme.typography.labelSmall,
                            color = VerevColors.Forest.copy(alpha = 0.52f),
                            maxLines = 1,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(chartHeight),
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(chartHeight),
                    ) {
                        drawAnalyticsGrid(columnCount = points.size)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(chartHeight),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        points.forEach { point ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(chartHeight * ((point.value / maxValue) * animatedRevealProgress))
                                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                                        .background(accent.copy(alpha = 0.88f)),
                                )
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Spacer(modifier = Modifier.width(52.dp))
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    points.forEach { point ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = point.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = VerevColors.Forest.copy(alpha = 0.62f),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AnalyticsGroupedBarChartFromPoints(
    primaryPoints: List<AnalyticsPoint>,
    secondaryPoints: List<AnalyticsPoint>,
    primaryAccent: Color,
    secondaryAccent: Color,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 148.dp,
    animationEpoch: Int = 0,
    valueFormatter: (Float) -> String = { formatCompactCount(it.toInt()) },
) {
    val chartPoints = remember(primaryPoints, secondaryPoints) {
        primaryPoints.mapIndexed { index, point ->
            AnalyticsChartPoint(
                label = point.label,
                primary = point.value,
                secondary = secondaryPoints.getOrNull(index)?.value ?: 0f,
            )
        }
    }
    val chartScale = remember(chartPoints) {
        analyticsChartScale(chartPoints.maxOfOrNull { max(it.primary, it.secondary) } ?: 0f)
    }
    val maxValue = chartScale.maxValue
    val yAxisValues = remember(chartScale, valueFormatter) {
        chartScale.axisValues.map(valueFormatter)
    }
    var revealProgress by remember(animationEpoch) { mutableStateOf(0f) }
    val animatedRevealProgress by animateFloatAsState(
        targetValue = revealProgress,
        animationSpec = tween(durationMillis = 760),
        label = "analyticsGroupedBarReveal",
    )
    LaunchedEffect(animationEpoch, primaryPoints, secondaryPoints) {
        revealProgress = 0f
        delay(40)
        revealProgress = 1f
    }
    AnalyticsChartFrame(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier.width(52.dp).height(chartHeight),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End,
                ) {
                    yAxisValues.forEach { value ->
                        Text(
                            text = value,
                            style = MaterialTheme.typography.labelSmall,
                            color = VerevColors.Forest.copy(alpha = 0.52f),
                            maxLines = 1,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(chartHeight),
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(chartHeight),
                    ) {
                        drawAnalyticsGrid(columnCount = chartPoints.size)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(chartHeight),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        chartPoints.forEach { point ->
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(chartHeight * ((point.primary / maxValue) * animatedRevealProgress))
                                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                        .background(primaryAccent.copy(alpha = 0.92f)),
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(chartHeight * ((point.secondary / maxValue) * animatedRevealProgress))
                                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                        .background(secondaryAccent.copy(alpha = 0.88f)),
                                )
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Spacer(modifier = Modifier.width(52.dp))
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    chartPoints.forEach { point ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = point.label,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                color = VerevColors.Forest.copy(alpha = 0.66f),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsChartFrame(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.04f),
            )
            .clip(RoundedCornerShape(22.dp))
            .background(VerevColors.SurfaceSoft)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

private data class AnalyticsChartPoint(
    val label: String,
    val primary: Float,
    val secondary: Float = 0f,
)

private fun List<com.vector.verevcodex.domain.model.promotions.PromotionPerformance>.toRevenueImpactPoints(): List<AnalyticsPoint> =
    take(5).map { promotion ->
        AnalyticsPoint(promotion.name.analyticsCompactLabel(), promotion.revenueImpact.toFloat())
    }

private fun List<com.vector.verevcodex.domain.model.loyalty.ProgramPerformance>.toProgramMemberPoints(): List<AnalyticsPoint> =
    take(5).map { program ->
        AnalyticsPoint(program.name.analyticsCompactLabel(), program.memberCount.toFloat())
    }

private fun List<StaffAnalytics>.toStaffRevenuePoints(): List<AnalyticsPoint> =
    take(5).map { staff ->
        AnalyticsPoint(staff.staffName.analyticsCompactLabel(), staff.revenueHandled.toFloat())
    }

private fun String.analyticsCompactLabel(): String {
    val words = trim().split(" ").filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> ""
        words.size == 1 -> words.first().take(6)
        else -> words.take(3).joinToString("") { it.take(1).uppercase() }
    }
}

private fun String.analyticsHumanize(): String =
    lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)

private data class AnalyticsChartScale(
    val maxValue: Float,
    val axisValues: List<Float>,
)

private fun analyticsChartScale(rawMaxValue: Float): AnalyticsChartScale {
    val safeMaxValue = max(rawMaxValue, 1f)
    val roughStep = safeMaxValue / 3f
    val magnitude = 10f.pow(floor(kotlin.math.log10(roughStep.toDouble())).toFloat())
    val normalized = roughStep / magnitude
    val step = max(
        when {
        normalized <= 1f -> 1f * magnitude
        normalized <= 2f -> 2f * magnitude
        normalized <= 5f -> 5f * magnitude
        else -> 10f * magnitude
        },
        1f,
    )
    val niceMax = max(step * 3f, 1f)
    return AnalyticsChartScale(
        maxValue = niceMax,
        axisValues = listOf(niceMax, niceMax - step, niceMax - (step * 2f), 0f),
    )
}

private fun DrawScope.analyticsPlotTop(): Float = 8.dp.toPx()

private fun DrawScope.analyticsPlotBottom(): Float = size.height - 8.dp.toPx()

private fun DrawScope.drawAnalyticsGrid(columnCount: Int) {
    val plotTop = analyticsPlotTop()
    val plotBottom = analyticsPlotBottom()
    val plotHeight = plotBottom - plotTop
    val strokeWidth = 1.dp.toPx()
    repeat(columnCount.coerceAtLeast(1)) { index ->
        val x = if (columnCount <= 1) {
            size.width / 2f
        } else {
            val step = size.width / columnCount.toFloat()
            (step * index) + (step / 2f)
        }
        drawLine(
            color = VerevColors.Forest.copy(alpha = 0.12f),
            start = androidx.compose.ui.geometry.Offset(x, plotTop),
            end = androidx.compose.ui.geometry.Offset(x, plotBottom),
            strokeWidth = strokeWidth,
        )
    }
    repeat(4) { index ->
        val y = plotTop + (plotHeight * (index / 3f))
        drawLine(
            color = VerevColors.Forest.copy(alpha = 0.10f),
            start = androidx.compose.ui.geometry.Offset(0f, y),
            end = androidx.compose.ui.geometry.Offset(size.width, y),
            strokeWidth = strokeWidth,
        )
    }
}

private fun AnalyticsTimeRange.labelRes(): Int = when (this) {
    AnalyticsTimeRange.WEEK -> R.string.merchant_range_week
    AnalyticsTimeRange.MONTH -> R.string.merchant_range_month
    AnalyticsTimeRange.QUARTER -> R.string.merchant_range_quarter
    AnalyticsTimeRange.YEAR -> R.string.merchant_range_year
}
