package com.vector.verevcodex.presentation.customers

import androidx.annotation.StringRes
import com.vector.verevcodex.core.wallet.GoogleWalletAvailability
import com.vector.verevcodex.core.wallet.GoogleWalletPassRequest
import com.vector.verevcodex.core.wallet.GoogleWalletSaveResult
import com.vector.verevcodex.domain.model.CustomerCredential

data class AddCustomerUiState(
    val selectedStoreId: String? = null,
    val selectedStoreName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val isSaving: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val createdCustomerId: String? = null,
    val generatedLoyaltyId: String = "",
    val activationLink: String = "",
    val successName: String = "",
    val barcodeValue: String = "",
    val walletPassRequest: GoogleWalletPassRequest? = null,
    val walletAvailability: GoogleWalletAvailability = GoogleWalletAvailability.NOT_CONFIGURED,
    val walletSaveResult: GoogleWalletSaveResult = GoogleWalletSaveResult.NONE,
    val walletIsSaving: Boolean = false,
    val selectedProvisioningOption: CustomerCardProvisioningOption? = null,
    val credentials: List<CustomerCredential> = emptyList(),
    val nfcWritePhase: NfcWritePhase = NfcWritePhase.IDLE,
    @StringRes val nfcStatusRes: Int? = null,
)
