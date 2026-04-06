package com.vector.verevcodex.presentation.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.TierProgramRule
import com.vector.verevcodex.domain.model.loyalty.displayValue
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun ProgramTypeHeroCard(
    type: LoyaltyProgramType,
    spec: ProgramScreenSpec,
    storeName: String,
    totalCount: Int,
    activeCount: Int,
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(26.dp),
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(type.gradient().first().copy(alpha = 0.12f), RoundedCornerShape(22.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Brush.linearGradient(type.gradient()), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(type.icon(), contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    Text(
                        text = if (storeName.isBlank()) {
                            stringResource(R.string.merchant_programs_no_store_selected)
                        } else {
                            stringResource(R.string.merchant_programs_store_scope, storeName)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.48f),
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProgramTypeMetricCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.merchant_programs_count_label),
                    value = totalCount.toString(),
                )
                ProgramTypeMetricCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.merchant_programs_active_label),
                    value = activeCount.toString(),
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProgramTypeMetricCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.merchant_programs_live_actions_label),
                    value = programsActionCount(type, totalCount, activeCount).toString(),
                )
                ProgramTypeMetricCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.merchant_program_reward_catalog_label),
                    value = totalCount.toString(),
                )
            }
        }
    }
}

private fun programsActionCount(type: LoyaltyProgramType, totalCount: Int, activeCount: Int): Int {
    val perProgramActions = when (type) {
        LoyaltyProgramType.POINTS -> 2
        LoyaltyProgramType.TIER -> 2
        LoyaltyProgramType.COUPON -> 1
        LoyaltyProgramType.DIGITAL_STAMP -> 1
        LoyaltyProgramType.PURCHASE_FREQUENCY -> 1
        LoyaltyProgramType.REFERRAL -> 1
    }
    return if (activeCount == 0) 0 else (activeCount * perProgramActions).coerceAtLeast(totalCount)
}

@Composable
private fun ProgramTypeMetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = VerevColors.AppBackground.copy(alpha = 0.72f),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.5f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
internal fun ProgramTypeInsightCard(
    type: LoyaltyProgramType,
    programs: List<RewardProgram>,
    rewards: List<Reward>,
    currencyCode: String = "AMD",
) {
    val leadProgram = programs.firstOrNull { it.active } ?: programs.firstOrNull()
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(30.dp),
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 22.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
        Text(
            text = stringResource(R.string.merchant_program_focus_title),
            style = MaterialTheme.typography.titleLarge,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
        if (leadProgram == null) {
            Text(
                text = stringResource(R.string.merchant_program_focus_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.66f),
            )
            return@Surface
        }
        when (type) {
            LoyaltyProgramType.POINTS -> ProgramFactGrid(
                facts = listOf(
                    stringResource(R.string.merchant_program_form_points_awarded) to leadProgram.configuration.pointsRule.pointsAwardedPerStep.toString(),
                    stringResource(R.string.merchant_program_form_points_step_amount) to formatWholeCurrency(leadProgram.configuration.pointsRule.spendStepAmount.toDouble(), currencyCode),
                    stringResource(R.string.merchant_program_form_points_minimum_redeem) to leadProgram.configuration.pointsRule.minimumRedeemPoints.toString(),
                    stringResource(R.string.merchant_program_reward_catalog_label) to rewards.count { it.activeStatus }.toString(),
                ),
            )
            LoyaltyProgramType.TIER -> ProgramFactGrid(
                facts = leadProgram.configuration.tierRule.toInsightFacts(),
            )
            LoyaltyProgramType.COUPON -> ProgramFactGrid(
                facts = listOf(
                    stringResource(R.string.merchant_program_form_coupon_name) to leadProgram.configuration.couponRule.couponName,
                    stringResource(R.string.merchant_program_form_coupon_points_cost) to leadProgram.configuration.couponRule.pointsCost.toString(),
                    stringResource(R.string.merchant_program_form_coupon_discount_amount) to formatWholeCurrency(leadProgram.configuration.couponRule.discountAmount, currencyCode),
                    stringResource(R.string.merchant_program_form_coupon_minimum_spend) to formatWholeCurrency(leadProgram.configuration.couponRule.minimumSpendAmount, currencyCode),
                ),
            )
            LoyaltyProgramType.DIGITAL_STAMP -> ProgramFactGrid(
                facts = listOf(
                    stringResource(R.string.merchant_program_form_checkin_visits) to leadProgram.configuration.checkInRule.visitsRequired.toString(),
                    stringResource(R.string.merchant_program_editor_reward_title) to leadProgram.configuration.checkInRule.rewardOutcome.displayValue(),
                ),
            )
            LoyaltyProgramType.PURCHASE_FREQUENCY -> ProgramFactGrid(
                facts = listOf(
                    stringResource(R.string.merchant_program_form_frequency_count) to leadProgram.configuration.purchaseFrequencyRule.purchaseCount.toString(),
                    stringResource(R.string.merchant_program_form_frequency_window_days) to leadProgram.configuration.purchaseFrequencyRule.windowDays.toString(),
                    stringResource(R.string.merchant_program_editor_reward_title) to leadProgram.configuration.purchaseFrequencyRule.rewardOutcome.displayValue(),
                ),
            )
            LoyaltyProgramType.REFERRAL -> ProgramFactGrid(
                facts = listOf(
                    stringResource(R.string.merchant_program_form_referral_referrer_points) to leadProgram.configuration.referralRule.referrerRewardOutcome.displayValue(),
                    stringResource(R.string.merchant_program_form_referral_referee_points) to leadProgram.configuration.referralRule.refereeRewardOutcome.displayValue(),
                    stringResource(R.string.merchant_program_form_referral_prefix) to leadProgram.configuration.referralRule.referralCodePrefix,
                ),
            )
        }
        }
    }
}

@Composable
private fun TierProgramRule.toInsightFacts(): List<Pair<String, String>> {
    val tierFacts = configurableLevels.map { level ->
        level.name to stringResource(
            R.string.merchant_program_tier_fact_format,
            level.threshold,
            level.bonusPercent,
        )
    }
    return if (tierFacts.isNotEmpty()) {
        tierFacts
    } else {
        listOf(
            stringResource(R.string.merchant_program_section_tier) to stringResource(R.string.merchant_program_preview_tier_empty_message),
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ProgramFactGrid(facts: List<Pair<String, String>>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 2,
    ) {
        facts.forEach { (label, value) ->
            Surface(
                modifier = Modifier.fillMaxWidth(0.48f),
                color = VerevColors.AppBackground.copy(alpha = 0.76f),
                shape = RoundedCornerShape(22.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.52f),
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
internal fun ProgramTypeGuidanceCard(type: LoyaltyProgramType) {
    Surface(
        color = type.gradient().first().copy(alpha = 0.10f),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Brush.linearGradient(type.gradient()), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(type.icon(), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.merchant_program_workflow_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(type.templateSubtitleRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.68f),
                )
            }
        }
    }
}

@Composable
internal fun ProgramTypeProgramCard(
    type: LoyaltyProgramType,
    program: RewardProgram,
    selectedStoreName: String,
    snapshot: ProgramOperationalSnapshot,
    isBusy: Boolean,
    onEdit: (() -> Unit)?,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: (() -> Unit)?,
    canManagePrograms: Boolean = true,
) {
    val status = program.statusPresentation()
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(26.dp),
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(type.gradient().first().copy(alpha = 0.12f))
                    .padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(Brush.linearGradient(type.gradient()), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(type.icon(), contentDescription = null, tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = program.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = program.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.62f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                MerchantStatusPill(
                    text = stringResource(status.textRes),
                    backgroundColor = status.backgroundColor,
                    contentColor = status.contentColor,
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(type.summaryTitleRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.5f),
                )
                Text(
                    text = program.rulesSummary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                if (snapshot.inactiveReasons.isNotEmpty()) {
                    ProgramInlineStatusCard(
                        title = inactiveReasonTitle(snapshot.inactiveReasons.first()),
                        supporting = inactiveReasonMessage(snapshot.inactiveReasons.first()),
                    )
                } else {
                    ProgramInlineStatusCard(
                        title = stringResource(R.string.merchant_program_preview_branch_title),
                        supporting = branchImpactSummary(selectedStoreName, snapshot.scanActions),
                    )
                }
                if (snapshot.overlapWarnings.isNotEmpty()) {
                    ProgramInlineStatusCard(
                        title = stringResource(R.string.merchant_program_overlap_summary_title),
                        supporting = snapshot.overlapWarnings.take(2).joinToString(separator = " • ") { warning ->
                            when (warning) {
                                is ProgramOverlapWarning.ProgramConflict -> warning.programName
                                is ProgramOverlapWarning.CampaignConflict -> warning.campaignName
                            }
                        },
                        highlighted = true,
                    )
                }
                if (canManagePrograms) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        onEdit?.let {
                            androidx.compose.material3.OutlinedButton(
                                onClick = it,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = VerevColors.Forest,
                                ),
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(
                                    text = stringResource(R.string.merchant_program_edit_action),
                                    modifier = Modifier.padding(start = 8.dp),
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        androidx.compose.material3.OutlinedButton(
                            onClick = { onToggleEnabled(!program.active) },
                            modifier = Modifier.weight(1f),
                            enabled = !isBusy,
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = VerevColors.Forest,
                            ),
                        ) {
                            Icon(
                                imageVector = if (program.active) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = if (program.active) stringResource(R.string.merchant_program_pause_action) else stringResource(R.string.merchant_program_resume_action),
                                modifier = Modifier.padding(start = 8.dp),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                if (canManagePrograms && onDelete != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = onDelete,
                            enabled = !isBusy,
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = VerevColors.ErrorText,
                            ),
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                text = stringResource(R.string.merchant_program_delete_action),
                                modifier = Modifier.padding(start = 8.dp),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramInlineStatusCard(
    title: String,
    supporting: String,
    highlighted: Boolean = false,
) {
    Surface(
        color = if (highlighted) Color(0xFFFFF6E8) else VerevColors.AppBackground,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.68f),
            )
        }
    }
}
