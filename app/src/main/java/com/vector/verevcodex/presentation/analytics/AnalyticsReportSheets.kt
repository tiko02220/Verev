package com.vector.verevcodex.presentation.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.model.reports.ReportAutoFrequency
import com.vector.verevcodex.domain.model.reports.ReportAutoSettings
import com.vector.verevcodex.domain.model.reports.ReportFormat
import com.vector.verevcodex.domain.model.reports.ReportSection
import com.vector.verevcodex.domain.model.reports.ReportWeekday
import com.vector.verevcodex.presentation.merchant.common.MerchantFilterChip
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard
import com.vector.verevcodex.presentation.common.sheets.AppBottomSheetDialog
import com.vector.verevcodex.presentation.reports.ReportsUiState
import com.vector.verevcodex.presentation.reports.descriptionRes
import com.vector.verevcodex.presentation.reports.labelRes
import com.vector.verevcodex.presentation.reports.titleRes
import com.vector.verevcodex.presentation.theme.VerevColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AnalyticsExportSheet(
    uiState: ReportsUiState,
    selectedRange: AnalyticsTimeRange,
    onDismiss: () -> Unit,
    onExport: (ReportFormat, Set<ReportSection>) -> Unit,
    onClearError: () -> Unit,
) {
    var selectedFormat by rememberSaveable { mutableStateOf(uiState.autoSettings.format) }
    var exportRequested by rememberSaveable { mutableStateOf(false) }
    val selectedSections = remember { mutableStateListOf(*uiState.autoSettings.includedSections.toTypedArray()) }

    LaunchedEffect(exportRequested, uiState.isExporting, uiState.latestExport) {
        if (!exportRequested || uiState.isExporting) return@LaunchedEffect
        if (uiState.latestExport != null) {
            exportRequested = false
            onDismiss()
        }
    }

    AnalyticsBottomSheetFrame(
        icon = Icons.Default.Download,
        title = stringResource(R.string.merchant_analytics_export_sheet_title),
        subtitle = stringResource(R.string.merchant_analytics_export_sheet_subtitle),
        onDismiss = onDismiss,
    ) {
        AnalyticsSheetSectionTitle(
            icon = Icons.Default.Description,
            title = stringResource(R.string.merchant_analytics_export_format_title),
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ReportFormat.entries.forEach { format ->
                AnalyticsSelectableTile(
                    modifier = Modifier.defaultMinSize(minWidth = 140.dp),
                    selected = selectedFormat == format,
                    title = stringResource(format.titleRes()),
                    subtitle = stringResource(
                        when (format) {
                            ReportFormat.DOCX -> R.string.merchant_analytics_export_format_docx_supporting
                            ReportFormat.XLSX -> R.string.merchant_analytics_export_format_xlsx_supporting
                            ReportFormat.PDF -> R.string.merchant_analytics_export_format_pdf_supporting
                        }
                    ),
                    onClick = { selectedFormat = format },
                )
            }
        }

        AnalyticsSheetSectionTitle(
            icon = Icons.Default.CalendarMonth,
            title = stringResource(R.string.merchant_analytics_export_range_title),
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnalyticsTimeRange.entries.forEach { range ->
                MerchantFilterChip(
                    text = stringResource(range.analyticsRangeLabelRes()),
                    selected = range == selectedRange,
                    onClick = {},
                )
            }
        }

        AnalyticsSheetSectionTitle(
            icon = Icons.Default.Settings,
            title = stringResource(R.string.merchant_analytics_export_sections_title),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ReportSection.entries.forEach { section ->
                MerchantPrimaryCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedSections.contains(section)) {
                                    selectedSections.remove(section)
                                } else {
                                    selectedSections.add(section)
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(VerevColors.Moss.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selectedSections.contains(section)) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = VerevColors.Moss,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = VerevColors.Forest.copy(alpha = 0.4f),
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(section.labelRes()),
                                style = MaterialTheme.typography.titleMedium,
                                color = VerevColors.Forest,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(section.descriptionRes()),
                                style = MaterialTheme.typography.bodySmall,
                                color = VerevColors.Forest.copy(alpha = 0.66f),
                            )
                        }
                    }
                }
            }
        }

        uiState.error?.let { error ->
            MerchantPrimaryCard {
                Text(
                    text = stringResource(R.string.merchant_reports_error_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResourceCompatError(),
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = error.ifBlank { stringResource(R.string.merchant_reports_error_generic) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.72f),
                )
            }
        }

        Button(
            onClick = {
                exportRequested = true
                onClearError()
                onExport(selectedFormat, selectedSections.toSet())
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            enabled = selectedSections.isNotEmpty() && !uiState.isExporting,
            colors = ButtonDefaults.buttonColors(
                containerColor = VerevColors.Gold,
                contentColor = Color.White,
            ),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            if (uiState.isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.size(8.dp))
            } else {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(text = stringResource(R.string.merchant_analytics_export_action))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AnalyticsAutoReportSettingsSheet(
    currentSettings: ReportAutoSettings,
    onDismiss: () -> Unit,
    onSave: (ReportAutoSettings) -> Unit,
) {
    var enabled by rememberSaveable { mutableStateOf(currentSettings.enabled) }
    var frequency by rememberSaveable { mutableStateOf(currentSettings.frequency) }
    var format by rememberSaveable { mutableStateOf(currentSettings.format) }
    var includeAllStores by rememberSaveable { mutableStateOf(currentSettings.includeAllStores) }
    var scheduledTime by rememberSaveable { mutableStateOf(currentSettings.scheduledTime) }
    var scheduledWeekday by rememberSaveable { mutableStateOf(currentSettings.scheduledWeekday) }
    var scheduledMonthDay by rememberSaveable { mutableStateOf(currentSettings.scheduledMonthDay.toString()) }
    val recipientEmails = remember { mutableStateListOf(*currentSettings.recipientEmails.toTypedArray()) }
    val includedSections = remember { mutableStateListOf(*currentSettings.includedSections.toTypedArray()) }
    var draftEmail by rememberSaveable { mutableStateOf("") }

    AnalyticsBottomSheetFrame(
        icon = Icons.Default.Settings,
        title = stringResource(R.string.merchant_reports_auto_title),
        subtitle = stringResource(R.string.merchant_analytics_auto_report_sheet_subtitle),
        onDismiss = onDismiss,
    ) {
        MerchantPrimaryCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(VerevColors.Moss.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = VerevColors.Moss)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.merchant_analytics_auto_report_toggle_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.merchant_analytics_auto_report_toggle_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.66f),
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                )
            }
        }

        if (enabled) {
            AnalyticsSheetSectionTitle(
                icon = Icons.Default.Schedule,
                title = stringResource(R.string.merchant_analytics_auto_schedule_title),
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ReportAutoFrequency.entries.size) { index ->
                    val option = ReportAutoFrequency.entries[index]
                    MerchantFilterChip(
                        text = stringResource(option.labelRes()),
                        selected = option == frequency,
                        onClick = { frequency = option },
                    )
                }
            }

            if (frequency == ReportAutoFrequency.WEEKLY) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ReportWeekday.entries.size) { index ->
                        val option = ReportWeekday.entries[index]
                        MerchantFilterChip(
                            text = stringResource(option.labelRes()),
                            selected = option == scheduledWeekday,
                            onClick = { scheduledWeekday = option },
                        )
                    }
                }
            }

            if (frequency == ReportAutoFrequency.MONTHLY) {
                OutlinedTextField(
                    value = scheduledMonthDay,
                    onValueChange = { scheduledMonthDay = it.filter(Char::isDigit).take(2) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.merchant_analytics_auto_month_day_title)) },
                    supportingText = { Text(stringResource(R.string.merchant_analytics_auto_month_day_supporting)) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                )
            }

            OutlinedTextField(
                value = scheduledTime,
                onValueChange = { scheduledTime = it.take(5) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.merchant_analytics_auto_time_title)) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
            )

            AnalyticsSheetSectionTitle(
                icon = Icons.Default.Email,
                title = stringResource(R.string.merchant_analytics_auto_recipients_title),
            )
            recipientEmails.forEach { email ->
                MerchantPrimaryCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = VerevColors.Moss)
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VerevColors.Forest,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { recipientEmails.remove(email) }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = VerevColors.Forest.copy(alpha = 0.45f))
                        }
                    }
                }
            }
            OutlinedTextField(
                value = draftEmail,
                onValueChange = { draftEmail = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.merchant_analytics_auto_add_email_title)) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                trailingIcon = {
                    Text(
                        text = stringResource(R.string.merchant_analytics_auto_add_email_action),
                        color = VerevColors.Moss,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            val normalized = draftEmail.trim()
                            if (normalized.contains("@") && normalized.isNotBlank()) {
                                recipientEmails.add(normalized)
                                draftEmail = ""
                            }
                        },
                    )
                },
            )

            AnalyticsSheetSectionTitle(
                icon = Icons.Default.Description,
                title = stringResource(R.string.merchant_analytics_auto_format_title),
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReportFormat.entries.forEach { f ->
                    AnalyticsSelectableTile(
                        modifier = Modifier.defaultMinSize(minWidth = 140.dp),
                        selected = format == f,
                        title = stringResource(f.titleRes()),
                        subtitle = stringResource(
                            when (f) {
                                ReportFormat.DOCX -> R.string.merchant_analytics_export_format_docx_supporting
                                ReportFormat.XLSX -> R.string.merchant_analytics_export_format_xlsx_supporting
                                ReportFormat.PDF -> R.string.merchant_analytics_export_format_pdf_supporting
                            }
                        ),
                        onClick = { format = f },
                    )
                }
            }

            AnalyticsSheetSectionTitle(
                icon = Icons.Default.Description,
                title = stringResource(R.string.merchant_analytics_export_sections_title),
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportSection.entries.forEach { section ->
                    MerchantPrimaryCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (includedSections.contains(section)) {
                                        includedSections.remove(section)
                                    } else {
                                        includedSections.add(section)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Switch(
                                checked = includedSections.contains(section),
                                onCheckedChange = {
                                    if (it) includedSections.add(section) else includedSections.remove(section)
                                },
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(section.labelRes()),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = VerevColors.Forest,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = stringResource(section.descriptionRes()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VerevColors.Forest.copy(alpha = 0.66f),
                                )
                            }
                        }
                    }
                }
            }

            MerchantPrimaryCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.merchant_reports_include_all_stores_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.merchant_analytics_auto_scope_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.66f),
                        )
                    }
                    Switch(
                        checked = includeAllStores,
                        onCheckedChange = { includeAllStores = it },
                    )
                }
            }
        }

        Button(
            onClick = {
                onSave(
                    ReportAutoSettings(
                        enabled = enabled,
                        frequency = frequency,
                        format = format,
                        includeAllStores = includeAllStores,
                        scheduledTime = scheduledTime.ifBlank { currentSettings.scheduledTime },
                        scheduledWeekday = scheduledWeekday,
                        scheduledMonthDay = scheduledMonthDay.toIntOrNull()?.coerceIn(1, 28) ?: 1,
                        recipientEmails = recipientEmails.map { it.trim() }.filter { it.isNotBlank() }.toSet(),
                        includedSections = includedSections.toSet().ifEmpty { ReportSection.entries.toSet() },
                    )
                )
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerevColors.Forest,
                contentColor = Color.White,
            ),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            Text(text = stringResource(R.string.merchant_analytics_auto_save_action))
        }
    }
}

@Composable
private fun AnalyticsBottomSheetFrame(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppBottomSheetDialog(
        onDismissRequest = onDismiss,
        contentPadding = PaddingValues(bottom = 12.dp),
    ) { dismiss, _ ->
        Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(listOf(VerevColors.Forest, VerevColors.Moss)),
                            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color.White.copy(alpha = 0.16f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(icon, contentDescription = null, tint = Color.White)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.72f),
                            )
                        }
                    }
                    IconButton(onClick = dismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
                    }
                }
        }
    }
}

@Composable
private fun AnalyticsSheetSectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = VerevColors.Moss, modifier = Modifier.size(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun AnalyticsSelectableTile(
    modifier: Modifier = Modifier,
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    MerchantPrimaryCard(
        modifier = modifier
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) VerevColors.Gold.copy(alpha = 0.85f) else VerevColors.Forest.copy(alpha = 0.08f),
                shape = RoundedCornerShape(24.dp),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (selected) {
                    Box(
                        modifier = Modifier
                            .background(VerevColors.Gold.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.merchant_reports_selected),
                            style = MaterialTheme.typography.labelMedium,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.66f),
            )
        }
    }
}

@Composable
private fun colorResourceCompatError(): Color = Color(0xFFB3261E)

private fun AnalyticsTimeRange.analyticsRangeLabelRes(): Int = when (this) {
    AnalyticsTimeRange.WEEK -> R.string.merchant_range_week
    AnalyticsTimeRange.MONTH -> R.string.merchant_range_month
    AnalyticsTimeRange.QUARTER -> R.string.merchant_range_quarter
    AnalyticsTimeRange.YEAR -> R.string.merchant_range_year
}
