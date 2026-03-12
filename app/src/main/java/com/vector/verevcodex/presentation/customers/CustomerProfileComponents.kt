package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.model.customer.CustomerCredential
import com.vector.verevcodex.domain.model.customer.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantSectionTitle
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatRelativeDateTime
import com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency
import com.vector.verevcodex.presentation.theme.VerevColors
import java.time.format.DateTimeFormatter

private val customerDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

@Composable
internal fun CustomerProfileHero(
    customer: Customer,
    relation: CustomerBusinessRelation?,
) {
    CustomerBodySection {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = VerevColors.White, modifier = Modifier.size(38.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = customer.displayName(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.merchant_customer_member_since, customer.enrolledDate.format(customerDateFormatter)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.66f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MerchantStatusPill(
                        text = customer.loyaltyTier.displayName(),
                        backgroundColor = customer.loyaltyTier.toUiColors().background,
                        contentColor = customer.loyaltyTier.toUiColors().content,
                    )
                    if (relation?.tags?.isNotEmpty() == true) {
                        MerchantStatusPill(
                            text = relation.tags.first(),
                            backgroundColor = VerevColors.Forest.copy(alpha = 0.08f),
                            contentColor = VerevColors.Forest,
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CustomerProfileMetric(stringResource(R.string.merchant_metric_points), formatCompactCount(customer.currentPoints), Modifier.weight(1f))
            CustomerProfileMetric(stringResource(R.string.merchant_metric_visits), formatCompactCount(customer.totalVisits), Modifier.weight(1f))
            CustomerProfileMetric(stringResource(R.string.merchant_metric_spent), formatWholeCurrency(customer.totalSpent), Modifier.weight(1f))
        }
    }
}

@Composable
internal fun CustomerProfileTabRow(
    selectedTab: CustomerProfileTab,
    onTabSelected: (CustomerProfileTab) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(CustomerProfileTab.entries) { tab ->
            MerchantFilterChip(
                text = stringResource(tab.labelRes),
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
            )
        }
    }
}

@Composable
internal fun CustomerProfileTierProgressSection(
    customer: Customer,
    transactions: List<Transaction>,
) {
    val progress = when (customer.loyaltyTier) {
        com.vector.verevcodex.domain.model.common.LoyaltyTier.BRONZE -> customer.currentPoints / 500f
        com.vector.verevcodex.domain.model.common.LoyaltyTier.SILVER -> customer.currentPoints / 1200f
        com.vector.verevcodex.domain.model.common.LoyaltyTier.GOLD -> customer.currentPoints / 2500f
        com.vector.verevcodex.domain.model.common.LoyaltyTier.VIP -> 1f
    }.coerceIn(0f, 1f)

    CustomerBodySection {
        MerchantSectionTitle(text = stringResource(R.string.merchant_customer_tier_progress_title))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(VerevColors.AppBackground)
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = stringResource(R.string.merchant_customer_tier_progress_current, customer.loyaltyTier.displayName()),
                        style = MaterialTheme.typography.titleMedium,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = VerevColors.Gold,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(VerevColors.Forest.copy(alpha = 0.08f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Brush.horizontalGradient(listOf(VerevColors.Gold, VerevColors.Tan))),
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomerProfileMetric(stringResource(R.string.merchant_customer_metric_recent_orders), formatCompactCount(transactions.take(30).size), Modifier.weight(1f))
                    CustomerProfileMetric(stringResource(R.string.merchant_customer_metric_avg_spend), formatWholeCurrency(transactions.map { it.amount }.average().takeIf { !it.isNaN() } ?: 0.0), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun CustomerProfileContactSection(
    customer: Customer,
    onEditContact: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_customer_contact_section))
        CustomerBodySection {
            CustomerProfileInfoRow(Icons.Default.Email, stringResource(R.string.auth_email_label), customer.email.ifBlank { stringResource(R.string.merchant_customer_not_provided) })
            CustomerProfileInfoRow(Icons.Default.Phone, stringResource(R.string.auth_phone), customer.phoneNumber.ifBlank { stringResource(R.string.merchant_customer_not_provided) })
            CustomerProfileInfoRow(Icons.Default.LocationOn, stringResource(R.string.merchant_customer_favorite_store), customer.favoriteStoreId ?: stringResource(R.string.merchant_customer_unknown_store))
            OutlinedButton(
                onClick = onEditContact,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(stringResource(R.string.merchant_customer_edit_contact))
            }
        }
    }
}

@Composable
internal fun CustomerProfileCredentialSection(
    credentials: List<CustomerCredential>,
    onManageCredentials: (() -> Unit)?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_customer_credentials_section))
        CustomerBodySection {
            credentials.sortedBy { it.method.name }.forEach { credential ->
                val icon = when (credential.method) {
                    CustomerCredentialMethod.BARCODE_IMAGE -> Icons.Default.QrCode2
                    CustomerCredentialMethod.GOOGLE_WALLET -> Icons.Default.Phone
                    CustomerCredentialMethod.NFC_CARD -> Icons.Default.CreditCard
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(VerevColors.AppBackground)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(VerevColors.Forest, VerevColors.Moss))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = VerevColors.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(credential.method.labelRes()),
                            style = MaterialTheme.typography.titleSmall,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = credential.referenceValue ?: formatRelativeDateTime(credential.updatedAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.6f),
                        )
                    }
                    MerchantStatusPill(
                        text = stringResource(credential.status.labelRes()),
                        backgroundColor = VerevColors.Gold.copy(alpha = 0.12f),
                        contentColor = VerevColors.Gold,
                    )
                }
            }
            onManageCredentials?.let {
                OutlinedButton(
                    onClick = it,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(stringResource(R.string.merchant_customer_credentials_manage_button))
                }
            }
        }
    }
}

@Composable
internal fun CustomerProfileTransactionSection(
    transactions: List<Transaction>,
    onOpenTransaction: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_recent_transactions))
        CustomerBodySection {
            transactions.take(6).forEachIndexed { index, transaction ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(VerevColors.AppBackground.copy(alpha = 0.72f))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(VerevColors.Forest.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = VerevColors.Forest)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = transaction.metadata.ifBlank { stringResource(R.string.merchant_transaction_item_fallback) },
                            style = MaterialTheme.typography.titleSmall,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = formatRelativeDateTime(transaction.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.58f),
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatWholeCurrency(transaction.amount), style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
                        OutlinedButton(onClick = { onOpenTransaction(transaction.id) }, shape = RoundedCornerShape(14.dp)) {
                            Text(stringResource(R.string.merchant_customer_transaction_open))
                        }
                    }
                }
                if (index < minOf(transactions.lastIndex, 5)) Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun CustomerProfileMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(VerevColors.AppBackground)
            .height(86.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.5f))
        Spacer(Modifier.height(6.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CustomerProfileInfoRow(
    icon: ImageVector,
    title: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(VerevColors.AppBackground.copy(alpha = 0.7f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = VerevColors.Forest)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest.copy(alpha = 0.78f))
        }
    }
}
