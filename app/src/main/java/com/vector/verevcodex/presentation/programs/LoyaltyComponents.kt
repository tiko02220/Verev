package com.vector.verevcodex.presentation.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.vector.verevcodex.domain.model.Reward
import com.vector.verevcodex.domain.model.RewardProgram
import com.vector.verevcodex.presentation.merchant.common.MerchantActionCard
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
) {
    MerchantPageHeader(
        title = stringResource(R.string.merchant_programs_title),
        subtitle = stringResource(R.string.merchant_programs_subtitle, totalPrograms, totalRewards),
    )
}

@Composable
internal fun ProgramsOverviewCard(
    programs: List<RewardProgram>,
    rewards: List<Reward>,
    campaigns: List<Campaign>,
) {
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
                value = formatCompactCount(programs.size),
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
internal fun ProgramListSection(programs: List<RewardProgram>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MerchantSectionTitle(text = stringResource(R.string.merchant_programs_active_section))
        programs.forEach { program ->
            MerchantPrimaryCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ProgramIcon(icon = Icons.Default.Loyalty, colors = listOf(VerevColors.Gold, VerevColors.Tan))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(program.name, style = MaterialTheme.typography.titleMedium, color = VerevColors.Forest, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.size(2.dp))
                        Text(program.description, style = MaterialTheme.typography.bodySmall, color = VerevColors.Forest.copy(alpha = 0.6f))
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
                    color = VerevColors.Forest.copy(alpha = 0.5f),
                )
            }
        }
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
