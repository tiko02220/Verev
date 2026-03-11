package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import com.vector.verevcodex.domain.model.Customer
import com.vector.verevcodex.domain.model.CustomerCredential
import com.vector.verevcodex.domain.model.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.CustomerCredentialStatus
import com.vector.verevcodex.domain.model.Transaction
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
internal fun CustomerProfileHero(customer: Customer) {
    CustomerBodySection {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.displayName(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.merchant_customer_member_since,
                        customer.enrolledDate.format(customerDateFormatter),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.62f),
                )
                Spacer(Modifier.height(6.dp))
                MerchantStatusPill(
                    text = customer.loyaltyTier.displayName(),
                    backgroundColor = VerevColors.Gold.copy(alpha = 0.16f),
                    contentColor = VerevColors.Gold,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CustomerProfileMetric(
                label = stringResource(R.string.merchant_metric_points),
                value = formatCompactCount(customer.currentPoints),
                modifier = Modifier.weight(1f),
            )
            CustomerProfileMetric(
                label = stringResource(R.string.merchant_metric_visits),
                value = formatCompactCount(customer.totalVisits),
                modifier = Modifier.weight(1f),
            )
            CustomerProfileMetric(
                label = stringResource(R.string.merchant_metric_spent),
                value = formatWholeCurrency(customer.totalSpent),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun CustomerProfileContactSection(customer: Customer) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_customer_contact_section))
        CustomerBodySection {
            CustomerProfileInfoRow(
                icon = Icons.Default.Email,
                title = stringResource(R.string.auth_email_label),
                value = customer.email,
            )
            CustomerProfileInfoRow(
                icon = Icons.Default.Phone,
                title = stringResource(R.string.auth_phone),
                value = customer.phoneNumber,
            )
            CustomerProfileInfoRow(
                icon = Icons.Default.LocationOn,
                title = stringResource(R.string.merchant_customer_favorite_store),
                value = customer.favoriteStoreId ?: stringResource(R.string.merchant_customer_unknown_store),
            )
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
            credentials.sortedBy { it.method.name }.forEachIndexed { index, credential ->
                CustomerProfileInfoRow(
                    icon = when (credential.method) {
                        CustomerCredentialMethod.BARCODE_IMAGE -> Icons.Default.CreditCard
                        CustomerCredentialMethod.GOOGLE_WALLET -> Icons.Default.Phone
                        CustomerCredentialMethod.NFC_CARD -> Icons.Default.CreditCard
                    },
                    title = stringResource(credential.method.labelRes()),
                    value = stringResource(credential.status.labelRes()),
                    subtitle = credential.referenceValue ?: formatRelativeDateTime(credential.updatedAt),
                )
                if (index < credentials.lastIndex) Spacer(Modifier.height(6.dp))
            }
            onManageCredentials?.let {
                Spacer(Modifier.height(6.dp))
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
internal fun CustomerProfileTransactionSection(transactions: List<Transaction>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_recent_transactions))
        CustomerBodySection {
            transactions.take(6).forEachIndexed { index, transaction ->
                CustomerProfileInfoRow(
                    icon = Icons.Default.CreditCard,
                    title = transaction.metadata.ifBlank { stringResource(R.string.merchant_transaction_item_fallback) },
                    value = formatWholeCurrency(transaction.amount),
                    subtitle = formatRelativeDateTime(transaction.timestamp),
                )
                if (index < minOf(transactions.lastIndex, 5)) Spacer(Modifier.height(6.dp))
            }
        }
    }
}

private fun CustomerCredentialMethod.labelRes(): Int = when (this) {
    CustomerCredentialMethod.BARCODE_IMAGE -> R.string.merchant_customer_credential_barcode
    CustomerCredentialMethod.GOOGLE_WALLET -> R.string.merchant_customer_credential_wallet
    CustomerCredentialMethod.NFC_CARD -> R.string.merchant_customer_credential_nfc
}

private fun CustomerCredentialStatus.labelRes(): Int = when (this) {
    CustomerCredentialStatus.AVAILABLE -> R.string.merchant_add_customer_method_available
    CustomerCredentialStatus.LINKED -> R.string.merchant_add_customer_method_linked
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
    subtitle: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(VerevColors.AppBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = VerevColors.Forest)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest.copy(alpha = 0.78f))
            subtitle?.let {
                Spacer(Modifier.height(2.dp))
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.52f))
            }
        }
    }
}
