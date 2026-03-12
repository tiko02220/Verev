package com.vector.verevcodex.presentation.customers

import androidx.annotation.StringRes
import com.vector.verevcodex.R
import com.vector.verevcodex.platform.wallet.GoogleWalletAvailability
import com.vector.verevcodex.platform.wallet.GoogleWalletSaveResult
import com.vector.verevcodex.domain.model.customer.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.customer.CustomerCredentialStatus

@StringRes
internal fun GoogleWalletAvailability.toStatusRes(): Int = when (this) {
    GoogleWalletAvailability.NOT_CONFIGURED -> R.string.merchant_add_customer_wallet_not_configured
    GoogleWalletAvailability.CHECKING -> R.string.merchant_add_customer_wallet_checking
    GoogleWalletAvailability.AVAILABLE -> R.string.merchant_add_customer_wallet_available
    GoogleWalletAvailability.UNAVAILABLE -> R.string.merchant_add_customer_wallet_unavailable
}

@StringRes
internal fun GoogleWalletSaveResult.toStatusRes(): Int? = when (this) {
    GoogleWalletSaveResult.NONE -> null
    GoogleWalletSaveResult.SAVED -> R.string.merchant_add_customer_wallet_saved
    GoogleWalletSaveResult.CANCELED -> R.string.merchant_add_customer_wallet_canceled
    GoogleWalletSaveResult.ERROR -> R.string.merchant_add_customer_wallet_error
}

internal fun CustomerCardProvisioningOption.toCredentialMethod(): CustomerCredentialMethod = when (this) {
    CustomerCardProvisioningOption.GOOGLE_WALLET -> CustomerCredentialMethod.GOOGLE_WALLET
    CustomerCardProvisioningOption.NFC_CARD -> CustomerCredentialMethod.NFC_CARD
    CustomerCardProvisioningOption.BARCODE_IMAGE -> CustomerCredentialMethod.BARCODE_IMAGE
}

internal fun AddCustomerUiState.credentialStatus(method: CustomerCredentialMethod): CustomerCredentialStatus? =
    credentials.firstOrNull { it.method == method }?.status
