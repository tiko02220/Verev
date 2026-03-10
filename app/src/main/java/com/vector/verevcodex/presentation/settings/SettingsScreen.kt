package com.vector.verevcodex.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Storefront
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
import com.vector.verevcodex.domain.model.StaffRole
import com.vector.verevcodex.presentation.navigation.ShellViewModel

@Composable
fun StoreManagementScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    shellViewModel: ShellViewModel = hiltViewModel(),
) {
    val shellState by shellViewModel.uiState.collectAsStateWithLifecycle()

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
                colors = listOf(com.vector.verevcodex.presentation.theme.VerevColors.Forest, com.vector.verevcodex.presentation.theme.VerevColors.Moss),
            )
        }
        item { SettingsBusinessCard(store = shellState.selectedStore, currentUser = shellState.currentUser) }
        shellState.stores.forEach { store ->
            item {
                SettingsMenuRow(
                    title = store.name,
                    subtitle = "${store.address} • ${store.workingHours}",
                    icon = Icons.Default.Storefront,
                    trailingLabel = if (store.active) stringResource(R.string.merchant_store_active) else stringResource(R.string.merchant_store_disabled),
                    onClick = { shellViewModel.selectStore(store.id) },
                )
            }
        }
    }
}

@Composable
fun BusinessSettingsScreen(
    contentPadding: PaddingValues = PaddingValues(),
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
