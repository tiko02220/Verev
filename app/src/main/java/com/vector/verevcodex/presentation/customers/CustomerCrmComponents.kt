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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.common.phone.sanitizePhoneNumberInput
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerBonusAction
import com.vector.verevcodex.domain.model.customer.CustomerBonusActionType
import com.vector.verevcodex.domain.model.customer.CustomerActivity
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.model.loyalty.PointsLedger
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.TierProgramRule
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.PromotionType
import com.vector.verevcodex.domain.model.transactions.TransactionApprovalRequest
import com.vector.verevcodex.domain.model.transactions.TransactionApprovalStatus
import com.vector.verevcodex.domain.model.transactions.TransactionStatus
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantSectionTitle
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
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
        CustomerSectionHeader(
            title = stringResource(R.string.merchant_customer_crm_section),
            actionIcon = Icons.Default.Edit,
            onAction = onEditCrm,
        )
        CustomerBodySection {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.merchant_customer_notes_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(VerevColors.AppBackground.copy(alpha = 0.8f))
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = relation?.notes?.ifBlank { stringResource(R.string.merchant_customer_notes_empty) }
                            ?: stringResource(R.string.merchant_customer_notes_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VerevColors.Forest.copy(alpha = 0.74f),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.merchant_customer_tags_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                if (relation?.tags?.isNotEmpty() == true) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        relation.tags.forEach { tag ->
                            MerchantStatusPill(
                                text = tag,
                                backgroundColor = VerevColors.Forest.copy(alpha = 0.08f),
                                contentColor = VerevColors.Forest,
                            )
                        }
                    }
                } else {
                    CustomerCrmRow(
                        icon = Icons.Default.LocalOffer,
                        title = stringResource(R.string.merchant_customer_tags_label),
                        value = stringResource(R.string.merchant_customer_tags_empty),
                    )
                }
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
        it.active &&
            !java.time.LocalDate.now().isBefore(it.startDate) &&
            !java.time.LocalDate.now().isAfter(it.endDate) &&
            (it.promotionType == PromotionType.PERCENT_DISCOUNT || it.promotionType == PromotionType.FIXED_DISCOUNT)
    }
    val promoCodeCampaigns = campaigns.filter {
        it.active &&
            !java.time.LocalDate.now().isBefore(it.startDate) &&
            !java.time.LocalDate.now().isAfter(it.endDate) &&
            !it.promoCode.isNullOrBlank()
    }
    val redeemableRewards = rewards.filter { customer.currentPoints >= it.pointsRequired }
    val rewardToRedeem = rewards.firstOrNull { it.id == rewardToRedeemId }
    val couponProgramToRedeem = programs.firstOrNull { it.id == couponProgramToRedeemId && it.configuration.couponEnabled }
    val discountCampaignToApply = campaigns.firstOrNull { it.id == discountCampaignToApplyId }
    val discountActions = bonusActions.filter { it.type == CustomerBonusActionType.DISCOUNT_APPLIED }.take(3)
    val tierActions = bonusActions.filter { it.type == CustomerBonusActionType.TIER_BENEFIT_RECORDED }.take(3)
    val nextTier = tierProgram?.configuration?.tierRule?.nextLevelAfter(customer.loyaltyTierLabel)
    val nextTierRequirement = nextTier?.threshold

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(VerevColors.Forest),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.merchant_customer_bonus_management_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = VerevColors.White,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = customer.displayName(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = VerevColors.White.copy(alpha = 0.7f),
                        )
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomerBonusSection.entries.forEach { section ->
                        BonusSectionTab(
                            label = stringResource(section.labelRes),
                            icon = section.icon(),
                            selected = selectedSection == section,
                            onClick = { selectedSection = section },
                        )
                    }
                }
            }
        }

        CustomerBodySection {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(VerevColors.Gold, VerevColors.Moss),
                        )
                    )
                    .padding(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = stringResource(R.string.merchant_customer_bonus_management_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = VerevColors.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BonusHeroMetric(
                            label = stringResource(R.string.merchant_metric_points),
                            value = customer.currentPoints.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        BonusHeroMetric(
                            label = stringResource(R.string.merchant_customer_bonus_metric_tier),
                            value = customer.loyaltyTierLabel,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            when (selectedSection) {
                CustomerBonusSection.POINTS -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(VerevColors.AppBackground.copy(alpha = 0.9f))
                            .padding(18.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.merchant_customer_bonus_current_balance_title),
                                style = MaterialTheme.typography.bodyMedium,
                                color = VerevColors.Forest.copy(alpha = 0.6f),
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                Text(
                                    text = formatCompactCount(customer.currentPoints),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = VerevColors.Forest,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text = "pts",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = VerevColors.Forest.copy(alpha = 0.6f),
                                )
                            }
                        }
                    }
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
                                    formatWholeCurrency(couponRule.discountAmount),
                                    formatWholeCurrency(couponRule.minimumSpendAmount),
                                )
                            } else {
                                stringResource(
                                    R.string.merchant_customer_bonus_coupon_program_summary,
                                    couponRule.pointsCost,
                                    formatWholeCurrency(couponRule.discountAmount),
                                    formatWholeCurrency(couponRule.minimumSpendAmount),
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
                    if (tierProgram != null) {
                        BonusManagementActionCard(
                            icon = Icons.Default.EmojiEvents,
                            title = stringResource(R.string.merchant_customer_bonus_tier_title),
                            subtitle = stringResource(R.string.merchant_customer_bonus_tier_subtitle),
                            actionLabel = stringResource(R.string.merchant_customer_bonus_tier_record_confirm),
                            onAction = onRecordTierBenefit,
                        )
                        CustomerCrmRow(
                            icon = Icons.Default.EmojiEvents,
                            title = stringResource(R.string.merchant_customer_bonus_tier_current, customer.loyaltyTierLabel),
                            value = nextTierRequirement?.let { threshold ->
                                stringResource(
                                    R.string.merchant_customer_bonus_tier_next_requirement,
                                    threshold,
                                    (threshold - customer.currentPoints).coerceAtLeast(0),
                                )
                            } ?: stringResource(R.string.merchant_customer_bonus_tier_top_level),
                            subtitle = tierProgram.configuration.tierRule.tierBenefitSummaryFor(customer.loyaltyTierLabel),
                        )
                        CustomerCrmRow(
                            icon = Icons.Default.EmojiEvents,
                            title = stringResource(R.string.merchant_customer_bonus_tier_thresholds_title),
                            value = tierProgram.configuration.tierRule.crmTierThresholdsSummary(),
                        )
                    } else {
                        CustomerCrmRow(
                            icon = Icons.Default.EmojiEvents,
                            title = stringResource(R.string.merchant_customer_bonus_tier_title),
                            value = stringResource(R.string.merchant_customer_bonus_tier_unavailable),
                            subtitle = relation?.notes?.takeIf { it.isNotBlank() },
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
private fun TierProgramRule.crmTierThresholdsSummary(): String =
    LocalContext.current.let { context ->
        configurableLevels.joinToString(separator = ", ") { level ->
            context.getString(
                R.string.merchant_customer_program_details_tier_level_format,
                level.name,
                level.threshold,
            )
        }
    }

@Composable
private fun TierProgramRule.tierBenefitSummaryFor(currentTierLabel: String): String =
    levelForTierName(currentTierLabel)
        ?.let {
            stringResource(
                R.string.merchant_customer_bonus_tier_level_bonus_summary,
                it.name,
                it.bonusPercent,
            )
        }
        ?: configurableLevels.lastOrNull()?.let {
            stringResource(
                R.string.merchant_customer_bonus_tier_level_bonus_summary,
                it.name,
                it.bonusPercent,
            )
        }
        ?: stringResource(R.string.merchant_customer_bonus_tier_base_summary)

@Composable
internal fun CustomerActivitySection(
    activities: List<CustomerActivity>,
    onOpenTransaction: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_customer_activity_section))
        activities.take(8).forEachIndexed { index, activity ->
            CustomerActivityCard(
                activity = activity,
                onClick = activity.transactionId?.let { id -> { onOpenTransaction(id) } },
            )
            if (index < minOf(activities.lastIndex, 7)) Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
internal fun CustomerTransactionDetailSection(
    transaction: Transaction,
    voidRequest: TransactionApprovalRequest?,
    canRequestVoid: Boolean,
    onRequestVoid: () -> Unit,
) {
    val netPoints = transaction.pointsEarned - transaction.pointsRedeemed
    val lineItems = transaction.items
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CustomerTransactionVoidSection(
            transaction = transaction,
            voidRequest = voidRequest,
            canRequestVoid = canRequestVoid,
            onRequestVoid = onRequestVoid,
        )
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
                        append(if (netPoints >= 0) "+" else "")
                        append(netPoints)
                        append(" pts")
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
                    value = lineItems.sumOf { it.quantity }.toString(),
                    modifier = Modifier.weight(1f),
                )
                MetricChip(
                    label = stringResource(R.string.merchant_customer_transaction_redeemed_points),
                    value = buildString {
                        append(transaction.pointsRedeemed)
                        append(" pts")
                    },
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
                if (lineItems.isEmpty()) {
                    CustomerCrmRow(
                        icon = Icons.Default.LocalOffer,
                        title = stringResource(R.string.merchant_customer_transaction_items_empty_title),
                        value = stringResource(R.string.merchant_customer_transaction_items_empty_subtitle),
                    )
                } else {
                    lineItems.forEachIndexed { index, item ->
                        CustomerCrmRow(
                            icon = Icons.Default.LocalOffer,
                            title = item.name,
                            value = formatWholeCurrency(item.quantity * item.unitPrice),
                            subtitle = stringResource(
                                R.string.merchant_customer_transaction_item_detailed_summary,
                                item.quantity,
                                formatWholeCurrency(item.unitPrice),
                            ),
                        )
                        if (index < lineItems.lastIndex) {
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerTransactionVoidSection(
    transaction: Transaction,
    voidRequest: TransactionApprovalRequest?,
    canRequestVoid: Boolean,
    onRequestVoid: () -> Unit,
) {
    val transactionStatusLabel = when (transaction.status) {
        TransactionStatus.COMPLETED -> stringResource(R.string.merchant_transaction_status_completed)
        TransactionStatus.PENDING_APPROVAL -> stringResource(R.string.merchant_transaction_status_pending_approval)
        TransactionStatus.REJECTED -> stringResource(R.string.merchant_transaction_status_rejected)
        TransactionStatus.VOIDED -> stringResource(R.string.merchant_transaction_status_voided)
        TransactionStatus.UNKNOWN -> stringResource(R.string.merchant_transaction_status_unknown)
    }
    val approvalStatusLabel = when (voidRequest?.status) {
        TransactionApprovalStatus.PENDING -> stringResource(R.string.merchant_transaction_void_status_pending)
        TransactionApprovalStatus.APPROVED -> stringResource(R.string.merchant_transaction_void_status_approved)
        TransactionApprovalStatus.REJECTED -> stringResource(R.string.merchant_transaction_void_status_rejected)
        TransactionApprovalStatus.CANCELLED -> stringResource(R.string.merchant_transaction_void_status_cancelled)
        TransactionApprovalStatus.UNKNOWN -> stringResource(R.string.merchant_transaction_void_status_unknown)
        null -> null
    }
    val helperText = when {
        transaction.status == TransactionStatus.VOIDED ->
            stringResource(R.string.merchant_transaction_void_completed_summary)
        voidRequest?.status == TransactionApprovalStatus.PENDING ->
            stringResource(R.string.merchant_transaction_void_pending_summary)
        voidRequest?.status == TransactionApprovalStatus.REJECTED ->
            stringResource(R.string.merchant_transaction_void_rejected_summary)
        canRequestVoid ->
            stringResource(R.string.merchant_transaction_void_request_summary)
        else ->
            stringResource(R.string.merchant_transaction_void_unavailable_summary)
    }

    CustomerBodySection {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.merchant_transaction_void_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MerchantStatusPill(
                    text = transactionStatusLabel,
                    backgroundColor = VerevColors.Forest.copy(alpha = 0.12f),
                    contentColor = VerevColors.Forest,
                )
                approvalStatusLabel?.let { label ->
                    MerchantStatusPill(
                        text = label,
                        backgroundColor = VerevColors.Gold.copy(alpha = 0.16f),
                        contentColor = VerevColors.Forest,
                    )
                }
            }
            CustomerCrmRow(
                icon = if (voidRequest?.status == TransactionApprovalStatus.PENDING) Icons.Default.Schedule else Icons.Default.RemoveCircle,
                title = stringResource(R.string.merchant_transaction_void_status_label),
                value = helperText,
                subtitle = voidRequest?.reasonText?.takeIf { it.isNotBlank() }?.let {
                    stringResource(R.string.merchant_transaction_void_reason_display, it)
                },
            )
            if (canRequestVoid) {
                OutlinedButton(
                    onClick = onRequestVoid,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = stringResource(R.string.merchant_transaction_request_void))
                }
            }
        }
    }
}

@Composable
internal fun CustomerTransactionDetailHero(transaction: Transaction) {
    val netPoints = transaction.pointsEarned - transaction.pointsRedeemed
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BonusHeroMetric(
            label = stringResource(R.string.merchant_transaction_amount),
            value = formatWholeCurrency(transaction.amount),
            modifier = Modifier.weight(1f),
        )
        BonusHeroMetric(
            label = stringResource(R.string.merchant_metric_points),
            value = buildString {
                append(if (netPoints >= 0) "+" else "")
                append(netPoints)
                append(" pts")
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun RequestTransactionVoidDialog(
    reason: String,
    reasonError: String?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onReasonChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerevColors.Gold,
                    contentColor = VerevColors.ForestDeep,
                ),
            ) {
                Text(
                    text = stringResource(
                        if (isSubmitting) R.string.merchant_transaction_void_submitting
                        else R.string.merchant_transaction_request_void
                    ),
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isSubmitting,
            ) {
                Text(text = stringResource(R.string.merchant_action_cancel))
            }
        },
        title = {
            Text(
                text = stringResource(R.string.merchant_transaction_void_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                color = VerevColors.Forest,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.merchant_transaction_void_dialog_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.74f),
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    label = { Text(stringResource(R.string.merchant_transaction_void_reason_label)) },
                    supportingText = {
                        reasonError?.let { Text(text = it) }
                    },
                    isError = reasonError != null,
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        containerColor = Color.White,
    )
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
    val validation = remember(firstName, email) {
        CustomerDialogValidation.validateContact(firstName, email)
    }

    CustomerEditDialog(
        title = stringResource(R.string.merchant_customer_edit_contact_title),
        subtitle = stringResource(R.string.merchant_customer_edit_contact_subtitle),
        isSaving = isSaving,
        onDismiss = onDismiss,
        confirmEnabled = !validation.hasErrors,
        onConfirm = { onSave(firstName.trim(), lastName.trim(), phone.trim(), email.trim()) },
        confirmLabel = stringResource(R.string.merchant_customer_save_changes),
        content = {
            CustomerDialogSectionTitle(
                icon = Icons.Default.Edit,
                title = stringResource(R.string.merchant_customer_dialog_identity_title),
            )
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it.replace("\n", "") },
                label = { Text(stringResource(R.string.merchant_add_customer_first_name)) },
                supportingText = {
                    if (validation.firstNameError) {
                        Text(stringResource(R.string.merchant_customer_error_first_name_required))
                    }
                },
                isError = validation.firstNameError,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it.replace("\n", "") },
                label = { Text(stringResource(R.string.merchant_add_customer_last_name)) },
                modifier = Modifier.fillMaxWidth(),
            )
            CustomerDialogSectionTitle(
                icon = Icons.Default.Mail,
                title = stringResource(R.string.merchant_customer_dialog_contact_title),
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = sanitizePhoneNumberInput(it) },
                label = { Text(stringResource(R.string.merchant_add_customer_phone)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.replace("\n", "") },
                label = { Text(stringResource(R.string.merchant_add_customer_email)) },
                supportingText = {
                    if (validation.emailError) {
                        Text(stringResource(R.string.merchant_customer_error_email_invalid))
                    }
                },
                isError = validation.emailError,
                modifier = Modifier.fillMaxWidth(),
            )
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
        subtitle = stringResource(R.string.merchant_customer_edit_crm_subtitle),
        isSaving = isSaving,
        onDismiss = onDismiss,
        onConfirm = {
            onSave(
                notes.trim(),
                tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }.distinct(),
            )
        },
        confirmLabel = stringResource(R.string.merchant_customer_save_changes),
        content = {
            CustomerDialogSectionTitle(
                icon = Icons.Default.NoteAlt,
                title = stringResource(R.string.merchant_customer_dialog_notes_title),
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.merchant_customer_notes_label)) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            CustomerDialogSectionTitle(
                icon = Icons.Default.LocalOffer,
                title = stringResource(R.string.merchant_customer_dialog_tags_title),
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
    val validation = remember(delta, reason) {
        CustomerDialogValidation.validatePointAdjustment(delta, reason)
    }

    CustomerEditDialog(
        title = stringResource(R.string.merchant_customer_adjust_points_title),
        subtitle = stringResource(R.string.merchant_customer_adjust_points_subtitle),
        isSaving = isSaving,
        onDismiss = onDismiss,
        confirmEnabled = !validation.hasErrors,
        onConfirm = { onSave(validation.parsedDelta ?: 0, reason.trim()) },
        confirmLabel = stringResource(R.string.merchant_customer_adjust_points),
        content = {
            CustomerDialogSectionTitle(
                icon = Icons.Default.Stars,
                title = stringResource(R.string.merchant_customer_dialog_points_title),
            )
            OutlinedTextField(
                value = delta,
                onValueChange = { delta = it.filter { ch -> ch.isDigit() || ch == '-' } },
                label = { Text(stringResource(R.string.merchant_customer_adjust_points_delta)) },
                supportingText = {
                    if (validation.deltaError) {
                        Text(stringResource(R.string.merchant_customer_error_points_delta_invalid))
                    }
                },
                isError = validation.deltaError,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text(stringResource(R.string.merchant_customer_adjust_points_reason)) },
                supportingText = {
                    if (validation.reasonError) {
                        Text(stringResource(R.string.merchant_customer_error_reason_required))
                    }
                },
                isError = validation.reasonError,
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
            .clip(RoundedCornerShape(22.dp))
            .background(VerevColors.AppBackground.copy(alpha = 0.72f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(VerevColors.White, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = VerevColors.Forest)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.64f))
            if (actionLabel != null && onAction != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(VerevColors.Forest.copy(alpha = 0.1f))
                        .clickable(onClick = onAction)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun BonusHeroMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(VerevColors.White.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.White.copy(alpha = 0.74f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = VerevColors.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun BonusSectionTab(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) VerevColors.Gold else VerevColors.White.copy(alpha = 0.14f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) VerevColors.Forest else VerevColors.White,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) VerevColors.Forest else VerevColors.White,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun CustomerActivityCard(
    activity: CustomerActivity,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(
                            VerevColors.Gold.copy(alpha = 0.16f),
                            VerevColors.Moss.copy(alpha = 0.14f),
                        ),
                    ),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(activity.icon(), contentDescription = null, tint = VerevColors.Forest)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(activity.title, style = MaterialTheme.typography.titleSmall, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
            Text(activity.description, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest.copy(alpha = 0.75f))
            Text(activity.subtitle(), style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.54f))
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = VerevColors.Forest.copy(alpha = 0.35f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private fun CustomerBonusSection.icon(): ImageVector = when (this) {
    CustomerBonusSection.POINTS -> Icons.AutoMirrored.Filled.TrendingUp
    CustomerBonusSection.REWARDS -> Icons.Default.CardGiftcard
    CustomerBonusSection.DISCOUNTS -> Icons.Default.LocalOffer
    CustomerBonusSection.COUPONS -> Icons.Default.LocalOffer
    CustomerBonusSection.TIER -> Icons.Default.EmojiEvents
}

@Composable
private fun CustomerEditDialog(
    title: String,
    subtitle: String,
    confirmLabel: String,
    isSaving: Boolean,
    confirmEnabled: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 12.dp,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = title,
                            color = VerevColors.Forest,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VerevColors.Forest.copy(alpha = 0.6f),
                        )
                    }
                    IconButton(onClick = onDismiss, enabled = !isSaving) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = VerevColors.Forest,
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    content = content,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text(stringResource(R.string.auth_cancel))
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = !isSaving && confirmEnabled,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerevColors.Gold,
                            contentColor = VerevColors.Forest,
                        ),
                    ) {
                        Text(confirmLabel, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerDialogSectionTitle(
    icon: ImageVector,
    title: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(VerevColors.Forest.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VerevColors.Forest,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
