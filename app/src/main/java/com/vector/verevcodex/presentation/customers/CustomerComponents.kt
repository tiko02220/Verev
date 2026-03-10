package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.Customer
import com.vector.verevcodex.domain.model.LoyaltyTier
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantPageHeader
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.merchant.common.MerchantSearchField
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.MerchantTextAction
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatRelativeDateTime
import com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun CustomersHeader(totalCount: Int) {
    MerchantPageHeader(
        title = stringResource(R.string.merchant_customers_title),
        subtitle = stringResource(R.string.merchant_customers_subtitle, totalCount),
    )
}

@Composable
internal fun CustomerSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    MerchantSearchField(
        value = query,
        onValueChange = onQueryChange,
        label = stringResource(R.string.merchant_customer_search_label),
        leadingIcon = Icons.Default.Search,
    )
}

@Composable
internal fun CustomerTierFilters(
    selectedTier: LoyaltyTier?,
    onTierSelected: (LoyaltyTier?) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            MerchantFilterChip(
                text = stringResource(R.string.merchant_filter_all),
                selected = selectedTier == null,
                onClick = { onTierSelected(null) },
            )
        }
        items(LoyaltyTier.entries) { tier ->
            MerchantFilterChip(
                text = tier.displayName(),
                selected = selectedTier == tier,
                onClick = { onTierSelected(tier) },
            )
        }
    }
}

@Composable
internal fun CustomerCard(
    customer: Customer,
    onRewardBoost: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MerchantPrimaryCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listOf(customer.firstName, customer.lastName).filter { it.isNotBlank() }.joinToString(" "),
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.size(2.dp))
                Text(
                    text = customer.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.6f),
                )
            }
            CustomerTierPill(customer.loyaltyTier)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CustomerMiniStat(
                label = stringResource(R.string.merchant_metric_points),
                value = formatCompactCount(customer.currentPoints),
                modifier = Modifier.weight(1f),
            )
            CustomerMiniStat(
                label = stringResource(R.string.merchant_metric_spent),
                value = formatWholeCurrency(customer.totalSpent),
                modifier = Modifier.weight(1f),
            )
            CustomerMiniStat(
                label = stringResource(R.string.merchant_metric_visits),
                value = formatCompactCount(customer.totalVisits),
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = stringResource(R.string.merchant_customer_last_visit, formatRelativeDateTime(customer.lastVisit)),
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.5f),
        )
        Button(
            onClick = onRewardBoost,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Gold, contentColor = Color.White),
        ) {
            Text(text = stringResource(R.string.merchant_customer_goodwill_points))
        }
        MerchantTextAction(
            text = stringResource(R.string.merchant_customer_view_profile),
            onClick = onOpenProfile,
        )
    }
}

@Composable
private fun CustomerMiniStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(VerevColors.AppBackground)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.5f))
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CustomerTierPill(tier: LoyaltyTier) {
    val (backgroundColor, contentColor) = when (tier) {
        LoyaltyTier.BRONZE -> VerevColors.Tan.copy(alpha = 0.2f) to VerevColors.Tan
        LoyaltyTier.SILVER -> Color(0xFFE2E8F0) to Color(0xFF475569)
        LoyaltyTier.GOLD -> Color(0xFFFef3C7) to VerevColors.Gold
        LoyaltyTier.VIP -> Color(0xFFE9D5FF) to Color(0xFF7C3AED)
    }
    MerchantStatusPill(text = tier.displayName(), backgroundColor = backgroundColor, contentColor = contentColor)
}
