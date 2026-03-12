package com.vector.verevcodex.presentation.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.presentation.merchant.common.MerchantActionCard
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.MerchantPageHeader
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.merchant.common.MerchantSectionTitle
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun ProgramsHeader(
    totalPrograms: Int,
    totalRewards: Int,
    storeName: String,
    onAddProgram: () -> Unit,
) {
    MerchantPageHeader(
        title = stringResource(R.string.merchant_programs_title),
        subtitle = stringResource(R.string.merchant_programs_subtitle, totalPrograms, totalRewards),
        actions = {
            Button(
                onClick = onAddProgram,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerevColors.Forest,
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.merchant_program_add_button),
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Medium,
                )
            }
        },
    )
    Text(
        text = if (storeName.isBlank()) {
            stringResource(R.string.merchant_programs_no_store_selected)
        } else {
            stringResource(R.string.merchant_programs_store_scope, storeName)
        },
        style = MaterialTheme.typography.bodyMedium,
        color = VerevColors.Forest.copy(alpha = 0.66f),
    )
}

@Composable
internal fun ProgramsOverviewCard(
    programs: List<RewardProgram>,
    rewards: List<Reward>,
    campaigns: List<Campaign>,
) {
    val enabledPrograms = programs.count { it.active }
    val liveActions = programs.filter { it.active }.sumOf { it.configuration.scanActions.size }
    MerchantPrimaryCard {
        Text(
            text = stringResource(R.string.merchant_programs_overview),
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.58f),
            fontWeight = FontWeight.Medium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProgramOverviewMetric(stringResource(R.string.merchant_metric_programs), formatCompactCount(enabledPrograms), Modifier.weight(1f))
            ProgramOverviewMetric(stringResource(R.string.merchant_metric_rewards), formatCompactCount(rewards.size), Modifier.weight(1f))
            ProgramOverviewMetric(stringResource(R.string.merchant_metric_campaigns), formatCompactCount(campaigns.size), Modifier.weight(1f))
        }
        HorizontalDivider(color = VerevColors.AppBackground)
        Text(
            text = stringResource(R.string.merchant_programs_actions_summary, liveActions),
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest.copy(alpha = 0.72f),
        )
    }
}

@Composable
internal fun ProgramTemplateSection(
    onCreateProgram: (LoyaltyProgramType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_program_templates_section))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(LoyaltyProgramType.entries.filterNot { it == LoyaltyProgramType.CASHBACK || it == LoyaltyProgramType.HYBRID }) { type ->
                ProgramTemplateCard(type = type, onClick = { onCreateProgram(type) })
            }
        }
    }
}

@Composable
private fun ProgramTemplateCard(
    type: LoyaltyProgramType,
    onClick: () -> Unit,
) {
    MerchantActionCard(
        title = type.displayName(),
        subtitle = stringResource(type.templateSubtitleRes()),
        icon = type.icon(),
        colors = type.gradient(),
        modifier = Modifier.size(width = 212.dp, height = 150.dp),
        onClick = onClick,
    )
}

@Composable
internal fun ProgramActionRow(
    onOpenRewards: () -> Unit,
    onOpenCampaigns: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MerchantActionCard(
            title = stringResource(R.string.merchant_rewards_manage_title),
            subtitle = stringResource(R.string.merchant_rewards_manage_subtitle),
            icon = Icons.Default.CardGiftcard,
            colors = listOf(VerevColors.Gold, VerevColors.Tan),
            modifier = Modifier.weight(1f),
            onClick = onOpenRewards,
        )
        MerchantActionCard(
            title = stringResource(R.string.merchant_campaigns_manage_title),
            subtitle = stringResource(R.string.merchant_campaigns_manage_subtitle),
            icon = Icons.Default.Campaign,
            colors = listOf(VerevColors.Moss, VerevColors.Forest),
            modifier = Modifier.weight(1f),
            onClick = onOpenCampaigns,
        )
    }
}

@Composable
internal fun ProgramModulesSection(
    onOpenPointsRewards: () -> Unit,
    onOpenTieredLoyalty: () -> Unit,
    onOpenCouponsManager: () -> Unit,
    onOpenCheckinRewards: () -> Unit,
    onOpenPurchaseFrequency: () -> Unit,
    onOpenReferralRewards: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_program_modules_title))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MerchantActionCard(
                title = stringResource(R.string.merchant_points_rewards_title),
                subtitle = stringResource(R.string.merchant_program_module_points_subtitle),
                icon = Icons.Default.Loyalty,
                colors = listOf(VerevColors.Gold, VerevColors.Tan),
                modifier = Modifier.weight(1f),
                onClick = onOpenPointsRewards,
            )
            MerchantActionCard(
                title = stringResource(R.string.merchant_tiered_loyalty_title),
                subtitle = stringResource(R.string.merchant_program_module_tier_subtitle),
                icon = Icons.Default.AutoGraph,
                colors = listOf(VerevColors.Moss, VerevColors.Forest),
                modifier = Modifier.weight(1f),
                onClick = onOpenTieredLoyalty,
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MerchantActionCard(
                title = stringResource(R.string.merchant_coupons_manager_title),
                subtitle = stringResource(R.string.merchant_program_module_coupons_subtitle),
                icon = Icons.Default.Sell,
                colors = listOf(VerevColors.Tan, VerevColors.Gold),
                modifier = Modifier.weight(1f),
                onClick = onOpenCouponsManager,
            )
            MerchantActionCard(
                title = stringResource(R.string.merchant_checkin_rewards_title),
                subtitle = stringResource(R.string.merchant_program_module_checkin_subtitle),
                icon = Icons.Default.CheckCircle,
                colors = listOf(VerevColors.Forest, VerevColors.Moss),
                modifier = Modifier.weight(1f),
                onClick = onOpenCheckinRewards,
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MerchantActionCard(
                title = stringResource(R.string.merchant_purchase_frequency_title),
                subtitle = stringResource(R.string.merchant_program_module_frequency_subtitle),
                icon = Icons.Default.Repeat,
                colors = listOf(VerevColors.Gold, VerevColors.Tan),
                modifier = Modifier.weight(1f),
                onClick = onOpenPurchaseFrequency,
            )
            MerchantActionCard(
                title = stringResource(R.string.merchant_referral_rewards_title),
                subtitle = stringResource(R.string.merchant_program_module_referral_subtitle),
                icon = Icons.Default.GroupAdd,
                colors = listOf(VerevColors.Moss, VerevColors.Forest),
                modifier = Modifier.weight(1f),
                onClick = onOpenReferralRewards,
            )
        }
    }
}

@Composable
internal fun ProgramListSection(
    programs: List<RewardProgram>,
    busyProgramId: String?,
    onEdit: (String) -> Unit,
    onToggleEnabled: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_programs_active_section))
        if (programs.isEmpty()) {
            MerchantEmptyStateCard(
                title = stringResource(R.string.merchant_programs_empty_title),
                subtitle = stringResource(R.string.merchant_programs_empty_subtitle),
                icon = Icons.Default.Loyalty,
            )
        }
        programs.forEach { program ->
            ProgramListItem(
                program = program,
                isBusy = busyProgramId == program.id,
                onEdit = { onEdit(program.id) },
                onToggleEnabled = { enabled -> onToggleEnabled(program.id, enabled) },
                onDelete = { onDelete(program.id) },
            )
        }
    }
}

@Composable
private fun ProgramListItem(
    program: RewardProgram,
    isBusy: Boolean,
    onEdit: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    MerchantPrimaryCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProgramIcon(icon = program.type.icon(), colors = program.type.gradient())
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(program.name, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
                Text(program.description, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.64f))
            }
            MerchantStatusPill(
                text = if (program.active) stringResource(R.string.merchant_program_active) else stringResource(R.string.merchant_program_disabled),
                backgroundColor = if (program.active) VerevColors.Moss.copy(alpha = 0.16f) else Color(0xFFF3F4F6),
                contentColor = if (program.active) VerevColors.Moss else VerevColors.Inactive,
            )
        }
        Text(
            text = stringResource(R.string.merchant_program_type_format, program.type.displayName()),
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.55f),
        )
        ProgramSummaryCard(program)
        ActiveFeatureChips(program)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProgramQuickAction(
                label = stringResource(R.string.merchant_program_edit_action),
                icon = Icons.Default.Edit,
                onClick = onEdit,
                modifier = Modifier.weight(1f),
            )
            ProgramQuickAction(
                label = stringResource(R.string.merchant_program_delete_action),
                icon = Icons.Default.DeleteOutline,
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                destructive = true,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.merchant_program_enabled_toggle_title), style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
                Text(stringResource(R.string.merchant_program_enabled_toggle_subtitle), style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.58f))
            }
            Switch(checked = program.active, onCheckedChange = onToggleEnabled, enabled = !isBusy)
        }
    }
}

@Composable
private fun ProgramSummaryCard(program: RewardProgram) {
    MerchantPrimaryCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Text(
            text = stringResource(program.type.summaryTitleRes()),
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.56f),
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = program.rulesSummary,
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveFeatureChips(program: RewardProgram) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (program.configuration.earningEnabled) ProgramFeatureChip(stringResource(R.string.merchant_program_feature_earn_points), Icons.Default.Loyalty)
        if (program.configuration.rewardRedemptionEnabled) ProgramFeatureChip(stringResource(R.string.merchant_program_feature_redeem_rewards), Icons.Default.Redeem)
        if (program.configuration.visitCheckInEnabled) ProgramFeatureChip(stringResource(R.string.merchant_program_feature_check_in), Icons.Default.CheckCircle)
        if (program.configuration.cashbackEnabled) ProgramFeatureChip(stringResource(R.string.merchant_program_feature_cashback), Icons.Default.Payments)
        if (program.configuration.tierTrackingEnabled) ProgramFeatureChip(stringResource(R.string.merchant_program_feature_tier_tracking), Icons.Default.AutoGraph)
        if (program.configuration.couponEnabled) ProgramFeatureChip(stringResource(R.string.merchant_program_feature_coupon), Icons.Default.Sell)
        if (program.configuration.purchaseFrequencyEnabled) ProgramFeatureChip(stringResource(R.string.merchant_program_feature_purchase_frequency), Icons.Default.Repeat)
        if (program.configuration.referralEnabled) ProgramFeatureChip(stringResource(R.string.merchant_program_feature_referral), Icons.Default.GroupAdd)
    }
}

@Composable
private fun ProgramFeatureChip(label: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .background(VerevColors.Forest.copy(alpha = 0.06f), RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = VerevColors.Forest, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest)
    }
}

@Composable
private fun ProgramQuickAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (destructive) Color(0xFFD94B4B) else VerevColors.Forest),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
internal fun RewardsPreviewSection(rewards: List<Reward>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_rewards_preview_section))
        rewards.take(3).forEach { reward ->
            MerchantPrimaryCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProgramIcon(icon = Icons.Default.CardGiftcard, colors = listOf(VerevColors.Moss, VerevColors.Forest))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(reward.name, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
                        Text(reward.rewardType.displayName(), style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.6f))
                    }
                    MerchantStatusPill(
                        text = stringResource(R.string.merchant_points_required_format, reward.pointsRequired),
                        backgroundColor = VerevColors.Gold.copy(alpha = 0.16f),
                        contentColor = VerevColors.Gold,
                    )
                }
            }
        }
    }
}

@Composable
internal fun CampaignPreviewSection(campaigns: List<Campaign>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_campaigns_preview_section))
        campaigns.take(3).forEach { campaign ->
            MerchantPrimaryCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProgramIcon(icon = Icons.Default.Campaign, colors = listOf(VerevColors.Tan, VerevColors.Gold))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(campaign.name, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
                        Text(campaign.target.description, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.6f))
                    }
                    MerchantStatusPill(
                        text = if (campaign.active) stringResource(R.string.merchant_program_active) else stringResource(R.string.merchant_program_disabled),
                        backgroundColor = if (campaign.active) VerevColors.Moss.copy(alpha = 0.16f) else Color(0xFFF3F4F6),
                        contentColor = if (campaign.active) VerevColors.Moss else VerevColors.Inactive,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgramOverviewMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.5f))
        Spacer(Modifier.size(8.dp))
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProgramIcon(icon: ImageVector, colors: List<Color>) {
    Row(
        modifier = Modifier
            .size(52.dp)
            .background(Brush.linearGradient(colors), RoundedCornerShape(16.dp)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProgramEditorSheet(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeChange: (LoyaltyProgramType) -> Unit,
    onActiveChanged: (Boolean) -> Unit,
    onPointsSpendStepAmountChange: (String) -> Unit,
    onPointsAwardedPerStepChange: (String) -> Unit,
    onPointsWelcomeBonusChange: (String) -> Unit,
    onPointsMinimumRedeemChange: (String) -> Unit,
    onCashbackPercentChange: (String) -> Unit,
    onCashbackMinimumSpendAmountChange: (String) -> Unit,
    onTierSilverThresholdChange: (String) -> Unit,
    onTierGoldThresholdChange: (String) -> Unit,
    onTierVipThresholdChange: (String) -> Unit,
    onTierBonusPercentChange: (String) -> Unit,
    onCouponNameChange: (String) -> Unit,
    onCouponPointsCostChange: (String) -> Unit,
    onCouponDiscountAmountChange: (String) -> Unit,
    onCouponMinimumSpendAmountChange: (String) -> Unit,
    onCheckInVisitsRequiredChange: (String) -> Unit,
    onCheckInRewardPointsChange: (String) -> Unit,
    onCheckInRewardNameChange: (String) -> Unit,
    onPurchaseFrequencyCountChange: (String) -> Unit,
    onPurchaseFrequencyWindowDaysChange: (String) -> Unit,
    onPurchaseFrequencyRewardPointsChange: (String) -> Unit,
    onPurchaseFrequencyRewardNameChange: (String) -> Unit,
    onReferralReferrerRewardPointsChange: (String) -> Unit,
    onReferralRefereeRewardPointsChange: (String) -> Unit,
    onReferralCodePrefixChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (editorState.isEditing) stringResource(R.string.merchant_program_editor_edit_title) else stringResource(R.string.merchant_program_editor_create_title),
                style = MaterialTheme.typography.headlineSmall,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            ProgramTypeSelector(selectedType = editorState.type, onTypeSelected = onTypeChange)
            ProgramEditorPreviewCard(editorState = editorState)
            MerchantFormField(
                value = editorState.name,
                onValueChange = onNameChange,
                label = stringResource(R.string.merchant_program_form_name),
                leadingIcon = Icons.Default.Loyalty,
                isError = fieldErrors.containsKey(PROGRAM_FIELD_NAME),
                errorText = fieldErrors[PROGRAM_FIELD_NAME]?.let { stringResource(it) },
                supportingText = stringResource(R.string.merchant_program_form_name_supporting),
            )
            MerchantFormField(
                value = editorState.description,
                onValueChange = onDescriptionChange,
                label = stringResource(R.string.merchant_program_form_description),
                leadingIcon = Icons.Default.Storefront,
                isError = fieldErrors.containsKey(PROGRAM_FIELD_DESCRIPTION),
                errorText = fieldErrors[PROGRAM_FIELD_DESCRIPTION]?.let { stringResource(it) },
                supportingText = stringResource(R.string.merchant_program_form_description_supporting),
            )
            ProgramToggleRow(
                title = stringResource(R.string.merchant_program_form_enabled),
                subtitle = stringResource(R.string.merchant_program_enabled_toggle_subtitle),
                checked = editorState.active,
                onCheckedChange = onActiveChanged,
            )
            ProgramRuleFields(
                editorState = editorState,
                fieldErrors = fieldErrors,
                onPointsSpendStepAmountChange = onPointsSpendStepAmountChange,
                onPointsAwardedPerStepChange = onPointsAwardedPerStepChange,
                onPointsWelcomeBonusChange = onPointsWelcomeBonusChange,
                onPointsMinimumRedeemChange = onPointsMinimumRedeemChange,
                onCashbackPercentChange = onCashbackPercentChange,
                onCashbackMinimumSpendAmountChange = onCashbackMinimumSpendAmountChange,
                onTierSilverThresholdChange = onTierSilverThresholdChange,
                onTierGoldThresholdChange = onTierGoldThresholdChange,
                onTierVipThresholdChange = onTierVipThresholdChange,
                onTierBonusPercentChange = onTierBonusPercentChange,
                onCouponNameChange = onCouponNameChange,
                onCouponPointsCostChange = onCouponPointsCostChange,
                onCouponDiscountAmountChange = onCouponDiscountAmountChange,
                onCouponMinimumSpendAmountChange = onCouponMinimumSpendAmountChange,
                onCheckInVisitsRequiredChange = onCheckInVisitsRequiredChange,
                onCheckInRewardPointsChange = onCheckInRewardPointsChange,
                onCheckInRewardNameChange = onCheckInRewardNameChange,
                onPurchaseFrequencyCountChange = onPurchaseFrequencyCountChange,
                onPurchaseFrequencyWindowDaysChange = onPurchaseFrequencyWindowDaysChange,
                onPurchaseFrequencyRewardPointsChange = onPurchaseFrequencyRewardPointsChange,
                onPurchaseFrequencyRewardNameChange = onPurchaseFrequencyRewardNameChange,
                onReferralReferrerRewardPointsChange = onReferralReferrerRewardPointsChange,
                onReferralRefereeRewardPointsChange = onReferralRefereeRewardPointsChange,
                onReferralCodePrefixChange = onReferralCodePrefixChange,
            )
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Forest, contentColor = Color.White),
            ) {
                Text(if (editorState.isEditing) stringResource(R.string.merchant_program_save_changes) else stringResource(R.string.merchant_program_create_submit))
            }
        }
    }
}

@Composable
private fun ProgramEditorPreviewCard(editorState: ProgramEditorState) {
    MerchantPrimaryCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProgramIcon(icon = editorState.type.icon(), colors = editorState.type.gradient())
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (editorState.name.isBlank()) editorState.type.displayName() else editorState.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(editorState.type.templateSubtitleRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.64f),
                )
            }
        }
        MerchantStatusPill(
            text = if (editorState.active) stringResource(R.string.merchant_program_active) else stringResource(R.string.merchant_program_disabled),
            backgroundColor = if (editorState.active) VerevColors.Moss.copy(alpha = 0.16f) else Color(0xFFF3F4F6),
            contentColor = if (editorState.active) VerevColors.Moss else VerevColors.Inactive,
        )
    }
}

@Composable
private fun ProgramRuleFields(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    onPointsSpendStepAmountChange: (String) -> Unit,
    onPointsAwardedPerStepChange: (String) -> Unit,
    onPointsWelcomeBonusChange: (String) -> Unit,
    onPointsMinimumRedeemChange: (String) -> Unit,
    onCashbackPercentChange: (String) -> Unit,
    onCashbackMinimumSpendAmountChange: (String) -> Unit,
    onTierSilverThresholdChange: (String) -> Unit,
    onTierGoldThresholdChange: (String) -> Unit,
    onTierVipThresholdChange: (String) -> Unit,
    onTierBonusPercentChange: (String) -> Unit,
    onCouponNameChange: (String) -> Unit,
    onCouponPointsCostChange: (String) -> Unit,
    onCouponDiscountAmountChange: (String) -> Unit,
    onCouponMinimumSpendAmountChange: (String) -> Unit,
    onCheckInVisitsRequiredChange: (String) -> Unit,
    onCheckInRewardPointsChange: (String) -> Unit,
    onCheckInRewardNameChange: (String) -> Unit,
    onPurchaseFrequencyCountChange: (String) -> Unit,
    onPurchaseFrequencyWindowDaysChange: (String) -> Unit,
    onPurchaseFrequencyRewardPointsChange: (String) -> Unit,
    onPurchaseFrequencyRewardNameChange: (String) -> Unit,
    onReferralReferrerRewardPointsChange: (String) -> Unit,
    onReferralRefereeRewardPointsChange: (String) -> Unit,
    onReferralCodePrefixChange: (String) -> Unit,
) {
    when (editorState.type) {
        LoyaltyProgramType.POINTS -> {
            ProgramSectionCard(
                title = stringResource(R.string.merchant_program_editor_earn_rules_title),
                subtitle = stringResource(R.string.merchant_program_editor_earn_rules_subtitle),
            ) {
                IntegerField(editorState.pointsSpendStepAmount, onPointsSpendStepAmountChange, stringResource(R.string.merchant_program_form_points_step_amount), Icons.Default.Payments, fieldErrors[PROGRAM_FIELD_POINTS_STEP], stringResource(R.string.merchant_program_form_points_step_amount_supporting))
                IntegerField(editorState.pointsAwardedPerStep, onPointsAwardedPerStepChange, stringResource(R.string.merchant_program_form_points_awarded), Icons.Default.Star, fieldErrors[PROGRAM_FIELD_POINTS_AWARDED], stringResource(R.string.merchant_program_form_points_awarded_supporting))
                IntegerField(editorState.pointsWelcomeBonus, onPointsWelcomeBonusChange, stringResource(R.string.merchant_program_form_points_welcome_bonus), Icons.Default.Add, null, stringResource(R.string.merchant_program_form_points_welcome_bonus_supporting))
            }
            ProgramSectionCard(
                title = stringResource(R.string.merchant_program_editor_redemption_rules_title),
                subtitle = stringResource(R.string.merchant_program_editor_redemption_rules_subtitle),
            ) {
                IntegerField(editorState.pointsMinimumRedeem, onPointsMinimumRedeemChange, stringResource(R.string.merchant_program_form_points_minimum_redeem), Icons.Default.Redeem, fieldErrors[PROGRAM_FIELD_POINTS_REDEEM], stringResource(R.string.merchant_program_form_points_minimum_redeem_supporting))
            }
        }
        LoyaltyProgramType.CASHBACK -> ProgramSectionCard(
            title = stringResource(R.string.merchant_program_section_cashback),
            subtitle = stringResource(R.string.merchant_program_editor_cashback_subtitle),
        ) {
            DecimalField(editorState.cashbackPercent, onCashbackPercentChange, stringResource(R.string.merchant_program_form_cashback_percent), Icons.Default.Percent, fieldErrors[PROGRAM_FIELD_CASHBACK_PERCENT], stringResource(R.string.merchant_program_form_cashback_percent_supporting))
            DecimalField(editorState.cashbackMinimumSpendAmount, onCashbackMinimumSpendAmountChange, stringResource(R.string.merchant_program_form_cashback_minimum_spend), Icons.Default.Payments, null, stringResource(R.string.merchant_program_form_cashback_minimum_spend_supporting))
        }
        LoyaltyProgramType.DIGITAL_STAMP -> {
            ProgramSectionCard(
                title = stringResource(R.string.merchant_program_editor_visit_goal_title),
                subtitle = stringResource(R.string.merchant_program_editor_visit_goal_subtitle),
            ) {
                IntegerField(editorState.checkInVisitsRequired, onCheckInVisitsRequiredChange, stringResource(R.string.merchant_program_form_checkin_visits), Icons.Default.CheckCircle, fieldErrors[PROGRAM_FIELD_CHECKIN_VISITS], stringResource(R.string.merchant_program_form_checkin_visits_supporting))
            }
            ProgramSectionCard(
                title = stringResource(R.string.merchant_program_editor_reward_title),
                subtitle = stringResource(R.string.merchant_program_editor_reward_subtitle),
            ) {
                IntegerField(editorState.checkInRewardPoints, onCheckInRewardPointsChange, stringResource(R.string.merchant_program_form_checkin_reward_points), Icons.Default.Star, fieldErrors[PROGRAM_FIELD_CHECKIN_REWARD], stringResource(R.string.merchant_program_form_checkin_reward_points_supporting))
                TextField(editorState.checkInRewardName, onCheckInRewardNameChange, stringResource(R.string.merchant_program_form_checkin_reward_name), Icons.Default.Tag, null, stringResource(R.string.merchant_program_form_checkin_reward_name_supporting))
            }
        }
        LoyaltyProgramType.TIER -> {
            ProgramSectionCard(
                title = stringResource(R.string.merchant_program_editor_thresholds_title),
                subtitle = stringResource(R.string.merchant_program_editor_thresholds_subtitle),
            ) {
                IntegerField(editorState.tierSilverThreshold, onTierSilverThresholdChange, stringResource(R.string.merchant_program_form_tier_silver), Icons.Default.Star, fieldErrors[PROGRAM_FIELD_TIER_SILVER], stringResource(R.string.merchant_program_form_tier_silver_supporting))
                IntegerField(editorState.tierGoldThreshold, onTierGoldThresholdChange, stringResource(R.string.merchant_program_form_tier_gold), Icons.Default.Star, fieldErrors[PROGRAM_FIELD_TIER_GOLD], stringResource(R.string.merchant_program_form_tier_gold_supporting))
                IntegerField(editorState.tierVipThreshold, onTierVipThresholdChange, stringResource(R.string.merchant_program_form_tier_vip), Icons.Default.Star, fieldErrors[PROGRAM_FIELD_TIER_VIP], stringResource(R.string.merchant_program_form_tier_vip_supporting))
            }
            ProgramSectionCard(
                title = stringResource(R.string.merchant_program_editor_member_benefits_title),
                subtitle = stringResource(R.string.merchant_program_editor_member_benefits_subtitle),
            ) {
                IntegerField(editorState.tierBonusPercent, onTierBonusPercentChange, stringResource(R.string.merchant_program_form_tier_bonus_percent), Icons.Default.Percent, null, stringResource(R.string.merchant_program_form_tier_bonus_percent_supporting))
            }
        }
        LoyaltyProgramType.COUPON -> {
            ProgramSectionCard(
                title = stringResource(R.string.merchant_program_editor_offer_details_title),
                subtitle = stringResource(R.string.merchant_program_editor_offer_details_subtitle),
            ) {
                TextField(editorState.couponName, onCouponNameChange, stringResource(R.string.merchant_program_form_coupon_name), Icons.Default.Sell, fieldErrors[PROGRAM_FIELD_COUPON_NAME], stringResource(R.string.merchant_program_form_coupon_name_supporting))
                DecimalField(editorState.couponDiscountAmount, onCouponDiscountAmountChange, stringResource(R.string.merchant_program_form_coupon_discount_amount), Icons.Default.Payments, fieldErrors[PROGRAM_FIELD_COUPON_DISCOUNT], stringResource(R.string.merchant_program_form_coupon_discount_amount_supporting))
            }
            ProgramSectionCard(
                title = stringResource(R.string.merchant_program_editor_redemption_rules_title),
                subtitle = stringResource(R.string.merchant_program_editor_coupon_redeem_subtitle),
            ) {
                IntegerField(editorState.couponPointsCost, onCouponPointsCostChange, stringResource(R.string.merchant_program_form_coupon_points_cost), Icons.Default.Redeem, fieldErrors[PROGRAM_FIELD_COUPON_POINTS], stringResource(R.string.merchant_program_form_coupon_points_cost_supporting))
                DecimalField(editorState.couponMinimumSpendAmount, onCouponMinimumSpendAmountChange, stringResource(R.string.merchant_program_form_coupon_minimum_spend), Icons.Default.Payments, null, stringResource(R.string.merchant_program_form_coupon_minimum_spend_supporting))
            }
        }
        LoyaltyProgramType.PURCHASE_FREQUENCY -> {
            ProgramSectionCard(
                title = stringResource(R.string.merchant_program_editor_trigger_rules_title),
                subtitle = stringResource(R.string.merchant_program_editor_trigger_rules_subtitle),
            ) {
                IntegerField(editorState.purchaseFrequencyCount, onPurchaseFrequencyCountChange, stringResource(R.string.merchant_program_form_frequency_count), Icons.Default.Repeat, fieldErrors[PROGRAM_FIELD_FREQUENCY_COUNT], stringResource(R.string.merchant_program_form_frequency_count_supporting))
                IntegerField(editorState.purchaseFrequencyWindowDays, onPurchaseFrequencyWindowDaysChange, stringResource(R.string.merchant_program_form_frequency_window_days), Icons.Default.Tune, fieldErrors[PROGRAM_FIELD_FREQUENCY_WINDOW], stringResource(R.string.merchant_program_form_frequency_window_days_supporting))
            }
            ProgramSectionCard(
                title = stringResource(R.string.merchant_program_editor_reward_title),
                subtitle = stringResource(R.string.merchant_program_editor_reward_subtitle),
            ) {
                IntegerField(editorState.purchaseFrequencyRewardPoints, onPurchaseFrequencyRewardPointsChange, stringResource(R.string.merchant_program_form_frequency_reward_points), Icons.Default.Star, fieldErrors[PROGRAM_FIELD_FREQUENCY_REWARD], stringResource(R.string.merchant_program_form_frequency_reward_points_supporting))
                TextField(editorState.purchaseFrequencyRewardName, onPurchaseFrequencyRewardNameChange, stringResource(R.string.merchant_program_form_frequency_reward_name), Icons.Default.Tag, null, stringResource(R.string.merchant_program_form_frequency_reward_name_supporting))
            }
        }
        LoyaltyProgramType.REFERRAL -> {
            ProgramSectionCard(
                title = stringResource(R.string.merchant_program_editor_referral_rewards_title),
                subtitle = stringResource(R.string.merchant_program_editor_referral_rewards_subtitle),
            ) {
                IntegerField(editorState.referralReferrerRewardPoints, onReferralReferrerRewardPointsChange, stringResource(R.string.merchant_program_form_referral_referrer_points), Icons.Default.GroupAdd, fieldErrors[PROGRAM_FIELD_REFERRAL_REFERRER], stringResource(R.string.merchant_program_form_referral_referrer_points_supporting))
                IntegerField(editorState.referralRefereeRewardPoints, onReferralRefereeRewardPointsChange, stringResource(R.string.merchant_program_form_referral_referee_points), Icons.Default.GroupAdd, fieldErrors[PROGRAM_FIELD_REFERRAL_REFEREE], stringResource(R.string.merchant_program_form_referral_referee_points_supporting))
            }
            ProgramSectionCard(
                title = stringResource(R.string.merchant_program_editor_invite_code_title),
                subtitle = stringResource(R.string.merchant_program_editor_invite_code_subtitle),
            ) {
                TextField(editorState.referralCodePrefix, onReferralCodePrefixChange, stringResource(R.string.merchant_program_form_referral_prefix), Icons.Default.Tag, fieldErrors[PROGRAM_FIELD_REFERRAL_PREFIX], stringResource(R.string.merchant_program_form_referral_prefix_supporting))
            }
        }
        LoyaltyProgramType.HYBRID -> {
            ProgramSectionCard(title = stringResource(R.string.merchant_program_section_points), subtitle = stringResource(R.string.merchant_program_editor_earn_rules_subtitle)) {
                IntegerField(editorState.pointsSpendStepAmount, onPointsSpendStepAmountChange, stringResource(R.string.merchant_program_form_points_step_amount), Icons.Default.Payments, fieldErrors[PROGRAM_FIELD_POINTS_STEP], null)
                IntegerField(editorState.pointsAwardedPerStep, onPointsAwardedPerStepChange, stringResource(R.string.merchant_program_form_points_awarded), Icons.Default.Star, fieldErrors[PROGRAM_FIELD_POINTS_AWARDED], null)
            }
            ProgramSectionCard(title = stringResource(R.string.merchant_program_section_cashback), subtitle = stringResource(R.string.merchant_program_editor_cashback_subtitle)) {
                DecimalField(editorState.cashbackPercent, onCashbackPercentChange, stringResource(R.string.merchant_program_form_cashback_percent), Icons.Default.Percent, null, null)
            }
            ProgramSectionCard(title = stringResource(R.string.merchant_program_section_check_in), subtitle = stringResource(R.string.merchant_program_editor_visit_goal_subtitle)) {
                IntegerField(editorState.checkInVisitsRequired, onCheckInVisitsRequiredChange, stringResource(R.string.merchant_program_form_checkin_visits), Icons.Default.CheckCircle, fieldErrors[PROGRAM_FIELD_CHECKIN_VISITS], null)
                IntegerField(editorState.checkInRewardPoints, onCheckInRewardPointsChange, stringResource(R.string.merchant_program_form_checkin_reward_points), Icons.Default.Star, null, null)
            }
            ProgramSectionCard(title = stringResource(R.string.merchant_program_section_referral), subtitle = stringResource(R.string.merchant_program_editor_referral_rewards_subtitle)) {
                IntegerField(editorState.referralReferrerRewardPoints, onReferralReferrerRewardPointsChange, stringResource(R.string.merchant_program_form_referral_referrer_points), Icons.Default.GroupAdd, fieldErrors[PROGRAM_FIELD_REFERRAL_REFERRER], null)
                IntegerField(editorState.referralRefereeRewardPoints, onReferralRefereeRewardPointsChange, stringResource(R.string.merchant_program_form_referral_referee_points), Icons.Default.GroupAdd, null, null)
            }
        }
    }
}

@Composable
private fun ProgramSectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    MerchantPrimaryCard {
        Text(title, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.62f))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
    }
}

@Composable
private fun TextField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, errorRes: Int?, supportingText: String?) {
    MerchantFormField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        leadingIcon = icon,
        isError = errorRes != null,
        errorText = errorRes?.let { stringResource(it) },
        supportingText = supportingText,
    )
}

@Composable
private fun IntegerField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, errorRes: Int?, supportingText: String?) {
    MerchantFormField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        leadingIcon = icon,
        isError = errorRes != null,
        errorText = errorRes?.let { stringResource(it) },
        supportingText = supportingText,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
private fun DecimalField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, errorRes: Int?, supportingText: String?) {
    MerchantFormField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        leadingIcon = icon,
        isError = errorRes != null,
        errorText = errorRes?.let { stringResource(it) },
        supportingText = supportingText,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

@Composable
private fun ProgramToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VerevColors.AppBackground, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.58f))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ProgramTypeSelector(selectedType: LoyaltyProgramType, onTypeSelected: (LoyaltyProgramType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.merchant_program_form_type),
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.58f),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(LoyaltyProgramType.entries) { type ->
                MerchantFilterChip(text = type.displayName(), selected = selectedType == type, onClick = { onTypeSelected(type) })
            }
        }
    }
}

@Composable
internal fun ProgramDeleteDialog(programName: String, isSubmitting: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.merchant_program_delete_title), color = VerevColors.Forest, fontWeight = FontWeight.SemiBold) },
        text = { Text(stringResource(R.string.merchant_program_delete_message, programName), color = VerevColors.Forest.copy(alpha = 0.72f)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD94B4B), contentColor = Color.White),
            ) { Text(stringResource(R.string.merchant_program_delete_confirm)) }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss, enabled = !isSubmitting) { Text(stringResource(R.string.merchant_program_delete_cancel)) } },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp),
    )
}
