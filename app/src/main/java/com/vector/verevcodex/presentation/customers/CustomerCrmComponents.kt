package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerBonusAction
import com.vector.verevcodex.domain.model.customer.CustomerBonusActionType
import com.vector.verevcodex.domain.model.customer.CustomerActivity
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.model.loyalty.PointsLedger
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.PromotionType
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantSectionTitle
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.merchant.common.formatRelativeDateTime
import com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun CustomerCrmSection(
    relation: CustomerBusinessRelation?,
    onEditCrm: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_customer_crm_section))
        CustomerBodySection {
            CustomerCrmRow(
                icon = Icons.Default.NoteAlt,
                title = stringResource(R.string.merchant_customer_notes_label),
                value = relation?.notes?.ifBlank { stringResource(R.string.merchant_customer_notes_empty) }
                    ?: stringResource(R.string.merchant_customer_notes_empty),
            )
            relation?.tags?.takeIf { it.isNotEmpty() }?.let { tags ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.merchant_customer_tags_label),
                        style = MaterialTheme.typography.titleSmall,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        tags.forEach { tag ->
                            MerchantStatusPill(
                                text = tag,
                                backgroundColor = VerevColors.Forest.copy(alpha = 0.08f),
                                contentColor = VerevColors.Forest,
                            )
                        }
                    }
                }
            } ?: CustomerCrmRow(
                icon = Icons.Default.LocalOffer,
                title = stringResource(R.string.merchant_customer_tags_label),
                value = stringResource(R.string.merchant_customer_tags_empty),
            )
            OutlinedButton(
                onClick = onEditCrm,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.merchant_customer_edit_crm))
            }
        }
    }
}

@Composable
internal fun CustomerManualPointsSection(
    ledgerEntries: List<PointsLedger>,
    onAdjustPoints: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_customer_points_section))
        CustomerBodySection {
            Button(
                onClick = onAdjustPoints,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerevColors.Gold,
                    contentColor = VerevColors.White,
                ),
            ) {
                Icon(Icons.Default.Stars, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.merchant_customer_adjust_points))
            }
            ledgerEntries.take(4).forEach { entry ->
                CustomerCrmRow(
                    icon = if (entry.delta >= 0) Icons.Default.Stars else Icons.Default.RemoveCircle,
                    title = entry.reason,
                    value = buildString {
                        append(if (entry.delta >= 0) "+" else "")
                        append(entry.delta)
                        append(" pts")
                    },
                    subtitle = formatRelativeDateTime(entry.createdAt),
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun CustomerBonusManagementSection(
    customer: Customer,
    relation: CustomerBusinessRelation?,
    ledgerEntries: List<PointsLedger>,
    bonusActions: List<CustomerBonusAction>,
    rewards: List<Reward>,
    programs: List<RewardProgram>,
    campaigns: List<Campaign>,
    isSaving: Boolean,
    onAdjustPoints: () -> Unit,
    onRedeemReward: (String) -> Unit,
    onRedeemCoupon: (String) -> Unit,
    onMarkDiscountApplied: (String) -> Unit,
    onRecordTierBenefit: () -> Unit,
) {
    var selectedSection by rememberSaveable { mutableStateOf(CustomerBonusSection.POINTS) }
    var rewardToRedeemId by rememberSaveable { mutableStateOf<String?>(null) }
    var couponProgramToRedeemId by rememberSaveable { mutableStateOf<String?>(null) }
    var discountCampaignToApplyId by rememberSaveable { mutableStateOf<String?>(null) }
    val pointsProgram = programs.firstOrNull { it.active && it.configuration.earningEnabled }
    val tierProgram = programs.firstOrNull { it.active && it.configuration.tierTrackingEnabled }
    val couponProgram = programs.firstOrNull { it.active && it.configuration.couponEnabled }
    val discountCampaigns = campaigns.filter {
        it.active && (it.promotionType == PromotionType.PERCENT_DISCOUNT || it.promotionType == PromotionType.FIXED_DISCOUNT)
    }
    val promoCodeCampaigns = campaigns.filter { it.active && !it.promoCode.isNullOrBlank() }
    val redeemableRewards = rewards.filter { customer.currentPoints >= it.pointsRequired }
    val rewardToRedeem = rewards.firstOrNull { it.id == rewardToRedeemId }
    val couponProgramToRedeem = programs.firstOrNull { it.id == couponProgramToRedeemId && it.configuration.couponEnabled }
    val discountCampaignToApply = campaigns.firstOrNull { it.id == discountCampaignToApplyId }
    val discountActions = bonusActions.filter { it.type == CustomerBonusActionType.DISCOUNT_APPLIED }.take(3)
    val tierActions = bonusActions.filter { it.type == CustomerBonusActionType.TIER_BENEFIT_RECORDED }.take(3)
    val nextTierRequirement = when (customer.loyaltyTier) {
        com.vector.verevcodex.domain.model.common.LoyaltyTier.BRONZE -> tierProgram?.configuration?.tierRule?.silverThreshold
        com.vector.verevcodex.domain.model.common.LoyaltyTier.SILVER -> tierProgram?.configuration?.tierRule?.goldThreshold
        com.vector.verevcodex.domain.model.common.LoyaltyTier.GOLD -> tierProgram?.configuration?.tierRule?.vipThreshold
        com.vector.verevcodex.domain.model.common.LoyaltyTier.VIP -> null
    }

    if (rewardToRedeem != null) {
        AlertDialog(
            onDismissRequest = { rewardToRedeemId = null },
            title = { Text(text = stringResource(R.string.merchant_customer_bonus_reward_redeem_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.merchant_customer_bonus_reward_redeem_message,
                        rewardToRedeem.name,
                        rewardToRedeem.pointsRequired,
                        customer.currentPoints,
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRedeemReward(rewardToRedeem.id)
                        rewardToRedeemId = null
                    },
                    enabled = !isSaving,
                ) {
                    Text(text = stringResource(R.string.merchant_customer_bonus_reward_redeem_confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { rewardToRedeemId = null }, enabled = !isSaving) {
                    Text(text = stringResource(R.string.auth_cancel))
                }
            },
        )
    }

    if (couponProgramToRedeem != null) {
        val couponRule = couponProgramToRedeem.configuration.couponRule
        AlertDialog(
            onDismissRequest = { couponProgramToRedeemId = null },
            title = { Text(text = stringResource(R.string.merchant_customer_bonus_coupon_redeem_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.merchant_customer_bonus_coupon_redeem_message,
                        couponRule.couponName,
                        couponRule.pointsCost,
                        customer.currentPoints,
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRedeemCoupon(couponProgramToRedeem.id)
                        couponProgramToRedeemId = null
                    },
                    enabled = !isSaving,
                ) {
                    Text(text = stringResource(R.string.merchant_customer_bonus_coupon_redeem_confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { couponProgramToRedeemId = null }, enabled = !isSaving) {
                    Text(text = stringResource(R.string.auth_cancel))
                }
            },
        )
    }

    if (discountCampaignToApply != null) {
        AlertDialog(
            onDismissRequest = { discountCampaignToApplyId = null },
            title = { Text(text = stringResource(R.string.merchant_customer_bonus_discount_apply_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.merchant_customer_bonus_discount_apply_message,
                        discountCampaignToApply.name,
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onMarkDiscountApplied(discountCampaignToApply.id)
                        discountCampaignToApplyId = null
                    },
                    enabled = !isSaving,
                ) {
                    Text(text = stringResource(R.string.merchant_customer_bonus_discount_apply_confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { discountCampaignToApplyId = null }, enabled = !isSaving) {
                    Text(text = stringResource(R.string.auth_cancel))
                }
            },
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_customer_bonus_management_title))
        CustomerBodySection {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip(
                    label = stringResource(R.string.merchant_metric_points),
                    value = customer.currentPoints.toString(),
                    modifier = Modifier.weight(1f),
                )
                MetricChip(
                    label = stringResource(R.string.merchant_customer_bonus_metric_tier),
                    value = customer.loyaltyTier.displayName(),
                    modifier = Modifier.weight(1f),
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomerBonusSection.entries.forEach { section ->
                    MerchantFilterChip(
                        text = stringResource(section.labelRes),
                        selected = selectedSection == section,
                        onClick = { selectedSection = section },
                    )
                }
            }
            when (selectedSection) {
                CustomerBonusSection.POINTS -> {
                    BonusManagementActionCard(
                        icon = Icons.Default.Stars,
                        title = stringResource(R.string.merchant_customer_adjust_points),
                        subtitle = stringResource(R.string.merchant_customer_bonus_adjust_subtitle),
                        actionLabel = stringResource(R.string.merchant_customer_bonus_open_editor),
                        onAction = onAdjustPoints,
                    )
                    pointsProgram?.let { program ->
                        BonusManagementActionCard(
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            title = program.name,
                            subtitle = stringResource(
                                R.string.merchant_customer_bonus_points_rule_summary,
                                program.configuration.pointsRule.pointsAwardedPerStep,
                                program.configuration.pointsRule.spendStepAmount,
                                program.configuration.pointsRule.minimumRedeemPoints,
                            ),
                        )
                    }
                    Text(
                        text = stringResource(R.string.merchant_customer_bonus_recent_activity),
                        style = MaterialTheme.typography.titleSmall,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                    ledgerEntries.take(5).forEach { entry ->
                        CustomerCrmRow(
                            icon = if (entry.delta >= 0) Icons.Default.Stars else Icons.Default.RemoveCircle,
                            title = entry.reason,
                            value = buildString {
                                append(if (entry.delta >= 0) "+" else "")
                                append(entry.delta)
                                append(" pts")
                            },
                            subtitle = formatRelativeDateTime(entry.createdAt),
                        )
                    }
                }
                CustomerBonusSection.REWARDS -> {
                    if (rewards.isEmpty()) {
                        CustomerCrmRow(
                            icon = Icons.Default.Redeem,
                            title = stringResource(R.string.merchant_customer_bonus_rewards_empty_title),
                            value = stringResource(R.string.merchant_customer_bonus_rewards_empty_subtitle),
                        )
                    } else {
                        rewards.sortedBy { it.pointsRequired }.forEach { reward ->
                            if (customer.currentPoints >= reward.pointsRequired) {
                                BonusManagementActionCard(
                                    icon = Icons.Default.Redeem,
                                    title = reward.name,
                                    subtitle = stringResource(
                                        R.string.merchant_customer_bonus_reward_ready_summary,
                                        reward.pointsRequired,
                                        reward.rewardType.displayName(),
                                    ),
                                    actionLabel = stringResource(R.string.merchant_customer_bonus_reward_redeem_confirm),
                                    onAction = { rewardToRedeemId = reward.id },
                                )
                            } else {
                                CustomerCrmRow(
                                    icon = Icons.Default.Redeem,
                                    title = reward.name,
                                    value = stringResource(
                                        R.string.merchant_customer_bonus_reward_gap,
                                        reward.pointsRequired - customer.currentPoints,
                                    ),
                                    subtitle = "${stringResource(R.string.merchant_points_required_format, reward.pointsRequired)} • ${reward.rewardType.displayName()}",
                                )
                            }
                        }
                        BonusManagementActionCard(
                            icon = Icons.Default.Redeem,
                            title = stringResource(R.string.merchant_customer_bonus_rewards_title),
                            subtitle = stringResource(
                                R.string.merchant_customer_bonus_rewards_ready_subtitle,
                                redeemableRewards.size,
                                rewards.size,
                            ),
                        )
                    }
                }
                CustomerBonusSection.DISCOUNTS -> {
                    if (discountCampaigns.isEmpty()) {
                        CustomerCrmRow(
                            icon = Icons.Default.LocalOffer,
                            title = stringResource(R.string.merchant_customer_bonus_discounts_empty_title),
                            value = stringResource(R.string.merchant_customer_bonus_discounts_empty_subtitle),
                        )
                    } else {
                        discountCampaigns.forEach { campaign ->
                            BonusManagementActionCard(
                                icon = Icons.Default.LocalOffer,
                                title = campaign.name,
                                subtitle = when (campaign.promotionType) {
                                    PromotionType.PERCENT_DISCOUNT -> stringResource(
                                        R.string.merchant_promotion_value_percent_discount,
                                        campaign.promotionValue.toInt(),
                                    )
                                    PromotionType.FIXED_DISCOUNT -> stringResource(
                                        R.string.merchant_promotion_value_fixed_discount,
                                        campaign.promotionValue.toInt(),
                                    )
                                    else -> campaign.description
                                } + " • " + campaign.target.description,
                                actionLabel = stringResource(R.string.merchant_customer_bonus_discount_apply_confirm),
                                onAction = { discountCampaignToApplyId = campaign.id },
                            )
                        }
                        discountActions.forEach { action ->
                            CustomerCrmRow(
                                icon = Icons.Default.History,
                                title = action.title,
                                value = action.details,
                                subtitle = formatRelativeDateTime(action.createdAt),
                            )
                        }
                    }
                }
                CustomerBonusSection.COUPONS -> {
                    couponProgram?.let { program ->
                        val couponRule = program.configuration.couponRule
                        val canRedeemCoupon = customer.currentPoints >= couponRule.pointsCost
                        BonusManagementActionCard(
                            icon = Icons.Default.LocalOffer,
                            title = couponRule.couponName,
                            subtitle = if (canRedeemCoupon) {
                                stringResource(
                                    R.string.merchant_customer_bonus_coupon_ready_summary,
                                    couponRule.pointsCost,
                                    couponRule.discountAmount.toInt(),
                                    couponRule.minimumSpendAmount.toInt(),
                                )
                            } else {
                                stringResource(
                                    R.string.merchant_customer_bonus_coupon_program_summary,
                                    couponRule.pointsCost,
                                    couponRule.discountAmount.toInt(),
                                    couponRule.minimumSpendAmount.toInt(),
                                )
                            },
                            actionLabel = if (canRedeemCoupon) {
                                stringResource(R.string.merchant_customer_bonus_coupon_redeem_confirm)
                            } else {
                                null
                            },
                            onAction = if (canRedeemCoupon) {
                                { couponProgramToRedeemId = program.id }
                            } else {
                                null
                            },
                        )
                    }
                    if (promoCodeCampaigns.isEmpty()) {
                        CustomerCrmRow(
                            icon = Icons.Default.LocalOffer,
                            title = stringResource(R.string.merchant_customer_bonus_coupons_empty_title),
                            value = stringResource(R.string.merchant_customer_bonus_coupons_empty_subtitle),
                        )
                    } else {
                        promoCodeCampaigns.forEach { campaign ->
                            CustomerCrmRow(
                                icon = Icons.Default.LocalOffer,
                                title = campaign.name,
                                value = campaign.promoCode.orEmpty(),
                                subtitle = campaign.description,
                            )
                        }
                    }
                }
                CustomerBonusSection.TIER -> {
                    BonusManagementActionCard(
                        icon = Icons.Default.EmojiEvents,
                        title = stringResource(R.string.merchant_customer_bonus_tier_title),
                        subtitle = relation?.notes?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.merchant_customer_bonus_tier_subtitle),
                        actionLabel = stringResource(R.string.merchant_customer_bonus_tier_record_confirm),
                        onAction = onRecordTierBenefit,
                    )
                    if (tierProgram != null) {
                        CustomerCrmRow(
                            icon = Icons.Default.EmojiEvents,
                            title = stringResource(R.string.merchant_customer_bonus_tier_current, customer.loyaltyTier.displayName()),
                            value = nextTierRequirement?.let { threshold ->
                                stringResource(
                                    R.string.merchant_customer_bonus_tier_next_requirement,
                                    threshold,
                                    (threshold - customer.currentPoints).coerceAtLeast(0),
                                )
                            } ?: stringResource(R.string.merchant_customer_bonus_tier_top_level),
                            subtitle = stringResource(
                                R.string.merchant_customer_bonus_tier_bonus_summary,
                                tierProgram.configuration.tierRule.tierBonusPercent,
                            ),
                        )
                        CustomerCrmRow(
                            icon = Icons.Default.EmojiEvents,
                            title = stringResource(R.string.merchant_customer_bonus_tier_thresholds_title),
                            value = stringResource(
                                R.string.merchant_customer_bonus_tier_thresholds_value,
                                tierProgram.configuration.tierRule.silverThreshold,
                                tierProgram.configuration.tierRule.goldThreshold,
                                tierProgram.configuration.tierRule.vipThreshold,
                            ),
                        )
                    }
                    tierActions.forEach { action ->
                        CustomerCrmRow(
                            icon = Icons.Default.History,
                            title = action.title,
                            value = action.details,
                            subtitle = formatRelativeDateTime(action.createdAt),
                        )
                    }
                }
            }
        }
    }
}

private enum class CustomerBonusSection(val labelRes: Int) {
    POINTS(R.string.merchant_customer_bonus_section_points),
    REWARDS(R.string.merchant_customer_bonus_section_rewards),
    DISCOUNTS(R.string.merchant_customer_bonus_section_discounts),
    COUPONS(R.string.merchant_customer_bonus_section_coupons),
    TIER(R.string.merchant_customer_bonus_section_tier),
}

@Composable
internal fun CustomerActivitySection(
    activities: List<CustomerActivity>,
    onOpenTransaction: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_customer_activity_section))
        CustomerBodySection {
            activities.take(8).forEachIndexed { index, activity ->
                CustomerActivityTimelineRow(
                    activity = activity,
                    onClick = activity.transactionId?.let { id -> { onOpenTransaction(id) } },
                )
                if (index < minOf(activities.lastIndex, 7)) {
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
internal fun CustomerTransactionDetailSection(transaction: Transaction) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CustomerBodySection {
            Text(
                text = stringResource(R.string.merchant_customer_transaction_summary_title),
                style = MaterialTheme.typography.titleMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricChip(
                    label = stringResource(R.string.merchant_transaction_amount),
                    value = formatWholeCurrency(transaction.amount),
                    modifier = Modifier.weight(1f),
                )
                MetricChip(
                    label = stringResource(R.string.merchant_metric_points),
                    value = buildString {
                        append(if (transaction.pointsEarned - transaction.pointsRedeemed >= 0) "+" else "")
                        append(transaction.pointsEarned - transaction.pointsRedeemed)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricChip(
                    label = stringResource(R.string.merchant_customer_transaction_items_count),
                    value = transaction.items.sumOf { it.quantity }.toString(),
                    modifier = Modifier.weight(1f),
                )
                MetricChip(
                    label = stringResource(R.string.merchant_customer_transaction_redeemed_points),
                    value = transaction.pointsRedeemed.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
            CustomerCrmRow(
                icon = Icons.Default.History,
                title = stringResource(R.string.merchant_customer_transaction_timestamp),
                value = formatRelativeDateTime(transaction.timestamp),
            )
            CustomerCrmRow(
                icon = Icons.Default.NoteAlt,
                title = stringResource(R.string.merchant_customer_transaction_metadata),
                value = transaction.metadata.ifBlank { stringResource(R.string.merchant_transaction_item_fallback) },
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MerchantSectionTitle(text = stringResource(R.string.merchant_customer_transaction_items))
            CustomerBodySection {
                transaction.items.forEachIndexed { index, item ->
                    CustomerCrmRow(
                        icon = Icons.Default.LocalOffer,
                        title = item.name,
                        value = stringResource(
                            R.string.merchant_customer_transaction_item_summary,
                            item.quantity,
                            formatWholeCurrency(item.unitPrice),
                        ),
                    )
                    if (index < transaction.items.lastIndex) {
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
internal fun EditCustomerContactDialog(
    customer: Customer,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit,
) {
    var firstName by remember(customer.id) { mutableStateOf(customer.firstName) }
    var lastName by remember(customer.id) { mutableStateOf(customer.lastName) }
    var phone by remember(customer.id) { mutableStateOf(customer.phoneNumber) }
    var email by remember(customer.id) { mutableStateOf(customer.email) }

    CustomerEditDialog(
        title = stringResource(R.string.merchant_customer_edit_contact_title),
        isSaving = isSaving,
        onDismiss = onDismiss,
        onConfirm = { onSave(firstName, lastName, phone, email) },
        confirmLabel = stringResource(R.string.merchant_customer_save_changes),
        content = {
            OutlinedTextField(value = firstName, onValueChange = { firstName = it.replace("\n", "") }, label = { Text(stringResource(R.string.merchant_add_customer_first_name)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = lastName, onValueChange = { lastName = it.replace("\n", "") }, label = { Text(stringResource(R.string.merchant_add_customer_last_name)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = phone, onValueChange = { phone = it.replace("\n", "") }, label = { Text(stringResource(R.string.merchant_add_customer_phone)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it.replace("\n", "") }, label = { Text(stringResource(R.string.merchant_add_customer_email)) }, modifier = Modifier.fillMaxWidth())
        },
    )
}

@Composable
internal fun EditCustomerCrmDialog(
    relation: CustomerBusinessRelation?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, List<String>) -> Unit,
) {
    var notes by remember(relation?.id) { mutableStateOf(relation?.notes.orEmpty()) }
    var tagsInput by remember(relation?.id) { mutableStateOf(relation?.tags?.joinToString(", ").orEmpty()) }

    CustomerEditDialog(
        title = stringResource(R.string.merchant_customer_edit_crm_title),
        isSaving = isSaving,
        onDismiss = onDismiss,
        onConfirm = {
            onSave(
                notes,
                tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() },
            )
        },
        confirmLabel = stringResource(R.string.merchant_customer_save_changes),
        content = {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.merchant_customer_notes_label)) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = tagsInput,
                onValueChange = { tagsInput = it.replace("\n", "") },
                label = { Text(stringResource(R.string.merchant_customer_tags_label)) },
                supportingText = { Text(stringResource(R.string.merchant_customer_tags_help)) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@Composable
internal fun AdjustCustomerPointsDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Int, String) -> Unit,
) {
    var delta by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    CustomerEditDialog(
        title = stringResource(R.string.merchant_customer_adjust_points_title),
        isSaving = isSaving,
        onDismiss = onDismiss,
        onConfirm = { onSave(delta.toIntOrNull() ?: 0, reason) },
        confirmLabel = stringResource(R.string.merchant_customer_adjust_points),
        content = {
            OutlinedTextField(
                value = delta,
                onValueChange = { delta = it.filter { ch -> ch.isDigit() || ch == '-' } },
                label = { Text(stringResource(R.string.merchant_customer_adjust_points_delta)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text(stringResource(R.string.merchant_customer_adjust_points_reason)) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@Composable
private fun CustomerCrmRow(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(18.dp),
        color = if (onClick != null) VerevColors.AppBackground.copy(alpha = 0.62f) else Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(VerevColors.AppBackground, RoundedCornerShape(14.dp))
                    .padding(10.dp),
            ) {
                Icon(icon, contentDescription = null, tint = VerevColors.Forest)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text(value, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest.copy(alpha = 0.74f))
                subtitle?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(VerevColors.AppBackground, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.52f))
        Text(value, style = MaterialTheme.typography.titleLarge, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BonusManagementActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(VerevColors.AppBackground)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(VerevColors.Forest.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = VerevColors.Forest)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.64f))
        }
        if (actionLabel != null && onAction != null) {
            OutlinedButton(onClick = onAction, shape = RoundedCornerShape(14.dp)) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun CustomerActivityTimelineRow(
    activity: CustomerActivity,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(VerevColors.AppBackground.copy(alpha = 0.72f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(VerevColors.Forest.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(activity.icon(), contentDescription = null, tint = VerevColors.Forest)
            }
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(36.dp)
                    .background(VerevColors.Forest.copy(alpha = 0.12f)),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(activity.title, style = MaterialTheme.typography.titleSmall, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
            Text(activity.description, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest.copy(alpha = 0.75f))
            Text(activity.subtitle(), style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.54f))
        }
    }
}

@Composable
private fun CustomerEditDialog(
    title: String,
    confirmLabel: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
                Text(
                    text = stringResource(R.string.merchant_customer_modal_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.6f),
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isSaving) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(R.string.auth_cancel))
            }
        },
    )
}
