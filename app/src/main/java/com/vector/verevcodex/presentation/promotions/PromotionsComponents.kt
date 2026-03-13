@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.vector.verevcodex.presentation.promotions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.PromotionType
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.merchant.common.MerchantSectionTitle
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.MerchantTextAction
import com.vector.verevcodex.presentation.theme.VerevColors

data class PromotionSummaryStats(
    val activeCount: Int,
    val totalRevenue: Double,
    val customersReached: Int,
    val averageRedemption: Float,
)

@Composable
internal fun PromotionsHeader(
    storeName: String,
    stats: PromotionSummaryStats,
    onBack: () -> Unit,
    onAddPromotion: () -> Unit,
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
                    .background(Color.White.copy(alpha = 0.16f), CircleShape)
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
                    text = stringResource(R.string.merchant_promotions_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.merchant_campaigns_store_subtitle, storeName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f),
                )
            }
        }
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White.copy(alpha = 0.12f)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.merchant_promotions_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.78f),
                        )
                    }
                    OutlinedButton(
                        onClick = onAddPromotion,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
                    ) {
                        Text(stringResource(R.string.merchant_promotion_add_action), color = Color.White)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PromotionHeroMetric(stringResource(R.string.merchant_metric_active), stats.activeCount.toString(), Modifier.weight(1f))
                    PromotionHeroMetric(stringResource(R.string.merchant_promotions_metric_payment), formatPromotionRevenue(stats.totalRevenue), Modifier.weight(1f))
                    PromotionHeroMetric(stringResource(R.string.merchant_promotions_metric_value), stats.customersReached.toString(), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PromotionHeroMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.12f),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.74f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun formatPromotionRevenue(value: Double): String =
    com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency(value)

@Composable
internal fun PromotionsFilterRow(selectedFilter: PromotionFilter, onSelected: (PromotionFilter) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(PromotionFilter.entries) { filter ->
            MerchantFilterChip(
                text = stringResource(filter.labelRes),
                selected = selectedFilter == filter,
                onClick = { onSelected(filter) },
            )
        }
    }
}

@Composable
internal fun PromotionListCard(
    promotion: Campaign,
    isBusy: Boolean,
    onOpen: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    MerchantPrimaryCard(modifier = Modifier.clickable(onClick = onOpen), contentPadding = PaddingValues(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Brush.linearGradient(listOf(VerevColors.Gold.copy(alpha = 0.18f), VerevColors.Tan.copy(alpha = 0.22f))), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = VerevColors.Forest)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(promotion.name, style = MaterialTheme.typography.titleLarge, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
                    MerchantStatusPill(
                        text = if (promotion.active) stringResource(R.string.merchant_metric_active) else stringResource(R.string.merchant_metric_inactive),
                        backgroundColor = if (promotion.active) VerevColors.Moss.copy(alpha = 0.14f) else VerevColors.ErrorContainer,
                        contentColor = if (promotion.active) VerevColors.Forest else VerevColors.ErrorText,
                    )
                }
                Text(promotion.description, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest.copy(alpha = 0.68f))
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
                    if (promotion.paymentFlowEnabled) {
                        MerchantStatusPill(
                            text = stringResource(R.string.merchant_promotions_filter_payment),
                            backgroundColor = VerevColors.Moss.copy(alpha = 0.14f),
                            contentColor = VerevColors.Forest,
                        )
                    }
                }
            }
        }
        Surface(shape = RoundedCornerShape(18.dp), color = VerevColors.AppBackground) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.merchant_metric_active), style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.72f))
                Switch(checked = promotion.active, onCheckedChange = onToggle, enabled = !isBusy)
            }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    MerchantTextAction(text = stringResource(R.string.merchant_edit), onClick = onEdit)
                    MerchantTextAction(text = stringResource(R.string.merchant_delete), onClick = onDelete)
                }
            }
        }
    }
}

@Composable
internal fun PromotionDetailCard(
    promotion: Campaign,
    onOpenPayment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    MerchantPrimaryCard(contentPadding = PaddingValues(20.dp)) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = VerevColors.Forest,
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(promotion.name, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(promotion.description, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                }
                Box(
                    modifier = Modifier.size(48.dp).background(VerevColors.Gold.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White)
                }
            }
        }
        PromotionMetricRow(stringResource(R.string.merchant_promotion_type_title), stringResource(promotion.promotionType.displayLabelRes()))
        PromotionMetricRow(stringResource(R.string.merchant_promotion_value_title), promotion.promotionValueText())
        PromotionMetricRow(stringResource(R.string.merchant_campaign_target), promotion.target.description)
        PromotionMetricRow(stringResource(R.string.merchant_campaign_start_date), promotion.startDate.toString())
        PromotionMetricRow(stringResource(R.string.merchant_campaign_end_date), promotion.endDate.toString())
        PromotionMetricRow(stringResource(R.string.merchant_promotion_code_title), promotion.promoCode ?: stringResource(R.string.merchant_promotion_code_none))
        PromotionMetricRow(
            stringResource(R.string.merchant_promotion_payment_flow_title),
            if (promotion.paymentFlowEnabled) stringResource(R.string.merchant_promotion_payment_flow_enabled) else stringResource(R.string.merchant_promotion_payment_flow_disabled),
        )
        if (promotion.paymentFlowEnabled) {
            OutlinedButton(onClick = onOpenPayment, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Icon(Icons.Default.Payments, contentDescription = null)
                Text(
                    text = stringResource(R.string.merchant_network_promotion_payment_title),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f), shape = RoundedCornerShape(18.dp)) {
                Text(stringResource(R.string.merchant_edit))
            }
            OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f), shape = RoundedCornerShape(18.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null)
                Text(stringResource(R.string.merchant_delete))
            }
        }
    }
}

@Composable
internal fun NetworkPromotionPaymentDialog(
    promotion: Campaign,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var paymentMethod by remember { mutableStateOf("card") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var errorRes by remember { mutableStateOf<Int?>(null) }
    val breakdown = promotion.toNetworkPromotionBreakdown()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    errorRes = when {
                        paymentMethod == "card" && cardNumber.length < 16 -> R.string.merchant_network_promotion_payment_error_card_number
                        paymentMethod == "card" && expiryDate.length < 5 -> R.string.merchant_network_promotion_payment_error_expiry
                        paymentMethod == "card" && cvv.length < 3 -> R.string.merchant_network_promotion_payment_error_cvv
                        else -> null
                    }
                    if (errorRes == null) onConfirm()
                },
            ) {
                Text(stringResource(R.string.merchant_network_promotion_payment_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.auth_cancel))
            }
        },
        title = {
            Text(
                text = stringResource(R.string.merchant_network_promotion_payment_title),
                style = MaterialTheme.typography.titleLarge,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PromotionMetricRow(
                    label = stringResource(R.string.merchant_network_promotion_payment_campaign),
                    value = promotion.name,
                )
                PromotionMetricRow(
                    label = stringResource(R.string.merchant_network_promotion_payment_period),
                    value = "${promotion.startDate} - ${promotion.endDate}",
                )
                PromotionMetricRow(
                    label = stringResource(R.string.merchant_network_promotion_payment_reach),
                    value = stringResource(R.string.merchant_network_promotion_payment_reach_value, breakdown.estimatedReach),
                )
                PromotionMetricRow(
                    label = stringResource(R.string.merchant_network_promotion_payment_total),
                    value = breakdown.totalLabel,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            paymentMethod = "card"
                            errorRes = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null)
                        Text(stringResource(R.string.merchant_network_promotion_payment_method_card), modifier = Modifier.padding(start = 6.dp))
                    }
                    OutlinedButton(
                        onClick = {
                            paymentMethod = "wallet"
                            errorRes = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Icon(Icons.Default.Stars, contentDescription = null)
                        Text(stringResource(R.string.merchant_network_promotion_payment_method_wallet), modifier = Modifier.padding(start = 6.dp))
                    }
                }
                if (paymentMethod == "card") {
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = {
                            errorRes = null
                            cardNumber = it.filter(Char::isDigit).take(16)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.merchant_network_promotion_payment_card_number)) },
                        singleLine = true,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = {
                                errorRes = null
                                expiryDate = it.take(5)
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.merchant_network_promotion_payment_expiry)) },
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = {
                                errorRes = null
                                cvv = it.filter(Char::isDigit).take(4)
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.merchant_network_promotion_payment_cvv)) },
                            singleLine = true,
                        )
                    }
                } else {
                    PromotionMetricRow(
                        label = stringResource(R.string.merchant_network_promotion_payment_wallet_balance),
                        value = stringResource(R.string.merchant_network_promotion_payment_wallet_balance_value),
                    )
                }
                errorRes?.let {
                    Text(
                        text = stringResource(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.ErrorText,
                    )
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
    )
}

@Composable
private fun PromotionMetricRow(label: String, value: String) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = VerevColors.AppBackground),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.66f))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
internal fun PromotionEditorDialog(
    editorState: PromotionEditorState,
    fieldErrors: Map<String, String>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onTypeChange: (PromotionType) -> Unit,
    onValueChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onPaymentFlowEnabledChange: (Boolean) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onTargetSegmentChange: (Int) -> Unit,
    onTargetDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (editorState.promotionId == null) stringResource(R.string.merchant_promotion_create_title) else stringResource(R.string.merchant_promotion_edit_title),
                color = VerevColors.Forest,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MerchantFormField(editorState.name, onNameChange, stringResource(R.string.merchant_name), leadingIcon = Icons.Default.Campaign, errorText = fieldErrors[PROMOTION_FIELD_NAME])
                MerchantFormField(editorState.description, onDescriptionChange, stringResource(R.string.merchant_description), leadingIcon = Icons.Default.LocalOffer)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MerchantFormField(
                        value = editorState.startDate,
                        onValueChange = onStartDateChange,
                        label = stringResource(R.string.merchant_campaign_start_date),
                        leadingIcon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f),
                        errorText = fieldErrors[PROMOTION_FIELD_START],
                    )
                    MerchantFormField(
                        value = editorState.endDate,
                        onValueChange = onEndDateChange,
                        label = stringResource(R.string.merchant_campaign_end_date),
                        leadingIcon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f),
                        errorText = fieldErrors[PROMOTION_FIELD_END],
                    )
                }
                Text(stringResource(R.string.merchant_promotion_type_title), style = MaterialTheme.typography.titleSmall, color = VerevColors.Forest)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PromotionType.entries.forEach { type ->
                        MerchantFilterChip(
                            text = stringResource(type.displayLabelRes()),
                            selected = editorState.promotionType == type,
                            onClick = { onTypeChange(type) },
                        )
                    }
                }
                MerchantFormField(
                    value = editorState.promotionValue,
                    onValueChange = onValueChange,
                    label = stringResource(R.string.merchant_promotion_value_title),
                    leadingIcon = Icons.Default.Percent,
                    errorText = fieldErrors[PROMOTION_FIELD_VALUE],
                )
                MerchantFormField(
                    value = editorState.promoCode,
                    onValueChange = onCodeChange,
                    label = stringResource(R.string.merchant_promotion_code_title),
                    leadingIcon = Icons.Default.LocalOffer,
                )
                Text(stringResource(R.string.merchant_campaign_target), style = MaterialTheme.typography.titleSmall, color = VerevColors.Forest)
                val segmentLabels = stringArrayResource(R.array.merchant_campaign_segment_entries)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    segmentLabels.forEachIndexed { index, label ->
                        MerchantFilterChip(
                            text = label,
                            selected = editorState.targetSegment.ordinal == index,
                            onClick = { onTargetSegmentChange(index) },
                        )
                    }
                }
                MerchantFormField(
                    value = editorState.targetDescription,
                    onValueChange = onTargetDescriptionChange,
                    label = stringResource(R.string.merchant_campaign_target),
                    leadingIcon = Icons.Default.Campaign,
                    errorText = fieldErrors[PROMOTION_FIELD_TARGET],
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.merchant_promotion_payment_flow_title), color = VerevColors.Forest)
                    Switch(checked = editorState.paymentFlowEnabled, onCheckedChange = onPaymentFlowEnabledChange)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.merchant_metric_active), color = VerevColors.Forest)
                    Switch(checked = editorState.active, onCheckedChange = onActiveChange)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = !isSubmitting) {
                Text(stringResource(R.string.merchant_save_changes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text(stringResource(R.string.auth_cancel))
            }
        },
    )
}

@Composable
internal fun PromotionDeleteDialog(
    promotionName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.merchant_promotion_delete_title), color = VerevColors.Forest) },
        text = { Text(stringResource(R.string.merchant_promotion_delete_message, promotionName), color = VerevColors.Forest.copy(alpha = 0.72f)) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSubmitting) { Text(stringResource(R.string.merchant_delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text(stringResource(R.string.auth_cancel)) }
        },
    )
}

@Composable
internal fun PromotionTemplateRow(onCreate: (PromotionType) -> Unit) {
    MerchantPrimaryCard(contentPadding = PaddingValues(18.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_promotion_templates_title))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(PromotionType.entries) { type ->
                ElevatedCard(
                    modifier = Modifier.clickable { onCreate(type) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(38.dp).background(VerevColors.Gold.copy(alpha = 0.16f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = when (type) {
                                    PromotionType.POINTS_MULTIPLIER -> Icons.Default.Stars
                                    PromotionType.PERCENT_DISCOUNT -> Icons.Default.Percent
                                    PromotionType.FIXED_DISCOUNT -> Icons.Default.Payments
                                    PromotionType.BONUS_POINTS -> Icons.Default.LocalOffer
                                },
                                contentDescription = null,
                                tint = VerevColors.Forest,
                            )
                        }
                        Text(stringResource(type.displayLabelRes()), color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
internal fun PromotionsEmptyState() {
    MerchantEmptyStateCard(
        title = stringResource(R.string.merchant_promotions_empty_title),
        subtitle = stringResource(R.string.merchant_promotions_empty_subtitle),
        icon = Icons.Default.Campaign,
    )
}
