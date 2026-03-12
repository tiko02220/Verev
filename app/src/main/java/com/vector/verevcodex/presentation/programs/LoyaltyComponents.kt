package com.vector.verevcodex.presentation.programs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.common.sheets.AppBottomSheetDialog
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.merchant_programs_title),
            style = MaterialTheme.typography.headlineMedium,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.merchant_programs_manage_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = VerevColors.Forest.copy(alpha = 0.7f),
        )
    }
}

@Composable
internal fun ProgramsOverviewCard(
    programs: List<RewardProgram>,
    rewards: List<Reward>,
    campaigns: List<Campaign>,
) {
    val enabledPrograms = programs.count { it.active }
    val liveActions = programs.filter { it.active }.sumOf { it.configuration.scanActions.size }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ProgramSectionHeader(
            title = stringResource(R.string.merchant_programs_overview),
            subtitle = stringResource(R.string.merchant_programs_actions_summary, liveActions),
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
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProgramOverviewMetric(
                label = stringResource(R.string.merchant_metric_campaigns),
                value = formatCompactCount(campaigns.size),
                modifier = Modifier.weight(1f),
            )
            ProgramOverviewMetric(
                label = stringResource(R.string.merchant_programs_live_actions_label),
                value = formatCompactCount(liveActions),
                modifier = Modifier.weight(1f),
            )
        }
        Surface(
            color = VerevColors.Gold.copy(alpha = 0.10f),
            shape = RoundedCornerShape(26.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                ProgramIcon(icon = Icons.Default.Tune, colors = listOf(VerevColors.Gold, VerevColors.Tan))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.merchant_program_workflow_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.merchant_programs_actions_summary, liveActions),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VerevColors.Forest.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProgramTemplateSection(
    onCreateProgram: (LoyaltyProgramType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProgramSectionHeader(
            title = stringResource(R.string.merchant_program_templates_section),
            subtitle = stringResource(R.string.merchant_programs_empty_subtitle),
        )
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            LoyaltyProgramType.entries.forEach { type ->
                ProgramTemplateCard(
                    type = type,
                    onClick = { onCreateProgram(type) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ProgramTemplateCard(
    type: LoyaltyProgramType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProgramIcon(icon = type.icon(), colors = type.gradient())
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(type.displayNameRes()),
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(type.templateSubtitleRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.62f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                color = VerevColors.AppBackground,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = VerevColors.Forest.copy(alpha = 0.68f),
                    modifier = Modifier.padding(10.dp),
                )
            }
        }
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
        ProgramManagementTile(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.merchant_rewards_manage_title),
            subtitle = stringResource(R.string.merchant_rewards_manage_subtitle),
            icon = Icons.Default.CardGiftcard,
            colors = listOf(VerevColors.Gold, VerevColors.Tan),
            onClick = onOpenRewards,
        )
        ProgramManagementTile(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.merchant_campaigns_manage_title),
            subtitle = stringResource(R.string.merchant_campaigns_manage_subtitle),
            icon = Icons.Default.Campaign,
            colors = listOf(VerevColors.Moss, VerevColors.Forest),
            onClick = onOpenCampaigns,
        )
    }
}

@Composable
internal fun ProgramsManagementBanner(
    storeName: String,
    onOpenConfiguredPrograms: () -> Unit,
) {
    Surface(
        onClick = onOpenConfiguredPrograms,
        color = VerevColors.Forest.copy(alpha = 0.08f),
        shape = RoundedCornerShape(30.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProgramIcon(icon = Icons.Default.Storefront, colors = listOf(VerevColors.Forest, VerevColors.Moss))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.merchant_programs_management_banner_title, if (storeName.isBlank()) stringResource(R.string.merchant_select_store) else storeName),
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.merchant_programs_overview),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.56f),
                )
            }
        }
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
    onOpenHybridPrograms: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProgramSectionHeader(
            title = stringResource(R.string.merchant_program_modules_title),
            subtitle = stringResource(R.string.merchant_programs_active_section),
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProgramModuleTile(
                title = stringResource(R.string.merchant_points_rewards_title),
                subtitle = stringResource(R.string.merchant_program_module_points_subtitle),
                icon = Icons.Default.Loyalty,
                colors = listOf(VerevColors.Gold, VerevColors.Tan),
                onClick = onOpenPointsRewards,
            )
            ProgramModuleTile(
                title = stringResource(R.string.merchant_tiered_loyalty_title),
                subtitle = stringResource(R.string.merchant_program_module_tier_subtitle),
                icon = Icons.Default.AutoGraph,
                colors = listOf(Color(0xFFB97E4B), Color(0xFF8B5A2B)),
                onClick = onOpenTieredLoyalty,
            )
            ProgramModuleTile(
                title = stringResource(R.string.merchant_coupons_manager_title),
                subtitle = stringResource(R.string.merchant_program_module_coupons_subtitle),
                icon = Icons.Default.Sell,
                colors = listOf(VerevColors.Tan, VerevColors.Gold),
                onClick = onOpenCouponsManager,
            )
            ProgramModuleTile(
                title = stringResource(R.string.merchant_checkin_rewards_title),
                subtitle = stringResource(R.string.merchant_program_module_checkin_subtitle),
                icon = Icons.Default.CheckCircle,
                colors = listOf(Color(0xFF7A9CC6), Color(0xFF466B8F)),
                onClick = onOpenCheckinRewards,
            )
            ProgramModuleTile(
                title = stringResource(R.string.merchant_purchase_frequency_title),
                subtitle = stringResource(R.string.merchant_program_module_frequency_subtitle),
                icon = Icons.Default.Repeat,
                colors = listOf(Color(0xFF5B8DEF), Color(0xFF315EBD)),
                onClick = onOpenPurchaseFrequency,
            )
            ProgramModuleTile(
                title = stringResource(R.string.merchant_referral_rewards_title),
                subtitle = stringResource(R.string.merchant_program_module_referral_subtitle),
                icon = Icons.Default.GroupAdd,
                colors = listOf(Color(0xFF22A06B), Color(0xFF0F7A4A)),
                onClick = onOpenReferralRewards,
            )
            ProgramModuleTile(
                title = stringResource(R.string.merchant_hybrid_programs_title),
                subtitle = stringResource(R.string.merchant_program_module_hybrid_subtitle),
                icon = Icons.Default.Campaign,
                colors = listOf(VerevColors.Gold, VerevColors.Forest),
                onClick = onOpenHybridPrograms,
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
    val visiblePrograms = programs
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProgramSectionHeader(
            title = stringResource(R.string.merchant_programs_active_section),
            subtitle = stringResource(
                R.string.merchant_programs_subtitle,
                visiblePrograms.count { it.active },
                visiblePrograms.size,
            ),
        )
        if (visiblePrograms.isEmpty()) {
            ProgramsModuleEmptyCard(
                title = stringResource(R.string.merchant_programs_empty_title),
                subtitle = stringResource(R.string.merchant_programs_empty_subtitle),
                icon = Icons.Default.Loyalty,
            )
        }
        visiblePrograms.forEach { program ->
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
internal fun ProgramListItem(
    program: RewardProgram,
    isBusy: Boolean,
    onEdit: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    val density = LocalDensity.current
    val revealWidthPx = with(density) { 104.dp.toPx() }
    val defaultShape = RoundedCornerShape(20.dp)
    var swipeOffset by remember(program.id) { mutableFloatStateOf(0f) }
    var dragStartOffset by remember(program.id) { mutableFloatStateOf(0f) }
    var isDragging by remember(program.id) { androidx.compose.runtime.mutableStateOf(false) }
    val animatedOffset by animateFloatAsState(
        targetValue = swipeOffset,
        animationSpec = if (isDragging) tween(durationMillis = 0) else tween(durationMillis = 180),
        label = "programSwipeOffset",
    )
    val swipeProgress = (-animatedOffset / revealWidthPx).coerceIn(0f, 1f)
    val swipeAwareShape = RoundedCornerShape(
        topStart = 20.dp,
        bottomStart = 20.dp,
        topEnd = lerp(20.dp, 0.dp, swipeProgress),
        bottomEnd = lerp(20.dp, 0.dp, swipeProgress),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent, defaultShape),
    ) {
        Row(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFFEF4444), defaultShape)
                .padding(end = 24.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                onClick = onDelete,
                enabled = animatedOffset <= -(revealWidthPx * 0.8f),
                color = Color.Transparent,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.merchant_program_delete_action),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.toInt(), 0) }
                .pointerInput(program.id, isBusy) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            if (!isBusy) {
                                isDragging = true
                                dragStartOffset = swipeOffset
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            if (isBusy) return@detectHorizontalDragGestures
                            swipeOffset = (swipeOffset + dragAmount).coerceIn(-revealWidthPx, 0f)
                        },
                        onDragEnd = {
                            isDragging = false
                            val wasOpen = dragStartOffset <= -(revealWidthPx * 0.85f)
                            val settleThreshold = if (wasOpen) 0.72f else 0.18f
                            swipeOffset = if (swipeOffset <= -(revealWidthPx * settleThreshold)) -revealWidthPx else 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            val wasOpen = dragStartOffset <= -(revealWidthPx * 0.85f)
                            val settleThreshold = if (wasOpen) 0.72f else 0.18f
                            swipeOffset = if (swipeOffset <= -(revealWidthPx * settleThreshold)) -revealWidthPx else 0f
                        },
                    )
                },
            shape = swipeAwareShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Brush.linearGradient(program.type.gradient()), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = program.type.icon(),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(program.type.displayNameRes()),
                            color = VerevColors.Forest.copy(alpha = 0.56f),
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = program.name.ifBlank { stringResource(program.type.displayNameRes()) },
                            color = VerevColors.Forest,
                            fontSize = 15.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                HorizontalDivider(color = VerevColors.Forest.copy(alpha = 0.10f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = onEdit,
                        color = program.type.gradient().first().copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = VerevColors.Forest,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = stringResource(R.string.merchant_program_configure_action),
                                color = VerevColors.Forest,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProgramStatusToggle(
                            checked = program.active,
                            enabled = !isBusy,
                            onCheckedChange = onToggleEnabled,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramStatusToggle(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val trackColor = if (checked) VerevColors.Moss else VerevColors.Forest.copy(alpha = 0.18f)
    val interactionSource = remember { MutableInteractionSource() }
    val toggleShape = RoundedCornerShape(100.dp)
    val knobOffset by animateFloatAsState(
        targetValue = if (checked) 20f else 0f,
        animationSpec = tween(durationMillis = 140),
        label = "programToggleOffset",
    )

    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 24.dp)
            .clip(toggleShape)
            .background(trackColor, toggleShape)
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = interactionSource,
            ) { onCheckedChange(!checked) }
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffset.dp)
                .size(20.dp)
                .background(Color.White, CircleShape),
        )
    }
}

@Composable
private fun ProgramSummaryCard(program: RewardProgram) {
    Surface(
        color = VerevColors.AppBackground.copy(alpha = 0.72f),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
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
            .background(VerevColors.AppBackground.copy(alpha = 0.8f), RoundedCornerShape(100.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, if (destructive) Color(0xFF7A3F3F) else VerevColors.Forest.copy(alpha = 0.38f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (destructive) Color(0xFFD94B4B) else VerevColors.Forest),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Text(text = label, modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun RewardsPreviewSection(rewards: List<Reward>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProgramSectionHeader(
            title = stringResource(R.string.merchant_rewards_preview_section),
            subtitle = stringResource(R.string.merchant_rewards_manage_subtitle),
        )
        rewards.take(3).forEach { reward ->
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(26.dp),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
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
}

@Composable
internal fun CampaignPreviewSection(campaigns: List<Campaign>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProgramSectionHeader(
            title = stringResource(R.string.merchant_campaigns_preview_section),
            subtitle = stringResource(R.string.merchant_campaigns_manage_subtitle),
        )
        campaigns.take(3).forEach { campaign ->
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(26.dp),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
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
}

@Composable
private fun ProgramOverviewMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = VerevColors.AppBackground.copy(alpha = 0.72f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.5f))
            Spacer(Modifier.size(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
        }
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

@Composable
private fun ProgramSectionHeader(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest.copy(alpha = 0.6f),
        )
    }
}

@Composable
internal fun ProgramsModuleEmptyCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ProgramIcon(icon = icon, colors = listOf(VerevColors.Forest, VerevColors.Moss))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.62f),
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProgramManagementTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .height(156.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White,
                            VerevColors.AppBackground,
                        ),
                    ),
                    shape = RoundedCornerShape(28.dp),
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            ProgramIcon(icon = icon, colors = colors)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.62f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ProgramModuleTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProgramIcon(icon = icon, colors = colors)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.62f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                color = VerevColors.AppBackground,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = VerevColors.Forest.copy(alpha = 0.64f),
                    modifier = Modifier.padding(10.dp),
                )
            }
        }
    }
}

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
    AppBottomSheetDialog(
        onDismissRequest = onDismiss,
        contentPadding = PaddingValues(0.dp),
    ) { _, dismissAfter ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 20.dp,
                        bottom = 24.dp,
                    )
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                ProgramEditorHeader(editorState = editorState)
                ProgramTypeSelector(selectedType = editorState.type, onTypeSelected = onTypeChange)
                ProgramEditorPreviewCard(editorState = editorState)
                ProgramSectionCard(
                    title = stringResource(R.string.merchant_program_editor_basics_title),
                    subtitle = stringResource(R.string.merchant_program_editor_basics_subtitle),
                ) {
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
                }
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
                    onClick = { dismissAfter(onSave) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(vertical = 18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Forest, contentColor = Color.White),
                ) {
                    Text(
                        if (editorState.isEditing) stringResource(R.string.merchant_program_save_changes) else stringResource(R.string.merchant_program_create_submit),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgramEditorHeader(editorState: ProgramEditorState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = if (editorState.isEditing) stringResource(R.string.merchant_program_editor_edit_title) else stringResource(R.string.merchant_program_editor_create_title),
            style = MaterialTheme.typography.displayMedium,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(editorState.type.templateSubtitleRes()),
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest.copy(alpha = 0.66f),
        )
    }
}

@Composable
private fun ProgramEditorPreviewCard(editorState: ProgramEditorState) {
    Surface(
        color = VerevColors.Forest.copy(alpha = 0.08f),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProgramIcon(icon = editorState.type.icon(), colors = editorState.type.gradient())
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (editorState.name.isBlank()) stringResource(editorState.type.displayNameRes()) else editorState.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(editorState.type.summaryTitleRes()),
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
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(30.dp),
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.62f))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
        }
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
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (selectedType == type) VerevColors.Forest else VerevColors.AppBackground,
                    modifier = Modifier.clickable { onTypeSelected(type) },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = type.icon(),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (selectedType == type) Color.White else VerevColors.Forest,
                        )
                        Text(
                            text = stringResource(type.displayNameRes()),
                            color = if (selectedType == type) Color.White else VerevColors.Forest,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
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
