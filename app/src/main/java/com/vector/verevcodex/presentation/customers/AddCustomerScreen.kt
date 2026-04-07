package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.merchant.common.MerchantErrorDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantLoadingOverlay
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun AddCustomerScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    viewModel: AddCustomerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var copied by remember(state.activationLink) { mutableStateOf(false) }

    val selectedOption = state.selectedProvisioningOption ?: CustomerCardProvisioningOption.BARCODE_IMAGE
    val customerName = state.successName.ifBlank { stringResource(R.string.merchant_add_customer_title) }
    val generalErrorRes = state.errorRes?.takeIf {
        it != R.string.merchant_add_customer_error_first_name &&
            it != R.string.merchant_add_customer_error_last_name &&
            it != R.string.merchant_add_customer_error_email &&
            it != R.string.merchant_add_customer_error_phone
    }
    val sharePayload = remember(
        selectedOption,
        customerName,
        state.generatedLoyaltyId,
        state.activationLink,
    ) {
        if (state.generatedLoyaltyId.isBlank() || state.activationLink.isBlank()) {
            null
        } else {
            buildProvisioningSharePayload(
                context = context,
                customerName = customerName,
                loyaltyId = state.generatedLoyaltyId,
                activationLink = state.activationLink,
                option = selectedOption,
            )
        }
    }

    fun openEmail() {
        val payload = sharePayload ?: return
        emailProvisioningPayload(context, payload)
    }

    fun openSms() {
        val payload = sharePayload ?: return
        smsProvisioningPayload(context, payload)
    }

    fun copyShareValue() {
        val payload = sharePayload ?: return
        clipboardManager.setText(AnnotatedString(payload.copyValue))
        copied = true
    }

    fun shareCurrentProvisioning() {
        val payload = sharePayload ?: return
        shareProvisioningPayload(context, payload)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.createdCustomerId == null) {
            AddCustomerEntryScaffold(
                bottomInset = contentPadding.calculateBottomPadding(),
                selectedStoreName = state.selectedStoreName,
                onBack = onBack,
            ) {
                AddCustomerFormSheet(
                    state = state,
                    onFirstNameChanged = viewModel::onFirstNameChanged,
                    onLastNameChanged = viewModel::onLastNameChanged,
                    onBirthDateChanged = viewModel::onBirthDateChanged,
                    onGenderSelected = viewModel::onGenderSelected,
                    onEmailChanged = viewModel::onEmailChanged,
                    onPhoneChanged = viewModel::onPhoneChanged,
                    onCreateCustomer = viewModel::createCustomer,
                    onCancel = onBack,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VerevColors.AppBackground)
                    .padding(bottom = contentPadding.calculateBottomPadding() + 16.dp),
            ) {
                AddCustomerSuccessSheet(
                    state = state,
                    copied = copied,
                    onClose = onBack,
                    onCopyLink = ::copyShareValue,
                    onOpenEmail = ::openEmail,
                    onOpenSms = ::openSms,
                    onShareLink = ::shareCurrentProvisioning,
                    onAddAnother = {
                        copied = false
                        viewModel.resetForm()
                    },
                    onSelectProvisioningOption = viewModel::selectProvisioningOption,
                    onStartNfcWrite = viewModel::startNfcWrite,
                    onRetryNfcWrite = viewModel::retryNfcWrite,
                    onNfcDone = onBack,
                )
            }
        }
        MerchantLoadingOverlay(
            isVisible = state.isSaving || state.walletIsSaving || state.nfcWritePhase == NfcWritePhase.WRITING,
            title = when {
                state.walletIsSaving -> stringResource(R.string.merchant_loader_wallet_title)
                state.nfcWritePhase == NfcWritePhase.WRITING -> stringResource(R.string.merchant_loader_nfc_write_title)
                else -> stringResource(R.string.merchant_loader_customer_create_title)
            },
            subtitle = when {
                state.walletIsSaving -> stringResource(R.string.merchant_loader_wallet_subtitle)
                state.nfcWritePhase == NfcWritePhase.WRITING -> stringResource(R.string.merchant_loader_nfc_write_subtitle)
                else -> stringResource(R.string.merchant_loader_customer_create_subtitle)
            },
        )
        generalErrorRes?.let { errorRes ->
            MerchantErrorDialog(
                message = stringResource(errorRes),
                onDismiss = viewModel::dismissError,
            )
        }
    }
}

@Composable
private fun AddCustomerEntryScaffold(
    bottomInset: androidx.compose.ui.unit.Dp,
    selectedStoreName: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VerevColors.AppBackground),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(VerevColors.Moss, VerevColors.ForestDeep)))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
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
                    text = stringResource(R.string.merchant_add_customer_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(
                        R.string.merchant_add_customer_subtitle,
                        selectedStoreName.ifBlank { stringResource(R.string.merchant_select_store) },
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.82f),
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
                    .padding(
                        start = 18.dp,
                        end = 18.dp,
                        top = 18.dp,
                        bottom = bottomInset + 18.dp,
                    )
                    .navigationBarsPadding(),
            ) {
                content()
            }
        }
    }
}
