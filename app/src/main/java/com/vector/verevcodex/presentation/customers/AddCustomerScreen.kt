package com.vector.verevcodex.presentation.customers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.core.platform.findActivity
import com.vector.verevcodex.presentation.merchant.common.MerchantBackHeader
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun AddCustomerScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    viewModel: AddCustomerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var copied by remember(state.activationLink) { mutableStateOf(false) }

    val selectedOption = state.selectedProvisioningOption ?: CustomerCardProvisioningOption.BARCODE_IMAGE
    val customerName = state.successName.ifBlank { stringResource(R.string.merchant_add_customer_title) }
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

    if (state.createdCustomerId == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VerevColors.AppBackground)
                .verticalScroll(rememberScrollState())
                .padding(
                    top = contentPadding.calculateTopPadding() + 20.dp,
                    bottom = contentPadding.calculateBottomPadding() + 96.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                MerchantBackHeader(
                    title = stringResource(R.string.merchant_add_customer_title),
                    subtitle = stringResource(
                        R.string.merchant_add_customer_subtitle,
                        state.selectedStoreName.ifBlank { stringResource(R.string.merchant_select_store) },
                    ),
                    onBack = onBack,
                )
                AddCustomerFormSheet(
                    state = state,
                    onFirstNameChanged = viewModel::onFirstNameChanged,
                    onLastNameChanged = viewModel::onLastNameChanged,
                    onEmailChanged = viewModel::onEmailChanged,
                    onPhoneChanged = viewModel::onPhoneChanged,
                    onCreateCustomer = viewModel::createCustomer,
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VerevColors.AppBackground)
                .padding(
                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                ),
        ) {
            AddCustomerSuccessSheet(
                state = state,
                copied = copied,
                onClose = onBack,
                onCopyLink = ::copyShareValue,
                onOpenEmail = ::openEmail,
                onOpenSms = ::openSms,
                onShareLink = ::shareCurrentProvisioning,
                onOpenProfile = { state.createdCustomerId?.let(onOpenProfile) },
                onAddAnother = {
                    copied = false
                    viewModel.resetForm()
                },
                onSelectProvisioningOption = viewModel::selectProvisioningOption,
                onLaunchGoogleWallet = ::shareCurrentProvisioning,
                onStartNfcWrite = viewModel::startNfcWrite,
                onRetryNfcWrite = viewModel::retryNfcWrite,
                onClearNfcState = viewModel::clearNfcState,
                onRefreshWalletStatus = viewModel::refreshWalletAvailability,
            )
        }
    }
}
