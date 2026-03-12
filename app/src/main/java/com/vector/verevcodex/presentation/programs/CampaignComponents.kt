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
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantGradientMetricCard
import com.vector.verevcodex.presentation.merchant.common.MerchantPageHeader
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.merchant.common.MerchantSectionTitle
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
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onBack)
                .padding(horizontal = 2.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = VerevColors.Forest, modifier = Modifier.size(22.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.auth_back),
                style = MaterialTheme.typography.titleMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
        }
        MerchantPageHeader(
            title = androidx.compose.ui.res.stringResource(R.string.merchant_campaigns_title),
            subtitle = androidx.compose.ui.res.stringResource(R.string.merchant_campaigns_store_subtitle, storeName),
        )
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
        MerchantGradientMetricCard(
            title = androidx.compose.ui.res.stringResource(R.string.merchant_metric_active),
            value = formatCompactCount(activeCount),
            subtitle = androidx.compose.ui.res.stringResource(R.string.merchant_campaigns_metric_subtitle),
            icon = Icons.Default.Campaign,
            colors = listOf(VerevColors.Moss, VerevColors.Forest),
            modifier = Modifier.weight(1f),
        )
        MerchantPrimaryCard(modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
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
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CampaignFilter.entries.forEach { filter ->
            MerchantFilterChip(
                text = androidx.compose.ui.res.stringResource(filter.labelRes),
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                modifier = Modifier.weight(1f),
            )
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
    MerchantPrimaryCard(modifier = Modifier.clickable(onClick = onOpen), contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp)) {
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

@Composable
internal fun CampaignDetailScreen(campaign: Campaign, onBack: () -> Unit) {
    val status = campaign.status()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CampaignsHeader(storeName = campaign.name, onBack = onBack)
        MerchantPrimaryCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(22.dp)) {
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
            MerchantSectionTitle(text = androidx.compose.ui.res.stringResource(R.string.merchant_campaign_detail_section))
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

@Composable
private fun CampaignDetailChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(VerevColors.AppBackground)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.52f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
    }
}
