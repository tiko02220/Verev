package com.vector.verevcodex.presentation.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatRelativeDateTime
import com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun ScanIntroCard(storeName: String) {
    MerchantPrimaryCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .padding(0.dp)
                    .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.merchant_scan_intro_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.merchant_scan_intro_subtitle, storeName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.64f),
                )
            }
        }
    }
}

@Composable
internal fun ScanCustomerCard(customer: Customer) {
    MerchantPrimaryCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(VerevColors.Moss, VerevColors.Forest))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listOf(customer.firstName, customer.lastName).filter { it.isNotBlank() }.joinToString(" "),
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = customer.email.ifBlank { customer.phoneNumber },
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.6f),
                )
            }
            CustomerTierPill(customer.loyaltyTier)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ScanMetric(label = stringResource(R.string.merchant_metric_points), value = formatCompactCount(customer.currentPoints), modifier = Modifier.weight(1f))
            ScanMetric(label = stringResource(R.string.merchant_metric_visits), value = formatCompactCount(customer.totalVisits), modifier = Modifier.weight(1f))
            ScanMetric(label = stringResource(R.string.merchant_metric_spent), value = formatWholeCurrency(customer.totalSpent), modifier = Modifier.weight(1f))
        }
        Text(
            text = stringResource(R.string.merchant_customer_last_visit, formatRelativeDateTime(customer.lastVisit)),
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.52f),
        )
    }
}

@Composable
internal fun ScanActionChips(
    selectedAction: ScanCustomerAction,
    onActionSelected: (ScanCustomerAction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MerchantFilterChip(
            text = stringResource(R.string.merchant_scan_action_add_points),
            selected = selectedAction == ScanCustomerAction.ADD_POINTS,
            onClick = { onActionSelected(ScanCustomerAction.ADD_POINTS) },
            modifier = Modifier.weight(1f),
        )
        MerchantFilterChip(
            text = stringResource(R.string.merchant_scan_action_redeem_points),
            selected = selectedAction == ScanCustomerAction.REDEEM_POINTS,
            onClick = { onActionSelected(ScanCustomerAction.REDEEM_POINTS) },
            modifier = Modifier.weight(1f),
        )
        MerchantFilterChip(
            text = stringResource(R.string.merchant_scan_action_check_in),
            selected = selectedAction == ScanCustomerAction.CHECK_IN,
            onClick = { onActionSelected(ScanCustomerAction.CHECK_IN) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun ScanTriggerButtons(
    onKnown: () -> Unit,
    onNew: () -> Unit,
) {
    Button(
        onClick = onKnown,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Gold, contentColor = Color.White),
    ) {
        Text(stringResource(R.string.merchant_scan_demo_known))
    }
    Button(
        onClick = onNew,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Moss, contentColor = Color.White),
    ) {
        Text(stringResource(R.string.merchant_scan_demo_new))
    }
}

@Composable
internal fun ScanPrimaryActions(
    isSubmitting: Boolean,
    onApply: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    Button(
        onClick = onApply,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isSubmitting,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Forest, contentColor = Color.White),
    ) {
        Text(stringResource(R.string.merchant_scan_submit_action))
    }
    Button(
        onClick = onOpenProfile,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = VerevColors.Forest),
    ) {
        Text(stringResource(R.string.merchant_customer_view_profile))
    }
}

@Composable
internal fun ScanFeedbackCard(title: String, subtitle: String, positive: Boolean) {
    MerchantPrimaryCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (positive) VerevColors.Moss.copy(alpha = 0.14f) else Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (positive) VerevColors.Moss else Color(0xFFB91C1C),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (positive) VerevColors.Forest else Color(0xFF7F1D1D),
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.64f),
                )
            }
        }
    }
}

@Composable
private fun ScanMetric(label: String, value: String, modifier: Modifier = Modifier) {
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
