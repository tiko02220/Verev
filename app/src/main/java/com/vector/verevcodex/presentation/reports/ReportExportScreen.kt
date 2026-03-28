package com.vector.verevcodex.presentation.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.ScheduleSend
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.reports.ReportAutoFrequency
import com.vector.verevcodex.domain.model.reports.ReportExport
import com.vector.verevcodex.domain.model.reports.ReportFormat
import com.vector.verevcodex.presentation.merchant.common.MerchantActionCard
import com.vector.verevcodex.presentation.merchant.common.MerchantErrorDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantLoadingOverlay
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.settings.SettingsBackRow
import com.vector.verevcodex.presentation.settings.SettingsDetailRow
import com.vector.verevcodex.presentation.settings.SettingsHeroCard
import com.vector.verevcodex.presentation.theme.VerevColors
import java.time.format.DateTimeFormatter

@Composable
fun ReportExportScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingSaveReport by remember { mutableStateOf<ReportExport?>(null) }
    var saveFeedbackRes by rememberSaveable { mutableStateOf<Int?>(null) }
    val saveReportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        val report = pendingSaveReport
        if (report != null && result.resultCode == android.app.Activity.RESULT_OK) {
            val destinationUri = result.data?.data
            saveFeedbackRes = if (destinationUri != null) {
                saveReportToUri(context, report, destinationUri)
                    .fold(
                        onSuccess = { R.string.merchant_reports_saved_success },
                        onFailure = { R.string.merchant_reports_saved_failed },
                    )
            } else {
                R.string.merchant_reports_saved_cancelled
            }
        } else if (report != null) {
            saveFeedbackRes = R.string.merchant_reports_saved_cancelled
        }
        pendingSaveReport = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 24.dp,
                bottom = contentPadding.calculateBottomPadding() + 104.dp,
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
                ExportTargetCard(
                    selectedStoreName = uiState.selectedStoreName,
                    includeAllStores = uiState.autoSettings.includeAllStores,
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
                        onClick = { viewModel.export(ReportFormat.DOCX) },
                    )
                    MerchantActionCard(
                        title = stringResource(R.string.merchant_reports_excel_title),
                        subtitle = stringResource(R.string.merchant_reports_excel_subtitle),
                        icon = Icons.Default.Payments,
                        colors = listOf(VerevColors.Moss, VerevColors.Forest),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.export(ReportFormat.XLSX) },
                    )
                    MerchantActionCard(
                        title = stringResource(R.string.merchant_reports_pdf_title),
                        subtitle = stringResource(R.string.merchant_reports_pdf_subtitle),
                        icon = Icons.Default.Description,
                        colors = listOf(VerevColors.ForestBright, VerevColors.Forest),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.export(ReportFormat.PDF) },
                    )
                }
            }
            item {
                AutoReportSettingsCard(
                    enabled = uiState.autoSettings.enabled,
                    frequency = uiState.autoSettings.frequency,
                    format = uiState.autoSettings.format,
                    includeAllStores = uiState.autoSettings.includeAllStores,
                    onEnabledChanged = viewModel::setAutoReportsEnabled,
                    onFrequencyChanged = viewModel::setAutoReportFrequency,
                    onFormatChanged = viewModel::setPreferredFormat,
                    onIncludeAllStoresChanged = viewModel::setIncludeAllStores,
                )
            }
            if (uiState.isExporting) {
                item {
                    ExportingCard()
                }
            }
            saveFeedbackRes?.let { feedbackRes ->
                item {
                    MerchantPrimaryCard {
                        Text(
                            text = stringResource(feedbackRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (feedbackRes == R.string.merchant_reports_saved_success) {
                                VerevColors.Forest
                            } else {
                                VerevColors.ErrorText
                            },
                        )
                    }
                }
            }
            uiState.latestExport?.let { report ->
                item {
                    LatestExportCard(
                        report = report,
                        onOpen = { openReport(context, report) },
                        onShare = { shareReport(context, report) },
                        onSave = {
                            pendingSaveReport = report
                            saveReportLauncher.launch(createSaveReportIntent(report))
                        },
                    )
                }
            }
        }
        MerchantLoadingOverlay(
            isVisible = uiState.isExporting || uiState.isSavingAutoSettings,
            title = stringResource(
                if (uiState.isExporting) {
                    R.string.merchant_loader_report_title
                } else {
                    R.string.merchant_loader_report_settings_title
                }
            ),
            subtitle = stringResource(
                if (uiState.isExporting) {
                    R.string.merchant_loader_report_subtitle
                } else {
                    R.string.merchant_loader_report_settings_subtitle
                }
            ),
        )
        uiState.error?.let { errorMessage ->
            MerchantErrorDialog(
                title = stringResource(R.string.merchant_reports_error_title),
                message = errorMessage.ifBlank { stringResource(R.string.merchant_reports_auto_settings_error) },
                onDismiss = viewModel::clearError,
            )
        }
    }
}

@Composable
private fun ExportTargetCard(
    selectedStoreName: String,
    includeAllStores: Boolean,
) {
    MerchantPrimaryCard {
        Text(
            text = stringResource(R.string.merchant_reports_scope_title),
            style = MaterialTheme.typography.titleMedium,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
        SettingsDetailRow(
            label = stringResource(R.string.merchant_reports_scope_label),
            value = if (includeAllStores) {
                stringResource(R.string.merchant_reports_scope_all_stores)
            } else {
                selectedStoreName.ifBlank { stringResource(R.string.merchant_reports_scope_current_store_fallback) }
            },
        )
    }
}

@Composable
private fun AutoReportSettingsCard(
    enabled: Boolean,
    frequency: ReportAutoFrequency,
    format: ReportFormat,
    includeAllStores: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    onFrequencyChanged: (ReportAutoFrequency) -> Unit,
    onFormatChanged: (ReportFormat) -> Unit,
    onIncludeAllStoresChanged: (Boolean) -> Unit,
) {
    MerchantPrimaryCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ScheduleSend,
                contentDescription = null,
                tint = VerevColors.Forest,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = stringResource(R.string.merchant_reports_auto_title),
                style = MaterialTheme.typography.titleMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChanged,
            )
        }
        Text(
            text = stringResource(R.string.merchant_reports_auto_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest.copy(alpha = 0.68f),
        )
        Text(
            text = stringResource(R.string.merchant_reports_frequency_title),
            style = MaterialTheme.typography.labelLarge,
            color = VerevColors.Forest,
            fontWeight = FontWeight.Medium,
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(ReportAutoFrequency.entries.size) { index ->
                val option = ReportAutoFrequency.entries[index]
                MerchantFilterChip(
                    text = stringResource(option.labelRes()),
                    selected = option == frequency,
                    onClick = { onFrequencyChanged(option) },
                )
            }
        }
        Text(
            text = stringResource(R.string.merchant_reports_preferred_format_title),
            style = MaterialTheme.typography.labelLarge,
            color = VerevColors.Forest,
            fontWeight = FontWeight.Medium,
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(ReportFormat.entries.size) { index ->
                val option = ReportFormat.entries[index]
                MerchantFilterChip(
                    text = stringResource(option.titleRes()),
                    selected = option == format,
                    onClick = { onFormatChanged(option) },
                )
            }
        }
        SettingsDetailRow(
            label = stringResource(R.string.merchant_reports_include_all_stores_title),
            value = if (includeAllStores) {
                stringResource(R.string.merchant_reports_include_all_stores_enabled)
            } else {
                stringResource(R.string.merchant_reports_include_all_stores_disabled)
            },
            trailing = {
                Switch(
                    checked = includeAllStores,
                    onCheckedChange = onIncludeAllStoresChanged,
                )
            },
        )
    }
}

@Composable
private fun ExportingCard() {
    MerchantPrimaryCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = VerevColors.Gold,
                strokeWidth = 2.5.dp,
            )
            Text(
                text = stringResource(R.string.merchant_reports_exporting),
                style = MaterialTheme.typography.titleMedium,
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun LatestExportCard(
    report: ReportExport,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
) {
    MerchantPrimaryCard {
        Text(
            text = stringResource(R.string.merchant_reports_prepared_title),
            style = MaterialTheme.typography.titleMedium,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
        SettingsDetailRow(
            label = stringResource(R.string.merchant_reports_file_name_label),
            value = report.fileName,
        )
        SettingsDetailRow(
            label = stringResource(R.string.merchant_reports_format_label),
            value = stringResource(report.format.titleRes()),
        )
        SettingsDetailRow(
            label = stringResource(R.string.merchant_reports_generated_label),
            value = report.generatedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")),
        )
        report.storageLocation?.let { location ->
            SettingsDetailRow(
                label = stringResource(R.string.merchant_reports_storage_location_label),
                value = location,
            )
        }
        Text(
            text = report.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = VerevColors.Forest.copy(alpha = 0.68f),
        )
        Text(
            text = if (report.storageLocation != null) {
                stringResource(R.string.merchant_reports_storage_hint_external)
            } else {
                stringResource(R.string.merchant_reports_storage_hint)
            },
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.6f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onOpen,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerevColors.Forest,
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.merchant_reports_open),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerevColors.Moss,
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.merchant_reports_save_as),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onShare,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerevColors.Gold,
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.merchant_reports_share),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
