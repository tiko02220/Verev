package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.vector.verevcodex.platform.android.findActivity
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun CustomerCredentialManagementScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    viewModel: CustomerCredentialManagementViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedOption by rememberSaveable { mutableStateOf(CustomerCardProvisioningOption.GOOGLE_WALLET) }
    var copied by rememberSaveable(state.activationLink) { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearTransientState() }
    }

    LaunchedEffect(selectedOption) {
        if (selectedOption == CustomerCardProvisioningOption.GOOGLE_WALLET) {
            viewModel.refreshWalletAvailability()
        }
    }

    val provisioningState = state.toProvisioningState()
    val customer = state.customer

    when {
        state.isMissingCustomer -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VerevColors.AppBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = contentPadding.calculateTopPadding() + 20.dp,
                        bottom = contentPadding.calculateBottomPadding() + 96.dp,
                    ),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    MerchantEmptyStateCard(
                        title = stringResource(R.string.merchant_customer_profile_missing_title),
                        subtitle = stringResource(R.string.merchant_customer_profile_missing_subtitle),
                        icon = Icons.Default.CreditCard,
                    )
                }
            }
        }
        customer == null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VerevColors.AppBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = contentPadding.calculateTopPadding() + 20.dp,
                        bottom = contentPadding.calculateBottomPadding() + 96.dp,
                    ),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    MerchantEmptyStateCard(
                        title = stringResource(R.string.merchant_customers_loading_title),
                        subtitle = stringResource(R.string.merchant_customers_loading_subtitle),
                        icon = Icons.Default.CreditCard,
                    )
                }
            }
        }
        else -> {
            val sharePayload = buildProvisioningSharePayload(
                context = context,
                customerName = customer.displayName(),
                loyaltyId = customer.loyaltyId,
                activationLink = state.activationLink,
                option = selectedOption,
            )
            fun launchWalletSave() {
                val activity = context.findActivity() ?: return
                viewModel.launchWalletSave(activity)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VerevColors.AppBackground)
                    .padding(
                        bottom = contentPadding.calculateBottomPadding() + 16.dp,
                    ),
            ) {
                CustomerCardScaffold(
                    selectedOption = selectedOption,
                    onBack = {
                        viewModel.clearTransientState()
                        onBack()
                    },
                    onSelectProvisioningOption = { selectedOption = it },
                ) {
                    ProvisioningOptionBody(
                        state = provisioningState,
                        copied = copied,
                        selectedOption = selectedOption,
                        onCopyLink = {
                            clipboardManager.setText(AnnotatedString(sharePayload.copyValue))
                            copied = true
                        },
                        onOpenEmail = { emailProvisioningPayload(context, sharePayload) },
                        onOpenSms = { smsProvisioningPayload(context, sharePayload) },
                        onShareLink = { shareProvisioningPayload(context, sharePayload) },
                        onLaunchGoogleWallet = ::launchWalletSave,
                        onStartNfcWrite = viewModel::startNfcWrite,
                        onRetryNfcWrite = viewModel::retryNfcWrite,
                        onClearNfcState = viewModel::clearTransientState,
                        onRefreshWalletStatus = viewModel::refreshWalletAvailability,
                    )
                    ProvisioningInfoCard(
                        title = stringResource(R.string.merchant_add_customer_code_label),
                        subtitle = stringResource(R.string.merchant_scan_loyalty_id, customer.loyaltyId),
                        icon = when (selectedOption) {
                            CustomerCardProvisioningOption.GOOGLE_WALLET -> Icons.Default.PhoneAndroid
                            CustomerCardProvisioningOption.NFC_CARD -> Icons.Default.CreditCard
                            CustomerCardProvisioningOption.BARCODE_IMAGE -> Icons.Default.QrCode2
                        },
                        accentColor = VerevColors.Tan,
                    )
                }
            }
        }
    }
}

private fun CustomerCredentialManagementUiState.toProvisioningState(): AddCustomerUiState =
    AddCustomerUiState(
        createdCustomerId = customer?.id,
        generatedLoyaltyId = customer?.loyaltyId.orEmpty(),
        activationLink = activationLink,
        successName = customer?.displayName().orEmpty(),
        barcodeValue = barcodeValue,
        walletPassRequest = walletPassRequest,
        walletAvailability = walletAvailability,
        walletSaveResult = walletSaveResult,
        walletIsSaving = walletIsSaving,
        credentials = credentials,
        nfcWritePhase = nfcWritePhase,
        nfcStatusRes = nfcStatusRes,
    )
