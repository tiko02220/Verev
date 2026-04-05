package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.common.input.sanitizeDecimalInput
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerMergePreview
import com.vector.verevcodex.domain.model.customer.CustomerSplitPreview
import com.vector.verevcodex.domain.model.loyalty.TierProgramRule
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.displayValue
import com.vector.verevcodex.presentation.common.sheets.AppBottomSheetDialog
import com.vector.verevcodex.presentation.merchant.common.formatCompactCount
import com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency
import com.vector.verevcodex.presentation.merchant.common.MerchantStatusPill
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun CustomerAdjustPointsSheet(
    currentPoints: Int,
    currentVisits: Int,
    availablePrograms: List<RewardProgram>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Int, String, String) -> Unit,
) {
    var amountText by rememberSaveable { mutableStateOf("") }
    var reason by rememberSaveable { mutableStateOf("") }
    var isDeduction by rememberSaveable { mutableStateOf(false) }
    var selectedProgramId by rememberSaveable { mutableStateOf(availablePrograms.firstOrNull()?.id.orEmpty()) }
    val parsedAmount = amountText.toIntOrNull()
    LaunchedEffect(availablePrograms) {
        if (selectedProgramId !in availablePrograms.map(RewardProgram::id)) {
            selectedProgramId = availablePrograms.firstOrNull()?.id.orEmpty()
        }
    }
    val validation = remember(amountText, reason) {
        CustomerDialogValidation.validatePointAdjustment(
            deltaText = if (isDeduction && amountText.isNotBlank()) "-$amountText" else amountText,
            reason = reason,
        )
    }
    val showDeltaError = validation.deltaError && amountText.isNotBlank()
    val showReasonError = validation.reasonError && reason.isNotBlank()
    val selectedProgram = availablePrograms.firstOrNull { it.id == selectedProgramId }
    val valueLabel = when (selectedProgram?.type) {
        LoyaltyProgramType.DIGITAL_STAMP,
        LoyaltyProgramType.PURCHASE_FREQUENCY -> stringResource(R.string.merchant_customer_sheet_visits_amount)
        else -> stringResource(R.string.merchant_customer_sheet_points_amount)
    }
    val addLabel = when (selectedProgram?.type) {
        LoyaltyProgramType.DIGITAL_STAMP,
        LoyaltyProgramType.PURCHASE_FREQUENCY -> stringResource(R.string.merchant_customer_sheet_add_progress)
        else -> stringResource(R.string.merchant_customer_sheet_add_bonus)
    }
    val deductLabel = when (selectedProgram?.type) {
        LoyaltyProgramType.DIGITAL_STAMP,
        LoyaltyProgramType.PURCHASE_FREQUENCY -> stringResource(R.string.merchant_customer_sheet_remove_progress)
        else -> stringResource(R.string.merchant_customer_sheet_deduct_points)
    }
    val isVisitProgram = selectedProgram?.type == LoyaltyProgramType.DIGITAL_STAMP ||
        selectedProgram?.type == LoyaltyProgramType.PURCHASE_FREQUENCY
    val currentValue = if (isVisitProgram) currentVisits else currentPoints

    CustomerActionSheetFrame(
        title = stringResource(R.string.merchant_customer_adjust_points_title),
        subtitle = stringResource(R.string.merchant_customer_adjust_points_subtitle),
        isSaving = isSaving,
        confirmEnabled = !validation.hasErrors && selectedProgramId.isNotBlank(),
        confirmLabel = stringResource(R.string.merchant_customer_adjust_points),
        onDismiss = onDismiss,
        onConfirm = {
            val parsed = amountText.toIntOrNull() ?: 0
            onSave(if (isDeduction) -parsed else parsed, reason.trim(), selectedProgramId)
        },
    ) {
        CustomerSheetBalanceCard(
            value = currentValue,
            unitLabel = stringResource(
                if (isVisitProgram) R.string.merchant_metric_visits else R.string.merchant_metric_points
            ).lowercase(),
            title = stringResource(
                if (isVisitProgram) R.string.merchant_customer_sheet_current_progress_title
                else R.string.merchant_customer_bonus_current_balance_title
            ),
        )
        CustomerSheetSectionTitle(
            icon = Icons.Default.LocalOffer,
            title = stringResource(R.string.merchant_customer_sheet_program_title),
        )
        CustomerProgramSelector(
            programs = availablePrograms,
            selectedProgramId = selectedProgramId,
            emptyLabel = stringResource(R.string.merchant_customer_program_missing),
            onSelect = { selectedProgramId = it },
        )
        selectedProgram?.let { program ->
            CustomerProgramDetailsCard(program = program)
        }
        CustomerSheetSectionTitle(icon = Icons.Default.Stars, title = stringResource(R.string.merchant_customer_sheet_action_title))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CustomerSheetToggleTile(
                modifier = Modifier.weight(1f),
                selected = !isDeduction,
                title = addLabel,
                icon = Icons.Default.Add,
                selectedColor = VerevColors.Moss,
                onClick = { isDeduction = false },
            )
            CustomerSheetToggleTile(
                modifier = Modifier.weight(1f),
                selected = isDeduction,
                title = deductLabel,
                icon = Icons.Default.Remove,
                selectedColor = VerevColors.Tan,
                onClick = { isDeduction = true },
            )
        }
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it.filter(Char::isDigit) },
            label = { Text(valueLabel) },
            supportingText = {
                if (showDeltaError) {
                    Text(stringResource(R.string.merchant_customer_error_points_delta_invalid))
                }
            },
            isError = showDeltaError,
            modifier = Modifier.fillMaxWidth(),
            colors = customerSheetFieldColors(),
        )
        if (!showDeltaError && parsedAmount != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VerevColors.AppBackground, RoundedCornerShape(16.dp))
                    .padding(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.merchant_customer_sheet_new_balance),
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.58f),
                    )
                    Text(
                        text = stringResource(
                            if (
                                isVisitProgram
                            ) {
                                R.string.merchant_customer_sheet_new_balance_visits_value
                            } else {
                                R.string.merchant_customer_sheet_new_balance_value
                            },
                            formatCompactCount(currentValue + if (isDeduction) -parsedAmount else parsedAmount),
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text(stringResource(R.string.merchant_customer_adjust_points_reason)) },
            supportingText = {
                if (showReasonError) {
                    Text(stringResource(R.string.merchant_customer_error_reason_required))
                }
            },
            isError = showReasonError,
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
            colors = customerSheetFieldColors(),
        )
    }
}

@Composable
internal fun CustomerTransactionSheet(
    availablePrograms: List<RewardProgram>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (amount: Double, description: String, programId: String) -> Unit,
) {
    var amountText by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedProgramId by rememberSaveable { mutableStateOf(availablePrograms.firstOrNull()?.id.orEmpty()) }
    LaunchedEffect(availablePrograms) {
        if (selectedProgramId !in availablePrograms.map(RewardProgram::id)) {
            selectedProgramId = availablePrograms.firstOrNull()?.id.orEmpty()
        }
    }
    val parsedAmount = amountText.toDoubleOrNull()
    val amountError = parsedAmount == null || parsedAmount <= 0.0
    val selectedProgram = availablePrograms.firstOrNull { it.id == selectedProgramId }

    CustomerActionSheetFrame(
        title = stringResource(R.string.merchant_customer_transaction_sheet_title),
        subtitle = stringResource(R.string.merchant_customer_transaction_sheet_subtitle),
        isSaving = isSaving,
        confirmEnabled = !amountError && selectedProgramId.isNotBlank(),
        confirmLabel = stringResource(R.string.merchant_customer_transaction_sheet_confirm),
        onDismiss = onDismiss,
        onConfirm = { onSave(parsedAmount ?: 0.0, description.trim(), selectedProgramId) },
    ) {
        CustomerSheetSectionTitle(
            icon = Icons.Default.LocalOffer,
            title = stringResource(R.string.merchant_customer_sheet_program_title),
        )
        CustomerProgramSelector(
            programs = availablePrograms,
            selectedProgramId = selectedProgramId,
            emptyLabel = stringResource(R.string.merchant_customer_program_missing),
            onSelect = { selectedProgramId = it },
        )
        selectedProgram?.let { program ->
            CustomerProgramDetailsCard(program = program)
        }
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = sanitizeDecimalInput(it) },
            label = { Text(stringResource(R.string.merchant_customer_transaction_sheet_amount)) },
            supportingText = {
                if (selectedProgram == null) {
                    Text(stringResource(R.string.merchant_customer_program_missing))
                } else if (amountError) {
                    Text(stringResource(R.string.merchant_scan_error_amount))
                } else {
                    val points = selectedProgram.manualTransactionPoints(parsedAmount ?: 0.0)
                    if (points > 0) {
                        Text(
                            stringResource(
                                R.string.merchant_customer_transaction_sheet_points_added,
                                points,
                            )
                        )
                    } else {
                        Text(stringResource(R.string.merchant_customer_transaction_sheet_no_points))
                    }
                }
            },
            isError = amountError && amountText.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = customerSheetFieldColors(),
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.merchant_customer_transaction_sheet_description)) },
            modifier = Modifier.fillMaxWidth(),
            colors = customerSheetFieldColors(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CustomerTagsSheet(
    currentTags: List<String>,
    suggestedTags: List<String>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit,
) {
    var tags by remember(currentTags) { mutableStateOf(currentTags.distinct()) }
    var draftTag by rememberSaveable { mutableStateOf("") }

    CustomerActionSheetFrame(
        title = stringResource(R.string.merchant_customer_tags_sheet_title),
        subtitle = stringResource(R.string.merchant_customer_tags_sheet_subtitle),
        isSaving = isSaving,
        confirmLabel = stringResource(R.string.merchant_customer_tags_sheet_confirm),
        onDismiss = onDismiss,
        onConfirm = { onSave(tags) },
    ) {
        CustomerSheetSectionTitle(icon = Icons.Default.LocalOffer, title = stringResource(R.string.merchant_customer_tags_sheet_current, tags.size))
        if (tags.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VerevColors.AppBackground, RoundedCornerShape(18.dp))
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.merchant_customer_tags_sheet_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VerevColors.Forest.copy(alpha = 0.58f),
                )
            }
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    Row(
                        modifier = Modifier
                            .background(
                                color = VerevColors.Gold.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(999.dp),
                            )
                            .padding(start = 12.dp, top = 10.dp, end = 8.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VerevColors.Forest,
                            fontWeight = FontWeight.Medium,
                        )
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(VerevColors.Forest.copy(alpha = 0.18f), CircleShape)
                                .clickable { tags = tags.filterNot { existing -> existing == tag } },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = VerevColors.Forest,
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                }
            }
        }
        CustomerSheetSectionTitle(icon = Icons.Default.Add, title = stringResource(R.string.merchant_customer_tags_sheet_add_custom))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draftTag,
                onValueChange = { draftTag = it.replace("\n", "") },
                label = { Text(stringResource(R.string.merchant_customer_tags_sheet_custom_label)) },
                modifier = Modifier.weight(1f),
                colors = customerSheetFieldColors(),
            )
            Button(
                onClick = {
                    val normalized = draftTag.trim()
                    if (normalized.isNotBlank() && normalized !in tags) {
                        tags = tags + normalized
                        draftTag = ""
                    }
                },
                enabled = draftTag.trim().isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Moss, contentColor = VerevColors.White),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.merchant_customer_tags_sheet_add))
            }
        }
        CustomerSheetSectionTitle(icon = Icons.Default.Stars, title = stringResource(R.string.merchant_customer_tags_sheet_quick_add))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestedTags.filterNot(tags::contains).forEach { tag ->
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(999.dp))
                        .clickable { tags = tags + tag }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.merchant_customer_tags_sheet_quick_chip, tag),
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.72f),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
internal fun CustomerMergeDuplicateSheet(
    currentCustomer: Customer,
    candidates: List<Customer>,
    preview: CustomerMergePreview?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSelectCandidate: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filteredCandidates = remember(query, candidates, currentCustomer.id) {
        val normalizedQuery = query.trim().lowercase()
        candidates
            .filter { it.id != currentCustomer.id }
            .filter { candidate ->
                normalizedQuery.isBlank() ||
                    candidate.displayName().lowercase().contains(normalizedQuery) ||
                    candidate.phoneNumber.lowercase().contains(normalizedQuery) ||
                    candidate.email.lowercase().contains(normalizedQuery)
            }
            .take(8)
    }

    CustomerActionSheetFrame(
        title = stringResource(R.string.merchant_customer_merge_sheet_title),
        subtitle = stringResource(R.string.merchant_customer_merge_sheet_subtitle),
        isSaving = isLoading,
        confirmLabel = stringResource(R.string.merchant_customer_merge_sheet_confirm),
        confirmEnabled = preview?.canMerge == true,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    ) {
        CustomerSheetSectionTitle(
            icon = Icons.Default.Person,
            title = stringResource(R.string.merchant_customer_merge_sheet_target_title),
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it.replace("\n", "") },
            label = { Text(stringResource(R.string.merchant_customer_merge_sheet_search_label)) },
            modifier = Modifier.fillMaxWidth(),
            colors = customerSheetFieldColors(),
        )
        if (filteredCandidates.isEmpty()) {
            Text(
                text = stringResource(R.string.merchant_customer_merge_sheet_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.6f),
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredCandidates.forEach { candidate ->
                    CustomerResolutionCandidateCard(
                        customer = candidate,
                        selected = preview?.targetCustomer?.id == candidate.id,
                        onClick = { onSelectCandidate(candidate.id) },
                    )
                }
            }
        }
        preview?.let {
            CustomerSheetSectionTitle(
                icon = Icons.Default.Check,
                title = stringResource(R.string.merchant_customer_merge_sheet_preview_title),
            )
            CustomerResolutionPairCard(
                source = it.sourceCustomer,
                target = it.targetCustomer,
                modeLabel = stringResource(R.string.merchant_customer_merge_sheet_mode_label),
            )
            CustomerResolutionSummaryCard(
                lines = listOf(
                    stringResource(R.string.merchant_customer_merge_sheet_summary_profiles, it.assessment.sourceOrganizationProfiles),
                    stringResource(R.string.merchant_customer_merge_sheet_summary_memberships, it.assessment.sourceMemberships),
                    stringResource(R.string.merchant_customer_merge_sheet_summary_credentials, it.assessment.sourceCredentials),
                    stringResource(R.string.merchant_customer_merge_sheet_summary_transactions, it.assessment.sourceTransactions),
                    stringResource(R.string.merchant_customer_merge_sheet_summary_points_ledger, it.assessment.sourcePointsLedgerEntries),
                ),
            )
            CustomerResolutionWarningBlock(
                title = stringResource(R.string.merchant_customer_merge_sheet_warnings_title),
                entries = it.warnings,
                emptyLabel = stringResource(R.string.merchant_customer_duplicate_no_warnings),
            )
            CustomerResolutionWarningBlock(
                title = stringResource(R.string.merchant_customer_merge_sheet_blockers_title),
                entries = it.blockingReasons,
                emptyLabel = stringResource(R.string.merchant_customer_duplicate_ready_to_apply),
            )
        }
    }
}

@Composable
internal fun CustomerSplitIdentitySheet(
    customer: Customer,
    preview: CustomerSplitPreview?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String) -> Unit,
) {
    var firstName by rememberSaveable(customer.id) { mutableStateOf(customer.firstName) }
    var lastName by rememberSaveable(customer.id) { mutableStateOf(customer.lastName) }
    var phoneNumber by rememberSaveable(customer.id) { mutableStateOf("") }
    var email by rememberSaveable(customer.id) { mutableStateOf(customer.email) }
    var notes by rememberSaveable(customer.id) { mutableStateOf("") }

    val canSubmit = preview?.canSplit == true &&
        firstName.isNotBlank() &&
        lastName.isNotBlank() &&
        phoneNumber.isNotBlank()

    CustomerActionSheetFrame(
        title = stringResource(R.string.merchant_customer_split_sheet_title),
        subtitle = stringResource(R.string.merchant_customer_split_sheet_subtitle),
        isSaving = isLoading,
        confirmLabel = stringResource(R.string.merchant_customer_split_sheet_confirm),
        confirmEnabled = canSubmit,
        onDismiss = onDismiss,
        onConfirm = { onSubmit(firstName, lastName, phoneNumber, email, notes) },
    ) {
        preview?.let {
            CustomerSheetSectionTitle(
                icon = Icons.Default.Check,
                title = stringResource(R.string.merchant_customer_split_sheet_preview_title),
            )
            CustomerResolutionSummaryCard(
                lines = listOf(
                    stringResource(R.string.merchant_customer_split_sheet_summary_profiles, it.assessment.organizationProfilesToMove),
                    stringResource(R.string.merchant_customer_split_sheet_summary_memberships, it.assessment.membershipsToMove),
                    stringResource(R.string.merchant_customer_split_sheet_summary_credentials, it.assessment.credentialsToMove),
                    stringResource(R.string.merchant_customer_split_sheet_summary_transactions, it.assessment.transactionsToMove),
                    stringResource(R.string.merchant_customer_split_sheet_summary_points_ledger, it.assessment.pointsLedgerEntriesToMove),
                ),
            )
            CustomerResolutionWarningBlock(
                title = stringResource(R.string.merchant_customer_merge_sheet_warnings_title),
                entries = it.warnings,
                emptyLabel = stringResource(R.string.merchant_customer_duplicate_no_warnings),
            )
            CustomerResolutionWarningBlock(
                title = stringResource(R.string.merchant_customer_merge_sheet_blockers_title),
                entries = it.blockingReasons,
                emptyLabel = stringResource(R.string.merchant_customer_duplicate_ready_to_apply),
            )
        }
        CustomerSheetSectionTitle(
            icon = Icons.Default.Person,
            title = stringResource(R.string.merchant_customer_split_sheet_identity_title),
        )
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it.replace("\n", "") },
            label = { Text(stringResource(R.string.merchant_customer_split_sheet_first_name)) },
            modifier = Modifier.fillMaxWidth(),
            colors = customerSheetFieldColors(),
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it.replace("\n", "") },
            label = { Text(stringResource(R.string.merchant_customer_split_sheet_last_name)) },
            modifier = Modifier.fillMaxWidth(),
            colors = customerSheetFieldColors(),
        )
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it.replace("\n", "") },
            label = { Text(stringResource(R.string.merchant_customer_split_sheet_phone_number)) },
            modifier = Modifier.fillMaxWidth(),
            colors = customerSheetFieldColors(),
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.replace("\n", "") },
            label = { Text(stringResource(R.string.merchant_customer_split_sheet_email)) },
            modifier = Modifier.fillMaxWidth(),
            colors = customerSheetFieldColors(),
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.merchant_customer_split_sheet_notes)) },
            modifier = Modifier.fillMaxWidth(),
            colors = customerSheetFieldColors(),
        )
    }
}

@Composable
private fun CustomerResolutionCandidateCard(
    customer: Customer,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (selected) VerevColors.Gold.copy(alpha = 0.14f) else VerevColors.AppBackground,
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = customer.displayName(),
                style = MaterialTheme.typography.titleSmall,
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOf(customer.phoneNumber, customer.email).filter { it.isNotBlank() }.joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.62f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected) {
            MerchantStatusPill(
                text = stringResource(R.string.merchant_customer_duplicate_selected_badge),
                backgroundColor = VerevColors.Forest,
                contentColor = VerevColors.White,
            )
        }
    }
}

@Composable
private fun CustomerResolutionPairCard(
    source: Customer,
    target: Customer,
    modeLabel: String,
) {
    CustomerResolutionSummaryCard(
        lines = listOf(
            "$modeLabel: ${source.displayName()} -> ${target.displayName()}",
            stringResource(R.string.merchant_customer_merge_sheet_pair_source, source.phoneNumber),
            stringResource(R.string.merchant_customer_merge_sheet_pair_target, target.phoneNumber),
        ),
    )
}

@Composable
private fun CustomerResolutionSummaryCard(lines: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VerevColors.AppBackground, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        lines.forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest,
            )
        }
    }
}

@Composable
private fun CustomerResolutionWarningBlock(
    title: String,
    entries: List<String>,
    emptyLabel: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = VerevColors.Forest,
            fontWeight = FontWeight.Medium,
        )
        if (entries.isEmpty()) {
            Text(
                text = emptyLabel,
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.62f),
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                entries.forEach { entry ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = entry.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerActionSheetFrame(
    title: String,
    subtitle: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String,
    confirmEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    AppBottomSheetDialog(
        onDismissRequest = onDismiss,
        contentPadding = PaddingValues(0.dp),
    ) { dismiss, dismissAfter ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.96f)
                .imePadding()
                .heightIn(min = 560.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.6f),
                    )
                }
                IconButton(
                    onClick = dismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(VerevColors.Forest.copy(alpha = 0.05f), CircleShape),
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = VerevColors.Forest)
                }
            }
            HorizontalDivider(color = VerevColors.Forest.copy(alpha = 0.08f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                content()
            }
            HorizontalDivider(color = VerevColors.Forest.copy(alpha = 0.08f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = dismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerevColors.AppBackground,
                        contentColor = VerevColors.Forest,
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp),
                ) {
                    Text(
                        text = stringResource(R.string.auth_cancel),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Button(
                    onClick = { dismissAfter(onConfirm) },
                    enabled = confirmEnabled && !isSaving,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerevColors.Gold,
                        contentColor = VerevColors.Forest,
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = confirmLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerProgramDetailsCard(program: RewardProgram) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VerevColors.AppBackground, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.merchant_customer_program_details_title),
            style = MaterialTheme.typography.titleSmall,
            color = VerevColors.Forest,
            fontWeight = FontWeight.SemiBold,
        )
        when (program.type) {
            LoyaltyProgramType.POINTS -> {
                CustomerProgramDetailRow(
                    label = stringResource(R.string.merchant_customer_program_details_earn_rate),
                    value = stringResource(
                        R.string.merchant_customer_program_details_points_rule,
                        program.configuration.pointsRule.pointsAwardedPerStep,
                        program.configuration.pointsRule.spendStepAmount,
                    ),
                )
                CustomerProgramDetailRow(
                    label = stringResource(R.string.merchant_customer_program_details_redeem_min),
                    value = "${program.configuration.pointsRule.minimumRedeemPoints} pts",
                )
            }
            LoyaltyProgramType.DIGITAL_STAMP -> {
                CustomerProgramDetailRow(
                    label = stringResource(R.string.merchant_customer_program_details_checkin_goal),
                    value = stringResource(
                        R.string.merchant_customer_program_details_checkin_rule,
                        program.configuration.checkInRule.visitsRequired,
                        program.configuration.checkInRule.rewardOutcome.displayValue(),
                    ),
                )
                CustomerProgramDetailRow(
                    label = stringResource(R.string.merchant_customer_program_details_reward_name),
                    value = program.configuration.checkInRule.rewardOutcome.displayValue(),
                )
            }
            LoyaltyProgramType.TIER -> {
                CustomerProgramDetailRow(
                    label = stringResource(R.string.merchant_customer_program_details_tiers),
                    value = program.configuration.tierRule.customerTierThresholdsSummary(),
                )
                CustomerProgramDetailRow(
                    label = stringResource(R.string.merchant_customer_program_details_bonus),
                    value = program.configuration.tierRule.customerTierBenefitsSummary(),
                )
            }
            LoyaltyProgramType.COUPON -> {
                CustomerProgramDetailRow(
                    label = stringResource(R.string.merchant_customer_program_details_coupon),
                    value = program.configuration.couponRule.couponName,
                )
                CustomerProgramDetailRow(
                    label = stringResource(R.string.merchant_customer_program_details_cost),
                    value = "${program.configuration.couponRule.pointsCost} pts",
                )
                CustomerProgramDetailRow(
                    label = stringResource(R.string.merchant_customer_program_details_discount),
                    value = formatWholeCurrency(program.configuration.couponRule.discountAmount),
                )
            }
            LoyaltyProgramType.PURCHASE_FREQUENCY -> {
                CustomerProgramDetailRow(
                    label = stringResource(R.string.merchant_customer_program_details_frequency_goal),
                    value = stringResource(
                        R.string.merchant_customer_program_details_frequency_rule,
                        program.configuration.purchaseFrequencyRule.purchaseCount,
                        program.configuration.purchaseFrequencyRule.windowDays,
                    ),
                )
                CustomerProgramDetailRow(
                    label = stringResource(R.string.merchant_customer_program_details_reward),
                    value = program.configuration.purchaseFrequencyRule.rewardOutcome.displayValue(),
                )
            }
            LoyaltyProgramType.REFERRAL -> {
                CustomerProgramDetailRow(
                    label = stringResource(R.string.merchant_customer_program_details_referral_code),
                    value = program.configuration.referralRule.referralCodePrefix,
                )
                CustomerProgramDetailRow(
                    label = stringResource(R.string.merchant_customer_program_details_referral_rewards),
                    value = stringResource(
                        R.string.merchant_customer_program_details_referral_rule,
                        program.configuration.referralRule.referrerRewardOutcome.displayValue(),
                        program.configuration.referralRule.refereeRewardOutcome.displayValue(),
                    ),
                )
            }
        }
    }
}

@Composable
private fun TierProgramRule.customerTierThresholdsSummary(): String =
    LocalContext.current.let { context ->
        configurableLevels.joinToString(separator = ", ") { level ->
            context.getString(
                R.string.merchant_customer_program_details_tier_level_format,
                level.name,
                level.threshold,
            )
        }
    }

@Composable
private fun TierProgramRule.customerTierBenefitsSummary(): String =
    LocalContext.current.let { context ->
        configurableLevels.joinToString(separator = ", ") { level ->
            context.getString(
                R.string.merchant_customer_program_details_tier_benefit_format,
                level.name,
                level.bonusPercent,
            )
        }
    }

@Composable
private fun CustomerProgramDetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest.copy(alpha = 0.58f),
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = VerevColors.Forest,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CustomerSheetSectionTitle(
    icon: ImageVector,
    title: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
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
private fun CustomerSheetBalanceCard(
    value: Int,
    unitLabel: String,
    title: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    listOf(VerevColors.Gold.copy(alpha = 0.14f), VerevColors.Moss.copy(alpha = 0.12f))
                ),
                shape = RoundedCornerShape(18.dp),
            )
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = VerevColors.Forest.copy(alpha = 0.6f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatCompactCount(value),
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = unitLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = VerevColors.Forest.copy(alpha = 0.58f),
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun CustomerSheetToggleTile(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = if (selected) selectedColor.copy(alpha = 0.12f) else Color.White,
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) selectedColor else VerevColors.Forest.copy(alpha = 0.4f),
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) selectedColor else VerevColors.Forest.copy(alpha = 0.62f),
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun CustomerProgramSelector(
    programs: List<RewardProgram>,
    selectedProgramId: String,
    emptyLabel: String,
    onSelect: (String) -> Unit,
) {
    if (programs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(VerevColors.AppBackground, RoundedCornerShape(16.dp))
                .padding(vertical = 18.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = emptyLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = VerevColors.Forest.copy(alpha = 0.58f),
            )
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        programs.forEach { program ->
            val selected = program.id == selectedProgramId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (selected) VerevColors.Moss.copy(alpha = 0.1f) else VerevColors.AppBackground,
                        shape = RoundedCornerShape(18.dp),
                    )
                    .clickable { onSelect(program.id) }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            brush = Brush.linearGradient(
                                if (selected) listOf(VerevColors.Gold, VerevColors.Moss)
                                else listOf(VerevColors.Forest.copy(alpha = 0.14f), VerevColors.Moss.copy(alpha = 0.12f)),
                            ),
                            shape = RoundedCornerShape(14.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(program.programTypeLabelRes()).take(1),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) VerevColors.White else VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = program.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(program.programTypeLabelRes()),
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.56f),
                    )
                    if (program.programEarnRateSummary().isNotBlank()) {
                        Text(
                            text = program.programEarnRateSummary(),
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.68f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            color = if (selected) VerevColors.Moss else Color.Transparent,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = VerevColors.White,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun customerSheetFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = VerevColors.AppBackground,
    unfocusedContainerColor = VerevColors.AppBackground,
    disabledContainerColor = VerevColors.AppBackground,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    focusedLabelColor = VerevColors.Moss,
    unfocusedLabelColor = VerevColors.Forest.copy(alpha = 0.52f),
    focusedTextColor = VerevColors.Forest,
    unfocusedTextColor = VerevColors.Forest,
    cursorColor = VerevColors.Moss,
)
