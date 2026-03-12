package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.platform.wallet.GoogleWalletAvailability
import com.vector.verevcodex.platform.wallet.GoogleWalletPassRequest
import com.vector.verevcodex.platform.wallet.GoogleWalletSaveResult
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerCredential

data class CustomerCredentialManagementUiState(
    val customer: Customer? = null,
    val credentials: List<CustomerCredential> = emptyList(),
    val activationLink: String = "",
    val barcodeValue: String = "",
    val walletPassRequest: GoogleWalletPassRequest? = null,
    val walletAvailability: GoogleWalletAvailability = GoogleWalletAvailability.NOT_CONFIGURED,
    val walletSaveResult: GoogleWalletSaveResult = GoogleWalletSaveResult.NONE,
    val walletIsSaving: Boolean = false,
    val nfcWritePhase: NfcWritePhase = NfcWritePhase.IDLE,
    val nfcStatusRes: Int? = null,
    val isMissingCustomer: Boolean = false,
)
