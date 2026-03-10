package com.vector.verevcodex.presentation.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.merchant.common.MerchantActionCard
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.settings.SettingsBackRow
import com.vector.verevcodex.presentation.settings.SettingsHeroCard
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun ReportExportScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val report by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 24.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SettingsBackRow(onBack = onBack)
        }
        item {
            SettingsHeroCard(
                title = stringResource(R.string.merchant_reports_title),
                subtitle = stringResource(R.string.merchant_reports_subtitle),
                icon = Icons.Default.Description,
                colors = listOf(VerevColors.Forest, VerevColors.Moss),
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MerchantActionCard(
                    title = stringResource(R.string.merchant_reports_docx_title),
                    subtitle = stringResource(R.string.merchant_reports_docx_subtitle),
                    icon = Icons.Default.Description,
                    colors = listOf(VerevColors.Gold, VerevColors.Tan),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.export("DOCX") },
                )
                MerchantActionCard(
                    title = stringResource(R.string.merchant_reports_excel_title),
                    subtitle = stringResource(R.string.merchant_reports_excel_subtitle),
                    icon = Icons.Default.Payments,
                    colors = listOf(VerevColors.Moss, VerevColors.Forest),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.export("XLSX") },
                )
            }
        }
        report?.let { prepared ->
            item {
                MerchantPrimaryCard {
                    androidx.compose.material3.Text(
                        text = stringResource(R.string.merchant_reports_prepared_title),
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = VerevColors.Forest,
                    )
                    androidx.compose.material3.Text(
                        text = prepared.fileName,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = VerevColors.Moss,
                    )
                    androidx.compose.material3.Text(
                        text = prepared.summary,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = VerevColors.Forest.copy(alpha = 0.64f),
                    )
                }
            }
        }
    }
}
