package com.vector.verevcodex.presentation.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.vector.verevcodex.domain.model.Campaign
import com.vector.verevcodex.domain.model.LoyaltyProgramType
import com.vector.verevcodex.domain.model.Reward
import com.vector.verevcodex.domain.model.RewardProgram
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
    val totalActions = programs.filter { it.active }.sumOf { it.configuration.scanActions.size }
    MerchantPrimaryCard {
        Text(
            text = stringResource(R.string.merchant_programs_overview),
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProgramOverviewMetric(
                label = stringResource(R.string.merchant_metric_programs),
                value = formatCompactCount(enabledPrograms),
                modifier = Modifier.weight(1f),
            )
            ProgramOverviewMetric(
                label = stringResource(R.string.merchant_metric_rewards),
                value = formatCompactCount(rewards.size),
                modifier = Modifier.weight(1f),
            )
            ProgramOverviewMetric(
                label = stringResource(R.string.merchant_metric_campaigns),
                value = formatCompactCount(campaigns.size),
                modifier = Modifier.weight(1f),
            )
        }
        HorizontalDivider(color = VerevColors.AppBackground)
        Text(
            text = stringResource(R.string.merchant_programs_actions_summary, totalActions),
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest.copy(alpha = 0.72f),
        )
    }
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
                Text(
                    text = program.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = program.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.64f),
                )
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
        ActiveFeatureChips(program = program)
        Text(
            text = program.rulesSummary,
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest.copy(alpha = 0.78f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
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
                Text(
                    text = stringResource(R.string.merchant_program_enabled_toggle_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.merchant_program_enabled_toggle_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.58f),
                )
            }
            Switch(
                checked = program.active,
                onCheckedChange = onToggleEnabled,
                enabled = !isBusy,
            )
        }
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
        if (program.configuration.rewardRedemptionEnabled) ProgramFeatureChip(stringResource(R.string.merchant_program_feature_redeem_rewards), Icons.Default.CardGiftcard)
        if (program.configuration.visitCheckInEnabled) ProgramFeatureChip(stringResource(R.string.merchant_program_feature_check_in), Icons.Default.CheckCircle)
        if (program.configuration.cashbackEnabled) ProgramFeatureChip(stringResource(R.string.merchant_program_feature_cashback), Icons.Default.Payments)
        if (program.configuration.tierTrackingEnabled) ProgramFeatureChip(stringResource(R.string.merchant_program_feature_tier_tracking), Icons.Default.AutoGraph)
    }
}

@Composable
private fun ProgramFeatureChip(label: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(VerevColors.Forest.copy(alpha = 0.06f))
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
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (destructive) Color(0xFFD94B4B) else VerevColors.Forest,
        ),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ProgramIcon(icon = Icons.Default.CardGiftcard, colors = listOf(VerevColors.Moss, VerevColors.Forest))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(reward.name, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.size(2.dp))
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ProgramIcon(icon = Icons.Default.Campaign, colors = listOf(VerevColors.Tan, VerevColors.Gold))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(campaign.name, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.size(2.dp))
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
private fun ProgramOverviewMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.5f))
        Spacer(Modifier.size(8.dp))
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProgramIcon(icon: ImageVector, colors: List<Color>) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProgramEditorSheet(
    editorState: ProgramEditorState,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeChange: (LoyaltyProgramType) -> Unit,
    onRulesSummaryChange: (String) -> Unit,
    onActiveChanged: (Boolean) -> Unit,
    onEarningChanged: (Boolean) -> Unit,
    onRewardRedemptionChanged: (Boolean) -> Unit,
    onVisitCheckInChanged: (Boolean) -> Unit,
    onCashbackChanged: (Boolean) -> Unit,
    onTierTrackingChanged: (Boolean) -> Unit,
    onSave: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (editorState.isEditing) {
                    stringResource(R.string.merchant_program_editor_edit_title)
                } else {
                    stringResource(R.string.merchant_program_editor_create_title)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            ProgramTypeSelector(selectedType = editorState.type, onTypeSelected = onTypeChange)
            MerchantFormField(
                value = editorState.name,
                onValueChange = onNameChange,
                label = stringResource(R.string.merchant_program_form_name),
                leadingIcon = Icons.Default.Loyalty,
            )
            MerchantFormField(
                value = editorState.description,
                onValueChange = onDescriptionChange,
                label = stringResource(R.string.merchant_program_form_description),
                leadingIcon = Icons.Default.Storefront,
            )
            MerchantFormField(
                value = editorState.rulesSummary,
                onValueChange = onRulesSummaryChange,
                label = stringResource(R.string.merchant_program_form_rules_summary),
                leadingIcon = Icons.Default.Visibility,
                singleLine = false,
            )
            ProgramToggleRow(
                title = stringResource(R.string.merchant_program_form_enabled),
                subtitle = stringResource(R.string.merchant_program_enabled_toggle_subtitle),
                checked = editorState.active,
                onCheckedChange = onActiveChanged,
            )
            MerchantSectionTitle(text = stringResource(R.string.merchant_program_features_section))
            ProgramToggleRow(
                title = stringResource(R.string.merchant_program_feature_earn_points),
                subtitle = stringResource(R.string.merchant_program_feature_earn_points_subtitle),
                checked = editorState.earningEnabled,
                onCheckedChange = onEarningChanged,
            )
            ProgramToggleRow(
                title = stringResource(R.string.merchant_program_feature_redeem_rewards),
                subtitle = stringResource(R.string.merchant_program_feature_redeem_rewards_subtitle),
                checked = editorState.rewardRedemptionEnabled,
                onCheckedChange = onRewardRedemptionChanged,
            )
            ProgramToggleRow(
                title = stringResource(R.string.merchant_program_feature_check_in),
                subtitle = stringResource(R.string.merchant_program_feature_check_in_subtitle),
                checked = editorState.visitCheckInEnabled,
                onCheckedChange = onVisitCheckInChanged,
            )
            ProgramToggleRow(
                title = stringResource(R.string.merchant_program_feature_cashback),
                subtitle = stringResource(R.string.merchant_program_feature_cashback_subtitle),
                checked = editorState.cashbackEnabled,
                onCheckedChange = onCashbackChanged,
            )
            ProgramToggleRow(
                title = stringResource(R.string.merchant_program_feature_tier_tracking),
                subtitle = stringResource(R.string.merchant_program_feature_tier_tracking_subtitle),
                checked = editorState.tierTrackingEnabled,
                onCheckedChange = onTierTrackingChanged,
            )
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Forest, contentColor = Color.White),
            ) {
                Text(
                    text = if (editorState.isEditing) {
                        stringResource(R.string.merchant_program_save_changes)
                    } else {
                        stringResource(R.string.merchant_program_create_submit)
                    },
                )
            }
        }
    }
}

@Composable
private fun ProgramToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(VerevColors.AppBackground)
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
private fun ProgramTypeSelector(
    selectedType: LoyaltyProgramType,
    onTypeSelected: (LoyaltyProgramType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        MerchantPrimaryCard(
            modifier = Modifier.clickable { expanded = true },
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        ) {
            Text(
                text = stringResource(R.string.merchant_program_form_type),
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.58f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedType.displayName(),
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
                Icon(Icons.Default.Edit, contentDescription = null, tint = VerevColors.Gold)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.92f),
        ) {
            LoyaltyProgramType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.displayName()) },
                    onClick = {
                        expanded = false
                        onTypeSelected(type)
                    },
                )
            }
        }
    }
}

@Composable
internal fun ProgramDeleteDialog(
    programName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.merchant_program_delete_title),
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.merchant_program_delete_message, programName),
                color = VerevColors.Forest.copy(alpha = 0.72f),
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD94B4B), contentColor = Color.White),
            ) {
                Text(stringResource(R.string.merchant_program_delete_confirm))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text(stringResource(R.string.merchant_program_delete_cancel))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp),
    )
}

private fun LoyaltyProgramType.gradient(): List<Color> = when (this) {
    LoyaltyProgramType.POINTS -> listOf(VerevColors.Gold, VerevColors.Tan)
    LoyaltyProgramType.CASHBACK -> listOf(VerevColors.Moss, VerevColors.Forest)
    LoyaltyProgramType.DIGITAL_STAMP -> listOf(Color(0xFF7A9CC6), Color(0xFF466B8F))
    LoyaltyProgramType.TIER -> listOf(Color(0xFFB97E4B), Color(0xFF8B5A2B))
    LoyaltyProgramType.HYBRID -> listOf(VerevColors.Gold, VerevColors.Forest)
}

private fun LoyaltyProgramType.icon(): ImageVector = when (this) {
    LoyaltyProgramType.POINTS -> Icons.Default.Loyalty
    LoyaltyProgramType.CASHBACK -> Icons.Default.Payments
    LoyaltyProgramType.DIGITAL_STAMP -> Icons.Default.CheckCircle
    LoyaltyProgramType.TIER -> Icons.Default.AutoGraph
    LoyaltyProgramType.HYBRID -> Icons.Default.Campaign
}
