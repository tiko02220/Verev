package com.vector.verevcodex.presentation.programs

import android.app.DatePickerDialog
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import com.vector.verevcodex.common.input.sanitizeDecimalInput
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.loyalty.ProgramBenefitResetType
import com.vector.verevcodex.domain.model.loyalty.ProgramRewardOutcomeType
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.model.loyalty.TierBenefitType
import com.vector.verevcodex.domain.model.loyalty.TierThresholdBasis
import com.vector.verevcodex.domain.model.loyalty.impliedProgramType
import com.vector.verevcodex.domain.model.loyalty.impliedRewardType
import com.vector.verevcodex.domain.model.loyalty.usesProgramBenefit
import com.vector.verevcodex.domain.model.loyalty.usesRewardItem
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantInlineToggle
import com.vector.verevcodex.presentation.merchant.common.MerchantSkeletonBlock
import com.vector.verevcodex.presentation.merchant.common.MerchantSkeletonCard
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.common.sheets.AppBottomSheetDialog
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency
import com.vector.verevcodex.presentation.theme.VerevColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class ProgramCreationStep(val titleRes: Int) {
    BASICS(R.string.merchant_program_step_basics),
    GOAL(R.string.merchant_program_step_goal),
    AUDIENCE(R.string.merchant_program_step_audience),
    AVAILABILITY(R.string.merchant_program_step_availability),
    REVIEW(R.string.merchant_program_step_review),
}

private fun ProgramCreationStep.scopedErrors(fieldErrors: Map<String, Int>): Map<String, Int> {
    val allowedKeys = when (this) {
        ProgramCreationStep.BASICS -> setOf(
            PROGRAM_FIELD_NAME,
            PROGRAM_FIELD_DESCRIPTION,
        )
        ProgramCreationStep.GOAL -> setOf(
            PROGRAM_FIELD_POINTS_STEP,
            PROGRAM_FIELD_POINTS_AWARDED,
            PROGRAM_FIELD_POINTS_REDEEM,
            PROGRAM_FIELD_CASHBACK_PERCENT,
            PROGRAM_FIELD_TIER_SILVER,
            PROGRAM_FIELD_COUPON_NAME,
            PROGRAM_FIELD_COUPON_POINTS,
            PROGRAM_FIELD_COUPON_DISCOUNT,
            PROGRAM_FIELD_CHECKIN_VISITS,
            PROGRAM_FIELD_CHECKIN_REWARD,
            PROGRAM_FIELD_FREQUENCY_COUNT,
            PROGRAM_FIELD_FREQUENCY_WINDOW,
            PROGRAM_FIELD_FREQUENCY_REWARD,
            PROGRAM_FIELD_REFERRAL_REFERRER,
            PROGRAM_FIELD_REFERRAL_REFEREE,
            PROGRAM_FIELD_REFERRAL_PREFIX,
        )
        ProgramCreationStep.AUDIENCE -> setOf(
            PROGRAM_FIELD_TARGET_AGE_MIN,
            PROGRAM_FIELD_TARGET_AGE_MAX,
        )
        ProgramCreationStep.AVAILABILITY -> setOf(
            PROGRAM_FIELD_SCHEDULE_START,
            PROGRAM_FIELD_SCHEDULE_END,
            PROGRAM_FIELD_BENEFIT_RESET_CUSTOM_DAYS,
        )
        ProgramCreationStep.REVIEW -> fieldErrors.keys
    }
    return fieldErrors.filterKeys { key ->
        key in allowedKeys || (this == ProgramCreationStep.GOAL && key.startsWith("tier_level_"))
    }
}

@Composable
internal fun ProgramsHeader(
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
internal fun ProgramsListSkeleton(
    canManagePrograms: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(3) {
            MerchantSkeletonCard(shape = RoundedCornerShape(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    MerchantSkeletonBlock(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(16.dp),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MerchantSkeletonBlock(
                            modifier = Modifier.fillMaxWidth(0.28f).height(12.dp),
                        )
                        MerchantSkeletonBlock(
                            modifier = Modifier.fillMaxWidth(0.52f).height(18.dp),
                        )
                        MerchantSkeletonBlock(
                            modifier = Modifier.fillMaxWidth(0.7f).height(14.dp),
                        )
                    }
                    MerchantSkeletonBlock(
                        modifier = Modifier.fillMaxWidth(0.16f).height(28.dp),
                        shape = RoundedCornerShape(999.dp),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MerchantSkeletonBlock(
                        modifier = Modifier.fillMaxWidth(0.26f).height(14.dp),
                    )
                    if (canManagePrograms) {
                        MerchantSkeletonBlock(
                            modifier = Modifier.size(width = 52.dp, height = 28.dp),
                            shape = RoundedCornerShape(999.dp),
                        )
                    }
                }
            }
        }
        if (canManagePrograms) {
            MerchantSkeletonCard(shape = RoundedCornerShape(20.dp)) {
                MerchantSkeletonBlock(
                    modifier = Modifier.fillMaxWidth().height(24.dp),
                    shape = RoundedCornerShape(14.dp),
                )
            }
        }
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
    onOpenProgramInfo: (LoyaltyProgramType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProgramModuleTile(
                title = stringResource(R.string.merchant_points_rewards_title),
                subtitle = stringResource(R.string.merchant_program_module_points_subtitle),
                icon = Icons.Default.Loyalty,
                colors = listOf(VerevColors.Gold, VerevColors.Tan),
                onClick = onOpenPointsRewards,
                onOpenInfo = { onOpenProgramInfo(LoyaltyProgramType.POINTS) },
            )
            ProgramModuleTile(
                title = stringResource(R.string.merchant_tiered_loyalty_title),
                subtitle = stringResource(R.string.merchant_program_module_tier_subtitle),
                icon = Icons.Default.AutoGraph,
                colors = listOf(Color(0xFFB97E4B), Color(0xFF8B5A2B)),
                onClick = onOpenTieredLoyalty,
                onOpenInfo = { onOpenProgramInfo(LoyaltyProgramType.TIER) },
            )
            ProgramModuleTile(
                title = stringResource(R.string.merchant_coupons_manager_title),
                subtitle = stringResource(R.string.merchant_program_module_coupons_subtitle),
                icon = Icons.Default.Sell,
                colors = listOf(VerevColors.Tan, VerevColors.Gold),
                onClick = onOpenCouponsManager,
                onOpenInfo = { onOpenProgramInfo(LoyaltyProgramType.COUPON) },
            )
            ProgramModuleTile(
                title = stringResource(R.string.merchant_checkin_rewards_title),
                subtitle = stringResource(R.string.merchant_program_module_checkin_subtitle),
                icon = Icons.Default.CheckCircle,
                colors = listOf(Color(0xFF7A9CC6), Color(0xFF466B8F)),
                onClick = onOpenCheckinRewards,
                onOpenInfo = { onOpenProgramInfo(LoyaltyProgramType.DIGITAL_STAMP) },
            )
            ProgramModuleTile(
                title = stringResource(R.string.merchant_purchase_frequency_title),
                subtitle = stringResource(R.string.merchant_program_module_frequency_subtitle),
                icon = Icons.Default.Repeat,
                colors = listOf(Color(0xFF5B8DEF), Color(0xFF315EBD)),
                onClick = onOpenPurchaseFrequency,
                onOpenInfo = { onOpenProgramInfo(LoyaltyProgramType.PURCHASE_FREQUENCY) },
            )
            ProgramModuleTile(
                title = stringResource(R.string.merchant_referral_rewards_title),
                subtitle = stringResource(R.string.merchant_program_module_referral_subtitle),
                icon = Icons.Default.GroupAdd,
                colors = listOf(Color(0xFF22A06B), Color(0xFF0F7A4A)),
                onClick = onOpenReferralRewards,
                onOpenInfo = { onOpenProgramInfo(LoyaltyProgramType.REFERRAL) },
            )
            ProgramModuleTile(
                title = stringResource(R.string.merchant_hybrid_programs_title),
                subtitle = stringResource(R.string.merchant_program_module_hybrid_subtitle),
                icon = Icons.Default.Campaign,
                colors = listOf(VerevColors.Gold, VerevColors.Forest),
                onClick = onOpenHybridPrograms,
                onOpenInfo = { onOpenProgramInfo(LoyaltyProgramType.HYBRID) },
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
    canManagePrograms: Boolean = true,
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
                onEdit = if (canManagePrograms) ({ onEdit(program.id) }) else null,
                onToggleEnabled = { enabled -> onToggleEnabled(program.id, enabled) },
                onDelete = if (canManagePrograms) ({ onDelete(program.id) }) else null,
                canManagePrograms = canManagePrograms,
            )
        }
    }
}

@Composable
internal fun ProgramListItem(
    program: RewardProgram,
    isBusy: Boolean,
    onEdit: (() -> Unit)?,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: (() -> Unit)?,
    canManagePrograms: Boolean = true,
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
                onClick = { onDelete?.invoke() },
                enabled = canManagePrograms && onDelete != null && animatedOffset <= -(revealWidthPx * 0.8f),
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
                .pointerInput(program.id, isBusy, canManagePrograms) {
                    if (!canManagePrograms) return@pointerInput
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
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
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
                        verticalArrangement = Arrangement.spacedBy(3.dp),
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
                        if (program.rulesSummary.isNotBlank()) {
                            Text(
                                text = program.rulesSummary,
                                color = VerevColors.Forest.copy(alpha = 0.68f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    MerchantStatusPill(
                        text = if (program.active) stringResource(R.string.merchant_program_active) else stringResource(R.string.merchant_program_disabled),
                        backgroundColor = if (program.active) VerevColors.Moss.copy(alpha = 0.14f) else Color(0xFFF3F4F6),
                        contentColor = if (program.active) VerevColors.Moss else VerevColors.Inactive,
                    )
                }

                if (canManagePrograms) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        onEdit?.let {
                            Surface(
                                onClick = it,
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
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        } ?: Spacer(modifier = Modifier.height(0.dp))
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
private fun ProgramLifecycleSummary(program: RewardProgram) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val today = LocalDate.now()
        when {
            !program.active -> {
                MerchantStatusPill(
                    text = stringResource(R.string.merchant_programs_status_paused),
                    backgroundColor = Color(0xFFF3F4F6),
                    contentColor = VerevColors.Inactive,
                )
            }
            program.autoScheduleEnabled && program.scheduleStartDate != null && program.scheduleEndDate != null -> {
                val start = program.scheduleStartDate
                val end = program.scheduleEndDate
                val statusRes = when {
                    program.annualRepeatEnabled && isAnnualProgramActive(start, end, today) -> R.string.merchant_programs_status_live
                    program.annualRepeatEnabled -> R.string.merchant_programs_status_scheduled
                    today.isBefore(start) -> R.string.merchant_programs_status_scheduled
                    today.isAfter(end) -> R.string.merchant_programs_status_completed
                    else -> R.string.merchant_programs_status_live
                }
                MerchantStatusPill(
                    text = stringResource(statusRes),
                    backgroundColor = program.type.gradient().first().copy(alpha = 0.14f),
                    contentColor = VerevColors.Forest,
                )
                MerchantStatusPill(
                    text = stringResource(
                        R.string.merchant_programs_schedule_window,
                        start.format(ProgramDateFormatter),
                        end.format(ProgramDateFormatter),
                    ),
                    backgroundColor = VerevColors.AppBackground,
                    contentColor = VerevColors.Forest.copy(alpha = 0.78f),
                )
                if (program.annualRepeatEnabled) {
                    MerchantStatusPill(
                        text = stringResource(R.string.merchant_programs_schedule_recurs_yearly),
                        backgroundColor = VerevColors.Gold.copy(alpha = 0.12f),
                        contentColor = VerevColors.Gold,
                    )
                }
            }
            else -> {
                MerchantStatusPill(
                    text = stringResource(R.string.merchant_programs_status_always_on),
                    backgroundColor = VerevColors.Moss.copy(alpha = 0.14f),
                    contentColor = VerevColors.Moss,
                )
            }
        }
    }
}

private fun isAnnualProgramActive(
    start: LocalDate,
    end: LocalDate,
    date: LocalDate,
): Boolean {
    val startMonthDay = java.time.MonthDay.from(start)
    val endMonthDay = java.time.MonthDay.from(end)
    val dateMonthDay = java.time.MonthDay.from(date)
    val window = if (!endMonthDay.isBefore(startMonthDay)) {
        startMonthDay.atYear(date.year) to endMonthDay.atYear(date.year)
    } else if (!dateMonthDay.isBefore(startMonthDay)) {
        startMonthDay.atYear(date.year) to endMonthDay.atYear(date.year + 1)
    } else {
        startMonthDay.atYear(date.year - 1) to endMonthDay.atYear(date.year)
    }
    return !date.isBefore(window.first) && !date.isAfter(window.second)
}

@Composable
private fun ProgramStatusToggle(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    MerchantInlineToggle(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        accent = VerevColors.Moss,
    )
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
internal fun ProgramSectionHeader(
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
    onOpenInfo: () -> Unit,
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
                onClick = onOpenInfo,
                color = VerevColors.AppBackground,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.merchant_program_info_action, title),
                    tint = VerevColors.Forest.copy(alpha = 0.64f),
                    modifier = Modifier.padding(10.dp),
                )
            }
        }
    }
}

@Composable
internal fun ProgramTypeInfoSheet(
    type: LoyaltyProgramType,
    currencyCode: String = "AMD",
    onDismiss: () -> Unit,
) {
    val editorState = remember(type) { defaultProgramEditorState(type) }
    val snapshot = remember(type) {
        editorState.toOperationalSnapshot(
            existingPrograms = emptyList(),
            campaigns = emptyList(),
            activeScanActions = emptyList(),
        )
    }
    val spec = type.screenSpec()
    AppBottomSheetDialog(
        onDismissRequest = onDismiss,
        contentPadding = PaddingValues(0.dp),
    ) { _, _ ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProgramIcon(icon = type.icon(), colors = type.gradient())
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(spec.titleRes),
                            style = MaterialTheme.typography.headlineSmall,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(spec.subtitleRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = VerevColors.Forest.copy(alpha = 0.68f),
                        )
                    }
                }
                ProgramSectionCard(
                    title = stringResource(R.string.merchant_program_focus_title),
                    subtitle = stringResource(R.string.merchant_program_info_summary_subtitle),
                ) {
                    ProgramPreviewBullet(
                        title = stringResource(R.string.merchant_program_preview_customer_title),
                        value = customerExperienceSummary(editorState, currencyCode),
                    )
                    ProgramPreviewBullet(
                        title = stringResource(R.string.merchant_program_preview_branch_title),
                        value = branchImpactSummary(
                            storeName = "",
                            scanActions = snapshot.scanActions,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
internal fun ProgramEditorSheet(
    editorState: ProgramEditorState,
    selectedStoreName: String,
    availableStores: List<Store>,
    availablePrograms: List<RewardProgram>,
    availableRewards: List<Reward>,
    fieldErrors: Map<String, Int>,
    isSubmitting: Boolean,
    currencyCode: String = "AMD",
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onApplyToAllBranchesChange: (Boolean) -> Unit,
    onToggleStoreTarget: (String) -> Unit,
    onTypeChange: (LoyaltyProgramType) -> Unit,
    onActiveChanged: (Boolean) -> Unit,
    onTargetGenderChange: (String) -> Unit,
    onAgeTargetingEnabledChange: (Boolean) -> Unit,
    onTargetAgeMinChange: (String) -> Unit,
    onTargetAgeMaxChange: (String) -> Unit,
    onOneTimePerCustomerChange: (Boolean) -> Unit,
    onAutoScheduleEnabledChange: (Boolean) -> Unit,
    onScheduleStartDateChange: (String) -> Unit,
    onScheduleEndDateChange: (String) -> Unit,
    onAnnualRepeatEnabledChange: (Boolean) -> Unit,
    onBenefitResetTypeChange: (ProgramBenefitResetType) -> Unit,
    onBenefitResetCustomDaysChange: (String) -> Unit,
    onPointsSpendStepAmountChange: (String) -> Unit,
    onPointsAwardedPerStepChange: (String) -> Unit,
    onPointsWelcomeBonusChange: (String) -> Unit,
    onPointsMinimumRedeemChange: (String) -> Unit,
    onCashbackPercentChange: (String) -> Unit,
    onCashbackMinimumSpendAmountChange: (String) -> Unit,
    onTierThresholdBasisChange: (TierThresholdBasis) -> Unit,
    onTierNameChange: (String, String) -> Unit,
    onTierThresholdChange: (String, String) -> Unit,
    onTierBenefitTypeChange: (String, TierBenefitType) -> Unit,
    onTierBonusPercentChange: (String, String) -> Unit,
    onTierRewardTypeChange: (String, ProgramRewardOutcomeType) -> Unit,
    onTierRewardLabelChange: (String, String) -> Unit,
    onTierRewardPointsChange: (String, String) -> Unit,
    onTierRewardRewardIdChange: (String, String?) -> Unit,
    onTierRewardProgramIdChange: (String, String?) -> Unit,
    onAddTier: () -> Unit,
    onRemoveTier: (String) -> Unit,
    onCouponNameChange: (String) -> Unit,
    onCouponPointsCostChange: (String) -> Unit,
    onCouponDiscountAmountChange: (String) -> Unit,
    onCouponMinimumSpendAmountChange: (String) -> Unit,
    onCheckInVisitsRequiredChange: (String) -> Unit,
    onPurchaseFrequencyCountChange: (String) -> Unit,
    onPurchaseFrequencyWindowDaysChange: (String) -> Unit,
    onReferralCodePrefixChange: (String) -> Unit,
    onRewardOutcomeTypeChange: (ProgramRewardSlot, ProgramRewardOutcomeType) -> Unit,
    onRewardOutcomeLabelChange: (ProgramRewardSlot, String) -> Unit,
    onRewardOutcomePointsChange: (ProgramRewardSlot, String) -> Unit,
    onRewardOutcomeRewardIdChange: (ProgramRewardSlot, String?) -> Unit,
    onRewardOutcomeProgramIdChange: (ProgramRewardSlot, String?) -> Unit,
    onOpenSubEditor: (ProgramSubEditor) -> Unit,
    onOpenTierBenefitEditor: (String) -> Unit,
    onOpenBenefitEditor: (ProgramRewardSlot) -> Unit,
    onClearTierBenefit: (String) -> Unit,
    onOpenRewardsCatalog: () -> Unit = {},
    onOpenProgramsCatalog: () -> Unit = {},
    onApplyEditorValidationErrors: (Map<String, Int>) -> Boolean,
    onSave: () -> Unit,
    fullScreen: Boolean = false,
) {
    val isCreateFlow = fullScreen && !editorState.isEditing
    val creationSteps = remember(editorState.type) { ProgramCreationStep.entries.toList() }
    var currentStepIndex by rememberSaveable(editorState.programId, editorState.type, fullScreen) { androidx.compose.runtime.mutableStateOf(0) }
    val currentStep = creationSteps.getOrElse(currentStepIndex) { ProgramCreationStep.BASICS }
    val hasPreviousStep = currentStepIndex > 0
    val hasNextStep = currentStepIndex < creationSteps.lastIndex
    val editorScrollState = rememberScrollState()
    val editorContent: @Composable (onPrimaryAction: () -> Unit, showBackAction: Boolean) -> Unit = { onPrimaryAction, showBackAction ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VerevColors.AppBackground),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(editorScrollState)
                    .imePadding()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 14.dp,
                        bottom = 28.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (showBackAction) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                if (isCreateFlow && hasPreviousStep) currentStepIndex -= 1 else onDismiss()
                            }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = VerevColors.Forest,
                            modifier = Modifier.size(22.dp),
                        )
                        Text(
                            text = stringResource(R.string.auth_back),
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                if (!isCreateFlow) {
                    ProgramEditSheetTopBar(
                        title = editorState.name.ifBlank { stringResource(editorState.type.displayNameRes()) },
                        onClose = onDismiss,
                    )
                }

                ProgramEditorHeader(
                    editorState = editorState,
                    currentStep = currentStepIndex,
                    totalSteps = creationSteps.size,
                    showProgress = isCreateFlow,
                )

                if (isCreateFlow) {
                    when (currentStep) {
                        ProgramCreationStep.BASICS -> {
                            ProgramSectionCard(
                                title = stringResource(R.string.merchant_program_editor_basics_title),
                                subtitle = stringResource(R.string.merchant_program_editor_basics_subtitle),
                            ) {
                                MerchantFormField(
                                    value = editorState.name,
                                    onValueChange = onNameChange,
                                    label = stringResource(R.string.merchant_program_form_name),
                                    leadingIcon = editorState.type.icon(),
                                    isError = fieldErrors.containsKey(PROGRAM_FIELD_NAME),
                                    errorText = fieldErrors[PROGRAM_FIELD_NAME]?.let { stringResource(it) },
                                    supportingText = null,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                                    ),
                                )
                                MerchantFormField(
                                    value = editorState.description,
                                    onValueChange = onDescriptionChange,
                                    label = stringResource(R.string.merchant_program_form_description),
                                    leadingIcon = Icons.Default.Info,
                                    singleLine = false,
                                    isError = fieldErrors.containsKey(PROGRAM_FIELD_DESCRIPTION),
                                    errorText = fieldErrors[PROGRAM_FIELD_DESCRIPTION]?.let { stringResource(it) },
                                    supportingText = null,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                                    ),
                                )
                                if (availableStores.size > 1) {
                                    ProgramBranchScopeSection(
                                        editorState = editorState,
                                        availableStores = availableStores,
                                        onApplyToAllBranchesChange = onApplyToAllBranchesChange,
                                        onToggleStoreTarget = onToggleStoreTarget,
                                    )
                                }
                            }
                        }

                        ProgramCreationStep.GOAL -> {
                            ProgramSectionCard(
                                title = stringResource(R.string.merchant_program_creation_goal_title),
                                subtitle = stringResource(R.string.merchant_program_creation_goal_subtitle),
                            ) {
                                ProgramGoalStepFields(
                                    editorState = editorState,
                                    availablePrograms = availablePrograms,
                                    availableRewards = availableRewards,
                                    currencyCode = currencyCode,
                                    fieldErrors = fieldErrors,
                                    onPointsSpendStepAmountChange = onPointsSpendStepAmountChange,
                                    onPointsAwardedPerStepChange = onPointsAwardedPerStepChange,
                                    onPointsWelcomeBonusChange = onPointsWelcomeBonusChange,
                                    onPointsMinimumRedeemChange = onPointsMinimumRedeemChange,
                                    onCashbackPercentChange = onCashbackPercentChange,
                                    onCashbackMinimumSpendAmountChange = onCashbackMinimumSpendAmountChange,
                                    onTierThresholdBasisChange = onTierThresholdBasisChange,
                                    onTierNameChange = onTierNameChange,
                                    onTierThresholdChange = onTierThresholdChange,
                                    onTierBenefitTypeChange = onTierBenefitTypeChange,
                                    onTierBonusPercentChange = onTierBonusPercentChange,
                                    onTierRewardTypeChange = onTierRewardTypeChange,
                                    onTierRewardLabelChange = onTierRewardLabelChange,
                                    onTierRewardPointsChange = onTierRewardPointsChange,
                                    onTierRewardRewardIdChange = onTierRewardRewardIdChange,
                                    onTierRewardProgramIdChange = onTierRewardProgramIdChange,
                                    onAddTier = onAddTier,
                                    onRemoveTier = onRemoveTier,
                                    onCouponNameChange = onCouponNameChange,
                                    onCouponPointsCostChange = onCouponPointsCostChange,
                                    onCouponDiscountAmountChange = onCouponDiscountAmountChange,
                                    onCouponMinimumSpendAmountChange = onCouponMinimumSpendAmountChange,
                                    onCheckInVisitsRequiredChange = onCheckInVisitsRequiredChange,
                                    onPurchaseFrequencyCountChange = onPurchaseFrequencyCountChange,
                                    onPurchaseFrequencyWindowDaysChange = onPurchaseFrequencyWindowDaysChange,
                                    onReferralCodePrefixChange = onReferralCodePrefixChange,
                                    onRewardOutcomeTypeChange = onRewardOutcomeTypeChange,
                                    onRewardOutcomePointsChange = onRewardOutcomePointsChange,
                                    onRewardOutcomeRewardIdChange = onRewardOutcomeRewardIdChange,
                                    onRewardOutcomeProgramIdChange = onRewardOutcomeProgramIdChange,
                                    onOpenTierBenefitEditor = onOpenTierBenefitEditor,
                                    onClearTierBenefit = onClearTierBenefit,
                                    onOpenRewardsCatalog = onOpenRewardsCatalog,
                                    onOpenProgramsCatalog = onOpenProgramsCatalog,
                                )
                            }
                        }

                        ProgramCreationStep.AUDIENCE -> {
                            ProgramSectionCard(
                                title = stringResource(R.string.merchant_program_audience_title),
                                subtitle = stringResource(R.string.merchant_program_audience_subtitle),
                            ) {
                                ProgramAudienceFields(
                                    editorState = editorState,
                                    fieldErrors = fieldErrors,
                                    onTargetGenderChange = onTargetGenderChange,
                                    onAgeTargetingEnabledChange = onAgeTargetingEnabledChange,
                                    onTargetAgeMinChange = onTargetAgeMinChange,
                                    onTargetAgeMaxChange = onTargetAgeMaxChange,
                                    onOneTimePerCustomerChange = onOneTimePerCustomerChange,
                                )
                            }
                        }

                        ProgramCreationStep.AVAILABILITY -> {
                            ProgramSectionCard(
                                title = stringResource(R.string.merchant_program_editor_schedule_title),
                                subtitle = stringResource(R.string.merchant_program_editor_schedule_subtitle),
                            ) {
                                ProgramToggleRow(
                                    title = stringResource(R.string.merchant_program_form_enabled),
                                    subtitle = stringResource(R.string.merchant_program_enabled_toggle_subtitle),
                                    checked = editorState.active,
                                    onCheckedChange = onActiveChanged,
                                )
                                ProgramScheduleFields(
                                    editorState = editorState,
                                    fieldErrors = fieldErrors,
                                    onAutoScheduleEnabledChange = onAutoScheduleEnabledChange,
                                    onScheduleStartDateChange = onScheduleStartDateChange,
                                    onScheduleEndDateChange = onScheduleEndDateChange,
                                    onAnnualRepeatEnabledChange = onAnnualRepeatEnabledChange,
                                    benefitResetType = editorState.benefitResetType,
                                    benefitResetCustomDays = editorState.benefitResetCustomDays,
                                    onBenefitResetTypeChange = onBenefitResetTypeChange,
                                    onBenefitResetCustomDaysChange = onBenefitResetCustomDaysChange,
                                )
                            }
                        }

                        ProgramCreationStep.REVIEW -> {
                            ProgramCreationReviewStep(
                                editorState = editorState,
                                selectedStoreName = selectedStoreName,
                                availableStores = availableStores,
                                availableRewards = availableRewards,
                                currencyCode = currencyCode,
                                fieldErrors = fieldErrors,
                                onEditBasics = { currentStepIndex = ProgramCreationStep.BASICS.ordinal },
                                onEditGoal = { currentStepIndex = ProgramCreationStep.GOAL.ordinal },
                                onEditAudience = { currentStepIndex = ProgramCreationStep.AUDIENCE.ordinal },
                                onEditAvailability = { currentStepIndex = ProgramCreationStep.AVAILABILITY.ordinal },
                            )
                        }
                    }
                    ProgramEditorStepActions(
                        hasPreviousStep = hasPreviousStep,
                        hasNextStep = hasNextStep,
                        isSubmitting = isSubmitting,
                        onBack = { currentStepIndex -= 1 },
                        onPrimaryAction = {
                            if (hasNextStep) {
                                val scopedErrors = currentStep.scopedErrors(editorState.validate())
                                if (onApplyEditorValidationErrors(scopedErrors)) {
                                    currentStepIndex += 1
                                }
                            } else {
                                onPrimaryAction()
                            }
                        },
                    )
                } else {
                    ProgramEditOverviewContent(
                        editorState = editorState,
                        availableRewards = availableRewards,
                        currencyCode = currencyCode,
                        fieldErrors = fieldErrors,
                        onOpenSubEditor = onOpenSubEditor,
                    )
                    Button(
                        onClick = onPrimaryAction,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(vertical = 18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Gold, contentColor = Color.White),
                    ) {
                        Text(
                            text = if (editorState.isEditing) stringResource(R.string.merchant_program_save_changes) else stringResource(R.string.merchant_program_create_submit),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }

    if (fullScreen) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = VerevColors.AppBackground,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VerevColors.AppBackground),
            ) {
                editorContent(onSave, true)
            }
        }
    } else {
        AppBottomSheetDialog(
            onDismissRequest = onDismiss,
            allowSwipeToDismiss = !editorState.isEditing,
            enableSheetGestures = !editorState.isEditing,
            fullHeight = editorState.isEditing,
            contentPadding = PaddingValues(0.dp),
        ) { _, _ ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
            ) {
                editorContent(onSave, false)
            }
        }
    }
}

@Composable
private fun ProgramEditorStepActions(
    hasPreviousStep: Boolean,
    hasNextStep: Boolean,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onPrimaryAction: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasPreviousStep) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(vertical = 18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = VerevColors.Forest,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            ) {
                Text(
                    text = stringResource(R.string.merchant_program_step_back),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Button(
            onClick = onPrimaryAction,
            modifier = if (hasPreviousStep) Modifier.weight(1f) else Modifier.fillMaxWidth(),
            enabled = !isSubmitting,
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(vertical = 18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerevColors.Gold,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = if (hasNextStep) stringResource(R.string.merchant_program_step_continue)
                else stringResource(R.string.merchant_program_create_submit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ProgramEditSheetTopBar(
    title: String,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.merchant_program_editor_edit_title),
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.56f),
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(VerevColors.AppBackground),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = VerevColors.Forest,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun ProgramEditOverviewContent(
    editorState: ProgramEditorState,
    availableRewards: List<Reward>,
    currencyCode: String,
    fieldErrors: Map<String, Int>,
    onOpenSubEditor: (ProgramSubEditor) -> Unit,
) {
    ProgramReviewSectionCard(
        title = stringResource(R.string.merchant_program_editor_basics_title),
        summaryTitle = editorState.name.ifBlank { stringResource(R.string.merchant_program_basics_name_missing) },
        summary = editorState.description.ifBlank { stringResource(R.string.merchant_program_basics_description_missing) },
        errorRes = fieldErrors[PROGRAM_FIELD_NAME] ?: fieldErrors[PROGRAM_FIELD_DESCRIPTION],
        onEdit = { onOpenSubEditor(ProgramSubEditor.BASICS_EDIT) },
    )

    when (editorState.type) {
        LoyaltyProgramType.HYBRID -> {
            ProgramReviewSectionCard(
                title = stringResource(R.string.merchant_program_points_earn_title),
                summaryTitle = stringResource(R.string.merchant_program_review_goal_heading),
                summary = stringResource(
                    R.string.merchant_program_points_summary,
                    editorState.pointsAwardedPerStep,
                    formatWholeCurrency(editorState.pointsSpendStepAmount.toDoubleOrNull() ?: 0.0, currencyCode),
                    editorState.pointsMinimumRedeem.ifBlank { "0" },
                ),
                errorRes = fieldErrors[PROGRAM_FIELD_POINTS_STEP] ?: fieldErrors[PROGRAM_FIELD_POINTS_AWARDED],
                onEdit = { onOpenSubEditor(ProgramSubEditor.EARN_RULES_EDIT) },
            )
            ProgramReviewSectionCard(
                title = stringResource(R.string.merchant_program_checkin_goal_title),
                summaryTitle = stringResource(R.string.merchant_program_review_goal_heading),
                summary = stringResource(
                    R.string.merchant_program_checkin_summary,
                    editorState.checkInVisitsRequired,
                    benefitSummary(editorState.checkInReward, availableRewards),
                ),
                errorRes = fieldErrors[PROGRAM_FIELD_CHECKIN_VISITS] ?: fieldErrors[PROGRAM_FIELD_CHECKIN_REWARD],
                onEdit = { onOpenSubEditor(ProgramSubEditor.CHECKIN_EDIT) },
            )
            ProgramReviewSectionCard(
                title = stringResource(R.string.merchant_program_referral_rewards_title),
                summaryTitle = stringResource(R.string.merchant_program_review_goal_heading),
                summary = stringResource(
                    R.string.merchant_program_referral_summary,
                    benefitSummary(editorState.referralReferrerReward, availableRewards),
                    benefitSummary(editorState.referralRefereeReward, availableRewards),
                    editorState.referralCodePrefix.ifBlank { "REF" },
                ),
                errorRes = fieldErrors[PROGRAM_FIELD_REFERRAL_REFERRER]
                    ?: fieldErrors[PROGRAM_FIELD_REFERRAL_REFEREE]
                    ?: fieldErrors[PROGRAM_FIELD_REFERRAL_PREFIX],
                onEdit = { onOpenSubEditor(ProgramSubEditor.REFERRAL_EDIT) },
            )
        }

        else -> {
            ProgramReviewSectionCard(
                title = stringResource(R.string.merchant_program_creation_goal_title),
                summaryTitle = stringResource(R.string.merchant_program_review_goal_heading),
                summary = reviewGoalSummary(editorState, availableRewards, currencyCode),
                errorRes = listOf(
                    PROGRAM_FIELD_POINTS_STEP,
                    PROGRAM_FIELD_POINTS_AWARDED,
                    PROGRAM_FIELD_POINTS_REDEEM,
                    PROGRAM_FIELD_CHECKIN_VISITS,
                    PROGRAM_FIELD_CHECKIN_REWARD,
                    PROGRAM_FIELD_FREQUENCY_COUNT,
                    PROGRAM_FIELD_FREQUENCY_WINDOW,
                    PROGRAM_FIELD_FREQUENCY_REWARD,
                    PROGRAM_FIELD_COUPON_NAME,
                    PROGRAM_FIELD_COUPON_POINTS,
                    PROGRAM_FIELD_COUPON_DISCOUNT,
                    PROGRAM_FIELD_REFERRAL_REFERRER,
                    PROGRAM_FIELD_REFERRAL_REFEREE,
                    PROGRAM_FIELD_REFERRAL_PREFIX,
                    PROGRAM_FIELD_TIER_SILVER,
                ).firstNotNullOfOrNull(fieldErrors::get),
                onEdit = { onOpenSubEditor(editorState.type.primaryGoalSubEditor()) },
            )
        }
    }

    ProgramReviewSectionCard(
        title = stringResource(R.string.merchant_program_audience_title),
        summaryTitle = stringResource(R.string.merchant_program_review_audience_heading),
        summary = audienceSummary(editorState),
        errorRes = fieldErrors[PROGRAM_FIELD_TARGET_AGE_MIN] ?: fieldErrors[PROGRAM_FIELD_TARGET_AGE_MAX],
        onEdit = { onOpenSubEditor(ProgramSubEditor.AUDIENCE_EDIT) },
    )
    ProgramReviewSectionCard(
        title = stringResource(R.string.merchant_program_editor_schedule_title),
        summaryTitle = stringResource(R.string.merchant_program_review_availability_heading),
        summary = availabilitySummary(editorState),
        errorRes = fieldErrors[PROGRAM_FIELD_SCHEDULE_START]
            ?: fieldErrors[PROGRAM_FIELD_SCHEDULE_END]
            ?: fieldErrors[PROGRAM_FIELD_BENEFIT_RESET_CUSTOM_DAYS],
        onEdit = { onOpenSubEditor(ProgramSubEditor.AVAILABILITY_EDIT) },
    )
}

private fun LoyaltyProgramType.primaryGoalSubEditor(): ProgramSubEditor = when (this) {
    LoyaltyProgramType.POINTS -> ProgramSubEditor.EARN_RULES_EDIT
    LoyaltyProgramType.TIER -> ProgramSubEditor.TIER_EDIT
    LoyaltyProgramType.COUPON -> ProgramSubEditor.REWARD_EDIT
    LoyaltyProgramType.DIGITAL_STAMP -> ProgramSubEditor.CHECKIN_EDIT
    LoyaltyProgramType.PURCHASE_FREQUENCY -> ProgramSubEditor.FREQUENCY_EDIT
    LoyaltyProgramType.REFERRAL -> ProgramSubEditor.REFERRAL_EDIT
    LoyaltyProgramType.HYBRID -> ProgramSubEditor.EARN_RULES_EDIT
}

@Composable
private fun ProgramEditorHeader(
    editorState: ProgramEditorState,
    currentStep: Int,
    totalSteps: Int,
    showProgress: Boolean,
) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(editorState.type.gradient()), RoundedCornerShape(22.dp))
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = editorState.type.icon(),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = if (editorState.isEditing) {
                            stringResource(R.string.merchant_program_editor_edit_title)
                        } else {
                            stringResource(R.string.merchant_program_creation_selected_program_label)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.76f),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(editorState.type.displayNameRes()),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (showProgress) {
                    MerchantStatusPill(
                        text = stringResource(
                            R.string.merchant_program_creation_progress_label,
                            currentStep + 1,
                            totalSteps,
                        ),
                        backgroundColor = Color.White.copy(alpha = 0.16f),
                        contentColor = Color.White,
                    )
                }
            }
            Text(
                text = stringResource(editorState.type.templateSubtitleRes()),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.82f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (showProgress) {
                Text(
                    text = stringResource(ProgramCreationStep.entries[currentStep].titleRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.88f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (showProgress) {
                LinearProgressIndicator(
                    progress = { ((currentStep + 1).toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.22f),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProgramBranchScopeSection(
    editorState: ProgramEditorState,
    availableStores: List<Store>,
    onApplyToAllBranchesChange: (Boolean) -> Unit,
    onToggleStoreTarget: (String) -> Unit,
) {
    val lockedStoreIds = editorState.lockedStoreIds.toSet()
    val targetStoreIds = editorState.targetStoreIds.toSet()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.merchant_program_branch_scope_title),
            style = MaterialTheme.typography.titleSmall,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = if (lockedStoreIds.isEmpty()) {
                stringResource(R.string.merchant_program_branch_scope_subtitle)
            } else {
                stringResource(R.string.merchant_program_branch_scope_edit_subtitle)
            },
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.62f),
        )
        if (lockedStoreIds.isEmpty()) {
            ProgramToggleRow(
                title = stringResource(R.string.merchant_program_branch_scope_all_title),
                subtitle = stringResource(R.string.merchant_program_branch_scope_all_subtitle),
                checked = editorState.applyToAllBranches,
                onCheckedChange = onApplyToAllBranchesChange,
            )
        }
        if (!editorState.applyToAllBranches || lockedStoreIds.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableStores.forEach { store ->
                    val isLocked = lockedStoreIds.contains(store.id)
                    val isSelected = targetStoreIds.contains(store.id)
                    if (isLocked) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = VerevColors.AppBackground,
                        ) {
                            Text(
                                text = store.name,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                color = VerevColors.Forest.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    } else {
                        com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip(
                            text = store.name,
                            selected = isSelected,
                            onClick = { onToggleStoreTarget(store.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramOperationalGuardrailSection(snapshot: ProgramOperationalSnapshot) {
    if (snapshot.overlapWarnings.isEmpty() && snapshot.inactiveReasons.isEmpty()) return
    ProgramSectionCard(
        title = stringResource(R.string.merchant_program_guardrails_title),
        subtitle = stringResource(R.string.merchant_program_guardrails_subtitle),
    ) {
        snapshot.overlapWarnings.forEach { warning ->
            ProgramNoticeRow(
                icon = Icons.Default.WarningAmber,
                title = when (warning) {
                    is ProgramOverlapWarning.ProgramConflict ->
                        stringResource(R.string.merchant_program_overlap_program_title, warning.programName)
                    is ProgramOverlapWarning.CampaignConflict ->
                        stringResource(R.string.merchant_program_overlap_campaign_title, warning.campaignName)
                },
                supporting = when (warning) {
                    is ProgramOverlapWarning.ProgramConflict ->
                        stringResource(
                            R.string.merchant_program_overlap_program_message,
                            sharedActionLabels(warning.sharedActions),
                        )
                    is ProgramOverlapWarning.CampaignConflict ->
                        stringResource(R.string.merchant_program_overlap_campaign_message)
                },
                containerColor = Color(0xFFFFF6E8),
                iconTint = VerevColors.Gold,
            )
        }
        snapshot.inactiveReasons.forEach { reason ->
            ProgramNoticeRow(
                icon = Icons.Default.Info,
                title = inactiveReasonTitle(reason),
                supporting = inactiveReasonMessage(reason),
                containerColor = VerevColors.Forest.copy(alpha = 0.07f),
                iconTint = VerevColors.Forest,
            )
        }
    }
}

@Composable
private fun ProgramNoticeRow(
    icon: ImageVector,
    title: String,
    supporting: String,
    containerColor: Color,
    iconTint: Color,
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun ProgramPreviewBullet(title: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.5f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun sharedActionLabels(actions: Set<RewardProgramScanAction>): String {
    val labels = buildList {
        actions
            .filterNot { action ->
                action == RewardProgramScanAction.APPLY_CASHBACK ||
                    action == RewardProgramScanAction.TRACK_TIER_PROGRESS
            }
            .forEach { action -> add(action.displayLabel()) }
    }
    return labels.joinToString()
}

@Composable
internal fun inactiveReasonTitle(reason: ProgramInactiveReason): String = when (reason) {
    ProgramInactiveReason.Disabled -> stringResource(R.string.merchant_program_inactive_disabled_title)
    is ProgramInactiveReason.StartsLater -> stringResource(R.string.merchant_program_inactive_scheduled_title)
    is ProgramInactiveReason.Ended -> stringResource(R.string.merchant_program_inactive_finished_title)
    ProgramInactiveReason.ReferralOnly -> stringResource(R.string.merchant_program_inactive_referral_title)
    ProgramInactiveReason.NoScanActions -> stringResource(R.string.merchant_program_inactive_actions_title)
    ProgramInactiveReason.NoActiveScanCoverage -> stringResource(R.string.merchant_program_inactive_scan_title)
}

@Composable
internal fun inactiveReasonMessage(reason: ProgramInactiveReason): String = when (reason) {
    ProgramInactiveReason.Disabled -> stringResource(R.string.merchant_program_inactive_disabled_message)
    is ProgramInactiveReason.StartsLater -> stringResource(
        R.string.merchant_program_inactive_scheduled_message,
        reason.startDate.format(ProgramDateFormatter),
    )
    is ProgramInactiveReason.Ended -> stringResource(
        R.string.merchant_program_inactive_finished_message,
        reason.endDate.format(ProgramDateFormatter),
    )
    ProgramInactiveReason.ReferralOnly -> stringResource(R.string.merchant_program_inactive_referral_message)
    ProgramInactiveReason.NoScanActions -> stringResource(R.string.merchant_program_inactive_actions_message)
    ProgramInactiveReason.NoActiveScanCoverage -> stringResource(R.string.merchant_program_inactive_scan_message)
}

@Composable
internal fun ProgramEnableGuardrailDialog(
    program: RewardProgram,
    selectedStoreName: String,
    snapshot: ProgramOperationalSnapshot,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.merchant_program_enable_dialog_title),
                color = VerevColors.Forest,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(
                        R.string.merchant_program_enable_dialog_message,
                        program.name,
                        branchImpactSummary(selectedStoreName, snapshot.scanActions),
                    ),
                    color = VerevColors.Forest.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                snapshot.overlapWarnings.take(3).forEach { warning ->
                    Text(
                        text = when (warning) {
                            is ProgramOverlapWarning.ProgramConflict ->
                                stringResource(R.string.merchant_program_overlap_program_title, warning.programName)
                            is ProgramOverlapWarning.CampaignConflict ->
                                stringResource(R.string.merchant_program_overlap_campaign_title, warning.campaignName)
                        },
                        color = VerevColors.Gold,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.merchant_program_enable_dialog_confirm))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.merchant_program_delete_cancel))
            }
        },
    )
}

@Composable
internal fun customerExperienceSummary(
    editorState: ProgramEditorState,
    currencyCode: String,
): String = when (editorState.type) {
    LoyaltyProgramType.POINTS -> stringResource(
        R.string.merchant_program_preview_points_message,
        editorState.pointsAwardedPerStep.toIntOrNull() ?: 0,
        formatWholeCurrency(editorState.pointsSpendStepAmount.toDoubleOrNull() ?: 0.0, currencyCode),
        editorState.pointsMinimumRedeem.toIntOrNull() ?: 0,
    )
    LoyaltyProgramType.DIGITAL_STAMP -> stringResource(
        R.string.merchant_program_preview_checkin_message,
        editorState.checkInVisitsRequired.toIntOrNull() ?: 0,
        editorState.checkInReward.previewValue(),
    )
    LoyaltyProgramType.TIER -> editorState.tierLevels
        .joinToString(separator = ", ") { level ->
            "${level.name.ifBlank { "Tier" }} ${level.threshold.toIntOrNull() ?: 0}"
        }
        .ifBlank { stringResource(R.string.merchant_program_preview_tier_empty_message) }
    LoyaltyProgramType.COUPON -> stringResource(
        R.string.merchant_program_preview_coupon_message,
        editorState.couponName.ifBlank { stringResource(R.string.merchant_program_form_coupon_name) },
        editorState.couponPointsCost.toIntOrNull() ?: 0,
    )
    LoyaltyProgramType.PURCHASE_FREQUENCY -> stringResource(
        R.string.merchant_program_preview_frequency_message,
        editorState.purchaseFrequencyCount.toIntOrNull() ?: 0,
        editorState.purchaseFrequencyWindowDays.toIntOrNull() ?: 0,
        editorState.purchaseFrequencyReward.previewValue(),
    )
    LoyaltyProgramType.REFERRAL -> stringResource(
        R.string.merchant_program_preview_referral_message,
        editorState.referralReferrerReward.previewValue(),
        editorState.referralRefereeReward.previewValue(),
    )
    LoyaltyProgramType.HYBRID -> stringResource(R.string.merchant_program_preview_hybrid_message)
}

private fun ProgramRewardOutcomeEditorState.previewValue(): String = when (type) {
    ProgramRewardOutcomeType.POINTS -> "${pointsAmount.toIntOrNull() ?: 0} pts"
    else -> when {
        type.usesRewardItem() -> label.ifBlank { "" }
        type.usesProgramBenefit() -> label.ifBlank { "" }
        else -> label
    }
}

private fun RewardProgram.programBenefitSubtitle(currencyCode: String): String = buildString {
    append(
        when (type) {
            LoyaltyProgramType.POINTS -> "Points rewards"
            LoyaltyProgramType.DIGITAL_STAMP -> "Check-in rewards"
            LoyaltyProgramType.TIER -> "Tiered loyalty"
            LoyaltyProgramType.COUPON -> "Coupon rewards"
            LoyaltyProgramType.PURCHASE_FREQUENCY -> "Purchase frequency"
            LoyaltyProgramType.REFERRAL -> "Referral rewards"
            LoyaltyProgramType.HYBRID -> "Hybrid program"
        },
    )
    val detail = when {
        configuration.visitCheckInEnabled -> "After ${configuration.checkInRule.visitsRequired} visits"
        configuration.purchaseFrequencyEnabled -> "${configuration.purchaseFrequencyRule.purchaseCount} purchases"
        configuration.couponEnabled -> configuration.couponRule.couponName
        configuration.tierTrackingEnabled -> "${configuration.tierRule.configurableLevels.size} tier levels"
        configuration.earningEnabled -> "${configuration.pointsRule.pointsAwardedPerStep} pts per ${formatWholeCurrency(configuration.pointsRule.spendStepAmount.toDouble(), currencyCode)}"
        configuration.referralEnabled -> "Referral rewards"
        else -> ""
    }
    if (detail.isNotBlank()) {
        append(" • ")
        append(detail)
    }
}

@Composable
internal fun branchImpactSummary(
    storeName: String,
    scanActions: Set<RewardProgramScanAction>,
): String {
    val displayStoreName = storeName.ifBlank { stringResource(R.string.merchant_select_store) }
    return if (scanActions.isEmpty()) {
        stringResource(R.string.merchant_program_branch_impact_no_scan_actions, displayStoreName)
    } else {
        stringResource(
            R.string.merchant_program_branch_impact_scan_actions,
            displayStoreName,
            scanActions.size,
        )
    }
}

@Composable
private fun RewardProgramScanAction.displayLabel(): String = when (this) {
    RewardProgramScanAction.EARN_POINTS -> stringResource(R.string.merchant_program_scan_action_earn_points)
    RewardProgramScanAction.REDEEM_REWARDS -> stringResource(R.string.merchant_program_scan_action_redeem_rewards)
    RewardProgramScanAction.CHECK_IN -> stringResource(R.string.merchant_program_scan_action_check_in)
    RewardProgramScanAction.APPLY_CASHBACK -> stringResource(R.string.merchant_program_scan_action_cashback)
    RewardProgramScanAction.TRACK_TIER_PROGRESS -> stringResource(R.string.merchant_program_scan_action_tier_tracking)
}

@Composable
internal fun ProgramScheduleSection(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    onAutoScheduleEnabledChange: (Boolean) -> Unit,
    onScheduleStartDateChange: (String) -> Unit,
    onScheduleEndDateChange: (String) -> Unit,
    onAnnualRepeatEnabledChange: (Boolean) -> Unit,
    benefitResetType: ProgramBenefitResetType,
    benefitResetCustomDays: String,
    onBenefitResetTypeChange: (ProgramBenefitResetType) -> Unit,
    onBenefitResetCustomDaysChange: (String) -> Unit,
) {
    ProgramSectionCard(
        title = stringResource(R.string.merchant_program_editor_schedule_title),
        subtitle = "",
    ) {
        ProgramScheduleFields(
            editorState = editorState,
            fieldErrors = fieldErrors,
            onAutoScheduleEnabledChange = onAutoScheduleEnabledChange,
            onScheduleStartDateChange = onScheduleStartDateChange,
            onScheduleEndDateChange = onScheduleEndDateChange,
            onAnnualRepeatEnabledChange = onAnnualRepeatEnabledChange,
            benefitResetType = benefitResetType,
            benefitResetCustomDays = benefitResetCustomDays,
            onBenefitResetTypeChange = onBenefitResetTypeChange,
            onBenefitResetCustomDaysChange = onBenefitResetCustomDaysChange,
        )
    }
}

@Composable
private fun ProgramScheduleFields(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    onAutoScheduleEnabledChange: (Boolean) -> Unit,
    onScheduleStartDateChange: (String) -> Unit,
    onScheduleEndDateChange: (String) -> Unit,
    onAnnualRepeatEnabledChange: (Boolean) -> Unit,
    benefitResetType: ProgramBenefitResetType,
    benefitResetCustomDays: String,
    onBenefitResetTypeChange: (ProgramBenefitResetType) -> Unit,
    onBenefitResetCustomDaysChange: (String) -> Unit,
) {
    val context = LocalContext.current
    ProgramToggleRow(
        title = stringResource(R.string.merchant_program_form_auto_schedule),
        subtitle = stringResource(R.string.merchant_program_form_auto_schedule_supporting),
        checked = editorState.autoScheduleEnabled,
        onCheckedChange = onAutoScheduleEnabledChange,
    )
    if (editorState.autoScheduleEnabled) {
        DateField(
            value = editorState.scheduleStartDate,
            label = stringResource(R.string.merchant_program_form_schedule_start_date),
            icon = Icons.Default.Event,
            errorRes = fieldErrors[PROGRAM_FIELD_SCHEDULE_START],
            supportingText = null,
            onClick = {
                context.openProgramDatePicker(editorState.scheduleStartDate) { onScheduleStartDateChange(it.toString()) }
            },
        )
        DateField(
            value = editorState.scheduleEndDate,
            label = stringResource(R.string.merchant_program_form_schedule_end_date),
            icon = Icons.Default.Event,
            errorRes = fieldErrors[PROGRAM_FIELD_SCHEDULE_END],
            supportingText = null,
            onClick = {
                context.openProgramDatePicker(editorState.scheduleEndDate) { onScheduleEndDateChange(it.toString()) }
            },
        )
        ProgramToggleRow(
            title = stringResource(R.string.merchant_program_form_annual_repeat),
            subtitle = stringResource(R.string.merchant_program_form_annual_repeat_supporting),
            checked = editorState.annualRepeatEnabled,
            onCheckedChange = onAnnualRepeatEnabledChange,
        )
    }
    ProgramBenefitResetSection(
        selectedType = benefitResetType,
        customDays = benefitResetCustomDays,
        errorRes = fieldErrors[PROGRAM_FIELD_BENEFIT_RESET_CUSTOM_DAYS],
        onTypeChange = onBenefitResetTypeChange,
        onCustomDaysChange = onBenefitResetCustomDaysChange,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProgramBenefitResetSection(
    selectedType: ProgramBenefitResetType,
    customDays: String,
    errorRes: Int?,
    onTypeChange: (ProgramBenefitResetType) -> Unit,
    onCustomDaysChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.merchant_program_benefit_reset_title),
            style = MaterialTheme.typography.titleSmall,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.merchant_program_benefit_reset_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.64f),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProgramBenefitResetType.entries.forEach { option ->
                com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip(
                    text = stringResource(option.labelRes()),
                    selected = selectedType == option,
                    onClick = { onTypeChange(option) },
                )
            }
        }
        if (selectedType == ProgramBenefitResetType.CUSTOM) {
            IntegerField(
                value = customDays,
                onValueChange = onCustomDaysChange,
                label = stringResource(R.string.merchant_program_benefit_reset_custom_label),
                icon = Icons.Default.Repeat,
                errorRes = errorRes,
                supportingText = null,
            )
        }
    }
}

@Composable
private fun ProgramGoalStepFields(
    editorState: ProgramEditorState,
    availablePrograms: List<RewardProgram>,
    availableRewards: List<Reward>,
    currencyCode: String,
    fieldErrors: Map<String, Int>,
    onPointsSpendStepAmountChange: (String) -> Unit,
    onPointsAwardedPerStepChange: (String) -> Unit,
    onPointsWelcomeBonusChange: (String) -> Unit,
    onPointsMinimumRedeemChange: (String) -> Unit,
    onCashbackPercentChange: (String) -> Unit,
    onCashbackMinimumSpendAmountChange: (String) -> Unit,
    onTierThresholdBasisChange: (TierThresholdBasis) -> Unit,
    onTierNameChange: (String, String) -> Unit,
    onTierThresholdChange: (String, String) -> Unit,
    onTierBenefitTypeChange: (String, TierBenefitType) -> Unit,
    onTierBonusPercentChange: (String, String) -> Unit,
    onTierRewardTypeChange: (String, ProgramRewardOutcomeType) -> Unit,
    onTierRewardLabelChange: (String, String) -> Unit,
    onTierRewardPointsChange: (String, String) -> Unit,
    onTierRewardRewardIdChange: (String, String?) -> Unit,
    onTierRewardProgramIdChange: (String, String?) -> Unit,
    onAddTier: () -> Unit,
    onRemoveTier: (String) -> Unit,
    onCouponNameChange: (String) -> Unit,
    onCouponPointsCostChange: (String) -> Unit,
    onCouponDiscountAmountChange: (String) -> Unit,
    onCouponMinimumSpendAmountChange: (String) -> Unit,
    onCheckInVisitsRequiredChange: (String) -> Unit,
    onPurchaseFrequencyCountChange: (String) -> Unit,
    onPurchaseFrequencyWindowDaysChange: (String) -> Unit,
    onReferralCodePrefixChange: (String) -> Unit,
    onRewardOutcomeTypeChange: (ProgramRewardSlot, ProgramRewardOutcomeType) -> Unit,
    onRewardOutcomePointsChange: (ProgramRewardSlot, String) -> Unit,
    onRewardOutcomeRewardIdChange: (ProgramRewardSlot, String?) -> Unit,
    onRewardOutcomeProgramIdChange: (ProgramRewardSlot, String?) -> Unit,
    onOpenTierBenefitEditor: (String) -> Unit,
    onClearTierBenefit: (String) -> Unit,
    onOpenRewardsCatalog: () -> Unit,
    onOpenProgramsCatalog: () -> Unit,
) {
    when (editorState.type) {
        LoyaltyProgramType.POINTS -> {
            IntegerField(
                value = editorState.pointsSpendStepAmount,
                onValueChange = onPointsSpendStepAmountChange,
                label = stringResource(R.string.merchant_program_form_points_step_amount),
                icon = Icons.Default.Payments,
                errorRes = fieldErrors[PROGRAM_FIELD_POINTS_STEP],
                supportingText = null,
            )
            IntegerField(
                value = editorState.pointsAwardedPerStep,
                onValueChange = onPointsAwardedPerStepChange,
                label = stringResource(R.string.merchant_program_form_points_awarded),
                icon = Icons.Default.Add,
                errorRes = fieldErrors[PROGRAM_FIELD_POINTS_AWARDED],
                supportingText = null,
            )
            IntegerField(
                value = editorState.pointsWelcomeBonus,
                onValueChange = onPointsWelcomeBonusChange,
                label = stringResource(R.string.merchant_program_form_points_welcome_bonus),
                icon = Icons.Default.Star,
                errorRes = null,
                supportingText = null,
            )
            IntegerField(
                value = editorState.pointsMinimumRedeem,
                onValueChange = onPointsMinimumRedeemChange,
                label = stringResource(R.string.merchant_program_form_points_minimum_redeem),
                icon = Icons.Default.CardGiftcard,
                errorRes = fieldErrors[PROGRAM_FIELD_POINTS_REDEEM],
                supportingText = null,
            )
        }

        LoyaltyProgramType.DIGITAL_STAMP -> {
            IntegerField(
                value = editorState.checkInVisitsRequired,
                onValueChange = onCheckInVisitsRequiredChange,
                label = stringResource(R.string.merchant_program_form_checkin_visits),
                icon = Icons.Default.Repeat,
                errorRes = fieldErrors[PROGRAM_FIELD_CHECKIN_VISITS],
                supportingText = null,
            )
            ProgramBenefitEditorCard(
                title = stringResource(R.string.merchant_program_checkin_reward_title),
            ) {
                ProgramSlotBenefitFields(
                    state = editorState.checkInReward,
                    slot = ProgramRewardSlot.CHECK_IN,
                    availablePrograms = availablePrograms,
                    availableRewards = availableRewards,
                    errorRes = fieldErrors[PROGRAM_FIELD_CHECKIN_REWARD],
                    pointsLabel = stringResource(R.string.merchant_program_reward_points_checkin_label),
                    onRewardOutcomeTypeChange = onRewardOutcomeTypeChange,
                    onRewardOutcomePointsChange = onRewardOutcomePointsChange,
                    onRewardOutcomeRewardIdChange = onRewardOutcomeRewardIdChange,
                    onRewardOutcomeProgramIdChange = onRewardOutcomeProgramIdChange,
                    onOpenRewardsCatalog = onOpenRewardsCatalog,
                    onOpenProgramsCatalog = onOpenProgramsCatalog,
                )
            }
        }

        LoyaltyProgramType.TIER -> {
            TierProgressExplainer()
            TierProgramLevelsEditor(
                tierThresholdBasis = editorState.tierThresholdBasis,
                tierLevels = editorState.tierLevels,
                availablePrograms = availablePrograms,
                availableRewards = availableRewards,
                currencyCode = currencyCode,
                fieldErrors = fieldErrors,
                onTierThresholdBasisChange = onTierThresholdBasisChange,
                onTierNameChange = onTierNameChange,
                onTierThresholdChange = onTierThresholdChange,
                onTierBenefitTypeChange = onTierBenefitTypeChange,
                onTierBonusPercentChange = onTierBonusPercentChange,
                onTierRewardTypeChange = onTierRewardTypeChange,
                onTierRewardPointsChange = onTierRewardPointsChange,
                onTierRewardRewardIdChange = onTierRewardRewardIdChange,
                onTierRewardProgramIdChange = onTierRewardProgramIdChange,
                onClearTierBenefit = onClearTierBenefit,
                onAddTier = onAddTier,
                onRemoveTier = onRemoveTier,
                onOpenRewardsCatalog = onOpenRewardsCatalog,
                onOpenProgramsCatalog = onOpenProgramsCatalog,
            )
        }

        LoyaltyProgramType.COUPON -> {
            TextField(
                value = editorState.couponName,
                onValueChange = onCouponNameChange,
                label = stringResource(R.string.merchant_program_form_coupon_name),
                icon = Icons.Default.CardGiftcard,
                errorRes = fieldErrors[PROGRAM_FIELD_COUPON_NAME],
                supportingText = null,
            )
            IntegerField(
                value = editorState.couponPointsCost,
                onValueChange = onCouponPointsCostChange,
                label = stringResource(R.string.merchant_program_form_coupon_points_cost),
                icon = Icons.Default.Star,
                errorRes = fieldErrors[PROGRAM_FIELD_COUPON_POINTS],
                supportingText = null,
            )
            DecimalField(
                value = editorState.couponDiscountAmount,
                onValueChange = onCouponDiscountAmountChange,
                label = stringResource(R.string.merchant_program_form_coupon_discount_amount),
                icon = Icons.Default.Payments,
                errorRes = fieldErrors[PROGRAM_FIELD_COUPON_DISCOUNT],
                supportingText = null,
            )
            DecimalField(
                value = editorState.couponMinimumSpendAmount,
                onValueChange = onCouponMinimumSpendAmountChange,
                label = stringResource(R.string.merchant_program_form_coupon_minimum_spend),
                icon = Icons.Default.Payments,
                errorRes = null,
                supportingText = null,
            )
        }

        LoyaltyProgramType.PURCHASE_FREQUENCY -> {
            IntegerField(
                value = editorState.purchaseFrequencyCount,
                onValueChange = onPurchaseFrequencyCountChange,
                label = stringResource(R.string.merchant_program_form_frequency_count),
                icon = Icons.Default.Repeat,
                errorRes = fieldErrors[PROGRAM_FIELD_FREQUENCY_COUNT],
                supportingText = null,
            )
            IntegerField(
                value = editorState.purchaseFrequencyWindowDays,
                onValueChange = onPurchaseFrequencyWindowDaysChange,
                label = stringResource(R.string.merchant_program_form_frequency_window_days),
                icon = Icons.Default.Event,
                errorRes = fieldErrors[PROGRAM_FIELD_FREQUENCY_WINDOW],
                supportingText = null,
            )
            ProgramBenefitEditorCard(
                title = stringResource(R.string.merchant_program_frequency_reward_title),
            ) {
                ProgramSlotBenefitFields(
                    state = editorState.purchaseFrequencyReward,
                    slot = ProgramRewardSlot.PURCHASE_FREQUENCY,
                    availablePrograms = availablePrograms,
                    availableRewards = availableRewards,
                    errorRes = fieldErrors[PROGRAM_FIELD_FREQUENCY_REWARD],
                    pointsLabel = stringResource(R.string.merchant_program_reward_points_frequency_label),
                    onRewardOutcomeTypeChange = onRewardOutcomeTypeChange,
                    onRewardOutcomePointsChange = onRewardOutcomePointsChange,
                    onRewardOutcomeRewardIdChange = onRewardOutcomeRewardIdChange,
                    onRewardOutcomeProgramIdChange = onRewardOutcomeProgramIdChange,
                    onOpenRewardsCatalog = onOpenRewardsCatalog,
                    onOpenProgramsCatalog = onOpenProgramsCatalog,
                )
            }
        }

        LoyaltyProgramType.REFERRAL -> {
            ProgramBenefitEditorCard(
                title = stringResource(R.string.merchant_program_referral_existing_reward_title),
            ) {
                ProgramSlotBenefitFields(
                    state = editorState.referralReferrerReward,
                    slot = ProgramRewardSlot.REFERRAL_REFERRER,
                    availablePrograms = availablePrograms,
                    availableRewards = availableRewards,
                    errorRes = fieldErrors[PROGRAM_FIELD_REFERRAL_REFERRER],
                    pointsLabel = stringResource(R.string.merchant_program_reward_points_referrer_label),
                    onRewardOutcomeTypeChange = onRewardOutcomeTypeChange,
                    onRewardOutcomePointsChange = onRewardOutcomePointsChange,
                    onRewardOutcomeRewardIdChange = onRewardOutcomeRewardIdChange,
                    onRewardOutcomeProgramIdChange = onRewardOutcomeProgramIdChange,
                    onOpenRewardsCatalog = onOpenRewardsCatalog,
                    onOpenProgramsCatalog = onOpenProgramsCatalog,
                )
            }
            ProgramBenefitEditorCard(
                title = stringResource(R.string.merchant_program_referral_new_reward_title),
            ) {
                ProgramSlotBenefitFields(
                    state = editorState.referralRefereeReward,
                    slot = ProgramRewardSlot.REFERRAL_REFEREE,
                    availablePrograms = availablePrograms,
                    availableRewards = availableRewards,
                    errorRes = fieldErrors[PROGRAM_FIELD_REFERRAL_REFEREE],
                    pointsLabel = stringResource(R.string.merchant_program_reward_points_referee_label),
                    onRewardOutcomeTypeChange = onRewardOutcomeTypeChange,
                    onRewardOutcomePointsChange = onRewardOutcomePointsChange,
                    onRewardOutcomeRewardIdChange = onRewardOutcomeRewardIdChange,
                    onRewardOutcomeProgramIdChange = onRewardOutcomeProgramIdChange,
                    onOpenRewardsCatalog = onOpenRewardsCatalog,
                    onOpenProgramsCatalog = onOpenProgramsCatalog,
                )
            }
            TextField(
                value = editorState.referralCodePrefix,
                onValueChange = onReferralCodePrefixChange,
                label = stringResource(R.string.merchant_program_form_referral_prefix),
                icon = Icons.Default.Groups,
                errorRes = fieldErrors[PROGRAM_FIELD_REFERRAL_PREFIX],
                supportingText = null,
            )
        }

        LoyaltyProgramType.HYBRID -> {
            ProgramBenefitEditorCard(
                title = stringResource(R.string.merchant_program_points_earn_title),
            ) {
                IntegerField(
                    value = editorState.pointsSpendStepAmount,
                    onValueChange = onPointsSpendStepAmountChange,
                    label = stringResource(R.string.merchant_program_form_points_step_amount),
                    icon = Icons.Default.Payments,
                    errorRes = fieldErrors[PROGRAM_FIELD_POINTS_STEP],
                    supportingText = null,
                )
                IntegerField(
                    value = editorState.pointsAwardedPerStep,
                    onValueChange = onPointsAwardedPerStepChange,
                    label = stringResource(R.string.merchant_program_form_points_awarded),
                    icon = Icons.Default.Add,
                    errorRes = fieldErrors[PROGRAM_FIELD_POINTS_AWARDED],
                    supportingText = null,
                )
                IntegerField(
                    value = editorState.pointsMinimumRedeem,
                    onValueChange = onPointsMinimumRedeemChange,
                    label = stringResource(R.string.merchant_program_form_points_minimum_redeem),
                    icon = Icons.Default.CardGiftcard,
                    errorRes = fieldErrors[PROGRAM_FIELD_POINTS_REDEEM],
                    supportingText = null,
                )
            }
            ProgramBenefitEditorCard(
                title = stringResource(R.string.merchant_program_checkin_goal_title),
            ) {
                IntegerField(
                    value = editorState.checkInVisitsRequired,
                    onValueChange = onCheckInVisitsRequiredChange,
                    label = stringResource(R.string.merchant_program_form_checkin_visits),
                    icon = Icons.Default.Repeat,
                    errorRes = fieldErrors[PROGRAM_FIELD_CHECKIN_VISITS],
                    supportingText = null,
                )
                ProgramSlotBenefitFields(
                    state = editorState.checkInReward,
                    slot = ProgramRewardSlot.CHECK_IN,
                    availablePrograms = availablePrograms,
                    availableRewards = availableRewards,
                    errorRes = fieldErrors[PROGRAM_FIELD_CHECKIN_REWARD],
                    pointsLabel = stringResource(R.string.merchant_program_reward_points_checkin_label),
                    onRewardOutcomeTypeChange = onRewardOutcomeTypeChange,
                    onRewardOutcomePointsChange = onRewardOutcomePointsChange,
                    onRewardOutcomeRewardIdChange = onRewardOutcomeRewardIdChange,
                    onRewardOutcomeProgramIdChange = onRewardOutcomeProgramIdChange,
                    onOpenRewardsCatalog = onOpenRewardsCatalog,
                    onOpenProgramsCatalog = onOpenProgramsCatalog,
                )
            }
            ProgramBenefitEditorCard(
                title = stringResource(R.string.merchant_program_referral_rewards_title),
            ) {
                ProgramSlotBenefitFields(
                    state = editorState.referralReferrerReward,
                    slot = ProgramRewardSlot.REFERRAL_REFERRER,
                    availablePrograms = availablePrograms,
                    availableRewards = availableRewards,
                    errorRes = fieldErrors[PROGRAM_FIELD_REFERRAL_REFERRER],
                    pointsLabel = stringResource(R.string.merchant_program_reward_points_referrer_label),
                    onRewardOutcomeTypeChange = onRewardOutcomeTypeChange,
                    onRewardOutcomePointsChange = onRewardOutcomePointsChange,
                    onRewardOutcomeRewardIdChange = onRewardOutcomeRewardIdChange,
                    onRewardOutcomeProgramIdChange = onRewardOutcomeProgramIdChange,
                    onOpenRewardsCatalog = onOpenRewardsCatalog,
                    onOpenProgramsCatalog = onOpenProgramsCatalog,
                )
                ProgramSlotBenefitFields(
                    state = editorState.referralRefereeReward,
                    slot = ProgramRewardSlot.REFERRAL_REFEREE,
                    availablePrograms = availablePrograms,
                    availableRewards = availableRewards,
                    errorRes = fieldErrors[PROGRAM_FIELD_REFERRAL_REFEREE],
                    pointsLabel = stringResource(R.string.merchant_program_reward_points_referee_label),
                    onRewardOutcomeTypeChange = onRewardOutcomeTypeChange,
                    onRewardOutcomePointsChange = onRewardOutcomePointsChange,
                    onRewardOutcomeRewardIdChange = onRewardOutcomeRewardIdChange,
                    onRewardOutcomeProgramIdChange = onRewardOutcomeProgramIdChange,
                    onOpenRewardsCatalog = onOpenRewardsCatalog,
                    onOpenProgramsCatalog = onOpenProgramsCatalog,
                )
                TextField(
                    value = editorState.referralCodePrefix,
                    onValueChange = onReferralCodePrefixChange,
                    label = stringResource(R.string.merchant_program_form_referral_prefix),
                    icon = Icons.Default.Groups,
                    errorRes = fieldErrors[PROGRAM_FIELD_REFERRAL_PREFIX],
                    supportingText = null,
                )
            }
        }
    }
}

@Composable
private fun ProgramCreationReviewStep(
    editorState: ProgramEditorState,
    selectedStoreName: String,
    availableStores: List<Store>,
    availableRewards: List<Reward>,
    currencyCode: String,
    fieldErrors: Map<String, Int>,
    onEditBasics: () -> Unit,
    onEditGoal: () -> Unit,
    onEditAudience: () -> Unit,
    onEditAvailability: () -> Unit,
) {
    val targetedBranchSummary = when {
        availableStores.size <= 1 -> selectedStoreName.ifBlank { stringResource(R.string.merchant_select_store) }
        editorState.applyToAllBranches -> stringResource(R.string.merchant_program_branch_scope_all_title)
        editorState.targetStoreIds.isEmpty() -> stringResource(R.string.merchant_select_store)
        else -> availableStores
            .filter { editorState.targetStoreIds.contains(it.id) }
            .joinToString(", ") { it.name }
            .ifBlank { stringResource(R.string.merchant_select_store) }
    }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProgramIcon(icon = editorState.type.icon(), colors = editorState.type.gradient())
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(editorState.type.displayNameRes()),
                        style = MaterialTheme.typography.titleMedium,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.merchant_program_review_ready_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.62f),
                    )
                }
            }
            ReviewSummaryLine(
                label = stringResource(R.string.merchant_program_branch_scope_title),
                value = targetedBranchSummary,
            )
            ReviewSummaryLine(
                label = stringResource(R.string.merchant_program_creation_goal_title),
                value = customerExperienceSummary(editorState, currencyCode),
            )
        }
    }

    ProgramReviewSectionCard(
        title = stringResource(R.string.merchant_program_editor_basics_title),
        summaryTitle = editorState.name.ifBlank { stringResource(R.string.merchant_program_basics_name_missing) },
        summary = editorState.description.ifBlank { stringResource(R.string.merchant_program_basics_description_missing) },
        errorRes = fieldErrors[PROGRAM_FIELD_NAME] ?: fieldErrors[PROGRAM_FIELD_DESCRIPTION],
        onEdit = onEditBasics,
    )
    ProgramReviewSectionCard(
        title = stringResource(R.string.merchant_program_creation_goal_title),
        summaryTitle = stringResource(R.string.merchant_program_review_goal_heading),
        summary = reviewGoalSummary(editorState, availableRewards, currencyCode),
        errorRes = listOf(
            PROGRAM_FIELD_POINTS_STEP,
            PROGRAM_FIELD_POINTS_AWARDED,
            PROGRAM_FIELD_POINTS_REDEEM,
            PROGRAM_FIELD_CHECKIN_VISITS,
            PROGRAM_FIELD_CHECKIN_REWARD,
            PROGRAM_FIELD_FREQUENCY_COUNT,
            PROGRAM_FIELD_FREQUENCY_WINDOW,
            PROGRAM_FIELD_FREQUENCY_REWARD,
            PROGRAM_FIELD_COUPON_NAME,
            PROGRAM_FIELD_COUPON_POINTS,
            PROGRAM_FIELD_COUPON_DISCOUNT,
            PROGRAM_FIELD_REFERRAL_REFERRER,
            PROGRAM_FIELD_REFERRAL_REFEREE,
            PROGRAM_FIELD_REFERRAL_PREFIX,
            PROGRAM_FIELD_CASHBACK_PERCENT,
            PROGRAM_FIELD_TIER_SILVER,
        ).firstNotNullOfOrNull(fieldErrors::get),
        onEdit = onEditGoal,
    )
    ProgramReviewSectionCard(
        title = stringResource(R.string.merchant_program_audience_title),
        summaryTitle = stringResource(R.string.merchant_program_review_audience_heading),
        summary = audienceSummary(editorState),
        errorRes = fieldErrors[PROGRAM_FIELD_TARGET_AGE_MIN] ?: fieldErrors[PROGRAM_FIELD_TARGET_AGE_MAX],
        onEdit = onEditAudience,
    )
    ProgramReviewSectionCard(
        title = stringResource(R.string.merchant_program_editor_schedule_title),
        summaryTitle = stringResource(R.string.merchant_program_review_availability_heading),
        summary = availabilitySummary(editorState),
        errorRes = fieldErrors[PROGRAM_FIELD_SCHEDULE_START]
            ?: fieldErrors[PROGRAM_FIELD_SCHEDULE_END]
            ?: fieldErrors[PROGRAM_FIELD_BENEFIT_RESET_CUSTOM_DAYS],
        onEdit = onEditAvailability,
    )
}

@Composable
private fun ProgramReviewSectionCard(
    title: String,
    summaryTitle: String,
    summary: String,
    errorRes: Int?,
    onEdit: () -> Unit,
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
                        text = summaryTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.56f),
                    )
                }
                Surface(
                    onClick = onEdit,
                    color = VerevColors.AppBackground,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.merchant_program_review_change_action),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = VerevColors.Forest,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.78f),
            )
            errorRes?.let { InlineProgramError(errorRes = it) }
        }
    }
}

@Composable
private fun ReviewSummaryLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.56f),
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun reviewGoalSummary(
    editorState: ProgramEditorState,
    availableRewards: List<Reward>,
    currencyCode: String,
): String = when (editorState.type) {
    LoyaltyProgramType.POINTS -> stringResource(
        R.string.merchant_program_points_summary,
        editorState.pointsAwardedPerStep,
        formatWholeCurrency(editorState.pointsSpendStepAmount.toDoubleOrNull() ?: 0.0, currencyCode),
        editorState.pointsMinimumRedeem,
    )
    LoyaltyProgramType.DIGITAL_STAMP -> stringResource(
        R.string.merchant_program_checkin_summary,
        editorState.checkInVisitsRequired,
        benefitSummary(editorState.checkInReward, availableRewards),
    )
    LoyaltyProgramType.TIER -> tierSummary(editorState, availableRewards)
    LoyaltyProgramType.COUPON -> stringResource(
        R.string.merchant_program_coupon_summary,
        editorState.couponName.ifBlank { stringResource(R.string.merchant_program_form_coupon_name) },
        formatWholeCurrency(editorState.couponDiscountAmount.toDoubleOrNull() ?: 0.0, currencyCode),
        editorState.couponPointsCost.ifBlank { "0" },
    )
    LoyaltyProgramType.PURCHASE_FREQUENCY -> stringResource(
        R.string.merchant_program_frequency_summary,
        editorState.purchaseFrequencyCount,
        editorState.purchaseFrequencyWindowDays,
        benefitSummary(editorState.purchaseFrequencyReward, availableRewards),
    )
    LoyaltyProgramType.REFERRAL -> stringResource(
        R.string.merchant_program_referral_summary,
        benefitSummary(editorState.referralReferrerReward, availableRewards),
        benefitSummary(editorState.referralRefereeReward, availableRewards),
        editorState.referralCodePrefix.ifBlank { "REF" },
    )
    LoyaltyProgramType.HYBRID -> customerExperienceSummary(editorState, currencyCode)
}

@Composable
private fun ProgramRuleFields(
    editorState: ProgramEditorState,
    availablePrograms: List<RewardProgram>,
    availableRewards: List<Reward>,
    currencyCode: String = "AMD",
    fieldErrors: Map<String, Int>,
    onPointsSpendStepAmountChange: (String) -> Unit,
    onPointsAwardedPerStepChange: (String) -> Unit,
    onPointsWelcomeBonusChange: (String) -> Unit,
    onPointsMinimumRedeemChange: (String) -> Unit,
    onCashbackPercentChange: (String) -> Unit,
    onCashbackMinimumSpendAmountChange: (String) -> Unit,
    onTierNameChange: (String, String) -> Unit,
    onTierThresholdChange: (String, String) -> Unit,
    onTierBonusPercentChange: (String, String) -> Unit,
    onTierRewardTypeChange: (String, ProgramRewardOutcomeType) -> Unit,
    onTierRewardLabelChange: (String, String) -> Unit,
    onTierRewardPointsChange: (String, String) -> Unit,
    onTierRewardRewardIdChange: (String, String?) -> Unit,
    onTierRewardProgramIdChange: (String, String?) -> Unit,
    onAddTier: () -> Unit,
    onRemoveTier: (String) -> Unit,
    onCouponNameChange: (String) -> Unit,
    onCouponPointsCostChange: (String) -> Unit,
    onCouponDiscountAmountChange: (String) -> Unit,
    onCouponMinimumSpendAmountChange: (String) -> Unit,
    onCheckInVisitsRequiredChange: (String) -> Unit,
    onPurchaseFrequencyCountChange: (String) -> Unit,
    onPurchaseFrequencyWindowDaysChange: (String) -> Unit,
    onReferralCodePrefixChange: (String) -> Unit,
    onRewardOutcomeTypeChange: (ProgramRewardSlot, ProgramRewardOutcomeType) -> Unit,
    onRewardOutcomeLabelChange: (ProgramRewardSlot, String) -> Unit,
    onRewardOutcomePointsChange: (ProgramRewardSlot, String) -> Unit,
    onRewardOutcomeRewardIdChange: (ProgramRewardSlot, String?) -> Unit,
    onRewardOutcomeProgramIdChange: (ProgramRewardSlot, String?) -> Unit,
    onOpenSubEditor: (ProgramSubEditor) -> Unit,
    onOpenTierBenefitEditor: (String) -> Unit,
    onOpenBenefitEditor: (ProgramRewardSlot) -> Unit,
    onClearTierBenefit: (String) -> Unit,
    onOpenRewardsCatalog: () -> Unit,
) {
    when (editorState.type) {
        LoyaltyProgramType.POINTS -> {
            CompactProgramRuleCard(
                title = stringResource(R.string.merchant_program_points_earn_title),
                summary = stringResource(
                    R.string.merchant_program_points_summary,
                    editorState.pointsAwardedPerStep,
                    formatWholeCurrency(editorState.pointsSpendStepAmount.toDoubleOrNull() ?: 0.0, currencyCode),
                    editorState.pointsMinimumRedeem,
                ),
                onEdit = { onOpenSubEditor(ProgramSubEditor.EARN_RULES_EDIT) },
            )
        }
        LoyaltyProgramType.DIGITAL_STAMP -> {
            CompactProgramRuleCard(
                title = stringResource(R.string.merchant_program_checkin_goal_title),
                summary = stringResource(
                    R.string.merchant_program_checkin_summary,
                    editorState.checkInVisitsRequired,
                    benefitSummary(editorState.checkInReward, availableRewards),
                ),
                errorRes = fieldErrors[PROGRAM_FIELD_CHECKIN_VISITS] ?: fieldErrors[PROGRAM_FIELD_CHECKIN_REWARD],
                onEdit = { onOpenSubEditor(ProgramSubEditor.CHECKIN_EDIT) },
            )
        }
        LoyaltyProgramType.TIER -> {
            CompactProgramRuleCard(
                title = stringResource(R.string.merchant_program_tier_edit_action),
                summary = tierSummary(editorState, availableRewards),
                errorRes = fieldErrors.values.firstOrNull(),
                onEdit = { onOpenSubEditor(ProgramSubEditor.TIER_EDIT) },
            )
        }
        LoyaltyProgramType.COUPON -> {
            CompactProgramRuleCard(
                title = stringResource(R.string.merchant_program_reward_edit_action),
                summary = stringResource(
                    R.string.merchant_program_coupon_summary,
                    editorState.couponName.ifBlank { stringResource(R.string.merchant_program_form_coupon_name) },
                    formatWholeCurrency(editorState.couponDiscountAmount.toDoubleOrNull() ?: 0.0, currencyCode),
                    editorState.couponPointsCost.ifBlank { "0" },
                ),
                errorRes = fieldErrors[PROGRAM_FIELD_COUPON_NAME]
                    ?: fieldErrors[PROGRAM_FIELD_COUPON_DISCOUNT]
                    ?: fieldErrors[PROGRAM_FIELD_COUPON_POINTS],
                onEdit = { onOpenSubEditor(ProgramSubEditor.REWARD_EDIT) },
            )
        }
        LoyaltyProgramType.PURCHASE_FREQUENCY -> {
            CompactProgramRuleCard(
                title = stringResource(R.string.merchant_program_frequency_goal_title),
                summary = stringResource(
                    R.string.merchant_program_frequency_summary,
                    editorState.purchaseFrequencyCount,
                    editorState.purchaseFrequencyWindowDays,
                    benefitSummary(editorState.purchaseFrequencyReward, availableRewards),
                ),
                errorRes = fieldErrors[PROGRAM_FIELD_FREQUENCY_COUNT]
                    ?: fieldErrors[PROGRAM_FIELD_FREQUENCY_WINDOW]
                    ?: fieldErrors[PROGRAM_FIELD_FREQUENCY_REWARD],
                onEdit = { onOpenSubEditor(ProgramSubEditor.FREQUENCY_EDIT) },
            )
        }
        LoyaltyProgramType.REFERRAL -> {
            CompactProgramRuleCard(
                title = stringResource(R.string.merchant_program_referral_rewards_title),
                summary = stringResource(
                    R.string.merchant_program_referral_summary,
                    benefitSummary(editorState.referralReferrerReward, availableRewards),
                    benefitSummary(editorState.referralRefereeReward, availableRewards),
                    editorState.referralCodePrefix.ifBlank { "REF" },
                ),
                errorRes = fieldErrors[PROGRAM_FIELD_REFERRAL_REFERRER]
                    ?: fieldErrors[PROGRAM_FIELD_REFERRAL_REFEREE]
                    ?: fieldErrors[PROGRAM_FIELD_REFERRAL_PREFIX],
                onEdit = { onOpenSubEditor(ProgramSubEditor.REFERRAL_EDIT) },
            )
        }
        LoyaltyProgramType.HYBRID -> {
            CompactProgramRuleCard(
                title = stringResource(R.string.merchant_program_points_earn_title),
                summary = stringResource(
                    R.string.merchant_program_points_summary,
                    editorState.pointsAwardedPerStep,
                    formatWholeCurrency(editorState.pointsSpendStepAmount.toDoubleOrNull() ?: 0.0, currencyCode),
                    editorState.pointsMinimumRedeem.ifBlank { "0" },
                ),
                errorRes = fieldErrors[PROGRAM_FIELD_POINTS_STEP] ?: fieldErrors[PROGRAM_FIELD_POINTS_AWARDED],
                onEdit = { onOpenSubEditor(ProgramSubEditor.EARN_RULES_EDIT) },
            )
            CompactProgramRuleCard(
                title = stringResource(R.string.merchant_program_checkin_goal_title),
                summary = stringResource(
                    R.string.merchant_program_checkin_summary,
                    editorState.checkInVisitsRequired,
                    benefitSummary(editorState.checkInReward, availableRewards),
                ),
                errorRes = fieldErrors[PROGRAM_FIELD_CHECKIN_VISITS] ?: fieldErrors[PROGRAM_FIELD_CHECKIN_REWARD],
                onEdit = { onOpenSubEditor(ProgramSubEditor.CHECKIN_EDIT) },
            )
            CompactProgramRuleCard(
                title = stringResource(R.string.merchant_program_referral_rewards_title),
                summary = stringResource(
                    R.string.merchant_program_referral_summary,
                    benefitSummary(editorState.referralReferrerReward, availableRewards),
                    benefitSummary(editorState.referralRefereeReward, availableRewards),
                    editorState.referralCodePrefix.ifBlank { "REF" },
                ),
                errorRes = fieldErrors[PROGRAM_FIELD_REFERRAL_REFERRER]
                    ?: fieldErrors[PROGRAM_FIELD_REFERRAL_REFEREE],
                onEdit = { onOpenSubEditor(ProgramSubEditor.REFERRAL_EDIT) },
            )
        }
    }
}

@Composable
private fun ProgramBenefitEditorCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
        content()
    }
}

@Composable
private fun ProgramSlotBenefitFields(
    state: ProgramRewardOutcomeEditorState,
    slot: ProgramRewardSlot,
    availablePrograms: List<RewardProgram>,
    availableRewards: List<Reward>,
    errorRes: Int?,
    pointsLabel: String,
    onRewardOutcomeTypeChange: (ProgramRewardSlot, ProgramRewardOutcomeType) -> Unit,
    onRewardOutcomePointsChange: (ProgramRewardSlot, String) -> Unit,
    onRewardOutcomeRewardIdChange: (ProgramRewardSlot, String?) -> Unit,
    onRewardOutcomeProgramIdChange: (ProgramRewardSlot, String?) -> Unit,
    onOpenRewardsCatalog: () -> Unit,
    onOpenProgramsCatalog: () -> Unit,
) {
    ProgramBenefitEditorFields(
        state = state,
        availablePrograms = availablePrograms,
        availableRewards = availableRewards,
        errorRes = errorRes,
        pointsLabel = pointsLabel,
        optional = false,
        onChoiceChange = { choice ->
            onRewardOutcomeTypeChange(
                slot,
                when (choice) {
                    ProgramBenefitChoice.POINTS -> ProgramRewardOutcomeType.POINTS
                    ProgramBenefitChoice.REWARD_CATALOG -> ProgramRewardOutcomeType.FREE_PRODUCT
                    ProgramBenefitChoice.PROGRAM -> ProgramRewardOutcomeType.PROGRAM_POINTS
                },
            )
        },
        onPointsChange = { onRewardOutcomePointsChange(slot, it) },
        onRewardIdChange = { onRewardOutcomeRewardIdChange(slot, it) },
        onProgramIdChange = { onRewardOutcomeProgramIdChange(slot, it) },
        onOpenRewardsCatalog = onOpenRewardsCatalog,
        onOpenProgramsCatalog = onOpenProgramsCatalog,
    )
}

@Composable
internal fun ProgramRewardOutcomeFields(
    state: ProgramRewardOutcomeEditorState,
    slot: ProgramRewardSlot,
    availablePrograms: List<RewardProgram>,
    availableRewards: List<Reward>,
    errorRes: Int?,
    onTypeChange: (ProgramRewardSlot, ProgramRewardOutcomeType) -> Unit,
    onLabelChange: (ProgramRewardSlot, String) -> Unit,
    onPointsChange: (ProgramRewardSlot, String) -> Unit,
    onRewardIdChange: (ProgramRewardSlot, String?) -> Unit,
    onProgramIdChange: (ProgramRewardSlot, String?) -> Unit,
    onOpenRewardsCatalog: () -> Unit = {},
    title: String = "",
    pointsLabel: String = "",
    rewardPickerEmptyLabel: String = "",
    programPickerEmptyLabel: String = "",
) {
    val selectedChoice = state.type.choice()
    val filteredRewards = availableRewards
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = title.ifBlank { stringResource(R.string.merchant_program_reward_outcome_title) },
            style = MaterialTheme.typography.titleSmall,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )

        RewardOutcomeChoiceList(
            selected = selectedChoice,
            onSelected = { choice ->
                onTypeChange(slot, defaultOutcomeTypeForChoice(choice))
            },
        )

        if (selectedChoice == ProgramRewardChoice.POINTS) {
            IntegerField(
                value = state.pointsAmount,
                onValueChange = { onPointsChange(slot, it) },
                label = pointsLabel.ifBlank { stringResource(R.string.merchant_program_reward_points_value) },
                icon = Icons.Default.Star,
                errorRes = errorRes,
                supportingText = null,
            )
        } else {
            RewardOutcomePickerList(
                options = filteredRewards.map {
                    RewardOutcomePickerOption(
                        id = it.id,
                        title = it.name,
                        subtitle = "${it.rewardType.displayName()} • ${it.pointsRequired} pts",
                    )
                },
                selectedId = state.rewardId,
                emptyLabel = rewardPickerEmptyLabel.ifBlank { rewardChoiceEmptyLabel(selectedChoice) },
                onSelected = { onRewardIdChange(slot, it) },
            )
            OutlinedButton(
                onClick = onOpenRewardsCatalog,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.merchant_program_open_reward_catalog_action))
            }
            errorRes?.let { InlineProgramError(errorRes = it) }
        }
    }
}

@Composable
internal fun ProgramBenefitSummarySection(
    title: String,
    state: ProgramRewardOutcomeEditorState,
    availableRewards: List<Reward>,
    errorRes: Int?,
    optional: Boolean,
    onOpenEditor: () -> Unit,
    onClear: (() -> Unit)? = null,
) {
    val configured = state.isConfigured()
    val selectedReward = availableRewards.firstOrNull { it.id == state.rewardId }
    val summaryTitle = when {
        !configured -> stringResource(
            if (optional) {
                R.string.merchant_program_benefit_none_optional
            } else {
                R.string.merchant_program_benefit_none_required
            },
        )
        state.type == ProgramRewardOutcomeType.POINTS -> stringResource(
            R.string.merchant_program_benefit_points_summary,
            state.pointsAmount.toIntOrNull() ?: 0,
        )
        else -> selectedReward?.name ?: state.label.ifBlank { stringResource(R.string.merchant_program_reward_catalog_label) }
    }
    val summarySubtitle = when {
        !configured -> stringResource(R.string.merchant_program_benefit_none_subtitle)
        state.type == ProgramRewardOutcomeType.POINTS -> stringResource(R.string.merchant_program_benefit_points_subtitle)
        else -> selectedReward?.rewardType?.displayName()
            ?: stringResource(R.string.merchant_program_benefit_reward_catalog_subtitle)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = if (state.type == ProgramRewardOutcomeType.POINTS) {
                    VerevColors.Gold.copy(alpha = 0.14f)
                } else {
                    VerevColors.Forest.copy(alpha = 0.10f)
                },
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    imageVector = if (state.type == ProgramRewardOutcomeType.POINTS) Icons.Default.Star else Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = if (state.type == ProgramRewardOutcomeType.POINTS) VerevColors.Gold else VerevColors.Forest,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(18.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = summaryTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = summarySubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.64f),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onOpenEditor,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VerevColors.Forest),
            ) {
                Text(
                    text = stringResource(
                        if (configured) R.string.merchant_program_benefit_edit_action
                        else if (optional) R.string.merchant_program_benefit_add_action
                        else R.string.merchant_program_benefit_set_action,
                    ),
                )
            }
            if (configured && optional && onClear != null) {
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VerevColors.ErrorText),
                ) {
                    Text(stringResource(R.string.merchant_program_benefit_remove_action))
                }
            }
        }
        errorRes?.let { InlineProgramError(errorRes = it) }
    }
}

@Composable
private fun CompactProgramRuleCard(
    title: String,
    summary: String,
    errorRes: Int? = null,
    onEdit: () -> Unit,
) {
    ProgramSectionCard(title = title, subtitle = "") {
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest.copy(alpha = 0.78f),
        )
        errorRes?.let { InlineProgramError(errorRes = it) }
        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VerevColors.Forest),
        ) {
            Text(stringResource(R.string.merchant_program_edit_details_action))
        }
    }
}

@Composable
private fun benefitSummary(
    state: ProgramRewardOutcomeEditorState,
    availableRewards: List<Reward>,
): String = when {
    !state.isConfigured() -> stringResource(R.string.merchant_program_benefit_none_short)
    state.type == ProgramRewardOutcomeType.POINTS -> stringResource(
        R.string.merchant_program_benefit_points_summary,
        state.pointsAmount.toIntOrNull() ?: 0,
    )
    else -> availableRewards.firstOrNull { it.id == state.rewardId }?.name
        ?: state.label.ifBlank { stringResource(R.string.merchant_program_reward_catalog_label) }
}

@Composable
private fun tierSummary(
    editorState: ProgramEditorState,
    availableRewards: List<Reward>,
): String {
    val levels = editorState.tierLevels.size
    val first = editorState.tierLevels.firstOrNull()
    val last = editorState.tierLevels.lastOrNull()
    val perks = editorState.tierLevels.count { it.rewardOutcome.isConfigured() }
    val baseRange = if (first != null && last != null) {
        stringResource(
            R.string.merchant_program_tier_summary_range,
            first.name.ifBlank { stringResource(R.string.merchant_program_tier_entry_title) },
            last.name.ifBlank { stringResource(R.string.merchant_program_tier_level_title, levels) },
        )
    } else {
        stringResource(R.string.merchant_program_preview_tier_empty_message)
    }
    val bonusRange = editorState.tierLevels
        .mapNotNull { it.bonusPercent.toIntOrNull() }
        .let { bonuses ->
            if (bonuses.isEmpty()) "0%" else "${bonuses.minOrNull() ?: 0}% - ${bonuses.maxOrNull() ?: 0}%"
        }
    val sampleBenefit = editorState.tierLevels.firstOrNull { it.rewardOutcome.isConfigured() }?.let {
        benefitSummary(it.rewardOutcome, availableRewards)
    } ?: stringResource(R.string.merchant_program_benefit_none_short)
    return stringResource(
        R.string.merchant_program_tier_summary,
        levels,
        baseRange,
        bonusRange,
        perks,
        sampleBenefit,
    )
}

@Composable
internal fun ProgramBenefitEditorFields(
    state: ProgramRewardOutcomeEditorState,
    availablePrograms: List<RewardProgram>,
    availableRewards: List<Reward>,
    errorRes: Int?,
    pointsLabel: String,
    currencyCode: String = "AMD",
    optional: Boolean,
    onChoiceChange: (ProgramBenefitChoice) -> Unit,
    onPointsChange: (String) -> Unit,
    onRewardIdChange: (String?) -> Unit,
    onProgramIdChange: (String?) -> Unit,
    onOpenRewardsCatalog: () -> Unit,
    onOpenProgramsCatalog: () -> Unit,
    onClear: (() -> Unit)? = null,
) {
    val selectedChoice = state.benefitChoice()
    var activeChooser by rememberSaveable { androidx.compose.runtime.mutableStateOf<ProgramBenefitChoice?>(null) }
    var emptyChoiceDialog by rememberSaveable { androidx.compose.runtime.mutableStateOf<ProgramBenefitChoice?>(null) }
    val rewardOptions = availableRewards.map {
        RewardOutcomePickerOption(
            id = it.id,
            title = it.name,
            subtitle = "${it.rewardType.displayName()} • ${it.pointsRequired} pts",
        )
    }
    val programOptions = availablePrograms.map {
        RewardOutcomePickerOption(
            id = it.id,
            title = it.name,
            subtitle = it.programBenefitSubtitle(currencyCode),
        )
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RewardBenefitChoiceList(
            selected = selectedChoice,
            onSelected = onChoiceChange,
        )
        if (selectedChoice == ProgramBenefitChoice.POINTS) {
            IntegerField(
                value = state.pointsAmount,
                onValueChange = onPointsChange,
                label = pointsLabel,
                icon = Icons.Default.Star,
                errorRes = errorRes,
                supportingText = null,
            )
        } else {
            val isProgramChoice = selectedChoice == ProgramBenefitChoice.PROGRAM
            val options = if (isProgramChoice) programOptions else rewardOptions
            val selectedOption = options.firstOrNull {
                it.id == if (isProgramChoice) state.programId else state.rewardId
            }
            BenefitSelectorField(
                label = stringResource(
                    if (isProgramChoice) {
                        R.string.merchant_program_benefit_program_selector_label
                    } else {
                        R.string.merchant_program_benefit_reward_selector_label
                    },
                ),
                value = selectedOption?.title.orEmpty(),
                placeholder = stringResource(
                    if (isProgramChoice) {
                        R.string.merchant_program_benefit_program_selector_placeholder
                    } else {
                        R.string.merchant_program_benefit_reward_selector_placeholder
                    },
                ),
                supporting = selectedOption?.subtitle,
                onClick = {
                    if (options.isEmpty()) {
                        emptyChoiceDialog = selectedChoice
                    } else {
                        activeChooser = selectedChoice
                    }
                },
            )
            errorRes?.let { InlineProgramError(errorRes = it) }
        }
        if (optional && state.isConfigured() && onClear != null) {
            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VerevColors.ErrorText),
            ) {
                Text(stringResource(R.string.merchant_program_benefit_remove_action))
            }
        }
    }
    activeChooser?.let { choice ->
        val options = if (choice == ProgramBenefitChoice.PROGRAM) programOptions else rewardOptions
        BenefitChooserSheet(
            title = stringResource(
                if (choice == ProgramBenefitChoice.PROGRAM) {
                    R.string.merchant_program_benefit_program_chooser_title
                } else {
                    R.string.merchant_program_benefit_reward_chooser_title
                },
            ),
            options = options,
            selectedId = if (choice == ProgramBenefitChoice.PROGRAM) state.programId else state.rewardId,
            onDismiss = { activeChooser = null },
            onSelect = { selectedId ->
                activeChooser = null
                if (choice == ProgramBenefitChoice.PROGRAM) {
                    onProgramIdChange(selectedId)
                } else {
                    onRewardIdChange(selectedId)
                }
            },
        )
    }
    emptyChoiceDialog?.let { choice ->
        AlertDialog(
            onDismissRequest = { emptyChoiceDialog = null },
            title = {
                Text(
                    text = stringResource(
                        if (choice == ProgramBenefitChoice.PROGRAM) {
                            R.string.merchant_program_benefit_empty_programs_title
                        } else {
                            R.string.merchant_program_benefit_empty_rewards_title
                        },
                    ),
                )
            },
            text = {
                Text(
                    text = stringResource(
                        if (choice == ProgramBenefitChoice.PROGRAM) {
                            R.string.merchant_program_benefit_empty_programs_message
                        } else {
                            R.string.merchant_program_benefit_empty_rewards_message
                        },
                    ),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        emptyChoiceDialog = null
                        if (choice == ProgramBenefitChoice.PROGRAM) onOpenProgramsCatalog() else onOpenRewardsCatalog()
                    },
                ) {
                    Text(
                        stringResource(
                            if (choice == ProgramBenefitChoice.PROGRAM) {
                                R.string.merchant_program_create_submit
                            } else {
                                R.string.merchant_reward_add_action
                            },
                        ),
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { emptyChoiceDialog = null }) {
                    Text(stringResource(R.string.merchant_close))
                }
            },
        )
    }
}

@Composable
private fun RewardBenefitChoiceList(
    selected: ProgramBenefitChoice,
    onSelected: (ProgramBenefitChoice) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProgramBenefitChoice.entries.forEach { choice ->
            MerchantFilterChip(
                text = stringResource(
                    when (choice) {
                        ProgramBenefitChoice.POINTS -> R.string.merchant_program_benefit_choice_points
                        ProgramBenefitChoice.REWARD_CATALOG -> R.string.merchant_program_benefit_choice_reward_catalog
                        ProgramBenefitChoice.PROGRAM -> R.string.merchant_program_benefit_choice_program
                    },
                ),
                selected = choice == selected,
                onClick = { onSelected(choice) },
            )
        }
    }
}

@Composable
private fun BenefitSelectorField(
    label: String,
    value: String,
    placeholder: String,
    supporting: String?,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = VerevColors.AppBackground,
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, VerevColors.Forest.copy(alpha = 0.10f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.56f),
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = value.ifBlank { placeholder },
                style = MaterialTheme.typography.bodyLarge,
                color = if (value.isBlank()) VerevColors.Forest.copy(alpha = 0.42f) else VerevColors.Forest,
                fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.SemiBold,
            )
            supporting?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.62f),
                )
            }
        }
    }
}

@Composable
private fun BenefitChooserSheet(
    title: String,
    options: List<RewardOutcomePickerOption>,
    selectedId: String?,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    AppBottomSheetDialog(
        onDismissRequest = onDismiss,
        fullHeight = true,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
    ) { dismiss, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedButton(onClick = dismiss) {
                    Text(stringResource(R.string.merchant_close))
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(options) { option ->
                    val isSelected = option.id == selectedId
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option.id) },
                        shape = RoundedCornerShape(22.dp),
                        color = if (isSelected) VerevColors.Forest.copy(alpha = 0.08f) else VerevColors.AppBackground,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) VerevColors.Forest else VerevColors.Forest.copy(alpha = 0.08f),
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = VerevColors.Forest,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = option.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VerevColors.Forest.copy(alpha = 0.62f),
                                )
                            }
                            if (isSelected) {
                                MerchantStatusPill(
                                    text = stringResource(R.string.merchant_program_reward_selected_label),
                                    backgroundColor = VerevColors.Forest.copy(alpha = 0.12f),
                                    contentColor = VerevColors.Forest,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class ProgramRewardChoice {
    POINTS,
    REWARD,
}

private fun ProgramRewardOutcomeType.choice(): ProgramRewardChoice = when (this) {
    ProgramRewardOutcomeType.POINTS -> ProgramRewardChoice.POINTS
    else -> ProgramRewardChoice.REWARD
}

private fun defaultOutcomeTypeForChoice(choice: ProgramRewardChoice): ProgramRewardOutcomeType = when (choice) {
    ProgramRewardChoice.POINTS -> ProgramRewardOutcomeType.POINTS
    ProgramRewardChoice.REWARD -> ProgramRewardOutcomeType.FREE_PRODUCT
}

@Composable
private fun RewardOutcomeChoiceList(
    selected: ProgramRewardChoice,
    onSelected: (ProgramRewardChoice) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ProgramRewardChoice.entries.forEach { choice ->
            val isSelected = choice == selected
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(choice) },
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) VerevColors.Forest.copy(alpha = 0.08f) else Color.White,
                border = BorderStroke(1.dp, if (isSelected) VerevColors.Forest else VerevColors.Forest.copy(alpha = 0.16f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = rewardOutcomeChoiceTitle(choice),
                            style = MaterialTheme.typography.titleSmall,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = rewardOutcomeChoiceSubtitle(choice),
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.62f),
                        )
                    }
                    if (isSelected) {
                        MerchantStatusPill(
                            text = stringResource(R.string.merchant_program_reward_selected_label),
                            backgroundColor = VerevColors.Forest.copy(alpha = 0.12f),
                            contentColor = VerevColors.Forest,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rewardOutcomeChoiceTitle(choice: ProgramRewardChoice): String = when (choice) {
    ProgramRewardChoice.POINTS -> stringResource(R.string.merchant_program_reward_choice_points)
    ProgramRewardChoice.REWARD -> stringResource(R.string.merchant_program_reward_choice_saved_reward)
}

@Composable
private fun rewardOutcomeChoiceSubtitle(choice: ProgramRewardChoice): String = when (choice) {
    ProgramRewardChoice.POINTS -> stringResource(R.string.merchant_program_reward_type_points_subtitle)
    ProgramRewardChoice.REWARD -> stringResource(R.string.merchant_program_reward_choice_saved_reward_subtitle)
}

@Composable
private fun rewardChoiceEmptyLabel(choice: ProgramRewardChoice): String = when (choice) {
    ProgramRewardChoice.REWARD -> stringResource(R.string.merchant_program_reward_picker_empty)
    else -> stringResource(R.string.merchant_program_reward_picker_empty)
}

@Composable
private fun rewardOutcomeTypeTitle(type: ProgramRewardOutcomeType): String = when (type) {
    ProgramRewardOutcomeType.POINTS -> stringResource(R.string.merchant_program_reward_type_points)
    ProgramRewardOutcomeType.FREE_PRODUCT -> stringResource(R.string.merchant_program_reward_type_free_product)
    ProgramRewardOutcomeType.DISCOUNT_COUPON -> stringResource(R.string.merchant_program_reward_type_discount_coupon)
    ProgramRewardOutcomeType.GIFT_ITEM -> stringResource(R.string.merchant_program_reward_type_gift_item)
    ProgramRewardOutcomeType.SPECIAL_PROMOTION -> stringResource(R.string.merchant_program_reward_type_special_promotion)
    ProgramRewardOutcomeType.PROGRAM_POINTS -> stringResource(R.string.merchant_program_reward_type_program_points)
    ProgramRewardOutcomeType.PROGRAM_DIGITAL_STAMP -> stringResource(R.string.merchant_program_reward_type_program_digital_stamp)
    ProgramRewardOutcomeType.PROGRAM_TIER -> stringResource(R.string.merchant_program_reward_type_program_tier)
    ProgramRewardOutcomeType.PROGRAM_COUPON -> stringResource(R.string.merchant_program_reward_type_program_coupon)
    ProgramRewardOutcomeType.PROGRAM_PURCHASE_FREQUENCY -> stringResource(R.string.merchant_program_reward_type_program_purchase_frequency)
    ProgramRewardOutcomeType.PROGRAM_REFERRAL -> stringResource(R.string.merchant_program_reward_type_program_referral)
    ProgramRewardOutcomeType.PROGRAM_HYBRID -> stringResource(R.string.merchant_program_reward_type_program_hybrid)
}

@Composable
private fun rewardOutcomeTypeSubtitle(type: ProgramRewardOutcomeType): String = when (type) {
    ProgramRewardOutcomeType.POINTS -> stringResource(R.string.merchant_program_reward_type_points_subtitle)
    ProgramRewardOutcomeType.FREE_PRODUCT -> stringResource(R.string.merchant_program_reward_type_free_product_subtitle)
    ProgramRewardOutcomeType.DISCOUNT_COUPON -> stringResource(R.string.merchant_program_reward_type_discount_coupon_subtitle)
    ProgramRewardOutcomeType.GIFT_ITEM -> stringResource(R.string.merchant_program_reward_type_gift_item_subtitle)
    ProgramRewardOutcomeType.SPECIAL_PROMOTION -> stringResource(R.string.merchant_program_reward_type_special_promotion_subtitle)
    ProgramRewardOutcomeType.PROGRAM_POINTS -> stringResource(R.string.merchant_program_reward_type_program_points_subtitle)
    ProgramRewardOutcomeType.PROGRAM_DIGITAL_STAMP -> stringResource(R.string.merchant_program_reward_type_program_digital_stamp_subtitle)
    ProgramRewardOutcomeType.PROGRAM_TIER -> stringResource(R.string.merchant_program_reward_type_program_tier_subtitle)
    ProgramRewardOutcomeType.PROGRAM_COUPON -> stringResource(R.string.merchant_program_reward_type_program_coupon_subtitle)
    ProgramRewardOutcomeType.PROGRAM_PURCHASE_FREQUENCY -> stringResource(R.string.merchant_program_reward_type_program_purchase_frequency_subtitle)
    ProgramRewardOutcomeType.PROGRAM_REFERRAL -> stringResource(R.string.merchant_program_reward_type_program_referral_subtitle)
    ProgramRewardOutcomeType.PROGRAM_HYBRID -> stringResource(R.string.merchant_program_reward_type_program_hybrid_subtitle)
}

private data class RewardOutcomePickerOption(
    val id: String,
    val title: String,
    val subtitle: String,
)

private fun decimalString(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

@Composable
private fun RewardOutcomePickerList(
    options: List<RewardOutcomePickerOption>,
    selectedId: String?,
    emptyLabel: String,
    onSelected: (String?) -> Unit,
) {
    if (options.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = VerevColors.Forest.copy(alpha = 0.05f),
        ) {
            Text(
                text = emptyLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.62f),
            )
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { option ->
            val isSelected = option.id == selectedId
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(option.id) },
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) VerevColors.Forest.copy(alpha = 0.08f) else Color.White,
                border = BorderStroke(1.dp, VerevColors.Forest.copy(alpha = if (isSelected) 1f else 0.18f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = option.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = option.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.62f),
                        )
                    }
                    if (isSelected) {
                        MerchantStatusPill(
                            text = stringResource(R.string.merchant_program_reward_selected_label),
                            backgroundColor = VerevColors.Forest.copy(alpha = 0.12f),
                            contentColor = VerevColors.Forest,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ProgramSectionCard(
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
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.62f))
            }
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
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            imeAction = androidx.compose.ui.text.input.ImeAction.Next,
        ),
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
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = androidx.compose.ui.text.input.ImeAction.Next,
        ),
    )
}

@Composable
private fun DecimalField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, errorRes: Int?, supportingText: String?) {
    MerchantFormField(
        value = value,
        onValueChange = { onValueChange(sanitizeDecimalInput(it)) },
        label = label,
        leadingIcon = icon,
        isError = errorRes != null,
        errorText = errorRes?.let { stringResource(it) },
        supportingText = supportingText,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = androidx.compose.ui.text.input.ImeAction.Next,
        ),
    )
}

@Composable
private fun DateField(value: String, label: String, icon: ImageVector, errorRes: Int?, supportingText: String?, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        MerchantFormField(
            value = value.toDisplayProgramDate(),
            onValueChange = {},
            label = label,
            leadingIcon = icon,
            readOnly = true,
            isError = errorRes != null,
            errorText = errorRes?.let { stringResource(it) },
            supportingText = supportingText,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onClick),
        )
    }
}

@Composable
internal fun ProgramsFeedbackBanner(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = VerevColors.Moss.copy(alpha = 0.12f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = VerevColors.Moss,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
internal fun ProgramToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.58f))
            }
        }
        MerchantInlineToggle(
            checked = checked,
            onCheckedChange = onCheckedChange,
            accent = VerevColors.Moss,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProgramAudienceSection(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    onTargetGenderChange: (String) -> Unit,
    onAgeTargetingEnabledChange: (Boolean) -> Unit,
    onTargetAgeMinChange: (String) -> Unit,
    onTargetAgeMaxChange: (String) -> Unit,
    onOneTimePerCustomerChange: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.merchant_program_audience_title),
            style = MaterialTheme.typography.titleSmall,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.merchant_program_audience_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.58f),
        )
        ProgramAudienceFields(
            editorState = editorState,
            fieldErrors = fieldErrors,
            onTargetGenderChange = onTargetGenderChange,
            onAgeTargetingEnabledChange = onAgeTargetingEnabledChange,
            onTargetAgeMinChange = onTargetAgeMinChange,
            onTargetAgeMaxChange = onTargetAgeMaxChange,
            onOneTimePerCustomerChange = onOneTimePerCustomerChange,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProgramAudienceFields(
    editorState: ProgramEditorState,
    fieldErrors: Map<String, Int>,
    onTargetGenderChange: (String) -> Unit,
    onAgeTargetingEnabledChange: (Boolean) -> Unit,
    onTargetAgeMinChange: (String) -> Unit,
    onTargetAgeMaxChange: (String) -> Unit,
    onOneTimePerCustomerChange: (Boolean) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(
            "ALL" to R.string.merchant_program_gender_all,
            "FEMALE" to R.string.merchant_program_gender_female,
            "MALE" to R.string.merchant_program_gender_male,
        ).forEach { (value, labelRes) ->
            com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip(
                text = stringResource(labelRes),
                selected = editorState.targetGender.equals(value, ignoreCase = true),
                onClick = { onTargetGenderChange(value) },
            )
        }
    }
    ProgramToggleRow(
        title = stringResource(R.string.merchant_program_age_filter_title),
        subtitle = stringResource(R.string.merchant_program_age_filter_subtitle),
        checked = editorState.ageTargetingEnabled,
        onCheckedChange = onAgeTargetingEnabledChange,
    )
    if (editorState.ageTargetingEnabled) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            MerchantFormField(
                value = editorState.targetAgeMin,
                onValueChange = onTargetAgeMinChange,
                label = stringResource(R.string.merchant_program_age_min_label),
                leadingIcon = Icons.Default.Event,
                modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = fieldErrors.containsKey(PROGRAM_FIELD_TARGET_AGE_MIN),
                errorText = fieldErrors[PROGRAM_FIELD_TARGET_AGE_MIN]?.let { stringResource(it) },
                supportingText = null,
            )
            MerchantFormField(
                value = editorState.targetAgeMax,
                onValueChange = onTargetAgeMaxChange,
                label = stringResource(R.string.merchant_program_age_max_label),
                leadingIcon = Icons.Default.Event,
                modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = fieldErrors.containsKey(PROGRAM_FIELD_TARGET_AGE_MAX),
                errorText = fieldErrors[PROGRAM_FIELD_TARGET_AGE_MAX]?.let { stringResource(it) },
                supportingText = null,
            )
        }
    }
    ProgramToggleRow(
        title = stringResource(R.string.merchant_program_one_time_title),
        subtitle = stringResource(R.string.merchant_program_one_time_subtitle),
        checked = editorState.oneTimePerCustomer,
        onCheckedChange = onOneTimePerCustomerChange,
    )
}

@Composable
private fun audienceSummary(editorState: ProgramEditorState): String {
    val gender = when (editorState.targetGender.uppercase()) {
        "FEMALE" -> stringResource(R.string.merchant_program_gender_female)
        "MALE" -> stringResource(R.string.merchant_program_gender_male)
        else -> stringResource(R.string.merchant_program_gender_all)
    }
    val age = if (editorState.ageTargetingEnabled) {
        val min = editorState.targetAgeMin.ifBlank { "?" }
        val max = editorState.targetAgeMax.ifBlank { "?" }
        stringResource(R.string.merchant_program_audience_age_summary, min, max)
    } else {
        stringResource(R.string.merchant_program_audience_all_ages)
    }
    val recurrence = if (editorState.oneTimePerCustomer) {
        stringResource(R.string.merchant_program_one_time_title)
    } else {
        stringResource(R.string.merchant_program_audience_repeat_allowed)
    }
    return "$gender • $age • $recurrence"
}

@Composable
private fun basicsSummary(editorState: ProgramEditorState): String {
    val name = editorState.name.ifBlank { stringResource(R.string.merchant_program_basics_name_missing) }
    val description = editorState.description.ifBlank { stringResource(R.string.merchant_program_basics_description_missing) }
    return "$name • $description"
}

@Composable
private fun availabilitySummary(editorState: ProgramEditorState): String {
    val active = if (editorState.active) {
        stringResource(R.string.merchant_program_availability_live)
    } else {
        stringResource(R.string.merchant_program_availability_paused)
    }
    val schedule = when {
        !editorState.autoScheduleEnabled -> stringResource(R.string.merchant_program_schedule_manual_summary)
        editorState.scheduleStartDate.isNotBlank() && editorState.scheduleEndDate.isNotBlank() ->
            stringResource(
                R.string.merchant_program_schedule_window_summary,
                editorState.scheduleStartDate.toDisplayProgramDate(),
                editorState.scheduleEndDate.toDisplayProgramDate(),
            )
        else -> stringResource(R.string.merchant_program_form_auto_schedule_supporting)
    }
    val recurrence = if (editorState.autoScheduleEnabled && editorState.annualRepeatEnabled) {
        stringResource(R.string.merchant_program_availability_repeats_yearly)
    } else {
        stringResource(R.string.merchant_program_availability_single_window)
    }
    val reset = when (editorState.benefitResetType) {
        ProgramBenefitResetType.NEVER -> stringResource(R.string.merchant_program_benefit_reset_never)
        ProgramBenefitResetType.MONTHLY -> stringResource(R.string.merchant_program_benefit_reset_monthly)
        ProgramBenefitResetType.YEARLY -> stringResource(R.string.merchant_program_benefit_reset_yearly)
        ProgramBenefitResetType.CUSTOM -> stringResource(
            R.string.merchant_program_benefit_reset_custom_summary,
            editorState.benefitResetCustomDays.ifBlank { "0" },
        )
    }
    return "$active • $schedule • $recurrence • $reset"
}

private fun ProgramBenefitResetType.labelRes(): Int = when (this) {
    ProgramBenefitResetType.NEVER -> R.string.merchant_program_benefit_reset_never
    ProgramBenefitResetType.MONTHLY -> R.string.merchant_program_benefit_reset_monthly
    ProgramBenefitResetType.YEARLY -> R.string.merchant_program_benefit_reset_yearly
    ProgramBenefitResetType.CUSTOM -> R.string.merchant_program_benefit_reset_custom
}

private val ProgramDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

private fun String.toDisplayProgramDate(): String = runCatching {
    LocalDate.parse(this).format(ProgramDateFormatter)
}.getOrDefault(this)

private fun android.content.Context.openProgramDatePicker(
    currentValue: String,
    onDateSelected: (LocalDate) -> Unit,
) {
    val initialDate = runCatching { LocalDate.parse(currentValue) }.getOrDefault(LocalDate.now())
    DatePickerDialog(
        this,
        { _, year, month, dayOfMonth ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth,
    ).show()
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
