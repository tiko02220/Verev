package com.vector.verevcodex.presentation.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.merchant.common.MerchantSectionTitle
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
    MerchantPrimaryCard(contentPadding = PaddingValues(22.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Brush.linearGradient(type.gradient()), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(type.icon(), contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
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
            ProgramTypeMetricCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.merchant_programs_live_actions_label),
                value = programsActionCount(type, totalCount, activeCount).toString(),
            )
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
        LoyaltyProgramType.CASHBACK -> 1
        LoyaltyProgramType.HYBRID -> 5
    }
    return if (activeCount == 0) 0 else (activeCount * perProgramActions).coerceAtLeast(totalCount)
}

@Composable
private fun ProgramTypeMetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    MerchantPrimaryCard(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
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

@Composable
internal fun ProgramTypeInsightCard(
    type: LoyaltyProgramType,
    programs: List<RewardProgram>,
    rewards: List<Reward>,
) {
    val leadProgram = programs.firstOrNull { it.active } ?: programs.firstOrNull()
    MerchantPrimaryCard {
        MerchantSectionTitle(text = stringResource(R.string.merchant_program_focus_title))
        if (leadProgram == null) {
            Text(
                text = stringResource(R.string.merchant_program_focus_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.66f),
            )
            return@MerchantPrimaryCard
        }
        when (type) {
            LoyaltyProgramType.POINTS -> ProgramFactGrid(
                facts = listOf(
                    stringResource(R.string.merchant_program_form_points_awarded) to leadProgram.configuration.pointsRule.pointsAwardedPerStep.toString(),
                    stringResource(R.string.merchant_program_form_points_step_amount) to formatWholeCurrency(leadProgram.configuration.pointsRule.spendStepAmount.toDouble()),
                    stringResource(R.string.merchant_program_form_points_minimum_redeem) to leadProgram.configuration.pointsRule.minimumRedeemPoints.toString(),
                    stringResource(R.string.merchant_program_reward_catalog_label) to rewards.count { it.activeStatus }.toString(),
                ),
            )
            LoyaltyProgramType.TIER -> ProgramFactGrid(
                facts = listOf(
                    stringResource(R.string.merchant_program_form_tier_silver) to leadProgram.configuration.tierRule.silverThreshold.toString(),
                    stringResource(R.string.merchant_program_form_tier_gold) to leadProgram.configuration.tierRule.goldThreshold.toString(),
                    stringResource(R.string.merchant_program_form_tier_vip) to leadProgram.configuration.tierRule.vipThreshold.toString(),
                    stringResource(R.string.merchant_program_form_tier_bonus_percent) to "${leadProgram.configuration.tierRule.tierBonusPercent}%",
                ),
            )
            LoyaltyProgramType.COUPON -> ProgramFactGrid(
                facts = listOf(
                    stringResource(R.string.merchant_program_form_coupon_name) to leadProgram.configuration.couponRule.couponName,
                    stringResource(R.string.merchant_program_form_coupon_points_cost) to leadProgram.configuration.couponRule.pointsCost.toString(),
                    stringResource(R.string.merchant_program_form_coupon_discount_amount) to formatWholeCurrency(leadProgram.configuration.couponRule.discountAmount),
                    stringResource(R.string.merchant_program_form_coupon_minimum_spend) to formatWholeCurrency(leadProgram.configuration.couponRule.minimumSpendAmount),
                ),
            )
            LoyaltyProgramType.DIGITAL_STAMP -> ProgramFactGrid(
                facts = listOf(
                    stringResource(R.string.merchant_program_form_checkin_visits) to leadProgram.configuration.checkInRule.visitsRequired.toString(),
                    stringResource(R.string.merchant_program_form_checkin_reward_points) to leadProgram.configuration.checkInRule.rewardPoints.toString(),
                    stringResource(R.string.merchant_program_form_checkin_reward_name) to leadProgram.configuration.checkInRule.rewardName,
                ),
            )
            LoyaltyProgramType.PURCHASE_FREQUENCY -> ProgramFactGrid(
                facts = listOf(
                    stringResource(R.string.merchant_program_form_frequency_count) to leadProgram.configuration.purchaseFrequencyRule.purchaseCount.toString(),
                    stringResource(R.string.merchant_program_form_frequency_window_days) to leadProgram.configuration.purchaseFrequencyRule.windowDays.toString(),
                    stringResource(R.string.merchant_program_form_frequency_reward_points) to leadProgram.configuration.purchaseFrequencyRule.rewardPoints.toString(),
                    stringResource(R.string.merchant_program_form_frequency_reward_name) to leadProgram.configuration.purchaseFrequencyRule.rewardName,
                ),
            )
            LoyaltyProgramType.REFERRAL -> ProgramFactGrid(
                facts = listOf(
                    stringResource(R.string.merchant_program_form_referral_referrer_points) to leadProgram.configuration.referralRule.referrerRewardPoints.toString(),
                    stringResource(R.string.merchant_program_form_referral_referee_points) to leadProgram.configuration.referralRule.refereeRewardPoints.toString(),
                    stringResource(R.string.merchant_program_form_referral_prefix) to leadProgram.configuration.referralRule.referralCodePrefix,
                ),
            )
            LoyaltyProgramType.CASHBACK,
            LoyaltyProgramType.HYBRID,
            -> ProgramFactGrid(facts = listOf(stringResource(R.string.merchant_programs_title) to leadProgram.rulesSummary))
        }
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
            MerchantPrimaryCard(
                modifier = Modifier.fillMaxWidth(0.48f),
                contentPadding = PaddingValues(14.dp),
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

@Composable
internal fun ProgramTypeGuidanceCard(type: LoyaltyProgramType) {
    MerchantPrimaryCard(
        contentPadding = PaddingValues(18.dp),
    ) {
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

@Composable
internal fun ProgramTypeProgramCard(
    type: LoyaltyProgramType,
    program: RewardProgram,
    isBusy: Boolean,
    onEdit: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    MerchantPrimaryCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Brush.linearGradient(type.gradient()), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(type.icon(), contentDescription = null, tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = program.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = program.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = VerevColors.Forest.copy(alpha = 0.62f),
                )
            }
            MerchantStatusPill(
                text = if (program.active) stringResource(R.string.merchant_program_active) else stringResource(R.string.merchant_program_disabled),
                backgroundColor = if (program.active) VerevColors.Moss.copy(alpha = 0.16f) else Color(0xFFF3F4F6),
                contentColor = if (program.active) VerevColors.Moss else VerevColors.Inactive,
            )
        }
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
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerevColors.Forest,
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(text = stringResource(R.string.merchant_program_edit_action), modifier = Modifier.padding(start = 8.dp))
            }
            Button(
                onClick = { onToggleEnabled(!program.active) },
                modifier = Modifier.weight(1f),
                enabled = !isBusy,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerevColors.AppBackground,
                    contentColor = VerevColors.Forest,
                ),
            ) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(
                    text = if (program.active) stringResource(R.string.merchant_program_pause_action) else stringResource(R.string.merchant_program_resume_action),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        Button(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFEECEC),
                contentColor = VerevColors.ErrorText,
            ),
        ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text = stringResource(R.string.merchant_program_delete_action), modifier = Modifier.padding(start = 8.dp))
        }
    }
}
