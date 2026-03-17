package com.vector.verevcodex.presentation.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.vector.verevcodex.presentation.merchant.common.MerchantErrorDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantLoadingOverlay
import com.vector.verevcodex.presentation.merchant.common.MerchantSuccessDialog
import com.vector.verevcodex.domain.model.common.StaffPermissions
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.domain.model.common.defaultPermissions
import com.vector.verevcodex.presentation.merchant.common.displayName

@Composable
fun StaffManagementScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: StaffViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var editingStaffId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteStaffId by rememberSaveable { mutableStateOf<String?>(null) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(StaffRole.STAFF) }
    var permissions by rememberSaveable(stateSaver = StaffPermissionsSaver) {
        mutableStateOf(StaffRole.STAFF.defaultPermissions())
    }
    val storeLabel = state.selectedStoreName.ifBlank { stringResource(R.string.merchant_business_location) }
    val successFeedback = state.messageRes?.let(::resolveStaffSuccessFeedback)

    fun dismissSuccessFeedback() {
        fullName = ""
        email = ""
        phoneNumber = ""
        password = ""
        role = StaffRole.STAFF
        permissions = StaffRole.STAFF.defaultPermissions()
        showEditor = false
        editingStaffId = null
        pendingDeleteStaffId = null
        viewModel.dismissFeedback()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            StaffHeader(
                memberCount = state.members.size,
                onBack = onBack,
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 18.dp,
                    bottom = contentPadding.calculateBottomPadding() + 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.members.isEmpty()) {
                    item { StaffEmptyState() }
                } else {
                    items(state.members, key = { it.id }) { member ->
                        StaffMemberCard(
                            member = member,
                            onEdit = {
                                viewModel.dismissFeedback()
                                fullName = "${member.firstName} ${member.lastName}".trim()
                                email = member.email
                                phoneNumber = member.phoneNumber
                                password = ""
                                role = member.role
                                permissions = member.permissions
                                editingStaffId = member.id
                                showEditor = true
                            },
                            onDelete = {
                                viewModel.dismissFeedback()
                                pendingDeleteStaffId = member.id
                            },
                        )
                    }
                }
                item {
                    StaffPrimaryAction(
                        text = androidx.compose.ui.res.stringResource(com.vector.verevcodex.R.string.merchant_staff_add_member),
                        onClick = {
                            viewModel.dismissFeedback()
                            fullName = ""
                            email = ""
                            phoneNumber = ""
                            password = ""
                            role = StaffRole.STAFF
                            permissions = StaffRole.STAFF.defaultPermissions()
                            editingStaffId = null
                            showEditor = true
                        },
                    )
                }
            }
        }
    }

    if (showEditor) {
        StaffAddMemberSheet(
            fullName = fullName,
            email = email,
            phoneNumber = phoneNumber,
            password = password,
            role = role,
            permissions = permissions,
            isSaving = state.isSaving,
            isEditing = editingStaffId != null,
            onFullNameChanged = { fullName = it },
            onEmailChanged = { email = it },
            onPhoneNumberChanged = { phoneNumber = it },
            onPasswordChanged = { password = it },
            onRoleSelected = {
                role = it
                permissions = it.defaultPermissions()
            },
            onPermissionsChanged = { permissions = it },
            onDismiss = {
                showEditor = false
                editingStaffId = null
            },
            onSave = {
                val editingId = editingStaffId
                if (editingId == null) {
                    viewModel.addMember(
                        fullName = fullName,
                        email = email,
                        phoneNumber = phoneNumber,
                        password = password,
                        role = role,
                        permissions = permissions,
                    )
                } else {
                    viewModel.updateMember(
                        staffId = editingId,
                        fullName = fullName,
                        email = email,
                        phoneNumber = phoneNumber,
                        role = role,
                        permissions = permissions,
                    )
                }
            },
        )
    }

    MerchantLoadingOverlay(
        isVisible = state.isSaving,
        title = androidx.compose.ui.res.stringResource(com.vector.verevcodex.R.string.merchant_loader_staff_title),
        subtitle = androidx.compose.ui.res.stringResource(com.vector.verevcodex.R.string.merchant_loader_staff_subtitle),
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

    pendingDeleteStaffId?.let { staffId ->
        state.members.firstOrNull { it.id == staffId }?.let { member ->
            StaffDeleteDialog(
                memberName = "${member.firstName} ${member.lastName}".trim().ifBlank { member.role.displayName() },
                onDismiss = { pendingDeleteStaffId = null },
                onConfirm = { viewModel.removeMember(staffId) },
            )
        }
    }
}

private val StaffPermissionsSaver = androidx.compose.runtime.saveable.listSaver<StaffPermissions, Boolean>(
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
