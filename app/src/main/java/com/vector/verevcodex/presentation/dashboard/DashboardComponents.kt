package com.vector.verevcodex.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.DashboardSnapshot
import com.vector.verevcodex.presentation.merchant.common.MerchantActionCard
import com.vector.verevcodex.presentation.merchant.common.MerchantMiniMetricCard
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.merchant.common.MerchantSectionTitle
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatCompactCurrency
import com.vector.verevcodex.presentation.merchant.common.formatPercent
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun DashboardOverviewCard(snapshot: DashboardSnapshot) {
    MerchantPrimaryCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)) {
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.merchant_branch_overview),
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DashboardOverviewMetric(
                label = androidx.compose.ui.res.stringResource(R.string.merchant_metric_transactions_short),
                value = formatCompactCount(snapshot.recentTransactions.size),
                delta = androidx.compose.ui.res.stringResource(R.string.merchant_metric_growth_up, 12),
                modifier = Modifier.weight(1f),
            )
            DashboardOverviewMetric(
                label = androidx.compose.ui.res.stringResource(R.string.merchant_metric_revenue),
                value = formatCompactCurrency(snapshot.recentTransactions.sumOf { it.amount }),
                delta = androidx.compose.ui.res.stringResource(R.string.merchant_metric_growth_up, 8),
                modifier = Modifier.weight(1f),
            )
            DashboardOverviewMetric(
                label = androidx.compose.ui.res.stringResource(R.string.merchant_metric_programs),
                value = formatCompactCount(snapshot.activePrograms.size),
                delta = androidx.compose.ui.res.stringResource(R.string.merchant_metric_active),
                modifier = Modifier.weight(1f),
            )
            DashboardOverviewMetric(
                label = androidx.compose.ui.res.stringResource(R.string.merchant_metric_members),
                value = formatCompactCount(snapshot.analytics.totalCustomers),
                delta = androidx.compose.ui.res.stringResource(R.string.merchant_metric_growth_count, 24),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun DashboardQuickActions(
    onOpenScan: () -> Unit,
    onOpenAddCustomer: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MerchantSectionTitle(text = androidx.compose.ui.res.stringResource(R.string.merchant_quick_actions))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MerchantActionCard(
                title = androidx.compose.ui.res.stringResource(R.string.merchant_action_scan_card),
                subtitle = androidx.compose.ui.res.stringResource(R.string.merchant_action_scan_card_subtitle),
                icon = Icons.Default.CreditCard,
                colors = listOf(VerevColors.Gold, VerevColors.Tan),
                modifier = Modifier.weight(1f),
                onClick = onOpenScan,
            )
            MerchantActionCard(
                title = androidx.compose.ui.res.stringResource(R.string.merchant_action_add_member),
                subtitle = androidx.compose.ui.res.stringResource(R.string.merchant_action_add_member_subtitle),
                icon = Icons.Default.PersonAdd,
                colors = listOf(VerevColors.Moss, VerevColors.Forest),
                modifier = Modifier.weight(1f),
                onClick = onOpenAddCustomer,
            )
        }
    }
}

@Composable
internal fun DashboardPromotionCard(snapshot: DashboardSnapshot, onOpenPromotions: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenPromotions),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(VerevColors.Gold, VerevColors.Tan)))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White)
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.merchant_promotions_title),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PromotionStatCard(
                    label = androidx.compose.ui.res.stringResource(R.string.merchant_metric_active),
                    value = formatCompactCount(snapshot.activeCampaigns.size),
                    modifier = Modifier.weight(1f),
                )
                PromotionStatCard(
                    label = androidx.compose.ui.res.stringResource(R.string.merchant_metric_revenue),
                    value = formatCompactCurrency(snapshot.recentTransactions.sumOf { it.amount }),
                    modifier = Modifier.weight(1f),
                )
                PromotionStatCard(
                    label = androidx.compose.ui.res.stringResource(R.string.merchant_metric_rate),
                    value = formatPercent(snapshot.analytics.rewardRedemptionRate),
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.merchant_promotions_subtitle),
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
internal fun DashboardTodayStats(snapshot: DashboardSnapshot) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MerchantSectionTitle(text = androidx.compose.ui.res.stringResource(R.string.merchant_today_stats))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MerchantMiniMetricCard(
                label = androidx.compose.ui.res.stringResource(R.string.merchant_metric_transactions),
                value = formatCompactCount(snapshot.analytics.visitsToday),
                icon = Icons.Default.LocalActivity,
                accentColor = VerevColors.Gold,
                modifier = Modifier.weight(1f),
            )
            MerchantMiniMetricCard(
                label = androidx.compose.ui.res.stringResource(R.string.merchant_metric_rewards),
                value = formatPercent(snapshot.analytics.rewardRedemptionRate),
                icon = Icons.Default.VolunteerActivism,
                accentColor = VerevColors.Moss,
                modifier = Modifier.weight(1f),
            )
        }
        if (snapshot.topStaff.isNotEmpty()) {
            DashboardTopPerformerCard(
                name = listOf(snapshot.topStaff.first().first.firstName, snapshot.topStaff.first().first.lastName)
                    .filter { it.isNotBlank() }
                    .joinToString(" "),
                transactions = snapshot.topStaff.first().second.transactionsProcessed,
                revenue = snapshot.topStaff.first().second.revenueHandled,
            )
        }
    }
}

@Composable
private fun DashboardTopPerformerCard(name: String, transactions: Int, revenue: Double) {
    MerchantPrimaryCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(VerevColors.Moss.copy(alpha = 0.18f), VerevColors.Forest.copy(alpha = 0.18f)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = VerevColors.Forest)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.merchant_dashboard_top_staff),
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.58f),
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCompactCount(transactions),
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = formatCompactCurrency(revenue),
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.58f),
                )
            }
        }
    }
}

@Composable
private fun DashboardOverviewMetric(
    label: String,
    value: String,
    delta: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = delta,
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Moss,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PromotionStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(12.dp),
    ) {
        Text(text = label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
        Text(text = value, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
    }
}
