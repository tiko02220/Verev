package com.vector.verevcodex.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.analytics.DashboardSnapshot
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun DashboardOverviewCard(snapshot: DashboardSnapshot) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.merchant_branch_overview),
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.9.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = VerevColors.Forest.copy(alpha = 0.58f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DashboardOverviewMetric(
                    label = stringResource(R.string.merchant_metric_transactions_short),
                    value = snapshot.recentTransactions.size.toString(),
                    delta = null,
                    modifier = Modifier.weight(1f),
                )
                DashboardOverviewMetric(
                    label = stringResource(R.string.merchant_metric_revenue),
                    value = compactDashboardCurrency(snapshot.recentTransactions.sumOf { it.amount }),
                    delta = dashboardGrowth(snapshot.analytics.revenueGrowthRate),
                    modifier = Modifier.weight(1f),
                )
                DashboardOverviewMetric(
                    label = stringResource(R.string.merchant_metric_programs),
                    value = snapshot.activePrograms.size.toString(),
                    delta = stringResource(R.string.merchant_metric_active),
                    modifier = Modifier.weight(1f),
                )
                DashboardOverviewMetric(
                    label = stringResource(R.string.merchant_metric_members),
                    value = snapshot.analytics.totalCustomers.toString(),
                    delta = dashboardGrowth(snapshot.analytics.customerGrowthRate),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
internal fun DashboardQuickActions(
    onOpenScan: () -> Unit,
    onOpenAddCustomer: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DashboardSectionTitle(text = stringResource(R.string.merchant_quick_actions))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardActionCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.merchant_action_scan_card),
                subtitle = stringResource(R.string.merchant_action_scan_card_subtitle),
                icon = Icons.Default.CreditCard,
                gradient = listOf(VerevColors.Gold, VerevColors.Tan),
                iconGlow = Color(0x4DFFBA00),
                onClick = onOpenScan,
            )
            DashboardActionCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.merchant_action_add_member),
                subtitle = stringResource(R.string.merchant_action_add_member_subtitle),
                icon = Icons.Default.PersonAdd,
                gradient = listOf(VerevColors.Moss, VerevColors.Forest),
                iconGlow = Color(0x4D6B9773),
                onClick = onOpenAddCustomer,
            )
        }
    }
}

@Composable
internal fun DashboardPromotionCard(
    snapshot: DashboardSnapshot,
    onOpenPromotions: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenPromotions),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(VerevColors.Gold, VerevColors.Tan)))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = stringResource(R.string.merchant_promotions_title),
                        style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = Color.White,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(horizontal = 9.dp, vertical = 7.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.84f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DashboardPromotionStatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.merchant_metric_active),
                    value = snapshot.activeCampaigns.size.toString(),
                )
                DashboardPromotionStatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.merchant_metric_revenue),
                    value = compactDashboardCurrency(snapshot.recentTransactions.sumOf { it.amount }),
                )
                DashboardPromotionStatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.merchant_metric_rate),
                    value = dashboardPercent(snapshot.analytics.rewardRedemptionRate),
                )
            }
            Text(
                text = stringResource(R.string.merchant_promotions_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
internal fun DashboardTodayStats(snapshot: DashboardSnapshot) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DashboardSectionTitle(text = stringResource(R.string.merchant_today_stats))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardTodayStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LocalActivity,
                accent = VerevColors.Gold,
                label = stringResource(R.string.merchant_metric_transactions),
                value = snapshot.analytics.visitsToday.toString(),
            )
            DashboardTodayStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.VolunteerActivism,
                accent = VerevColors.Moss,
                label = stringResource(R.string.merchant_metric_rewards),
                value = dashboardPercent(snapshot.analytics.rewardRedemptionRate),
            )
        }
    }
}

@Composable
internal fun DashboardStateCard(
    title: String,
    subtitle: String,
    icon: ImageVector = Icons.Default.Storefront,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    VerevColors.Gold.copy(alpha = 0.18f),
                                    VerevColors.Tan.copy(alpha = 0.18f),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = VerevColors.Forest, modifier = Modifier.size(30.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VerevColors.Forest.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun DashboardSectionTitle(text: String) {
        Text(
        text = text,
        style = TextStyle(
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.3).sp,
            fontWeight = FontWeight.Normal,
        ),
        color = VerevColors.Forest,
    )
}

@Composable
private fun DashboardActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    iconGlow: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(iconGlow),
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        letterSpacing = (-0.3).sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = VerevColors.Forest,
                )
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = VerevColors.Forest.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun DashboardOverviewMetric(
    label: String,
    value: String,
    delta: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.sp,
                lineHeight = 14.sp,
                letterSpacing = 0.6.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = VerevColors.Forest.copy(alpha = 0.5f),
        )
        if (value.endsWith(" AMD")) {
            DashboardMoneyValue(value = value)
        } else {
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = VerevColors.Forest,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        if (!delta.isNullOrBlank()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (delta.startsWith("+")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = VerevColors.Moss,
                        modifier = Modifier.size(12.dp),
                    )
                }
                Text(
                    text = delta,
                    style = TextStyle(
                        fontSize = 9.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = if (delta == stringResource(R.string.merchant_metric_active)) VerevColors.Forest.copy(alpha = 0.4f) else VerevColors.Moss,
                )
            }
        }
    }
}

@Composable
private fun DashboardPromotionStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Normal,
            ),
            color = Color.White.copy(alpha = 0.72f),
        )
        if (value.endsWith(" AMD")) {
            DashboardMoneyValue(
                value = value,
                color = Color.White,
                numberSize = 17.sp,
                codeSize = 11.sp,
            )
        } else {
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 17.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = Color.White,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DashboardTodayStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = VerevColors.Forest.copy(alpha = 0.6f),
                )
            }
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = VerevColors.Forest,
            )
        }
    }
}

@Composable
private fun DashboardMoneyValue(
    value: String,
    color: Color = VerevColors.Forest,
    numberSize: androidx.compose.ui.unit.TextUnit = 20.sp,
    codeSize: androidx.compose.ui.unit.TextUnit = 11.sp,
) {
    val splitIndex = value.lastIndexOf(' ')
    if (splitIndex <= 0 || splitIndex >= value.lastIndex) {
        Text(
            text = value,
            style = TextStyle(
                fontSize = numberSize,
                lineHeight = numberSize,
                fontWeight = FontWeight.SemiBold,
            ),
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        return
    }
    val amount = value.substring(0, splitIndex)
    val currency = value.substring(splitIndex + 1)
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontSize = numberSize,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                ),
            ) { append(amount) }
            append(" ")
            withStyle(
                SpanStyle(
                    fontSize = codeSize,
                    fontWeight = FontWeight.Medium,
                    color = color.copy(alpha = 0.88f),
                ),
            ) { append(currency) }
        },
        modifier = Modifier.height((numberSize.value + 8).dp),
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}

private fun compactDashboardCurrency(amount: Double): String =
    com.vector.verevcodex.presentation.merchant.common.formatCompactCurrency(amount)

private fun dashboardPercent(value: Double): String = "${value.toInt()}%"

private fun dashboardGrowth(value: Double): String? {
    if (value == 0.0 || value.isNaN()) return null
    val rounded = kotlin.math.round(value).toInt()
    val prefix = if (rounded > 0) "+" else ""
    return "$prefix$rounded%"
}
