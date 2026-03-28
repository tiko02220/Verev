package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
internal fun AddCustomerSuccessSheet(
    state: AddCustomerUiState,
    copied: Boolean,
    onClose: () -> Unit,
    onCopyLink: () -> Unit,
    onOpenEmail: () -> Unit,
    onOpenSms: () -> Unit,
    onShareLink: () -> Unit,
    onAddAnother: () -> Unit,
    onSelectProvisioningOption: (CustomerCardProvisioningOption) -> Unit,
    onStartNfcWrite: () -> Unit,
    onRetryNfcWrite: () -> Unit,
    onNfcDone: () -> Unit,
) {
    val selectedOption = state.selectedProvisioningOption ?: CustomerCardProvisioningOption.GOOGLE_WALLET

    CustomerCardScaffold(
        selectedOption = selectedOption,
        onBack = onClose,
        onSelectProvisioningOption = onSelectProvisioningOption,
    ) {
        ProvisioningOptionBody(
            state = state,
            copied = copied,
            selectedOption = selectedOption,
            onCopyLink = onCopyLink,
            onOpenEmail = onOpenEmail,
            onOpenSms = onOpenSms,
            onShareLink = onShareLink,
            onStartNfcWrite = onStartNfcWrite,
            onRetryNfcWrite = onRetryNfcWrite,
            onNfcDone = onNfcDone,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onAddAnother,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, VerevColors.Forest.copy(alpha = 0.16f)),
            ) {
                Text(
                    text = stringResource(R.string.merchant_add_customer_add_another),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Button(
                onClick = onClose,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerevColors.Forest,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = stringResource(R.string.merchant_add_customer_close),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}

@Composable
internal fun ProvisioningChoiceSection(
    selectedOption: CustomerCardProvisioningOption?,
    onSelectProvisioningOption: (CustomerCardProvisioningOption) -> Unit,
) {
    val activeOption = selectedOption ?: CustomerCardProvisioningOption.GOOGLE_WALLET
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CustomerCardTab(
            title = stringResource(R.string.merchant_customer_card_wallet),
            icon = Icons.Default.PhoneAndroid,
            selected = activeOption == CustomerCardProvisioningOption.GOOGLE_WALLET,
            modifier = Modifier.weight(1f),
            onClick = { onSelectProvisioningOption(CustomerCardProvisioningOption.GOOGLE_WALLET) },
        )
        CustomerCardTab(
            title = stringResource(R.string.merchant_customer_card_nfc),
            icon = Icons.Default.CreditCard,
            selected = activeOption == CustomerCardProvisioningOption.NFC_CARD,
            modifier = Modifier.weight(1f),
            onClick = { onSelectProvisioningOption(CustomerCardProvisioningOption.NFC_CARD) },
        )
        CustomerCardTab(
            title = stringResource(R.string.merchant_customer_card_barcode),
            icon = Icons.Default.QrCode2,
            selected = activeOption == CustomerCardProvisioningOption.BARCODE_IMAGE,
            modifier = Modifier.weight(1f),
            onClick = { onSelectProvisioningOption(CustomerCardProvisioningOption.BARCODE_IMAGE) },
        )
    }
}

@Composable
internal fun BarcodeProvisioningPanel(
    state: AddCustomerUiState,
    onPrimaryAction: () -> Unit = {},
    onOpenSms: () -> Unit = {},
    onOpenEmail: () -> Unit = {},
    onOpenShare: () -> Unit = {},
) {
    var showExpandedBarcode by remember(state.barcodeValue) { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = stringResource(R.string.merchant_add_customer_barcode_title_design),
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFF6F6F6),
                    border = BorderStroke(1.dp, VerevColors.Forest.copy(alpha = 0.12f)),
                    modifier = Modifier.clickable { showExpandedBarcode = true },
                ) {
                    LoyaltyBarcodeCard(
                        value = state.barcodeValue,
                        modifier = Modifier.fillMaxWidth().height(170.dp).padding(horizontal = 18.dp, vertical = 26.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.merchant_add_customer_barcode_tap_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = VerevColors.Gold,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Surface(shape = RoundedCornerShape(28.dp), color = Color.White) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.merchant_add_customer_barcode_actions_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                GradientPrimaryProvisioningButton(
                    text = stringResource(R.string.merchant_add_customer_barcode_primary_action),
                    icon = Icons.Default.Share,
                    onClick = onPrimaryAction,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ShareActionTile(
                        icon = Icons.Default.Sms,
                        label = stringResource(R.string.merchant_add_customer_share_sms),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenSms,
                    )
                    ShareActionTile(
                        icon = Icons.Default.Email,
                        label = stringResource(R.string.merchant_add_customer_share_email),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenEmail,
                    )
                    ShareActionTile(
                        icon = Icons.Default.Share,
                        label = stringResource(R.string.merchant_add_customer_share_share),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenShare,
                    )
                }
            }
        }

        ProvisioningInfoCard(
            title = stringResource(R.string.merchant_add_customer_barcode_info_title),
            subtitle = stringResource(R.string.merchant_add_customer_barcode_supporting),
            icon = Icons.Default.QrCode2,
            accentColor = VerevColors.Moss,
        )

        if (showExpandedBarcode) {
            ExpandedCodeDialog(
                title = stringResource(R.string.merchant_customer_card_barcode),
                onDismiss = { showExpandedBarcode = false },
            ) {
                LoyaltyBarcodeCard(
                    value = state.barcodeValue,
                    modifier = Modifier.fillMaxWidth().height(320.dp),
                )
            }
        }
    }
}

@Composable
internal fun WalletProvisioningPanel(
    state: AddCustomerUiState,
    copied: Boolean,
    onCopyLink: () -> Unit,
    onOpenEmail: () -> Unit,
    onOpenSms: () -> Unit,
    onShareLink: () -> Unit,
) {
    var showExpandedQr by remember(state.activationLink, state.generatedLoyaltyId) { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = Color.Transparent,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(VerevColors.ForestDeep, VerevColors.Forest, Color(0xFF6E9B77))))
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = stringResource(R.string.merchant_add_customer_wallet_panel_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().clickable { showExpandedQr = true },
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        LoyaltyQrCode(
                            value = state.activationLink.ifBlank { state.generatedLoyaltyId },
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                        )
                        Text(
                            text = stringResource(R.string.merchant_add_customer_wallet_scan_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = VerevColors.Forest.copy(alpha = 0.72f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Surface(shape = RoundedCornerShape(24.dp), color = Color.White.copy(alpha = 0.12f)) {
                    Text(
                        text = stringResource(R.string.merchant_add_customer_wallet_customer_delivery_note),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        ShareLinkCard(
            activationLink = state.activationLink,
            onCopyLink = onCopyLink,
            onOpenEmail = onOpenEmail,
            onOpenSms = onOpenSms,
            onOpenShare = onShareLink,
            copied = copied,
        )

        ProvisioningInfoCard(
            title = stringResource(R.string.merchant_add_customer_how_it_works_title),
            subtitle = stringResource(R.string.merchant_add_customer_wallet_how_it_works),
            icon = Icons.Default.PhoneAndroid,
            accentColor = VerevColors.Gold,
        )
    }

    if (showExpandedQr) {
        ExpandedCodeDialog(
            title = stringResource(R.string.merchant_customer_card_wallet),
            onDismiss = { showExpandedQr = false },
        ) {
            LoyaltyQrCode(
                value = state.activationLink.ifBlank { state.generatedLoyaltyId },
                modifier = Modifier.fillMaxWidth().height(360.dp),
            )
        }
    }
}

@Composable
internal fun NfcProvisioningPanel(
    state: AddCustomerUiState,
    onStartNfcWrite: () -> Unit,
    onRetryNfcWrite: () -> Unit,
    onNfcDone: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Surface(shape = RoundedCornerShape(28.dp), color = Color.White) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(Brush.linearGradient(listOf(VerevColors.Tan, VerevColors.Forest))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp),
                    )
                }
                Text(
                    text = when (state.nfcWritePhase) {
                        NfcWritePhase.SUCCESS -> stringResource(R.string.merchant_add_customer_nfc_success_title)
                        else -> stringResource(R.string.merchant_add_customer_nfc_active_title)
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = state.nfcStatusRes?.let { stringResource(it) }
                        ?: stringResource(R.string.merchant_add_customer_nfc_panel_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = VerevColors.Forest.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center,
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            border = BorderStroke(1.dp, VerevColors.Tan.copy(alpha = 0.28f)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.merchant_add_customer_how_it_works_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                OrderedInstructionRow(1, stringResource(R.string.merchant_add_customer_nfc_step_1))
                OrderedInstructionRow(2, stringResource(R.string.merchant_add_customer_nfc_step_2))
                OrderedInstructionRow(3, stringResource(R.string.merchant_add_customer_nfc_step_3))
                OrderedInstructionRow(4, stringResource(R.string.merchant_add_customer_nfc_step_4))
            }
        }

        when (state.nfcWritePhase) {
            NfcWritePhase.IDLE -> GradientPrimaryProvisioningButton(
                text = stringResource(R.string.merchant_add_customer_nfc_write),
                icon = Icons.Default.Nfc,
                onClick = onStartNfcWrite,
            )
            NfcWritePhase.READY, NfcWritePhase.WRITING -> {
                GradientPrimaryProvisioningButton(
                    text = stringResource(R.string.merchant_add_customer_nfc_waiting_for_tap),
                    icon = Icons.Default.Nfc,
                    enabled = false,
                    onClick = onStartNfcWrite,
                )
                OutlinedButton(
                    onClick = onNfcDone,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(stringResource(R.string.auth_cancel))
                }
            }
            NfcWritePhase.SUCCESS -> GradientPrimaryProvisioningButton(
                text = stringResource(R.string.merchant_add_customer_nfc_done),
                icon = Icons.Default.Check,
                onClick = onNfcDone,
            )
            NfcWritePhase.ERROR -> GradientPrimaryProvisioningButton(
                text = stringResource(R.string.merchant_add_customer_nfc_retry),
                icon = Icons.Default.Nfc,
                onClick = onRetryNfcWrite,
            )
        }
    }
}

@Composable
internal fun CustomerCardScaffold(
    selectedOption: CustomerCardProvisioningOption,
    onBack: () -> Unit,
    onSelectProvisioningOption: (CustomerCardProvisioningOption) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().background(VerevColors.AppBackground),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(VerevColors.ForestDeep, VerevColors.Forest, Color(0xFF6E9B77))))
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.auth_back),
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Text(
                        text = stringResource(R.string.merchant_customer_card_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                ProvisioningChoiceSection(
                    selectedOption = selectedOption,
                    onSelectProvisioningOption = onSelectProvisioningOption,
                )
            }
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            color = VerevColors.AppBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 18.dp, vertical = 18.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun CustomerCardTab(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Color.White else Color.White.copy(alpha = 0.12f),
        border = if (selected) BorderStroke(2.dp, VerevColors.ForestDeep.copy(alpha = 0.88f)) else null,
        shadowElevation = if (selected) 4.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier.height(76.dp).padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) VerevColors.Forest else Color.White.copy(alpha = 0.84f),
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) VerevColors.Forest else Color.White.copy(alpha = 0.92f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ExpandedCodeDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = VerevColors.AppBackground) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier.size(52.dp).clip(CircleShape).background(VerevColors.Forest.copy(alpha = 0.12f)).clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.auth_back),
                            tint = VerevColors.Forest,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = VerevColors.Forest,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp), color = Color.White) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) { content() }
                }
            }
        }
    }
}

@Composable
private fun OrderedInstructionRow(index: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "$index.",
            style = MaterialTheme.typography.titleSmall,
            color = VerevColors.Tan,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = VerevColors.Forest.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun GradientPrimaryProvisioningButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = VerevColors.Forest.copy(alpha = 0.34f),
            contentColor = Color.White,
            disabledContentColor = Color.White,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan, VerevColors.Forest))),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ShareLinkCard(
    activationLink: String,
    onCopyLink: () -> Unit,
    onOpenEmail: () -> Unit,
    onOpenSms: () -> Unit,
    onOpenShare: () -> Unit,
    copied: Boolean,
) {
    Surface(shape = RoundedCornerShape(28.dp), color = Color.White) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.merchant_add_customer_share_link_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Surface(shape = RoundedCornerShape(999.dp), color = VerevColors.AppBackground) {
                    Text(
                        text = stringResource(R.string.merchant_add_customer_optional),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = VerevColors.Moss,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = VerevColors.AppBackground,
                border = BorderStroke(1.dp, VerevColors.Forest.copy(alpha = 0.08f)),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.merchant_add_customer_card_url_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = VerevColors.Forest.copy(alpha = 0.56f),
                    )
                    Text(
                        text = activationLink,
                        style = MaterialTheme.typography.titleSmall,
                        color = VerevColors.Forest,
                    )
                }
            }
            GradientPrimaryProvisioningButton(
                text = stringResource(if (copied) R.string.auth_copied else R.string.merchant_add_customer_copy_link_primary),
                icon = Icons.Default.ContentCopy,
                onClick = onCopyLink,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ShareActionTile(Icons.Default.Sms, stringResource(R.string.merchant_add_customer_share_sms), Modifier.weight(1f), onOpenSms)
                ShareActionTile(Icons.Default.Email, stringResource(R.string.merchant_add_customer_share_email), Modifier.weight(1f), onOpenEmail)
                ShareActionTile(Icons.Default.Share, stringResource(R.string.merchant_add_customer_share_share), Modifier.weight(1f), onOpenShare)
            }
        }
    }
}

@Composable
private fun ShareActionTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.dp, VerevColors.Moss.copy(alpha = 0.26f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(VerevColors.Moss.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = VerevColors.Moss)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = VerevColors.Forest,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun ProvisioningInfoCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accentColor)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = VerevColors.Forest,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VerevColors.Forest.copy(alpha = 0.78f),
                )
            }
        }
    }
}

@Composable
internal fun ProvisioningOptionBody(
    state: AddCustomerUiState,
    copied: Boolean,
    selectedOption: CustomerCardProvisioningOption,
    onCopyLink: () -> Unit,
    onOpenEmail: () -> Unit,
    onOpenSms: () -> Unit,
    onShareLink: () -> Unit,
    onStartNfcWrite: () -> Unit,
    onRetryNfcWrite: () -> Unit,
    onNfcDone: () -> Unit,
) {
    when (selectedOption) {
        CustomerCardProvisioningOption.GOOGLE_WALLET -> WalletProvisioningPanel(
            state = state,
            copied = copied,
            onCopyLink = onCopyLink,
            onOpenEmail = onOpenEmail,
            onOpenSms = onOpenSms,
            onShareLink = onShareLink,
        )
        CustomerCardProvisioningOption.NFC_CARD -> NfcProvisioningPanel(
            state = state,
            onStartNfcWrite = onStartNfcWrite,
            onRetryNfcWrite = onRetryNfcWrite,
            onNfcDone = onNfcDone,
        )
        CustomerCardProvisioningOption.BARCODE_IMAGE -> BarcodeProvisioningPanel(
            state = state,
            onPrimaryAction = onShareLink,
            onOpenSms = onOpenSms,
            onOpenEmail = onOpenEmail,
            onOpenShare = onShareLink,
        )
    }
}
