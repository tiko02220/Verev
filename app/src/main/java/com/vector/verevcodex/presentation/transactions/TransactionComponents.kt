@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.vector.verevcodex.presentation.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.presentation.customers.displayName
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.MerchantTextAction
import com.vector.verevcodex.presentation.merchant.common.displayName as roleDisplayName
import com.vector.verevcodex.presentation.merchant.common.formatRelativeDateTime
import com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency
import com.vector.verevcodex.presentation.promotions.displayLabelRes
import com.vector.verevcodex.presentation.promotions.promotionValueText
import com.vector.verevcodex.presentation.theme.VerevColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TransactionHeaderSection(
    title: String,
    subtitle: String,
    storeName: String,
    cashierName: String,
    onBack: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.auth_back),
                    tint = Color.White,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CheckoutContextPill(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.PointOfSale,
                title = stringResource(R.string.merchant_transaction_store_label),
                value = storeName,
                containerColor = Color.White.copy(alpha = 0.12f),
                iconTint = Color.White,
                titleColor = Color.White.copy(alpha = 0.72f),
                valueColor = Color.White,
            )
            CheckoutContextPill(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Badge,
                title = stringResource(R.string.merchant_transaction_cashier_label),
                value = cashierName,
                containerColor = Color.White.copy(alpha = 0.12f),
                iconTint = Color.White,
                titleColor = Color.White.copy(alpha = 0.72f),
                valueColor = Color.White,
            )
        }
    }
}

@Composable
internal fun CheckoutContextCard(
    storeName: String,
    cashierName: String,
) {
    TransactionSurfaceCard {
        TransactionSectionTitle(text = stringResource(R.string.merchant_transaction_context_title))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CheckoutContextPill(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.PointOfSale,
                title = stringResource(R.string.merchant_transaction_store_label),
                value = storeName,
            )
            CheckoutContextPill(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Badge,
                title = stringResource(R.string.merchant_transaction_cashier_label),
                value = cashierName,
            )
        }
    }
}

@Composable
private fun CheckoutContextPill(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    containerColor: Color = VerevColors.AppBackground,
    iconTint: Color = VerevColors.Forest,
    titleColor: Color = VerevColors.Forest.copy(alpha = 0.62f),
    valueColor: Color = VerevColors.Forest,
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.padding(0.dp))
                Text(title, style = MaterialTheme.typography.bodySmall, color = titleColor)
            }
            Text(
                text = value.ifBlank { stringResource(R.string.merchant_transaction_context_pending) },
                style = MaterialTheme.typography.titleMedium,
                color = valueColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
internal fun CustomerSelectionCard(
    query: String,
    selectedCustomer: Customer?,
    customers: List<Customer>,
    customerError: String?,
    onQueryChanged: (String) -> Unit,
    onCustomerSelected: (String) -> Unit,
    onClearSelectedCustomer: () -> Unit,
) {
    TransactionSurfaceCard {
        TransactionSectionTitle(text = stringResource(R.string.merchant_transaction_customer_title))
        MerchantFormField(
            value = query,
            onValueChange = onQueryChanged,
            label = stringResource(R.string.merchant_transaction_customer_search),
            leadingIcon = Icons.Default.Search,
            errorText = customerError,
        )
        selectedCustomer?.let { customer ->
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = VerevColors.Moss.copy(alpha = 0.14f)),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = customer.displayName(),
                            style = MaterialTheme.typography.titleMedium,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        MerchantTextAction(
                            text = stringResource(R.string.merchant_transaction_change_customer),
                            onClick = onClearSelectedCustomer,
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MerchantStatusPill(
                            text = stringResource(R.string.merchant_transaction_points_available, customer.currentPoints),
                            backgroundColor = VerevColors.Gold.copy(alpha = 0.16f),
                            contentColor = VerevColors.Forest,
                        )
                        MerchantStatusPill(
                            text = customer.loyaltyId,
                            backgroundColor = VerevColors.Forest.copy(alpha = 0.1f),
                            contentColor = VerevColors.Forest,
                        )
                    }
                }
            }
        }
        if (customers.isEmpty()) {
            MerchantEmptyStateCard(
                title = stringResource(R.string.merchant_transaction_customer_empty_title),
                subtitle = stringResource(R.string.merchant_transaction_customer_empty_subtitle),
                icon = Icons.Default.Person,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                customers.forEach { customer ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCustomerSelected(customer.id) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = customer.displayName(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = VerevColors.Forest,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text = customer.phoneNumber.ifBlank { customer.email.ifBlank { customer.loyaltyId } },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = VerevColors.Forest.copy(alpha = 0.62f),
                                )
                            }
                            Text(
                                text = stringResource(R.string.merchant_transaction_points_short, customer.currentPoints),
                                style = MaterialTheme.typography.bodyMedium,
                                color = VerevColors.Forest,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CheckoutLineItemsCard(
    lineItems: List<CheckoutLineItemDraft>,
    fieldErrors: Map<String, String>,
    onAddLineItem: () -> Unit,
    onRemoveLineItem: (String) -> Unit,
    onNameChanged: (String, String) -> Unit,
    onQuantityChanged: (String, String) -> Unit,
    onPriceChanged: (String, String) -> Unit,
) {
    TransactionSurfaceCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TransactionSectionTitle(text = stringResource(R.string.merchant_transaction_items_title))
            OutlinedButton(
                onClick = onAddLineItem,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.merchant_transaction_add_item))
            }
        }
        lineItems.forEachIndexed { index, lineItem ->
            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = VerevColors.AppBackground),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.merchant_transaction_item_label, index + 1),
                            style = MaterialTheme.typography.titleMedium,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (lineItems.size > 1) {
                            MerchantTextAction(
                                text = stringResource(R.string.merchant_transaction_remove_item),
                                onClick = { onRemoveLineItem(lineItem.id) },
                            )
                        }
                    }
                    MerchantFormField(
                        value = lineItem.name,
                        onValueChange = { onNameChanged(lineItem.id, it) },
                        label = stringResource(R.string.merchant_transaction_item_name),
                        leadingIcon = Icons.Default.Inventory2,
                        errorText = fieldErrors[transactionItemNameFieldKey(lineItem.id)],
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MerchantFormField(
                            value = lineItem.quantity,
                            onValueChange = { onQuantityChanged(lineItem.id, it) },
                            label = stringResource(R.string.merchant_transaction_item_quantity),
                            leadingIcon = Icons.Default.Sell,
                            modifier = Modifier.weight(1f),
                            errorText = fieldErrors[transactionItemQuantityFieldKey(lineItem.id)],
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        )
                        MerchantFormField(
                            value = lineItem.unitPrice,
                            onValueChange = { onPriceChanged(lineItem.id, it) },
                            label = stringResource(R.string.merchant_transaction_item_price),
                            leadingIcon = Icons.Default.CreditCard,
                            modifier = Modifier.weight(1f),
                            errorText = fieldErrors[transactionItemPriceFieldKey(lineItem.id)],
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun CheckoutPromotionCard(
    promotions: List<Campaign>,
    selectedPromotionId: String?,
    onPromotionSelected: (String?) -> Unit,
) {
    TransactionSurfaceCard {
        TransactionSectionTitle(text = stringResource(R.string.merchant_transaction_promotion_title))
        if (promotions.isEmpty()) {
            MerchantEmptyStateCard(
                title = stringResource(R.string.merchant_transaction_promotion_empty_title),
                subtitle = stringResource(R.string.merchant_transaction_promotion_empty_subtitle),
                icon = Icons.Default.LocalOffer,
            )
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MerchantFilterChip(
                    text = stringResource(R.string.merchant_transaction_promotion_none),
                    selected = selectedPromotionId == null,
                    onClick = { onPromotionSelected(null) },
                )
                promotions.forEach { promotion ->
                    MerchantFilterChip(
                        text = promotion.name,
                        selected = selectedPromotionId == promotion.id,
                        onClick = { onPromotionSelected(promotion.id) },
                    )
                }
            }
            promotions.firstOrNull { it.id == selectedPromotionId }?.let { promotion ->
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = VerevColors.AppBackground),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = promotion.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = promotion.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.68f),
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            MerchantStatusPill(
                                text = stringResource(promotion.promotionType.displayLabelRes()),
                                backgroundColor = VerevColors.Forest.copy(alpha = 0.1f),
                                contentColor = VerevColors.Forest,
                            )
                            MerchantStatusPill(
                                text = promotion.promotionValueText(),
                                backgroundColor = VerevColors.Gold.copy(alpha = 0.15f),
                                contentColor = VerevColors.Forest,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CheckoutLoyaltyCard(
    selectedCustomer: Customer?,
    applyRedemption: Boolean,
    redeemPointsInput: String,
    totals: CheckoutTotals,
    redeemError: String?,
    onToggleRedemption: (Boolean) -> Unit,
    onRedeemPointsChanged: (String) -> Unit,
) {
    TransactionSurfaceCard {
        TransactionSectionTitle(text = stringResource(R.string.merchant_transaction_loyalty_title))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.merchant_transaction_redeem_points_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(
                        R.string.merchant_transaction_redeem_points_subtitle,
                        selectedCustomer?.currentPoints ?: 0,
                        totals.redeemablePoints,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.62f),
                )
            }
            Switch(
                checked = applyRedemption,
                onCheckedChange = onToggleRedemption,
                enabled = selectedCustomer != null,
            )
        }
        if (applyRedemption) {
            MerchantFormField(
                value = redeemPointsInput,
                onValueChange = onRedeemPointsChanged,
                label = stringResource(R.string.merchant_transaction_redeem_points_field),
                leadingIcon = Icons.Default.Loyalty,
                errorText = redeemError,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            )
        }
    }
}

@Composable
internal fun CheckoutSummaryCard(
    note: String,
    onNoteChanged: (String) -> Unit,
    totals: CheckoutTotals,
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
) {
    TransactionSurfaceCard {
        TransactionSectionTitle(text = stringResource(R.string.merchant_transaction_summary_title))
        MerchantFormField(
            value = note,
            onValueChange = onNoteChanged,
            label = stringResource(R.string.merchant_transaction_note),
            leadingIcon = Icons.AutoMirrored.Filled.ReceiptLong,
            singleLine = false,
            supportingText = stringResource(R.string.merchant_transaction_note_hint),
        )
        ElevatedCard(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = VerevColors.AppBackground),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SummaryRow(
                    label = stringResource(R.string.merchant_transaction_subtotal),
                    value = formatWholeCurrency(totals.subtotal),
                )
                if (totals.promotionDiscount > 0.0) {
                    SummaryRow(
                        label = stringResource(R.string.merchant_transaction_promotion_discount),
                        value = "-${formatWholeCurrency(totals.promotionDiscount)}",
                    )
                }
                SummaryRow(
                    label = stringResource(R.string.merchant_transaction_redeemed_points),
                    value = totals.redeemedPoints.toString(),
                )
                SummaryRow(
                    label = stringResource(R.string.merchant_transaction_points_earned),
                    value = totals.pointsEarned.toString(),
                )
                if (totals.promotionBonusPoints > 0) {
                    SummaryRow(
                        label = stringResource(R.string.merchant_transaction_promotion_bonus_points),
                        value = "+${totals.promotionBonusPoints}",
                    )
                }
                SummaryRow(
                    label = stringResource(R.string.merchant_transaction_final_total),
                    value = formatWholeCurrency(totals.finalAmount),
                    emphasize = true,
                )
            }
        }
        Button(
            onClick = onSubmit,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Forest, contentColor = Color.White),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            Text(
                text = if (isSubmitting) stringResource(R.string.merchant_transaction_submitting) else stringResource(R.string.merchant_transaction_submit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    emphasize: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            color = VerevColors.Forest.copy(alpha = if (emphasize) 1f else 0.72f),
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal,
        )
        Text(
            text = value,
            style = if (emphasize) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            color = VerevColors.Forest,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
internal fun CheckoutFeedbackCard(
    text: String,
    positive: Boolean,
) {
    TransactionSurfaceCard(contentPadding = PaddingValues(16.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (positive) VerevColors.Forest else Color(0xFFB42318),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun RecentTransactionsCard(records: List<RecentCheckoutRecord>) {
    TransactionSurfaceCard {
        TransactionSectionTitle(text = stringResource(R.string.merchant_transaction_recent_title))
        if (records.isEmpty()) {
            MerchantEmptyStateCard(
                title = stringResource(R.string.merchant_transaction_recent_empty_title),
                subtitle = stringResource(R.string.merchant_transaction_recent_empty_subtitle),
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                records.forEach { record ->
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = VerevColors.AppBackground),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = record.customerName.ifBlank { stringResource(R.string.merchant_transaction_unknown_customer) },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = VerevColors.Forest,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = record.summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = VerevColors.Forest.copy(alpha = 0.66f),
                                    )
                                }
                                Text(
                                    text = formatWholeCurrency(record.amount),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = VerevColors.Forest,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    MerchantStatusPill(
                                        text = stringResource(R.string.merchant_transaction_items_short, record.itemCount),
                                        backgroundColor = VerevColors.Forest.copy(alpha = 0.1f),
                                        contentColor = VerevColors.Forest,
                                    )
                                    MerchantStatusPill(
                                        text = stringResource(R.string.merchant_transaction_points_delta_short, record.pointsEarned, record.pointsRedeemed),
                                        backgroundColor = VerevColors.Gold.copy(alpha = 0.16f),
                                        contentColor = VerevColors.Forest,
                                    )
                                }
                                Text(
                                    text = formatRelativeDateTime(record.timestamp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VerevColors.Forest.copy(alpha = 0.56f),
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
private fun TransactionSurfaceCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

@Composable
private fun TransactionSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = VerevColors.Forest,
        fontWeight = FontWeight.SemiBold,
    )
}
