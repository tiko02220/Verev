package com.vector.verevcodex.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.navigation.ShellViewModel
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun BusinessDetailsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    onOpenStoreManagement: () -> Unit,
    onOpenAddBranch: () -> Unit = {},
    onOpenEditBranch: (String) -> Unit = {},
    onOpenBranding: () -> Unit,
    onOpenBranchStaffConfig: (String) -> Unit = {},
    onOpenBranchProgramsConfig: (String) -> Unit = {},
    shellViewModel: ShellViewModel = hiltViewModel(),
) {
    val shellState by shellViewModel.uiState.collectAsStateWithLifecycle()
    val selectedStore = shellState.selectedStore

    SettingsDetailInnerScaffold(
        title = stringResource(R.string.merchant_business_details_title),
        subtitle = stringResource(R.string.merchant_business_details_subtitle),
        onBack = onBack,
        contentPadding = contentPadding,
    ) {
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_business_details_current_location)) {
                SettingsDetailRow(
                    label = stringResource(R.string.merchant_business_details_name_label),
                    value = selectedStore?.name ?: stringResource(R.string.merchant_select_store),
                )
                HorizontalDivider(color = VerevColors.Inactive.copy(alpha = 0.16f))
                SettingsDetailRow(
                    label = stringResource(R.string.merchant_business_details_category_label),
                    value = selectedStore?.category ?: stringResource(R.string.merchant_settings_value_unavailable),
                )
                HorizontalDivider(color = VerevColors.Inactive.copy(alpha = 0.16f))
                SettingsDetailRow(
                    label = stringResource(R.string.merchant_business_details_address_label),
                    value = selectedStore?.address ?: stringResource(R.string.merchant_settings_value_unavailable),
                )
                HorizontalDivider(color = VerevColors.Inactive.copy(alpha = 0.16f))
                SettingsDetailRow(
                    label = stringResource(R.string.merchant_business_details_contact_label),
                    value = selectedStore?.contactInfo ?: stringResource(R.string.merchant_settings_value_unavailable),
                )
                HorizontalDivider(color = VerevColors.Inactive.copy(alpha = 0.16f))
                SettingsDetailRow(
                    label = stringResource(R.string.merchant_business_details_hours_label),
                    value = selectedStore?.workingHours ?: stringResource(R.string.merchant_settings_value_unavailable),
                )
            }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_business_details_branch_tools_title)) {
                SettingsMenuRow(
                    title = stringResource(R.string.merchant_add_branch),
                    subtitle = stringResource(R.string.merchant_business_details_add_branch_subtitle),
                    icon = Icons.Default.Storefront,
                    trailingLabel = "",
                    onClick = onOpenAddBranch,
                )
                SettingsMenuRow(
                    title = stringResource(R.string.merchant_edit_branch),
                    subtitle = stringResource(R.string.merchant_store_edit_branch_subtitle),
                    icon = Icons.Default.Description,
                    trailingLabel = "",
                    onClick = {
                        selectedStore?.id?.let(onOpenEditBranch)
                    },
                )
                SettingsMenuRow(
                    title = stringResource(R.string.merchant_manage_locations),
                    subtitle = stringResource(R.string.merchant_business_details_manage_locations_subtitle),
                    icon = Icons.Default.Business,
                    trailingLabel = "",
                    onClick = onOpenStoreManagement,
                )
                SettingsMenuRow(
                    title = stringResource(R.string.merchant_branch_staff_config_title),
                    subtitle = stringResource(R.string.merchant_business_details_branch_staff_subtitle),
                    icon = Icons.Default.Tune,
                    trailingLabel = "",
                    onClick = {
                        selectedStore?.id?.let(onOpenBranchStaffConfig)
                    },
                )
                SettingsMenuRow(
                    title = stringResource(R.string.merchant_branch_programs_title),
                    subtitle = stringResource(R.string.merchant_business_details_branch_programs_subtitle),
                    icon = Icons.Default.Tune,
                    trailingLabel = "",
                    onClick = {
                        selectedStore?.id?.let(onOpenBranchProgramsConfig)
                    },
                )
            }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_business_details_branding_title)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsColorSwatch(
                            hex = selectedStore?.primaryColor ?: VerevColors.Gold.toHexString(),
                            color = parseHexColor(selectedStore?.primaryColor, VerevColors.Gold),
                        )
                        SettingsColorSwatch(
                            hex = selectedStore?.secondaryColor ?: VerevColors.Moss.toHexString(),
                            color = parseHexColor(selectedStore?.secondaryColor, VerevColors.Moss),
                        )
                    }
                    TextButton(onClick = onOpenBranding) {
                        Text(stringResource(R.string.merchant_open_branding))
                    }
                }
            }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_business_details_locations_title)) {
                shellState.stores.forEach { store ->
                    SettingsMenuRow(
                        title = store.name,
                        subtitle = store.address,
                        icon = Icons.Default.Storefront,
                        trailingLabel = if (store.id == selectedStore?.id) {
                            stringResource(R.string.merchant_current_location)
                        } else if (store.active) {
                            stringResource(R.string.merchant_store_active)
                        } else {
                            stringResource(R.string.merchant_store_disabled)
                        },
                        onClick = { shellViewModel.selectStore(store.id) },
                    )
                }
                Button(
                    onClick = onOpenStoreManagement,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Forest, contentColor = Color.White),
                ) {
                    Text(stringResource(R.string.merchant_manage_locations))
                }
            }
        }
    }
}

@Composable
fun PaymentMethodsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    onOpenPlanSelection: () -> Unit = {},
    onOpenInvoices: () -> Unit = {},
    onOpenInvoiceDetail: (String) -> Unit = {},
    viewModel: PaymentMethodsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val planSpec = BillingPlanUiCatalog.specFor(state.planKey)
    var showAddCardDialog by rememberSaveable { mutableStateOf(false) }

    if (showAddCardDialog) {
        AddPaymentMethodDialog(
            isSaving = false,
            onDismiss = { showAddCardDialog = false },
            onSave = { brand, last4, month, year, isDefault ->
                viewModel.addCard(brand, last4, month, year, isDefault)
                showAddCardDialog = false
            },
        )
    }

    SettingsDetailInnerScaffold(
        title = stringResource(R.string.merchant_payment_methods_title),
        subtitle = stringResource(R.string.merchant_payment_methods_subtitle),
        onBack = onBack,
        contentPadding = contentPadding,
    ) {
        state.messageRes?.let { messageRes ->
            item {
                SettingsDetailSection(title = stringResource(messageRes)) {
                    Text(
                        text = stringResource(R.string.merchant_payment_methods_sync_note),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VerevColors.Forest.copy(alpha = 0.68f),
                    )
                }
                LaunchedEffect(messageRes) { viewModel.dismissMessage() }
            }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_payment_methods_plan_title)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.horizontalGradient(listOf(VerevColors.Gold, VerevColors.Tan)))
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(planSpec.nameRes),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = state.planPrice,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.88f),
                        )
                        Text(
                            text = stringResource(R.string.merchant_payment_methods_plan_renewal, state.renewalLabel),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.72f),
                        )
                    }
                    SettingsSectionBadge(text = stringResource(R.string.merchant_store_active))
                }
                TextButton(
                    onClick = onOpenPlanSelection,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.merchant_plan_selection_open))
                }
            }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_payment_methods_saved_methods)) {
                state.methods.forEachIndexed { index, method ->
                    PaymentMethodRow(
                        method = method,
                        onMakeDefault = { viewModel.makeDefault(method.id) },
                        onRemove = { viewModel.removeMethod(method.id) },
                    )
                    if (index < state.methods.lastIndex) {
                        HorizontalDivider(color = VerevColors.Inactive.copy(alpha = 0.16f))
                    }
                }
                Button(
                    onClick = { showAddCardDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = VerevColors.Forest),
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null)
                    Text(
                        text = stringResource(R.string.merchant_payment_methods_add_card_cta),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_payment_methods_invoices_title)) {
                state.invoices.forEachIndexed { index, invoice ->
                    SettingsMenuRow(
                        title = invoice.title,
                        subtitle = invoice.subtitle,
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        trailingLabel = invoice.amount,
                        onClick = { onOpenInvoiceDetail(invoice.id) },
                    )
                    SettingsSectionBadge(text = stringResource(invoice.statusRes))
                    if (index < state.invoices.lastIndex) {
                        HorizontalDivider(color = VerevColors.Inactive.copy(alpha = 0.16f))
                    }
                }
                TextButton(
                    onClick = onOpenInvoices,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.merchant_all_invoices_open))
                }
            }
        }
    }
}

@Composable
fun BrandingScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    shellViewModel: ShellViewModel = hiltViewModel(),
    viewModel: BrandingViewModel = hiltViewModel(),
) {
    val shellState by shellViewModel.uiState.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedPalette = state.palettes.firstOrNull { it.id == state.selectedPaletteId }

    SettingsDetailInnerScaffold(
        title = stringResource(R.string.merchant_branding_title),
        subtitle = stringResource(R.string.merchant_branding_subtitle),
        onBack = onBack,
        contentPadding = contentPadding,
    ) {
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_branding_preview_title)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    selectedPalette?.primary ?: VerevColors.Gold,
                                    selectedPalette?.accent ?: VerevColors.Tan,
                                ),
                            ),
                        )
                        .padding(20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Text(
                            text = shellState.selectedStore?.name ?: stringResource(R.string.merchant_settings_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.merchant_branding_preview_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                    }
                }
            }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_branding_theme_title)) {
                SettingsChipRow(
                    options = listOf(
                        ThemeModeUi.LIGHT to stringResource(R.string.merchant_branding_theme_light),
                        ThemeModeUi.DARK to stringResource(R.string.merchant_branding_theme_dark),
                        ThemeModeUi.AUTO to stringResource(R.string.merchant_branding_theme_auto),
                    ),
                    selected = state.themeMode,
                    onSelected = viewModel::setTheme,
                )
            }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_branding_palette_title)) {
                state.palettes.forEach { palette ->
                    SettingsPaletteCard(
                        palette = palette,
                        selected = palette.id == state.selectedPaletteId,
                        onClick = { viewModel.selectPalette(palette.id) },
                    )
                }
                TextButton(onClick = viewModel::reset) {
                    Icon(Icons.Default.Tune, contentDescription = null)
                    Text(
                        text = stringResource(R.string.merchant_branding_reset),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun PrivacyTermsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
) {
    var activeTab by rememberSaveable { mutableStateOf(PrivacyTabUi.PRIVACY) }
    var expandedSectionId by rememberSaveable { mutableStateOf("privacy_main") }
    val sections = rememberPrivacySections(activeTab)

    SettingsDetailInnerScaffold(
        title = stringResource(R.string.merchant_privacy_title),
        subtitle = stringResource(R.string.merchant_privacy_subtitle),
        onBack = onBack,
        contentPadding = contentPadding,
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PrivacyTabButton(
                    title = stringResource(R.string.merchant_privacy_tab_policy),
                    icon = Icons.Default.Shield,
                    modifier = Modifier.weight(1f),
                    selected = activeTab == PrivacyTabUi.PRIVACY,
                    onClick = {
                        activeTab = PrivacyTabUi.PRIVACY
                        expandedSectionId = "privacy_main"
                    },
                )
                PrivacyTabButton(
                    title = stringResource(R.string.merchant_privacy_tab_terms),
                    icon = Icons.Default.Description,
                    modifier = Modifier.weight(1f),
                    selected = activeTab == PrivacyTabUi.TERMS,
                    onClick = {
                        activeTab = PrivacyTabUi.TERMS
                        expandedSectionId = "terms_main"
                    },
                )
            }
        }
        items(sections, key = { it.id }) { section ->
            SettingsDetailSection(title = section.title) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedSectionId = if (expandedSectionId == section.id) "" else section.id },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(section.accentColors.map { it.copy(alpha = 0.12f) })),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (activeTab == PrivacyTabUi.PRIVACY) Icons.Default.Shield else Icons.Default.Description,
                                contentDescription = null,
                                tint = VerevColors.Forest,
                            )
                        }
                        Column {
                            Text(
                                text = section.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = VerevColors.Forest.copy(alpha = 0.72f),
                            )
                        }
                    }
                    Icon(
                        imageVector = if (expandedSectionId == section.id) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = VerevColors.Inactive,
                    )
                }
                if (expandedSectionId == section.id) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        section.bullets.forEach { bullet ->
                            SettingsCheckBullet(text = bullet)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsDetailInnerScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    contentPadding: PaddingValues,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SettingsCompactHeader(
            title = title,
            subtitle = subtitle,
            onBack = onBack,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

@Composable
private fun PaymentMethodRow(
    method: PaymentMethodUi,
    onMakeDefault: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(VerevColors.Forest, VerevColors.Moss))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White)
                }
                Column {
                    Text(
                        text = stringResource(R.string.merchant_payment_methods_card_label, method.brand, method.last4),
                        style = MaterialTheme.typography.titleMedium,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = stringResource(R.string.merchant_payment_methods_card_expiry, method.expiry),
                        style = MaterialTheme.typography.bodySmall,
                        color = VerevColors.Forest.copy(alpha = 0.56f),
                    )
                }
            }
            if (method.isDefault) {
                SettingsSectionBadge(text = stringResource(R.string.merchant_payment_methods_default))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!method.isDefault) {
                TextButton(onClick = onMakeDefault) {
                    Text(stringResource(R.string.merchant_payment_methods_set_default))
                }
            }
            TextButton(onClick = onRemove) {
                Text(
                    text = stringResource(R.string.merchant_payment_methods_remove),
                    color = Color(0xFFDC2626),
                )
            }
        }
    }
}

@Composable
private fun PrivacyTabButton(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) Brush.horizontalGradient(listOf(VerevColors.Forest, VerevColors.Moss)) else Brush.horizontalGradient(listOf(Color.White, Color.White)),
            )
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = VerevColors.Inactive.copy(alpha = 0.22f),
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (selected) Color.White else VerevColors.Forest)
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) Color.White else VerevColors.Forest,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun rememberPrivacySections(tab: PrivacyTabUi): List<PrivacySectionUi> = when (tab) {
    PrivacyTabUi.PRIVACY -> listOf(
        PrivacySectionUi(
            id = "privacy_main",
            title = stringResource(R.string.merchant_privacy_section_privacy_title),
            subtitle = stringResource(R.string.merchant_privacy_section_privacy_subtitle),
            bullets = listOf(
                stringResource(R.string.merchant_privacy_privacy_point_one),
                stringResource(R.string.merchant_privacy_privacy_point_two),
                stringResource(R.string.merchant_privacy_privacy_point_three),
            ),
            accentColors = listOf(VerevColors.Moss, VerevColors.Forest),
        ),
        PrivacySectionUi(
            id = "privacy_data",
            title = stringResource(R.string.merchant_privacy_section_data_title),
            subtitle = stringResource(R.string.merchant_privacy_section_data_subtitle),
            bullets = listOf(
                stringResource(R.string.merchant_privacy_data_point_one),
                stringResource(R.string.merchant_privacy_data_point_two),
                stringResource(R.string.merchant_privacy_data_point_three),
            ),
            accentColors = listOf(VerevColors.Gold, VerevColors.Tan),
        ),
    )
    PrivacyTabUi.TERMS -> listOf(
        PrivacySectionUi(
            id = "terms_main",
            title = stringResource(R.string.merchant_terms_section_service_title),
            subtitle = stringResource(R.string.merchant_terms_section_service_subtitle),
            bullets = listOf(
                stringResource(R.string.merchant_terms_service_point_one),
                stringResource(R.string.merchant_terms_service_point_two),
                stringResource(R.string.merchant_terms_service_point_three),
            ),
            accentColors = listOf(VerevColors.Forest, VerevColors.Moss),
        ),
        PrivacySectionUi(
            id = "terms_security",
            title = stringResource(R.string.merchant_terms_section_security_title),
            subtitle = stringResource(R.string.merchant_terms_section_security_subtitle),
            bullets = listOf(
                stringResource(R.string.merchant_terms_security_point_one),
                stringResource(R.string.merchant_terms_security_point_two),
                stringResource(R.string.merchant_terms_security_point_three),
            ),
            accentColors = listOf(VerevColors.Gold, VerevColors.Tan),
        ),
    )
}

private fun parseHexColor(raw: String?, fallback: Color): Color = try {
    raw?.removePrefix("#")?.takeIf { it.length == 6 }?.let { value ->
        Color(android.graphics.Color.parseColor("#$value"))
    } ?: fallback
} catch (_: IllegalArgumentException) {
    fallback
}
