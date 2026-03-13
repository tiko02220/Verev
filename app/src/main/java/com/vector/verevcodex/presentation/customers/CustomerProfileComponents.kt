package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.model.customer.CustomerCredential
import com.vector.verevcodex.domain.model.customer.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.presentation.merchant.common.MerchantSectionTitle
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatRelativeDateTime
import com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency
import com.vector.verevcodex.presentation.theme.VerevColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val customerDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val customerTransactionDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

@Composable
internal fun CustomerProfileHero(
    customer: Customer,
    relation: CustomerBusinessRelation?,
    scopedLastVisit: java.time.LocalDateTime?,
    showTierBadge: Boolean,
    onEditTags: (() -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = VerevColors.White, modifier = Modifier.size(30.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.weight(1f, fill = false),
                        text = customer.displayName(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = VerevColors.White,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (showTierBadge) {
                        MerchantStatusPill(
                            text = customer.loyaltyTier.displayName(),
                            backgroundColor = customer.loyaltyTier.toUiColors().background,
                            contentColor = customer.loyaltyTier.toUiColors().content,
                        )
                    }
                }
                Text(
                    text = buildString {
                        append(stringResource(R.string.merchant_customer_member_since, customer.enrolledDate.format(customerDateFormatter)))
                        scopedLastVisit?.let {
                            append(" • ")
                            append(stringResource(R.string.merchant_customer_last_visit, formatRelativeDateTime(it)))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.White.copy(alpha = 0.72f),
                )
                val tags = relation?.tags.orEmpty()
                if (tags.isNotEmpty() || onEditTags != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        tags.take(2).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color.White.copy(alpha = 0.18f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VerevColors.White.copy(alpha = 0.92f),
                                )
                            }
                        }
                        onEditTags?.let {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(VerevColors.Gold.copy(alpha = 0.9f))
                                    .clickable(onClick = it)
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.merchant_customer_add_tag),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VerevColors.White,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CustomerProfileTabRow(
    selectedTab: CustomerProfileTab,
    onTabSelected: (CustomerProfileTab) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(CustomerProfileTab.entries) { tab ->
            val isSelected = selectedTab == tab
            Column(
                modifier = Modifier
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(tab.labelRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) VerevColors.White else VerevColors.White.copy(alpha = 0.62f),
                    fontWeight = FontWeight.Medium,
                )
                Box(
                    modifier = Modifier
                        .size(width = 34.dp, height = 2.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isSelected) VerevColors.Gold else Color.Transparent),
                )
            }
        }
    }
}

@Composable
internal fun CustomerProfileBalanceSection(
    customer: Customer,
    scopedVisits: Int,
    scopedSpent: Double,
    progress: Float,
    nextTierThreshold: Int?,
    showTierProgress: Boolean,
    onOpenBonusManager: () -> Unit,
    onOpenTransactions: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Moss)))
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.merchant_customer_bonus_current_balance_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.White.copy(alpha = 0.82f),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = formatCompactCount(customer.currentPoints),
                        style = MaterialTheme.typography.headlineLarge,
                        color = VerevColors.White,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "pts",
                        style = MaterialTheme.typography.titleMedium,
                        color = VerevColors.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CustomerBalanceActionButton(
                    icon = Icons.Default.Add,
                    label = stringResource(R.string.merchant_customer_add_transaction),
                    onClick = onOpenTransactions,
                    modifier = Modifier.weight(1f),
                )
                CustomerBalanceActionButton(
                    icon = Icons.Default.Stars,
                    label = stringResource(R.string.merchant_customer_adjust_points),
                    onClick = onOpenBonusManager,
                    modifier = Modifier.weight(1f),
                )
            }
            if (showTierProgress) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.18f)),
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = stringResource(R.string.merchant_customer_tier_progress_title),
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.White.copy(alpha = 0.8f),
                        )
                        Text(
                            text = nextTierThreshold?.let { threshold ->
                                stringResource(
                                    R.string.merchant_customer_bonus_tier_progress_needed,
                                    (threshold - customer.currentPoints).coerceAtLeast(0),
                                )
                            } ?: stringResource(R.string.merchant_customer_bonus_tier_top_level),
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.White,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.22f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerBalanceActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VerevColors.White,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = VerevColors.White,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
internal fun CustomerProfileStatsGrid(
    scopedVisits: Int,
    scopedSpent: Double,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CustomerOverviewStatCard(
            icon = Icons.Default.ShoppingBag,
            iconTint = VerevColors.Moss,
            iconBackground = VerevColors.Moss.copy(alpha = 0.1f),
            label = stringResource(R.string.merchant_metric_visits),
            value = formatCompactCount(scopedVisits),
            supporting = stringResource(R.string.merchant_customer_overview_visits_supporting),
            modifier = Modifier.weight(1f),
        )
        CustomerOverviewStatCard(
            icon = Icons.Default.LocalOffer,
            iconTint = VerevColors.Gold,
            iconBackground = VerevColors.Gold.copy(alpha = 0.14f),
            label = stringResource(R.string.merchant_metric_spent),
            value = formatWholeCurrency(scopedSpent),
            supporting = stringResource(R.string.merchant_customer_overview_spent_supporting),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun CustomerProfileNotesPreviewSection(
    relation: CustomerBusinessRelation?,
    onEditNotes: (() -> Unit)? = null,
) {
    val notes = relation?.notes?.takeIf { it.isNotBlank() } ?: return
    CustomerBodySection {
        CustomerSectionHeader(
            title = stringResource(R.string.merchant_customer_notes_label),
            actionIcon = Icons.Default.Edit,
            onAction = onEditNotes,
        )
        Text(
            text = notes,
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest.copy(alpha = 0.74f),
        )
    }
}

@Composable
internal fun CustomerProfileActivityPreviewSection(
    activities: List<com.vector.verevcodex.domain.model.customer.CustomerActivity>,
    onOpenActivity: () -> Unit,
) {
    if (activities.isEmpty()) return
    CustomerBodySection {
        CustomerSectionHeader(
            title = stringResource(R.string.merchant_customer_activity_section),
            actionLabel = stringResource(R.string.merchant_view_details),
            onAction = onOpenActivity,
        )
        activities.take(3).forEachIndexed { index, activity ->
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
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = activity.icon(),
                        contentDescription = null,
                        tint = VerevColors.Forest,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = activity.subtitle(),
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.58f),
                    )
                }
            }
            if (index < minOf(activities.lastIndex, 2)) Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun CustomerOverviewStatCard(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    label: String,
    value: String,
    supporting: String,
    modifier: Modifier = Modifier,
) {
    CustomerBodySection(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.58f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = VerevColors.Forest,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = supporting,
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.5f),
        )
    }
}

@Composable
internal fun CustomerProfileContactSection(
    customer: Customer,
    activeStoreName: String?,
    activeStoreAddress: String?,
    onEditContact: () -> Unit,
) {
    CustomerBodySection(contentPadding = PaddingValues(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.merchant_customer_contact_information_section),
                style = MaterialTheme.typography.titleMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(VerevColors.Moss.copy(alpha = 0.1f))
                    .clickable(onClick = onEditContact),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = VerevColors.Moss,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(VerevColors.Forest.copy(alpha = 0.06f)))
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CustomerProfileInfoRow(
                Icons.Default.Email,
                stringResource(R.string.auth_email_label),
                customer.email.ifBlank { stringResource(R.string.merchant_customer_not_provided) },
                iconTint = VerevColors.Moss,
                iconBackground = VerevColors.Moss.copy(alpha = 0.12f),
                carded = false,
            )
            CustomerProfileInfoRow(
                Icons.Default.Phone,
                stringResource(R.string.auth_phone),
                customer.phoneNumber.ifBlank { stringResource(R.string.merchant_customer_not_provided) },
                iconTint = VerevColors.Gold,
                iconBackground = VerevColors.Gold.copy(alpha = 0.14f),
                carded = false,
            )
            CustomerProfileInfoRow(
                Icons.Default.LocationOn,
                stringResource(R.string.auth_address),
                activeStoreAddress ?: stringResource(R.string.merchant_customer_not_provided),
                activeStoreName,
                iconTint = VerevColors.Tan,
                iconBackground = VerevColors.Tan.copy(alpha = 0.16f),
                carded = false,
            )
        }
    }
}

@Composable
internal fun CustomerProfileCredentialSection(
    credentials: List<CustomerCredential>,
    onManageCredentials: (() -> Unit)?,
) {
    CustomerBodySection(contentPadding = PaddingValues(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.merchant_customer_credentials_section),
                style = MaterialTheme.typography.titleMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
            if (onManageCredentials != null) {
                Text(
                    text = stringResource(R.string.merchant_customer_credentials_manage_button),
                    style = MaterialTheme.typography.labelLarge,
                    color = VerevColors.Gold,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onManageCredentials),
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(VerevColors.Forest.copy(alpha = 0.06f)))
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            credentials.sortedBy { it.method.name }.forEach { credential ->
                val icon = credential.method.icon()
                val colors = credential.method.cardColors()
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
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(colors)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = VerevColors.White)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MerchantStatusPill(
                            text = stringResource(credential.status.labelRes()),
                            backgroundColor = VerevColors.Gold.copy(alpha = 0.12f),
                            contentColor = VerevColors.Gold,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = VerevColors.Forest.copy(alpha = 0.35f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
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
    val groupedTransactions = transactions
        .sortedByDescending { it.timestamp }
        .groupBy { it.timestamp.toLocalDate() }
        .toList()
        .sortedByDescending { it.first }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_recent_transactions))
        groupedTransactions.forEachIndexed { groupIndex, (date, dayTransactions) ->
            CustomerTransactionDateHeader(date = date)
            Spacer(Modifier.height(2.dp))
            dayTransactions.forEachIndexed { index, transaction ->
                val netPoints = transaction.pointsEarned - transaction.pointsRedeemed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .clickable { onOpenTransaction(transaction.id) }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(VerevColors.AppBackground),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = VerevColors.Forest)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = transaction.metadata.ifBlank { stringResource(R.string.merchant_transaction_item_fallback) },
                            style = MaterialTheme.typography.titleSmall,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = VerevColors.Forest.copy(alpha = 0.4f),
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = formatRelativeDateTime(transaction.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = VerevColors.Forest.copy(alpha = 0.58f),
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = formatWholeCurrency(transaction.amount),
                            style = MaterialTheme.typography.titleMedium,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (netPoints != 0) {
                            Text(
                                text = buildString {
                                    append(if (netPoints > 0) "+" else "")
                                    append(netPoints)
                                    append(" pts")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (netPoints > 0) VerevColors.Moss else VerevColors.Tan,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = VerevColors.Forest.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                if (index < dayTransactions.lastIndex) Spacer(Modifier.height(6.dp))
            }
            if (groupIndex < groupedTransactions.lastIndex) Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CustomerTransactionDateHeader(date: LocalDate) {
    Text(
        text = when (date) {
            LocalDate.now() -> stringResource(R.string.merchant_date_today)
            LocalDate.now().minusDays(1) -> stringResource(R.string.merchant_date_yesterday)
            else -> date.format(customerTransactionDateFormatter)
        },
        style = MaterialTheme.typography.titleSmall,
        color = VerevColors.Forest.copy(alpha = 0.72f),
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun CustomerProfileHeroMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .height(84.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = VerevColors.White.copy(alpha = 0.7f))
        Spacer(Modifier.height(6.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = VerevColors.White, fontWeight = FontWeight.Medium)
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
    subtitle: String? = null,
    iconTint: Color = VerevColors.Forest,
    iconBackground: Color = Color.White,
    carded: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (carded) VerevColors.AppBackground.copy(alpha = 0.7f) else Color.Transparent)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest.copy(alpha = 0.78f))
            subtitle?.let {
                Spacer(Modifier.height(2.dp))
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.56f))
            }
        }
    }
}

private fun CustomerCredentialMethod.cardColors(): List<Color> = when (this) {
    CustomerCredentialMethod.BARCODE_IMAGE -> listOf(VerevColors.Forest, VerevColors.Moss)
    CustomerCredentialMethod.GOOGLE_WALLET -> listOf(VerevColors.Gold, VerevColors.Tan)
    CustomerCredentialMethod.NFC_CARD -> listOf(VerevColors.ForestBright, VerevColors.Forest)
}

private fun CustomerCredentialMethod.icon(): ImageVector = when (this) {
    CustomerCredentialMethod.BARCODE_IMAGE -> Icons.Default.QrCode2
    CustomerCredentialMethod.GOOGLE_WALLET -> Icons.Default.Phone
    CustomerCredentialMethod.NFC_CARD -> Icons.Default.CreditCard
}

@Composable
internal fun CustomerSectionHeader(
    title: String,
    actionLabel: String? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MerchantSectionTitle(text = title)
        if (onAction != null && (actionLabel != null || actionIcon != null)) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(VerevColors.Forest.copy(alpha = 0.08f))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                actionIcon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = VerevColors.Forest,
                        modifier = Modifier.size(16.dp),
                    )
                }
                actionLabel?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelLarge,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
