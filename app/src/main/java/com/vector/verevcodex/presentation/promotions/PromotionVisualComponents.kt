@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.vector.verevcodex.presentation.promotions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stars
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.PromotionType
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatCompactCurrency
import com.vector.verevcodex.presentation.theme.VerevColors
import kotlin.math.max

data class PromotionSummaryStats(
    val activeCount: Int,
    val revenueLabel: String,
    val customerCountLabel: String,
    val averageRateLabel: String,
)

@Composable
internal fun PromotionsHeader(
    storeName: String,
    stats: PromotionSummaryStats,
    onBack: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.auth_back),
                    tint = Color.White,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.merchant_promotions_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                Text(
                    text = stringResource(R.string.merchant_campaigns_store_subtitle, storeName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.74f),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PromotionHeaderStatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.merchant_metric_active),
                    value = stats.activeCount.toString(),
                    icon = Icons.Default.Campaign,
                    containerBrush = Brush.linearGradient(listOf(VerevColors.Moss, VerevColors.ForestDeep)),
                    contentColor = Color.White,
                )
                PromotionHeaderStatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.merchant_promotions_metric_revenue),
                    value = stats.revenueLabel,
                    icon = Icons.Default.Payments,
                    containerColor = Color.White,
                    contentColor = VerevColors.Forest,
                    iconTint = VerevColors.Gold,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PromotionHeaderStatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.merchant_promotions_metric_customers),
                    value = stats.customerCountLabel,
                    icon = Icons.Default.LocalOffer,
                    containerColor = Color.White,
                    contentColor = VerevColors.Forest,
                    iconTint = VerevColors.Moss,
                )
                PromotionHeaderStatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.merchant_promotions_metric_rate),
                    value = stats.averageRateLabel,
                    icon = Icons.Default.Percent,
                    containerColor = Color.White,
                    contentColor = VerevColors.Forest,
                    iconTint = VerevColors.Gold,
                )
            }
        }
    }
}

@Composable
private fun PromotionHeaderStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerBrush: Brush? = null,
    containerColor: Color = Color.Transparent,
    contentColor: Color,
    iconTint: Color = Color.White,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = if (containerBrush == null) containerColor else Color.Transparent,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerBrush ?: Brush.linearGradient(listOf(containerColor, containerColor)))
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = if (containerBrush == null) 0.08f else 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.7f),
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                )
            }
        }
    }
}

@Composable
internal fun PromotionsFilterRow(selectedFilter: PromotionFilter, onSelected: (PromotionFilter) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PromotionFilter.entries.forEach { filter ->
            val selected = selectedFilter == filter
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.Transparent,
                shadowElevation = if (selected) 8.dp else 4.dp,
                modifier = Modifier.clickable { onSelected(filter) },
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (selected) {
                                Brush.horizontalGradient(listOf(VerevColors.Gold, VerevColors.Moss))
                            } else {
                                Brush.horizontalGradient(listOf(Color.White, Color.White))
                            },
                        )
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = stringResource(filter.labelRes),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (selected) Color.White else VerevColors.Forest.copy(alpha = 0.68f),
                    )
                }
            }
        }
    }
}

@Composable
internal fun PromotionsCreateButton(onCreate: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCreate),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        shadowElevation = 12.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(VerevColors.Gold, VerevColors.Moss)))
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Text(
                text = stringResource(R.string.merchant_promotion_create_action),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}

@Composable
internal fun PromotionListCard(
    promotion: Campaign,
    performance: PromotionPerformanceSummary,
    onOpen: () -> Unit,
) {
    val status = promotion.promotionStatus()
    val accent = status.accentColor()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(accent.copy(alpha = 0.16f), accent.copy(alpha = 0.05f)),
                        ),
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.24f), accent.copy(alpha = 0.1f)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = promotion.promotionType.icon(),
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = promotion.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = VerevColors.Forest,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = promotion.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.62f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    PromotionStatusBadge(status = status)
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PromotionInfoChip(
                        label = promotion.promotionValueText(),
                        icon = Icons.Default.LocalOffer,
                        containerColor = accent.copy(alpha = 0.14f),
                        contentColor = accent,
                    )
                    PromotionInfoChip(
                        label = stringResource(promotion.promotionType.displayLabelRes()),
                        icon = promotion.promotionType.icon(),
                        containerColor = VerevColors.Forest.copy(alpha = 0.08f),
                        contentColor = VerevColors.Forest,
                    )
                    if (promotion.paymentFlowEnabled) {
                        PromotionInfoChip(
                            label = stringResource(R.string.merchant_promotions_filter_payment),
                            icon = Icons.Default.Payments,
                            containerColor = VerevColors.Gold.copy(alpha = 0.14f),
                            contentColor = VerevColors.Forest,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PromotionValuePill(label = promotion.promotionValueText(), accent = accent)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = VerevColors.Forest.copy(alpha = 0.42f),
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = promotion.endDateText(),
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.62f),
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    PromotionCardStat(
                        label = stringResource(R.string.merchant_promotions_metric_used),
                        value = formatCompactCount(performance.usageCount),
                        valueColor = VerevColors.Forest,
                        modifier = Modifier.weight(1f),
                    )
                    PromotionCardStatDivider()
                    PromotionCardStat(
                        label = stringResource(R.string.merchant_promotions_metric_revenue),
                        value = formatCompactCurrency(performance.revenue),
                        valueColor = VerevColors.Moss,
                        modifier = Modifier.weight(1f),
                    )
                    PromotionCardStatDivider()
                    PromotionCardStat(
                        label = stringResource(R.string.merchant_promotions_metric_rate),
                        value = formatPromotionRate(performance.redemptionRate),
                        valueColor = VerevColors.Gold,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
internal fun PromotionDetailCard(
    promotion: Campaign,
    performance: PromotionPerformanceSummary,
    weeklyPerformance: List<PromotionDailyPerformance>,
    recentRedemptions: List<PromotionRedemptionSummary>,
    onOpenPayment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    val status = promotion.promotionStatus()
    val accent = status.accentColor()

    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(Brush.verticalGradient(listOf(accent, accent.copy(alpha = 0.86f))))
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.clickable(onClick = onBack),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    Text(
                        text = stringResource(R.string.auth_back),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                }
                PromotionStatusBadge(status = status, filled = false)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = promotion.promotionType.icon(),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = promotion.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = promotion.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.82f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PromotionInfoChip(
                            label = stringResource(promotion.promotionType.displayLabelRes()),
                            icon = promotion.promotionType.icon(),
                            containerColor = Color.White.copy(alpha = 0.18f),
                            contentColor = Color.White,
                        )
                        if (promotion.paymentFlowEnabled) {
                            PromotionInfoChip(
                                label = stringResource(R.string.merchant_promotions_filter_payment),
                                icon = Icons.Default.Payments,
                                containerColor = Color.White.copy(alpha = 0.18f),
                                contentColor = Color.White,
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                PromotionActionPill(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.merchant_edit),
                    icon = Icons.Default.Edit,
                    onClick = onEdit,
                )
                PromotionActionPill(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.merchant_delete),
                    icon = Icons.Default.DeleteOutline,
                    onClick = onDelete,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PromotionDiscountHero(promotion = promotion, accent = accent)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PromotionDetailMetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.merchant_promotions_metric_used),
                    value = formatCompactCount(performance.usageCount),
                    supporting = stringResource(R.string.merchant_promotions_metric_total),
                    icon = Icons.Default.Stars,
                    iconTint = VerevColors.Moss,
                )
                PromotionDetailMetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.merchant_promotions_metric_revenue),
                    value = formatCompactCurrency(performance.revenue),
                    supporting = stringResource(R.string.merchant_promotions_metric_value),
                    icon = Icons.Default.Payments,
                    iconTint = VerevColors.Gold,
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PromotionDetailMetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.merchant_promotions_metric_customers),
                    value = formatCompactCount(performance.customerCount),
                    supporting = stringResource(R.string.merchant_promotion_target_title),
                    icon = Icons.Default.LocalOffer,
                    iconTint = VerevColors.Moss,
                )
                PromotionDetailMetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.merchant_promotions_metric_rate),
                    value = formatPromotionRate(performance.redemptionRate),
                    supporting = stringResource(R.string.merchant_promotions_performance_title),
                    icon = Icons.Default.Percent,
                    iconTint = VerevColors.Gold,
                )
            }

            PromotionSectionCard(title = stringResource(R.string.merchant_promotions_details_title)) {
                PromotionKeyValueRow(
                    label = stringResource(R.string.merchant_campaign_start_date),
                    value = promotion.startDateText(),
                )
                PromotionKeyValueRow(
                    label = stringResource(R.string.merchant_campaign_end_date),
                    value = promotion.endDateText(),
                )
                PromotionKeyValueRow(
                    label = stringResource(R.string.merchant_campaign_target),
                    value = stringResource(promotion.target.segment.displayLabelRes()),
                )
                PromotionKeyValueRow(
                    label = stringResource(R.string.merchant_promotion_type_title),
                    value = stringResource(promotion.promotionType.displayLabelRes()),
                )
                PromotionKeyValueRow(
                    label = stringResource(R.string.merchant_promotion_code_title),
                    value = promotion.promoCode ?: stringResource(R.string.merchant_promotion_code_none),
                )
                PromotionKeyValueRow(
                    label = stringResource(R.string.merchant_promotion_payment_flow_title),
                    value = if (promotion.paymentFlowEnabled) {
                        stringResource(R.string.merchant_promotion_payment_flow_enabled)
                    } else {
                        stringResource(R.string.merchant_promotion_payment_flow_disabled)
                    },
                    isLast = !promotion.paymentFlowEnabled,
                )
                if (promotion.paymentFlowEnabled) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clickable(onClick = onOpenPayment),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Transparent,
                        shadowElevation = 6.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.horizontalGradient(listOf(VerevColors.Gold, VerevColors.Moss)))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text(
                                text = stringResource(R.string.merchant_network_promotion_payment_title),
                                modifier = Modifier.padding(start = 8.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            PromotionPerformanceCard(weeklyPerformance = weeklyPerformance)
            PromotionRecentRedemptionsCard(redemptions = recentRedemptions)
        }
    }
}

@Composable
internal fun PromotionsEmptyState() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(VerevColors.Forest.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null, tint = VerevColors.Forest.copy(alpha = 0.34f), modifier = Modifier.size(30.dp))
            }
            Text(
                text = stringResource(R.string.merchant_promotions_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.merchant_promotions_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.56f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PromotionValuePill(label: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.18f), accent.copy(alpha = 0.08f))))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = accent,
            )
        }
    }
}

@Composable
private fun PromotionCardStat(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VerevColors.Forest.copy(alpha = 0.56f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PromotionCardStatDivider() {
    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .width(1.dp)
            .height(26.dp)
            .background(VerevColors.Forest.copy(alpha = 0.08f)),
    )
}

@Composable
private fun PromotionStatusBadge(status: PromotionStatus, filled: Boolean = true) {
    val accent = status.accentColor()
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (filled) accent else Color.White.copy(alpha = 0.16f),
    ) {
        Text(
            text = stringResource(status.labelRes()).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (filled) Color.White else Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun PromotionInfoChip(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor,
        )
    }
}

@Composable
private fun PromotionActionPill(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.16f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun PromotionDiscountHero(promotion: Campaign, accent: Color) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.12f), accent.copy(alpha = 0.04f))))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = accent)
            }
            Text(
                text = promotion.promotionValueText(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(promotion.target.segment.displayLabelRes()),
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PromotionDetailMetricCard(
    title: String,
    value: String,
    supporting: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = VerevColors.Forest.copy(alpha = 0.62f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = VerevColors.Forest,
            )
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.5f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PromotionSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = VerevColors.Forest,
                )
                content()
            },
        )
    }
}

@Composable
private fun PromotionKeyValueRow(label: String, value: String, isLast: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(0.46f),
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.58f),
            )
            Text(
                text = value,
                modifier = Modifier.weight(0.54f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = VerevColors.Forest,
                textAlign = TextAlign.End,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(VerevColors.Forest.copy(alpha = 0.08f)),
            )
        }
    }
}

@Composable
private fun PromotionPerformanceCard(weeklyPerformance: List<PromotionDailyPerformance>) {
    PromotionSectionCard(title = stringResource(R.string.merchant_promotions_performance_title)) {
        val maxRevenue = max(1.0, weeklyPerformance.maxOfOrNull { it.revenue } ?: 1.0)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            weeklyPerformance.forEach { point ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = formatCompactCount(point.usageCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = VerevColors.Forest.copy(alpha = 0.52f),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(((point.revenue / maxRevenue) * 90).coerceAtLeast(8.0).dp)
                                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                .background(Brush.verticalGradient(listOf(VerevColors.Gold, VerevColors.Moss))),
                        )
                    }
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = VerevColors.Forest.copy(alpha = 0.62f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PromotionRecentRedemptionsCard(redemptions: List<PromotionRedemptionSummary>) {
    PromotionSectionCard(title = stringResource(R.string.merchant_promotions_recent_redemptions_title)) {
        if (redemptions.isEmpty()) {
            Text(
                text = stringResource(R.string.merchant_promotions_recent_redemptions_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.52f),
            )
        } else {
            redemptions.forEachIndexed { index, redemption ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(VerevColors.Forest.copy(alpha = 0.06f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = VerevColors.Moss, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = redemption.customerName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = VerevColors.Forest,
                            )
                            Text(
                                text = redemption.timeLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = VerevColors.Forest.copy(alpha = 0.5f),
                            )
                        }
                        Text(
                            text = formatCompactCurrency(redemption.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = VerevColors.Forest,
                        )
                    }
                    if (index != redemptions.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(VerevColors.Forest.copy(alpha = 0.08f)),
                        )
                    }
                }
            }
        }
    }
}

private fun PromotionType.icon() = when (this) {
    PromotionType.POINTS_MULTIPLIER -> Icons.Default.Stars
    PromotionType.PERCENT_DISCOUNT -> Icons.Default.Percent
    PromotionType.FIXED_DISCOUNT -> Icons.Default.Payments
    PromotionType.BONUS_POINTS -> Icons.Default.LocalOffer
    PromotionType.BUY_ONE_GET_ONE -> Icons.Default.Campaign
    PromotionType.FREE_ITEM -> Icons.Default.LocalOffer
}

private fun PromotionStatus.accentColor(): Color = when (this) {
    PromotionStatus.ACTIVE -> VerevColors.Moss
    PromotionStatus.SCHEDULED -> VerevColors.Gold
    PromotionStatus.EXPIRED -> VerevColors.Inactive
}

private fun PromotionStatus.labelRes(): Int = when (this) {
    PromotionStatus.ACTIVE -> R.string.merchant_campaign_filter_active
    PromotionStatus.SCHEDULED -> R.string.merchant_campaign_filter_scheduled
    PromotionStatus.EXPIRED -> R.string.merchant_campaign_filter_expired
}
