package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.MerchantSkeletonBlock
import com.vector.verevcodex.presentation.merchant.common.MerchantSkeletonCard
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatCompactCurrency
import com.vector.verevcodex.presentation.common.sheets.AppBottomSheetDialog
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun CustomersHeaderPanel(
    title: String,
    subtitle: String,
    totalCount: Int,
    totalVisits: Int,
    totalRevenue: Double,
    query: String,
    selectedTier: LoyaltyTier?,
    selectedSort: CustomerListSortOption,
    hasActiveTierProgram: Boolean,
    onOpenAddCustomer: () -> Unit,
    onOpenFilter: () -> Unit,
    onOpenSort: () -> Unit,
    onQueryChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VerevColors.Forest.copy(alpha = 0.7f),
                )
            }
            CustomerHeaderIconAction(
                onClick = onOpenAddCustomer,
                icon = Icons.Default.Add,
                contentDescription = stringResource(R.string.merchant_add_customer_cta),
                emphasized = true,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = query,
                onValueChange = onQueryChange,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = VerevColors.Forest.copy(alpha = 0.38f),
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.merchant_customer_search_label),
                        color = VerevColors.Forest.copy(alpha = 0.4f),
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = VerevColors.Gold,
                    unfocusedBorderColor = VerevColors.Forest.copy(alpha = 0.08f),
                    cursorColor = VerevColors.Forest,
                ),
            )
            CustomerHeaderIconAction(
                onClick = onOpenFilter,
                icon = Icons.Default.FilterList,
                contentDescription = stringResource(R.string.merchant_customers_filter_action),
                emphasized = selectedTier != null && hasActiveTierProgram,
            )
            CustomerHeaderIconAction(
                onClick = onOpenSort,
                icon = Icons.AutoMirrored.Filled.Sort,
                contentDescription = stringResource(R.string.merchant_customers_sort_action),
                emphasized = selectedSort != CustomerListSortOption.RECENT_ACTIVITY,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(VerevColors.Moss, VerevColors.Forest)))
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CustomerHeaderFigure(
                    value = formatCompactCount(totalCount),
                    label = stringResource(R.string.merchant_customers_header_members),
                )
                CustomerHeaderFigure(
                    value = formatCompactCount(totalVisits),
                    label = stringResource(R.string.merchant_metric_visits),
                )
                CustomerHeaderFigure(
                    value = formatCompactCurrency(totalRevenue),
                    label = stringResource(R.string.merchant_metric_revenue),
                )
            }
        }
    }
}

@Composable
internal fun CustomerListSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(4) {
            MerchantSkeletonCard(shape = RoundedCornerShape(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MerchantSkeletonBlock(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        MerchantSkeletonBlock(
                            modifier = Modifier.fillMaxWidth(0.46f).height(18.dp),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MerchantSkeletonBlock(
                                modifier = Modifier.fillMaxWidth(0.24f).height(14.dp),
                            )
                            MerchantSkeletonBlock(
                                modifier = Modifier.fillMaxWidth(0.18f).height(14.dp),
                            )
                        }
                    }
                    MerchantSkeletonBlock(
                        modifier = Modifier.size(34.dp),
                        shape = CircleShape,
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerHeaderIconAction(
    icon: ImageVector,
    contentDescription: String,
    emphasized: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (emphasized) VerevColors.Gold else VerevColors.Forest.copy(alpha = 0.72f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
internal fun CustomerTierFilterSheet(
    hasActiveTierProgram: Boolean,
    selectedTier: LoyaltyTier?,
    onDismiss: () -> Unit,
    onTierSelected: (LoyaltyTier?) -> Unit,
) {
    AppBottomSheetDialog(
        onDismissRequest = onDismiss,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) { dismiss, dismissAfter ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            CustomerSheetHeader(
                title = stringResource(R.string.merchant_customers_filter_sheet_title),
                subtitle = stringResource(
                    if (hasActiveTierProgram) {
                        R.string.merchant_customers_filter_sheet_subtitle
                    } else {
                        R.string.merchant_customers_filter_sheet_disabled_subtitle
                    }
                ),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CustomerSheetOptionRow(
                    title = stringResource(R.string.merchant_filter_all),
                    selected = selectedTier == null || !hasActiveTierProgram,
                    onClick = { dismissAfter { onTierSelected(null) } },
                )
                if (hasActiveTierProgram) {
                    LoyaltyTier.entries.forEach { tier ->
                        CustomerSheetOptionRow(
                            title = tier.displayName(),
                            selected = selectedTier == tier,
                            onClick = { dismissAfter { onTierSelected(tier) } },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun CustomerSortSheet(
    selectedSort: CustomerListSortOption,
    onDismiss: () -> Unit,
    onSortSelected: (CustomerListSortOption) -> Unit,
) {
    AppBottomSheetDialog(
        onDismissRequest = onDismiss,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) { _, dismissAfter ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            CustomerSheetHeader(
                title = stringResource(R.string.merchant_customers_sort_sheet_title),
                subtitle = stringResource(R.string.merchant_customers_sort_sheet_subtitle),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CustomerListSortOption.entries.forEach { option ->
                    CustomerSheetOptionRow(
                        title = stringResource(option.labelRes),
                        selected = selectedSort == option,
                        onClick = { dismissAfter { onSortSelected(option) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerSheetHeader(
    title: String,
    subtitle: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = VerevColors.Forest,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest.copy(alpha = 0.62f),
        )
    }
}

@Composable
private fun CustomerSheetOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) VerevColors.Gold.copy(alpha = 0.12f) else VerevColors.AppBackground,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = VerevColors.Forest,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun CustomerCard(
    customer: CustomerListCardUi,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val model = customer.customer
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(onClick = onOpenProfile)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(VerevColors.Moss, VerevColors.Forest))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = VerevColors.White,
                modifier = Modifier.size(24.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f, fill = false),
                    text = listOf(model.firstName, model.lastName).filter { it.isNotBlank() }.joinToString(" "),
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (customer.showsTierBadge) {
                    CustomerTierPill(model.loyaltyTier, model.loyaltyTierLabel)
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CustomerInlineMetric(
                    dotColor = VerevColors.Gold,
                    value = formatCompactCount(model.currentPoints),
                    label = stringResource(R.string.merchant_metric_points).lowercase(),
                    valueColor = VerevColors.Gold,
                )
                CustomerDividerDot()
                CustomerInlineMetric(
                    dotColor = VerevColors.Moss,
                    value = formatCompactCount(model.totalVisits),
                    label = stringResource(R.string.merchant_metric_visits).lowercase(),
                    valueColor = VerevColors.Moss,
                )
            }
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(VerevColors.AppBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = VerevColors.Forest.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun CustomerHeaderFigure(
    value: String,
    label: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = VerevColors.White,
            fontWeight = FontWeight.Normal,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.White.copy(alpha = 0.72f),
        )
    }
}

@Composable
private fun CustomerTierPill(tier: LoyaltyTier, label: String) {
    val colors = tier.toUiColors()
    MerchantStatusPill(
        text = label,
        backgroundColor = colors.background,
        contentColor = colors.content,
    )
}

@Composable
private fun CustomerTierFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) VerevColors.Gold else Color.White)
            .clickable(onClick = onClick)
            .height(44.dp)
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = if (selected) VerevColors.White else VerevColors.Forest,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun CustomerInlineMetric(
    dotColor: Color,
    value: String,
    label: String,
    valueColor: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.48f),
        )
    }
}

@Composable
private fun CustomerDividerDot() {
    Text(
        text = "|",
        style = MaterialTheme.typography.bodySmall,
        color = VerevColors.Forest.copy(alpha = 0.18f),
    )
}
