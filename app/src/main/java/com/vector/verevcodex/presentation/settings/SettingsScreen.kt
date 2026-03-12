package com.vector.verevcodex.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.presentation.navigation.ShellViewModel
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun StoreManagementScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    onOpenAddBranch: () -> Unit = {},
    onOpenEditBranch: (String) -> Unit = {},
    onOpenBranchStaffConfig: (String) -> Unit = {},
    onOpenBranchProgramsConfig: (String) -> Unit = {},
    viewModel: StoreManagementViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
                title = stringResource(R.string.merchant_store_management_title),
                subtitle = stringResource(R.string.merchant_store_management_subtitle),
                icon = Icons.Default.Storefront,
                colors = listOf(VerevColors.Forest, VerevColors.Moss),
            )
        }
        state.errorRes?.let { errorRes ->
            item { SettingsDetailSection(title = stringResource(errorRes)) {} }
        }
        state.messageRes?.let { messageRes ->
            item { SettingsDetailSection(title = stringResource(messageRes)) {} }
        }
        item {
            Button(onClick = onOpenAddBranch, modifier = Modifier.fillParentMaxWidth()) {
                androidx.compose.material3.Text(stringResource(R.string.merchant_add_branch))
            }
        }
        items(state.stores, key = { it.id }) { store ->
            BranchStoreCard(
                store = store,
                selected = store.id == state.selectedStoreId,
                onSelect = { viewModel.selectStore(store.id) },
                onEdit = { onOpenEditBranch(store.id) },
                onToggleActive = { viewModel.toggleStoreActive(store) },
                onConfigureStaff = { onOpenBranchStaffConfig(store.id) },
                onConfigurePrograms = { onOpenBranchProgramsConfig(store.id) },
            )
        }
    }
}

@Composable
private fun BranchStoreCard(
    store: Store,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onConfigureStaff: () -> Unit,
    onConfigurePrograms: () -> Unit,
) {
    SettingsDetailSection(title = store.name) {
        SettingsDetailRow(
            label = stringResource(R.string.merchant_business_details_address_label),
            value = store.address,
            trailing = {
                SettingsSectionBadge(
                    text = if (selected) {
                        stringResource(R.string.merchant_current_location)
                    } else if (store.active) {
                        stringResource(R.string.merchant_store_active)
                    } else {
                        stringResource(R.string.merchant_store_disabled)
                    },
                )
            },
        )
        SettingsDetailRow(
            label = stringResource(R.string.merchant_business_details_contact_label),
            value = store.contactInfo,
        )
        SettingsDetailRow(
            label = stringResource(R.string.merchant_business_details_hours_label),
            value = store.workingHours,
        )
        SettingsMenuRow(
            title = stringResource(R.string.merchant_select_store),
            subtitle = stringResource(R.string.merchant_store_make_current),
            icon = Icons.Default.Storefront,
            trailingLabel = "",
            onClick = onSelect,
        )
        SettingsMenuRow(
            title = stringResource(R.string.merchant_edit_branch),
            subtitle = stringResource(R.string.merchant_store_edit_branch_subtitle),
            icon = Icons.Default.Description,
            trailingLabel = "",
            onClick = onEdit,
        )
        SettingsMenuRow(
            title = stringResource(R.string.merchant_branch_staff_config_title),
            subtitle = stringResource(R.string.merchant_branch_staff_config_subtitle),
            icon = Icons.Default.Groups,
            trailingLabel = "",
            onClick = onConfigureStaff,
        )
        SettingsMenuRow(
            title = stringResource(R.string.merchant_branch_programs_title),
            subtitle = stringResource(R.string.merchant_branch_programs_subtitle),
            icon = Icons.Default.Campaign,
            trailingLabel = "",
            onClick = onConfigurePrograms,
        )
        SettingsMenuRow(
            title = if (store.active) stringResource(R.string.merchant_disable_branch) else stringResource(R.string.merchant_enable_branch),
            subtitle = stringResource(R.string.merchant_store_toggle_active_subtitle),
            icon = Icons.Default.Shield,
            trailingLabel = "",
            onClick = onToggleActive,
        )
    }
}

@Composable
fun BusinessSettingsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onOpenPersonalInformation: () -> Unit = {},
    onOpenPasswordSecurity: () -> Unit = {},
    onOpenEmailNotifications: () -> Unit = {},
    onOpenBusinessDetails: () -> Unit = {},
    onOpenPrograms: () -> Unit = {},
    onOpenStaff: () -> Unit = {},
    onOpenReports: () -> Unit = {},
    onOpenPayments: () -> Unit = {},
    onOpenBranding: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    shellViewModel: ShellViewModel = hiltViewModel(),
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val shellState by shellViewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val roleSubtitle = when (shellState.currentUser?.role) {
        StaffRole.OWNER -> stringResource(R.string.merchant_settings_role_owner_subtitle)
        StaffRole.STORE_MANAGER -> stringResource(R.string.merchant_settings_role_manager_subtitle)
        StaffRole.CASHIER, StaffRole.STAFF -> stringResource(R.string.merchant_settings_role_staff_subtitle)
        null -> stringResource(R.string.merchant_settings_role_default_subtitle)
    }

    val groups = buildList {
        add(
            stringResource(R.string.merchant_settings_group_account) to listOf(
                SettingsMenuItem(
                    title = stringResource(R.string.merchant_settings_personal_information),
                    subtitle = stringResource(R.string.merchant_settings_personal_information_subtitle),
                    icon = Icons.Default.Person,
                    onClick = onOpenPersonalInformation,
                ),
                SettingsMenuItem(
                    title = stringResource(R.string.merchant_settings_password_security),
                    subtitle = stringResource(R.string.merchant_settings_password_security_subtitle),
                    icon = Icons.Default.Shield,
                    onClick = onOpenPasswordSecurity,
                ),
                SettingsMenuItem(
                    title = stringResource(R.string.merchant_settings_email_notifications),
                    subtitle = stringResource(R.string.merchant_settings_email_notifications_subtitle),
                    icon = Icons.Default.Email,
                    onClick = onOpenEmailNotifications,
                ),
            )
        )
        if (shellState.currentUser?.role == StaffRole.OWNER || shellState.currentUser?.role == StaffRole.STORE_MANAGER) {
            add(
                stringResource(R.string.merchant_settings_group_business) to listOf(
                    SettingsMenuItem(
                        title = stringResource(R.string.merchant_settings_business_details),
                        subtitle = stringResource(R.string.merchant_settings_business_details_subtitle),
                        icon = Icons.Default.Storefront,
                        onClick = onOpenBusinessDetails,
                    ),
                    SettingsMenuItem(
                        title = stringResource(R.string.merchant_settings_promotions),
                        subtitle = stringResource(R.string.merchant_settings_promotions_subtitle),
                        icon = Icons.Default.Campaign,
                        onClick = onOpenPrograms,
                    ),
                    SettingsMenuItem(
                        title = stringResource(R.string.merchant_settings_staff),
                        subtitle = stringResource(R.string.merchant_settings_staff_subtitle),
                        icon = Icons.Default.Groups,
                        onClick = onOpenStaff,
                    ),
                )
            )
        }
        add(
            stringResource(R.string.merchant_settings_group_tools) to listOf(
                SettingsMenuItem(
                    title = stringResource(R.string.merchant_settings_reports),
                    subtitle = stringResource(R.string.merchant_settings_reports_subtitle),
                    icon = Icons.Default.Description,
                    onClick = onOpenReports,
                ),
                SettingsMenuItem(
                    title = stringResource(R.string.merchant_settings_payments),
                    subtitle = stringResource(R.string.merchant_settings_payments_subtitle),
                    icon = Icons.Default.Payments,
                    onClick = onOpenPayments,
                ),
                SettingsMenuItem(
                    title = stringResource(R.string.merchant_settings_branding),
                    subtitle = stringResource(R.string.merchant_settings_branding_subtitle),
                    icon = Icons.Default.Palette,
                    onClick = onOpenBranding,
                ),
                SettingsMenuItem(
                    title = stringResource(R.string.merchant_settings_privacy),
                    subtitle = stringResource(R.string.merchant_settings_privacy_subtitle),
                    icon = Icons.Default.PrivacyTip,
                    onClick = onOpenPrivacy,
                ),
            )
        )
    }

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
        item { SettingsHeader(roleSubtitle = roleSubtitle) }
        item { SettingsBusinessCard(store = shellState.selectedStore, currentUser = shellState.currentUser) }
        groups.forEach { group ->
            item { SettingsGroup(title = group.first, items = group.second) }
        }
        item {
            androidx.compose.material3.Text(
                text = stringResource(R.string.merchant_settings_version_label),
                modifier = Modifier,
                color = com.vector.verevcodex.presentation.theme.VerevColors.Inactive,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            )
        }
        item { SettingsLogoutButton(onClick = { showLogoutDialog = true }) }
    }

    if (showLogoutDialog) {
        SettingsLogoutDialog(
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            },
            onCancel = { showLogoutDialog = false },
        )
    }
}
