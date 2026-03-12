package com.vector.verevcodex.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard
import com.vector.verevcodex.presentation.staff.StaffAddMemberSheet
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun BranchStaffConfigScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    viewModel: BranchStaffConfigViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(StaffRole.STORE_MANAGER) }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SettingsBackRow(onBack = onBack) }
        item {
            SettingsHeroCard(
                title = stringResource(R.string.merchant_branch_staff_config_title),
                subtitle = state.storeName.ifBlank { stringResource(R.string.merchant_branch_staff_config_subtitle) },
                icon = Icons.Default.Groups,
                colors = listOf(VerevColors.Forest, VerevColors.Moss),
            )
        }
        state.errorRes?.let { errorRes ->
            item {
                SettingsDetailSection(title = stringResource(errorRes)) {
                    Text(
                        text = stringResource(R.string.merchant_branch_staff_config_error_support),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VerevColors.Forest.copy(alpha = 0.68f),
                    )
                }
            }
        }
        state.messageRes?.let { messageRes ->
            item {
                SettingsDetailSection(title = stringResource(messageRes)) {
                    Text(
                        text = stringResource(R.string.merchant_branch_staff_config_message_support),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VerevColors.Forest.copy(alpha = 0.68f),
                    )
                }
            }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_branch_staff_members_title)) {
                if (state.members.isEmpty()) {
                    MerchantEmptyStateCard(
                        title = stringResource(R.string.merchant_branch_staff_empty_title),
                        subtitle = stringResource(R.string.merchant_branch_staff_empty_subtitle),
                        icon = Icons.Default.Groups,
                    )
                } else {
                    state.members.forEachIndexed { index, member ->
                        SettingsMenuRow(
                            title = "${member.firstName} ${member.lastName}".trim(),
                            subtitle = member.email,
                            icon = Icons.Default.Groups,
                            trailingLabel = stringResource(member.role.permissionsSummaryRes()),
                            onClick = {},
                        )
                        if (index < state.members.lastIndex) {
                            androidx.compose.material3.HorizontalDivider(
                                color = VerevColors.Inactive.copy(alpha = 0.16f),
                            )
                        }
                    }
                }
                Button(
                    onClick = { showAddSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                ) {
                    Text(stringResource(R.string.merchant_branch_staff_add_member))
                }
            }
        }
    }

    if (showAddSheet) {
        StaffAddMemberSheet(
            fullName = fullName,
            email = email,
            password = password,
            role = role,
            isSaving = state.isSaving,
            onFullNameChanged = { fullName = it },
            onEmailChanged = { email = it },
            onPasswordChanged = { password = it },
            onRoleSelected = { role = it },
            onDismiss = { showAddSheet = false },
            onSave = {
                viewModel.addMember(fullName, email, password, role)
                fullName = ""
                email = ""
                password = ""
                role = StaffRole.STORE_MANAGER
                showAddSheet = false
            },
        )
    }
}
