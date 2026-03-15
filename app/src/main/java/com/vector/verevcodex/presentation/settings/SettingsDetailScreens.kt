package com.vector.verevcodex.presentation.settings

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.MainActivity
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.navigation.ShellViewModel
import com.vector.verevcodex.presentation.theme.VerevColors
import java.io.OutputStreamWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            SettingsPageIntro(
                title = stringResource(R.string.merchant_business_details_locations_heading),
                subtitle = stringResource(R.string.merchant_business_details_locations_subtitle),
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(shellState.stores, key = { it.id }) { store ->
                    SettingsBranchCard(
                        name = store.name,
                        address = store.address,
                        isSelected = store.id == selectedStore?.id,
                        isMain = store.id == shellState.stores.firstOrNull()?.id,
                        active = store.active,
                        onClick = { shellViewModel.selectStore(store.id) },
                    )
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(VerevColors.Forest, VerevColors.Moss)))
                    .padding(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = selectedStore?.name ?: stringResource(R.string.merchant_select_store),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = selectedStore?.address ?: stringResource(R.string.merchant_settings_value_unavailable),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.78f),
                        )
                    }
                    SettingsSectionBadge(
                        text = if (selectedStore?.active == true) {
                            stringResource(R.string.merchant_store_active)
                        } else {
                            stringResource(R.string.merchant_store_disabled)
                        },
                    )
                }
            }
        }
        item {
            SettingsDetailSection(title = stringResource(R.string.merchant_business_details_current_location)) {
                SettingsDetailRow(
                    label = stringResource(R.string.merchant_business_details_category_label),
                    value = selectedStore?.category ?: stringResource(R.string.merchant_settings_value_unavailable),
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
            SettingsPageIntro(
                title = stringResource(R.string.merchant_business_details_branch_tools_title),
                subtitle = stringResource(R.string.merchant_business_details_branch_tools_subtitle),
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    SettingsActionTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Add,
                        title = stringResource(R.string.merchant_add_branch),
                        subtitle = stringResource(R.string.merchant_business_details_add_branch_subtitle),
                        accent = VerevColors.Gold,
                        onClick = onOpenAddBranch,
                    )
                    SettingsActionTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Description,
                        title = stringResource(R.string.merchant_edit_branch),
                        subtitle = stringResource(R.string.merchant_store_edit_branch_subtitle),
                        accent = VerevColors.Moss,
                        onClick = { selectedStore?.id?.let(onOpenEditBranch) },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    SettingsActionTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Business,
                        title = stringResource(R.string.merchant_manage_locations),
                        subtitle = stringResource(R.string.merchant_business_details_manage_locations_subtitle),
                        accent = VerevColors.Forest,
                        onClick = onOpenStoreManagement,
                    )
                    SettingsActionTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Tune,
                        title = stringResource(R.string.merchant_branch_staff_config_title),
                        subtitle = stringResource(R.string.merchant_business_details_branch_staff_subtitle),
                        accent = VerevColors.Tan,
                        onClick = { selectedStore?.id?.let(onOpenBranchStaffConfig) },
                    )
                }
                SettingsActionTile(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.merchant_branch_programs_title),
                    subtitle = stringResource(R.string.merchant_business_details_branch_programs_subtitle),
                    accent = VerevColors.Moss,
                    onClick = { selectedStore?.id?.let(onOpenBranchProgramsConfig) },
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

    Column(modifier = Modifier.fillMaxSize()) {
        SettingsLargeGradientHeader(
            title = stringResource(R.string.merchant_payment_methods_title),
            subtitle = stringResource(R.string.merchant_payment_methods_subtitle),
            onBack = onBack,
            colors = listOf(VerevColors.Forest, VerevColors.Moss),
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
            state.messageRes?.let { messageRes ->
                item { LaunchedEffect(messageRes) { viewModel.dismissMessage() } }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan)))
                        .padding(18.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = Color.White)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                        }
                        Button(
                            onClick = onOpenPlanSelection,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = VerevColors.Tan),
                        ) {
                            Text(stringResource(R.string.merchant_plan_selection_open))
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SettingsPageIntro(
                        title = stringResource(R.string.merchant_payment_methods_saved_methods),
                        subtitle = stringResource(R.string.merchant_payment_methods_sync_note),
                    )
                }
            }
            items(state.methods, key = { it.id }) { method ->
                PaymentMethodCard(
                    method = method,
                    onMakeDefault = { viewModel.makeDefault(method.id) },
                    onRemove = { viewModel.removeMethod(method.id) },
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(2.dp, VerevColors.Gold.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
                        .clickable(onClick = { showAddCardDialog = true })
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan))),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(R.string.merchant_payment_methods_add_card_cta),
                                style = MaterialTheme.typography.titleMedium,
                                color = VerevColors.Forest,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(R.string.merchant_payment_methods_sync_note),
                                style = MaterialTheme.typography.bodySmall,
                                color = VerevColors.Forest.copy(alpha = 0.6f),
                            )
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SettingsPageIntro(
                        title = stringResource(R.string.merchant_payment_methods_invoices_title),
                        subtitle = stringResource(R.string.merchant_payment_methods_invoices_subtitle),
                    )
                    TextButton(onClick = onOpenInvoices) {
                        Text(stringResource(R.string.merchant_all_invoices_open))
                    }
                }
            }
            items(state.invoices, key = { it.id }) { invoice ->
                BillingHistoryCard(
                    title = invoice.title,
                    subtitle = invoice.subtitle,
                    amount = invoice.amount,
                    status = stringResource(invoice.statusRes),
                    onClick = { onOpenInvoiceDetail(invoice.id) },
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(VerevColors.Moss.copy(alpha = 0.06f))
                        .border(1.dp, VerevColors.Moss.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = VerevColors.Moss)
                        Text(
                            text = stringResource(R.string.merchant_payment_methods_security_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.72f),
                        )
                    }
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
    val context = LocalContext.current
    val activity = context as? MainActivity
    val logoBitmap = rememberSettingsImageBitmap(state.logoUri)
    val logoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            viewModel.updateLogoUri(uri.toString())
        }
    }

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
            SettingsDetailSection(title = stringResource(R.string.merchant_branding_logo_title)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFF6F7F4)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (logoBitmap != null) {
                            Image(
                                bitmap = logoBitmap,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = VerevColors.Forest.copy(alpha = 0.58f),
                                modifier = Modifier.size(30.dp),
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.merchant_branding_logo_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = VerevColors.Forest.copy(alpha = 0.72f),
                        )
                        TextButton(
                            onClick = {
                                activity?.suppressRelockForTransientSystemUi()
                                logoPickerLauncher.launch(arrayOf("image/*"))
                            },
                        ) {
                            Text(stringResource(R.string.merchant_branding_upload_logo))
                        }
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
                state.palettes.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        pair.forEach { palette ->
                            Box(modifier = Modifier.weight(1f)) {
                                SettingsPaletteCard(
                                    palette = palette,
                                    selected = palette.id == state.selectedPaletteId,
                                    onClick = { viewModel.selectPalette(palette.id) },
                                )
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
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
    var expandedSectionId by rememberSaveable { mutableStateOf(defaultPrivacySectionId(PrivacyTabUi.PRIVACY)) }
    val sections = rememberPrivacySections(activeTab)
    val context = LocalContext.current
    val activity = context as? MainActivity
    val exportTitle = stringResource(
        if (activeTab == PrivacyTabUi.PRIVACY) {
            R.string.merchant_privacy_tab_policy
        } else {
            R.string.merchant_privacy_tab_terms
        },
    )
    val exportLastUpdatedLabel = stringResource(R.string.merchant_privacy_last_updated)
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(PRIVACY_EXPORT_MIME_TYPE)) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                OutputStreamWriter(output).use { writer ->
                    writer.write(
                        buildPrivacyExportText(
                            title = exportTitle,
                            lastUpdatedLabel = exportLastUpdatedLabel,
                            sections = sections,
                        ),
                    )
                }
            }
        }
    }

    SettingsDetailInnerScaffold(
        title = stringResource(R.string.merchant_privacy_title),
        subtitle = stringResource(R.string.merchant_privacy_subtitle),
        onBack = onBack,
        contentPadding = contentPadding,
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, VerevColors.Inactive.copy(alpha = 0.18f), RoundedCornerShape(22.dp))
                    .padding(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PrivacyTabButton(
                        title = stringResource(R.string.merchant_privacy_tab_policy),
                        icon = Icons.Default.Shield,
                        modifier = Modifier.weight(1f),
                        selected = activeTab == PrivacyTabUi.PRIVACY,
                        onClick = {
                            activeTab = PrivacyTabUi.PRIVACY
                            expandedSectionId = defaultPrivacySectionId(PrivacyTabUi.PRIVACY)
                        },
                    )
                    PrivacyTabButton(
                        title = stringResource(R.string.merchant_privacy_tab_terms),
                        icon = Icons.Default.Description,
                        modifier = Modifier.weight(1f),
                        selected = activeTab == PrivacyTabUi.TERMS,
                        onClick = {
                            activeTab = PrivacyTabUi.TERMS
                            expandedSectionId = defaultPrivacySectionId(PrivacyTabUi.TERMS)
                        },
                    )
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(VerevColors.Gold.copy(alpha = 0.14f), VerevColors.Tan.copy(alpha = 0.12f))))
                    .border(1.dp, VerevColors.Gold.copy(alpha = 0.18f), RoundedCornerShape(22.dp))
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                .clip(RoundedCornerShape(14.dp))
                                .background(VerevColors.Gold.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = VerevColors.Gold)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(R.string.merchant_privacy_last_updated),
                                style = MaterialTheme.typography.bodyMedium,
                                color = VerevColors.Forest,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = stringResource(R.string.merchant_privacy_version_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = VerevColors.Forest.copy(alpha = 0.62f),
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            activity?.suppressRelockForTransientSystemUi()
                            exportLauncher.launch(privacyExportFileName(activeTab))
                        },
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Text(stringResource(R.string.merchant_privacy_export))
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(modifier = Modifier.height(0.dp))
            }
        }
        items(sections, key = { it.id }) { section ->
            SettingsPrivacyCard(
                title = section.title,
                subtitle = section.subtitle,
                accentColors = section.accentColors,
                expanded = expandedSectionId == section.id,
                onClick = { expandedSectionId = if (expandedSectionId == section.id) "" else section.id },
                tab = activeTab,
                bullets = section.bullets,
            )
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
