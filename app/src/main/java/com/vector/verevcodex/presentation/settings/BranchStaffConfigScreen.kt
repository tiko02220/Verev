package com.vector.verevcodex.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.vector.verevcodex.domain.model.common.StaffPermissions
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.domain.model.common.defaultPermissions
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard
import com.vector.verevcodex.presentation.merchant.common.MerchantErrorDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantLoadingOverlay
import com.vector.verevcodex.presentation.merchant.common.MerchantSuccessDialog
import com.vector.verevcodex.presentation.staff.StaffAddMemberSheet
import com.vector.verevcodex.presentation.staff.resolveStaffSuccessFeedback
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
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(StaffRole.STORE_MANAGER) }
    var permissions by rememberSaveable(stateSaver = BranchStaffPermissionsSaver) {
        mutableStateOf(StaffRole.STORE_MANAGER.defaultPermissions())
    }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    val storeLabel = state.storeName.ifBlank { stringResource(R.string.merchant_business_location) }
    val successFeedback = state.messageRes?.let(::resolveStaffSuccessFeedback)

    fun dismissSuccessFeedback() {
        fullName = ""
        email = ""
        phoneNumber = ""
        password = ""
        role = StaffRole.STORE_MANAGER
        permissions = StaffRole.STORE_MANAGER.defaultPermissions()
        showAddSheet = false
        viewModel.dismissFeedback()
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                                trailingLabel = member.permissionsSummary.ifBlank { member.role.name.replace('_', ' ') },
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
    }

    if (showAddSheet) {
        StaffAddMemberSheet(
            fullName = fullName,
            email = email,
            phoneNumber = phoneNumber,
            password = password,
            role = role,
            permissions = permissions,
            isSaving = state.isSaving,
            isEditing = false,
            onFullNameChanged = { fullName = it },
            onEmailChanged = { email = it },
            onPhoneNumberChanged = { phoneNumber = it },
            onPasswordChanged = { password = it },
            onRoleSelected = {
                role = it
                permissions = it.defaultPermissions()
            },
            onPermissionsChanged = { permissions = it },
            onDismiss = { showAddSheet = false },
            onSave = {
                viewModel.addMember(fullName, email, phoneNumber, password, role, permissions)
            },
        )
    }

    MerchantLoadingOverlay(
        isVisible = state.isSaving,
        title = stringResource(R.string.merchant_loader_branch_staff_title),
        subtitle = stringResource(R.string.merchant_loader_branch_staff_subtitle),
    )

    val errorMessage = state.errorMessage ?: state.errorRes?.let { stringResource(it) }
    if (errorMessage != null) {
        MerchantErrorDialog(
            title = stringResource(R.string.merchant_error_dialog_title),
            message = errorMessage,
            onDismiss = viewModel::dismissFeedback,
        )
    }

    successFeedback?.let { feedback ->
        MerchantSuccessDialog(
            title = stringResource(feedback.titleRes),
            message = stringResource(feedback.messageRes, storeLabel),
            onDismiss = ::dismissSuccessFeedback,
        )
    }
}

private val BranchStaffPermissionsSaver = androidx.compose.runtime.saveable.listSaver<StaffPermissions, Boolean>(
    save = { listOf(it.viewAnalytics, it.managePrograms, it.processTransactions, it.manageCustomers, it.manageStaff, it.viewSettings) },
    restore = {
        StaffPermissions(
            viewAnalytics = it[0],
            managePrograms = it[1],
            processTransactions = it[2],
            manageCustomers = it[3],
            manageStaff = it[4],
            viewSettings = it[5],
        )
    },
)
