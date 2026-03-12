package com.vector.verevcodex.presentation.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatCompactCurrency
import com.vector.verevcodex.presentation.merchant.common.formatPercent
import com.vector.verevcodex.presentation.promotions.displayLabelRes
import com.vector.verevcodex.presentation.promotions.promotionValueText
import com.vector.verevcodex.presentation.theme.VerevColors
import java.time.LocalDate

internal enum class CampaignFilter(val labelRes: Int) {
    ALL(R.string.merchant_filter_all),
    ACTIVE(R.string.merchant_campaign_filter_active),
    SCHEDULED(R.string.merchant_campaign_filter_scheduled),
    EXPIRED(R.string.merchant_campaign_filter_expired),
}

internal enum class CampaignStatus {
    ACTIVE,
    SCHEDULED,
    EXPIRED,
}

internal fun Campaign.status(today: LocalDate = LocalDate.now()): CampaignStatus = when {
    !active || endDate.isBefore(today) -> CampaignStatus.EXPIRED
    startDate.isAfter(today) -> CampaignStatus.SCHEDULED
    else -> CampaignStatus.ACTIVE
}

internal fun Campaign.matches(filter: CampaignFilter): Boolean = when (filter) {
    CampaignFilter.ALL -> true
    CampaignFilter.ACTIVE -> status() == CampaignStatus.ACTIVE
    CampaignFilter.SCHEDULED -> status() == CampaignStatus.SCHEDULED
    CampaignFilter.EXPIRED -> status() == CampaignStatus.EXPIRED
}

@Composable
internal fun CampaignsHeader(storeName: String, onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.clickable(onClick = onBack),
                color = VerevColors.AppBackground,
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = VerevColors.Forest, modifier = Modifier.size(20.dp))
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.auth_back),
                        style = MaterialTheme.typography.titleSmall,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.merchant_campaigns_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.merchant_campaigns_store_subtitle, storeName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.66f),
                )
            }
        }
    }
}

@Composable
internal fun CampaignStatsGrid(state: CampaignsUiState) {
    val activeCount = state.campaigns.count { it.status() == CampaignStatus.ACTIVE }
    val scheduledCount = state.campaigns.count { it.status() == CampaignStatus.SCHEDULED }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(28.dp),
            color = Color.Transparent,
        ) {
            Box(
                modifier = Modifier
                    .background(Brush.linearGradient(listOf(VerevColors.Moss, VerevColors.Forest)), RoundedCornerShape(28.dp))
                    .padding(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White)
                    }
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.merchant_metric_active),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.82f),
                    )
                    Text(
                        text = formatCompactCount(activeCount),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.merchant_campaigns_metric_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.72f),
                    )
                }
            }
        }
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CampaignMiniMetric(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = androidx.compose.ui.res.stringResource(R.string.merchant_metric_revenue),
                    value = formatCompactCurrency(state.revenue),
                    accent = VerevColors.Gold,
                )
                CampaignMiniMetric(
                    icon = Icons.Default.Groups,
                    label = androidx.compose.ui.res.stringResource(R.string.merchant_metric_members),
                    value = formatCompactCount(state.customerCount),
                    accent = VerevColors.Moss,
                )
                CampaignMiniMetric(
                    icon = Icons.Default.Percent,
                    label = androidx.compose.ui.res.stringResource(R.string.merchant_metric_rate),
                    value = formatPercent(state.rewardRate),
                    accent = VerevColors.Tan,
                )
                CampaignMiniMetric(
                    icon = Icons.Default.Schedule,
                    label = androidx.compose.ui.res.stringResource(R.string.merchant_campaign_filter_scheduled),
                    value = formatCompactCount(scheduledCount),
                    accent = VerevColors.Forest,
                )
            }
        }
    }
}

@Composable
private fun CampaignMiniMetric(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.54f))
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
internal fun CampaignFilterRow(selectedFilter: CampaignFilter, onFilterSelected: (CampaignFilter) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(CampaignFilter.entries) { filter ->
            Surface(
                modifier = Modifier
                    .clickable { onFilterSelected(filter) },
                shape = RoundedCornerShape(18.dp),
                color = if (selectedFilter == filter) VerevColors.Forest else Color.White,
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(filter.labelRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedFilter == filter) Color.White else VerevColors.Forest,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
internal fun CampaignCard(campaign: Campaign, onOpen: () -> Unit) {
    val status = campaign.status()
    val (background, content) = when (status) {
        CampaignStatus.ACTIVE -> VerevColors.Moss.copy(alpha = 0.16f) to VerevColors.Moss
        CampaignStatus.SCHEDULED -> Color(0xFFFef3C7) to VerevColors.Gold
        CampaignStatus.EXPIRED -> Color(0xFFF1F5F9) to VerevColors.Inactive
    }
    Surface(
        modifier = Modifier.clickable(onClick = onOpen),
        color = Color.Transparent,
        shape = RoundedCornerShape(30.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White,
                            VerevColors.AppBackground,
                        ),
                    ),
                    shape = RoundedCornerShape(30.dp),
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
                        .background(Brush.linearGradient(listOf(VerevColors.Gold.copy(alpha = 0.14f), VerevColors.Tan.copy(alpha = 0.14f)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = VerevColors.Forest)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = campaign.name, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
                    Text(text = campaign.description, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.6f))
                }
                MerchantStatusPill(
                    text = when (status) {
                        CampaignStatus.ACTIVE -> androidx.compose.ui.res.stringResource(R.string.merchant_campaign_filter_active)
                        CampaignStatus.SCHEDULED -> androidx.compose.ui.res.stringResource(R.string.merchant_campaign_filter_scheduled)
                        CampaignStatus.EXPIRED -> androidx.compose.ui.res.stringResource(R.string.merchant_campaign_filter_expired)
                    },
                    backgroundColor = background,
                    contentColor = content,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CampaignDetailChip(
                    modifier = Modifier.weight(1f),
                    label = androidx.compose.ui.res.stringResource(R.string.merchant_promotion_type_title),
                    value = androidx.compose.ui.res.stringResource(campaign.promotionType.displayLabelRes()),
                )
                CampaignDetailChip(
                    modifier = Modifier.weight(1f),
                    label = androidx.compose.ui.res.stringResource(R.string.merchant_promotion_value_title),
                    value = campaign.promotionValueText(),
                )
            }
        }
    }
}

@Composable
internal fun CampaignDetailScreen(campaign: Campaign, onBack: () -> Unit) {
    val status = campaign.status()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CampaignDetailHeader(campaign = campaign, status = status, onBack = onBack)
        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(32.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                VerevColors.Gold.copy(alpha = 0.14f),
                                Color.White,
                            ),
                        ),
                        shape = RoundedCornerShape(32.dp),
                    )
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
                }
                Text(text = campaign.name, style = MaterialTheme.typography.headlineSmall, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
                Text(text = campaign.description, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest.copy(alpha = 0.64f))
                MerchantStatusPill(
                    text = when (status) {
                        CampaignStatus.ACTIVE -> androidx.compose.ui.res.stringResource(R.string.merchant_campaign_filter_active)
                        CampaignStatus.SCHEDULED -> androidx.compose.ui.res.stringResource(R.string.merchant_campaign_filter_scheduled)
                        CampaignStatus.EXPIRED -> androidx.compose.ui.res.stringResource(R.string.merchant_campaign_filter_expired)
                    },
                    backgroundColor = when (status) {
                        CampaignStatus.ACTIVE -> VerevColors.Moss.copy(alpha = 0.16f)
                        CampaignStatus.SCHEDULED -> Color(0xFFFef3C7)
                        CampaignStatus.EXPIRED -> Color(0xFFF1F5F9)
                    },
                    contentColor = when (status) {
                        CampaignStatus.ACTIVE -> VerevColors.Moss
                        CampaignStatus.SCHEDULED -> VerevColors.Gold
                        CampaignStatus.EXPIRED -> VerevColors.Inactive
                    },
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.merchant_campaign_detail_section),
                    style = MaterialTheme.typography.titleLarge,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                CampaignDetailChip(label = androidx.compose.ui.res.stringResource(R.string.merchant_campaign_start_date), value = campaign.startDate.toString())
                CampaignDetailChip(label = androidx.compose.ui.res.stringResource(R.string.merchant_campaign_end_date), value = campaign.endDate.toString())
                CampaignDetailChip(
                    label = androidx.compose.ui.res.stringResource(R.string.merchant_promotion_type_title),
                    value = androidx.compose.ui.res.stringResource(campaign.promotionType.displayLabelRes()),
                )
                CampaignDetailChip(label = androidx.compose.ui.res.stringResource(R.string.merchant_promotion_value_title), value = campaign.promotionValueText())
                CampaignDetailChip(label = androidx.compose.ui.res.stringResource(R.string.merchant_campaign_target), value = campaign.target.description)
            }
        }
    }
}

@Composable
private fun CampaignDetailHeader(
    campaign: Campaign,
    status: CampaignStatus,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.clickable(onClick = onBack),
            color = VerevColors.AppBackground,
            shape = RoundedCornerShape(20.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = VerevColors.Forest, modifier = Modifier.size(20.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.auth_back),
                    style = MaterialTheme.typography.titleSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = campaign.name,
                style = MaterialTheme.typography.headlineMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = when (status) {
                    CampaignStatus.ACTIVE -> androidx.compose.ui.res.stringResource(R.string.merchant_campaign_filter_active)
                    CampaignStatus.SCHEDULED -> androidx.compose.ui.res.stringResource(R.string.merchant_campaign_filter_scheduled)
                    CampaignStatus.EXPIRED -> androidx.compose.ui.res.stringResource(R.string.merchant_campaign_filter_expired)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.66f),
            )
        }
    }
}

@Composable
private fun CampaignDetailChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(VerevColors.Forest.copy(alpha = 0.06f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.52f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
    }
}
