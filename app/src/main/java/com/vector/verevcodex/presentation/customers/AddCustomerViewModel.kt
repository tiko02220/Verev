package com.vector.verevcodex.presentation.customers

import android.app.Activity
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.common.identifiers.LoyaltyIdCodec
import com.vector.verevcodex.platform.nfc.NfcCardWriteCoordinator
import com.vector.verevcodex.platform.nfc.NfcCardWriteRequest
import com.vector.verevcodex.platform.nfc.NfcCardWriteState
import com.vector.verevcodex.platform.wallet.GoogleWalletPassFactory
import com.vector.verevcodex.platform.wallet.GoogleWalletSaveResult
import com.vector.verevcodex.platform.wallet.GoogleWalletProvisioningManager
import com.vector.verevcodex.domain.model.customer.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.customer.CustomerCredentialStatus
import com.vector.verevcodex.domain.model.customer.CustomerDraft
import com.vector.verevcodex.domain.usecase.customer.CreateCustomerUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerCredentialsUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.customer.UpsertCustomerCredentialUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class AddCustomerViewModel @Inject constructor(
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    private val createCustomerUseCase: CreateCustomerUseCase,
    private val observeCustomerCredentialsUseCase: ObserveCustomerCredentialsUseCase,
    private val upsertCustomerCredentialUseCase: UpsertCustomerCredentialUseCase,
    private val googleWalletPassFactory: GoogleWalletPassFactory,
    private val googleWalletProvisioningManager: GoogleWalletProvisioningManager,
    private val nfcCardWriteCoordinator: NfcCardWriteCoordinator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddCustomerUiState())
    val uiState: StateFlow<AddCustomerUiState> = _uiState.asStateFlow()
    private var credentialObservationJob: Job? = null

    init {
        observeSelectedStoreUseCase()
            .onEach { store ->
                _uiState.value = _uiState.value.copy(
                    selectedStoreId = store?.id,
                    selectedStoreName = store?.name.orEmpty(),
                )
            }
            .launchIn(viewModelScope)

        googleWalletProvisioningManager.state
            .onEach { walletState ->
                _uiState.value = _uiState.value.copy(
                    walletAvailability = walletState.availability,
                    walletSaveResult = walletState.saveResult,
                    walletIsSaving = walletState.isSaving,
                )
                if (walletState.saveResult == GoogleWalletSaveResult.SAVED) {
                    syncCredentialMethod(
                        method = CustomerCredentialMethod.GOOGLE_WALLET,
                        referenceValue = _uiState.value.walletPassRequest?.objectId,
                    )
                }
            }
            .launchIn(viewModelScope)

        nfcCardWriteCoordinator.state
            .onEach { writeState ->
                _uiState.value = _uiState.value.copy(
                    nfcWritePhase = writeState.toUiPhase(),
                    nfcStatusRes = writeState.toStatusRes(),
                )
                if (writeState is NfcCardWriteState.Success) {
                    syncCredentialMethod(
                        method = CustomerCredentialMethod.NFC_CARD,
                        referenceValue = writeState.request.loyaltyId,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onFirstNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(firstName = sanitize(value), errorRes = null)
    }

    fun onLastNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(lastName = sanitize(value), errorRes = null)
    }

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(email = sanitize(value), errorRes = null)
    }

    fun onPhoneChanged(value: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = sanitize(value), errorRes = null)
    }

    fun createCustomer() {
        val state = _uiState.value
        val storeId = state.selectedStoreId
        when {
            storeId.isNullOrBlank() -> publishError(R.string.merchant_add_customer_error_store)
            state.firstName.isBlank() -> publishError(R.string.merchant_add_customer_error_first_name)
            state.lastName.isBlank() -> publishError(R.string.merchant_add_customer_error_last_name)
            state.email.isBlank() -> publishError(R.string.merchant_add_customer_error_email)
            state.phoneNumber.isBlank() -> publishError(R.string.merchant_add_customer_error_phone)
            else -> {
                viewModelScope.launch {
                    _uiState.value = state.copy(isSaving = true, errorRes = null)
                    runCatching {
                        createCustomerUseCase(
                            CustomerDraft(
                                firstName = state.firstName,
                                lastName = state.lastName,
                                phoneNumber = state.phoneNumber,
                                email = state.email,
                            ),
                            storeId,
                        )
                    }.onSuccess { customer ->
                        val customerName = listOf(customer.firstName, customer.lastName).joinToString(" ").trim()
                        val activationLink = LoyaltyIdCodec.activationUrl(customer.loyaltyId)
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            createdCustomerId = customer.id,
                            generatedLoyaltyId = customer.loyaltyId,
                            activationLink = activationLink,
                            successName = customerName,
                            barcodeValue = customer.loyaltyId,
                            selectedProvisioningOption = CustomerCardProvisioningOption.BARCODE_IMAGE,
                            walletPassRequest = googleWalletPassFactory.createLoyaltyPass(
                                loyaltyId = customer.loyaltyId,
                                customerName = customerName,
                                currentPoints = customer.currentPoints,
                                activationLink = activationLink,
                            ),
                        )
                        observeCredentials(customer.id)
                        googleWalletProvisioningManager.refreshAvailability()
                    }.onFailure {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            errorRes = R.string.merchant_add_customer_error_generic,
                        )
                    }
                }
            }
        }
    }

    fun selectProvisioningOption(option: CustomerCardProvisioningOption) {
        _uiState.value = _uiState.value.copy(selectedProvisioningOption = option)
        when (option) {
            CustomerCardProvisioningOption.GOOGLE_WALLET -> googleWalletProvisioningManager.refreshAvailability()
            CustomerCardProvisioningOption.NFC_CARD -> {
                if (_uiState.value.nfcWritePhase == NfcWritePhase.IDLE) startNfcWrite()
            }
            CustomerCardProvisioningOption.BARCODE_IMAGE -> Unit
        }
    }

    fun startNfcWrite() {
        val state = _uiState.value
        if (state.generatedLoyaltyId.isBlank() || state.activationLink.isBlank()) return
        nfcCardWriteCoordinator.startWrite(
            NfcCardWriteRequest(
                loyaltyId = state.generatedLoyaltyId,
                activationLink = state.activationLink,
                customerName = state.successName,
            )
        )
    }

    fun retryNfcWrite() {
        nfcCardWriteCoordinator.retry()
    }

    fun clearNfcState() {
        nfcCardWriteCoordinator.clear()
    }

    fun refreshWalletAvailability() {
        googleWalletProvisioningManager.refreshAvailability()
    }

    fun launchWalletSave(activity: Activity) {
        _uiState.value.walletPassRequest?.let { request ->
            googleWalletProvisioningManager.launchSave(activity, request)
        }
    }

    fun clearWalletTransientState() {
        googleWalletProvisioningManager.clearTransientResult()
    }

    fun resetForm() {
        googleWalletProvisioningManager.clearTransientResult()
        nfcCardWriteCoordinator.clear()
        credentialObservationJob?.cancel()
        _uiState.value = AddCustomerUiState(
            selectedStoreId = _uiState.value.selectedStoreId,
            selectedStoreName = _uiState.value.selectedStoreName,
        )
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorRes = null)
    }

    private fun publishError(@StringRes errorRes: Int) {
        _uiState.value = _uiState.value.copy(errorRes = errorRes)
    }

    private fun sanitize(value: String): String = value.replace("\n", "")

    private fun observeCredentials(customerId: String) {
        credentialObservationJob?.cancel()
        credentialObservationJob = observeCustomerCredentialsUseCase(customerId)
            .onEach { credentials ->
                _uiState.value = _uiState.value.copy(credentials = credentials)
            }
            .launchIn(viewModelScope)
    }

    private fun syncCredentialMethod(
        method: CustomerCredentialMethod,
        referenceValue: String?,
    ) {
        val state = _uiState.value
        val customerId = state.createdCustomerId ?: return
        val loyaltyId = state.generatedLoyaltyId
        if (loyaltyId.isBlank()) return
        val existing = state.credentials.firstOrNull { it.method == method }
        if (existing?.status == CustomerCredentialStatus.LINKED && existing.referenceValue == referenceValue) return
        viewModelScope.launch {
            upsertCustomerCredentialUseCase(
                customerId = customerId,
                loyaltyId = loyaltyId,
                method = method,
                status = CustomerCredentialStatus.LINKED,
                referenceValue = referenceValue,
            )
        }
    }
}
