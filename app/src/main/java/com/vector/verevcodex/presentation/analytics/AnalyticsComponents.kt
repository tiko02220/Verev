package com.vector.verevcodex.presentation.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.BusinessAnalytics
import com.vector.verevcodex.domain.model.StaffAnalytics
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantGradientMetricCard
import com.vector.verevcodex.presentation.merchant.common.MerchantMenuRow
import com.vector.verevcodex.presentation.merchant.common.MerchantPageHeader
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.merchant.common.MerchantSectionTitle
import com.vector.verevcodex.presentation.merchant.common.MerchantTextAction
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatCompactCurrency
import com.vector.verevcodex.presentation.merchant.common.formatPercent
import com.vector.verevcodex.presentation.theme.VerevColors
import kotlin.math.max

@Composable
internal fun AnalyticsHeader(
    onOpenReports: () -> Unit,
    onOpenStaffAnalytics: () -> Unit,
) {
    MerchantPageHeader(
        title = stringResource(R.string.merchant_analytics_title),
        subtitle = stringResource(R.string.merchant_analytics_subtitle),
        actions = {
            IconButton(onClick = onOpenReports) {
                Icon(Icons.Default.Download, contentDescription = null, tint = VerevColors.Moss)
            }
            IconButton(onClick = onOpenStaffAnalytics) {
                Icon(Icons.Default.QueryStats, contentDescription = null, tint = VerevColors.Forest)
            }
        },
    )
}

@Composable
internal fun AnalyticsRangeFilters(
    selectedRange: AnalyticsRange,
    onRangeSelected: (AnalyticsRange) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AnalyticsRange.entries.forEach { range ->
            MerchantFilterChip(
                text = stringResource(range.labelRes),
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
            )
        }
    }
}

@Composable
internal fun AnalyticsHeroCards(analytics: BusinessAnalytics) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MerchantGradientMetricCard(
                title = stringResource(R.string.merchant_metric_revenue),
                value = formatCompactCurrency(analytics.averagePurchaseValue * analytics.visitsToday),
                subtitle = stringResource(R.string.merchant_metric_growth_up, 21),
                icon = Icons.Default.Payments,
                colors = listOf(VerevColors.Gold, VerevColors.Tan),
                modifier = Modifier.weight(1f),
            )
            MerchantGradientMetricCard(
                title = stringResource(R.string.merchant_metric_customers),
                value = formatCompactCount(analytics.totalCustomers),
                subtitle = stringResource(R.string.merchant_metric_growth_up, 15),
                icon = Icons.Default.Groups,
                colors = listOf(VerevColors.Moss, VerevColors.Forest),
                modifier = Modifier.weight(1f),
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnalyticsMiniCard(
                label = stringResource(R.string.merchant_metric_average_purchase),
                value = formatCompactCurrency(analytics.averagePurchaseValue),
                icon = Icons.Default.Assessment,
                accent = VerevColors.Moss,
                modifier = Modifier.weight(1f),
            )
            AnalyticsMiniCard(
                label = stringResource(R.string.merchant_metric_reward_roi),
                value = formatPercent(analytics.rewardRedemptionRate),
                icon = Icons.Default.CardGiftcard,
                accent = VerevColors.Gold,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun AnalyticsRevenueTrendSection(analytics: BusinessAnalytics) {
    val points = buildRevenueTrendPoints(analytics)
    AnalyticsChartCard(
        title = stringResource(R.string.merchant_analytics_revenue_chart_title),
        subtitle = stringResource(R.string.merchant_analytics_revenue_chart_subtitle),
    ) {
        AnalyticsAreaChart(points = points, lineColor = VerevColors.Gold)
    }
}

@Composable
internal fun AnalyticsCustomerGrowthSection(analytics: BusinessAnalytics) {
    val points = buildCustomerGrowthPoints(analytics)
    AnalyticsChartCard(
        title = stringResource(R.string.merchant_analytics_customers_chart_title),
        subtitle = stringResource(R.string.merchant_analytics_customers_chart_subtitle),
    ) {
        AnalyticsBarChart(points = points)
    }
}

@Composable
internal fun AnalyticsPerformanceSection(analytics: BusinessAnalytics) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_analytics_performance_section))
        MerchantPrimaryCard {
            AnalyticsInfoRow(
                icon = Icons.Default.Payments,
                title = stringResource(R.string.merchant_metric_visits_today),
                subtitle = stringResource(R.string.merchant_metric_visits_today_subtitle),
                value = formatCompactCount(analytics.visitsToday),
            )
            AnalyticsInfoRow(
                icon = Icons.Default.Groups,
                title = stringResource(R.string.merchant_metric_new_customers),
                subtitle = stringResource(R.string.merchant_metric_new_customers_subtitle),
                value = formatCompactCount(analytics.newCustomers),
            )
            AnalyticsInfoRow(
                icon = Icons.Default.QueryStats,
                title = stringResource(R.string.merchant_metric_retention),
                subtitle = stringResource(R.string.merchant_metric_retention_subtitle),
                value = formatPercent(analytics.retentionRate),
            )
        }
    }
}

@Composable
internal fun AnalyticsInsightSection(
    analytics: BusinessAnalytics,
    staffAnalytics: List<StaffAnalytics>,
    onOpenStaffAnalytics: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_analytics_insights_section))
        MerchantMenuRow(
            title = stringResource(R.string.merchant_insight_top_customer),
            subtitle = analytics.topCustomerName,
            icon = Icons.Default.Groups,
            onClick = {},
            trailing = {
                Text(
                    text = formatPercent(analytics.rewardRedemptionRate),
                    color = VerevColors.Gold,
                    fontWeight = FontWeight.SemiBold,
                )
            },
        )
        staffAnalytics.take(3).forEach { staff ->
            MerchantMenuRow(
                title = stringResource(R.string.merchant_staff_performance_title),
                subtitle = stringResource(
                    R.string.merchant_staff_performance_subtitle,
                    formatCompactCount(staff.transactionsProcessed),
                    formatCompactCurrency(staff.revenueHandled),
                ),
                icon = Icons.Default.Person,
                onClick = onOpenStaffAnalytics,
                trailing = {
                    Text(
                        text = formatCompactCurrency(staff.averageTransactionValue),
                        color = VerevColors.Moss,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
            )
        }
    }
}

internal enum class AnalyticsRange(val labelRes: Int) {
    WEEK(R.string.merchant_range_week),
    MONTH(R.string.merchant_range_month),
    QUARTER(R.string.merchant_range_quarter),
    YEAR(R.string.merchant_range_year),
}

@Composable
private fun AnalyticsMiniCard(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    MerchantPrimaryCard(modifier = modifier, contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.6f))
        }
        Text(text = value, style = MaterialTheme.typography.headlineSmall, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AnalyticsInfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(VerevColors.AppBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = VerevColors.Forest)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.6f))
        }
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AnalyticsChartCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    MerchantPrimaryCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.6f))
        }
        content()
    }
}

@Composable
private fun AnalyticsAreaChart(
    points: List<AnalyticsChartPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 180.dp,
) {
    val maxValue = max(points.maxOfOrNull { it.primary } ?: 1f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = chartHeight),
        ) {
            if (points.size < 2) return@Canvas

            val spacing = size.width / (points.size - 1).coerceAtLeast(1)
            val linePath = Path()
            val fillPath = Path()

            points.forEachIndexed { index, point ->
                val x = spacing * index
                val y = size.height - ((point.primary / maxValue) * (size.height * 0.82f))
                if (index == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, size.height)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(size.width, size.height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.28f), Color.Transparent),
                ),
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f, cap = StrokeCap.Round),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            points.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun AnalyticsBarChart(
    points: List<AnalyticsChartPoint>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 180.dp,
) {
    val maxValue = max(points.maxOfOrNull { it.primary } ?: 1f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            points.forEach { point ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height((point.primary / maxValue * chartHeight.value).dp)
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                .background(VerevColors.Moss.copy(alpha = 0.24f)),
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height((point.secondary / maxValue * chartHeight.value).dp)
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                .background(VerevColors.Gold.copy(alpha = 0.8f)),
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.5f),
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AnalyticsLegendDot(color = VerevColors.Moss.copy(alpha = 0.24f), label = stringResource(R.string.merchant_metric_customers))
            AnalyticsLegendDot(color = VerevColors.Gold.copy(alpha = 0.8f), label = stringResource(R.string.merchant_metric_new_customers))
        }
    }
}

@Composable
private fun AnalyticsLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.6f))
    }
}

private data class AnalyticsChartPoint(
    val label: String,
    val primary: Float,
    val secondary: Float = 0f,
)

private fun buildRevenueTrendPoints(analytics: BusinessAnalytics): List<AnalyticsChartPoint> {
    val baseRevenue = max((analytics.averagePurchaseValue * analytics.visitsToday).toFloat(), 120f)
    val weights = listOf(0.64f, 0.79f, 0.58f, 0.9f, 1.08f, 1.2f, 0.95f)
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return labels.mapIndexed { index, label ->
        AnalyticsChartPoint(label = label, primary = baseRevenue * weights[index])
    }
}

private fun buildCustomerGrowthPoints(analytics: BusinessAnalytics): List<AnalyticsChartPoint> {
    val total = max(analytics.totalCustomers.toFloat(), 1f)
    val newCustomers = max(analytics.newCustomers.toFloat(), 1f)
    val labels = listOf("W1", "W2", "W3", "W4")
    val totalWeights = listOf(0.78f, 0.86f, 0.93f, 1f)
    val newWeights = listOf(0.42f, 0.57f, 0.74f, 1f)
    return labels.mapIndexed { index, label ->
        AnalyticsChartPoint(
            label = label,
            primary = total * totalWeights[index],
            secondary = newCustomers * newWeights[index],
        )
    }
}
