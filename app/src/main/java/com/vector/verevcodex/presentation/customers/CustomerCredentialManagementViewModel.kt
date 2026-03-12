package com.vector.verevcodex.presentation.customers

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.common.identifiers.LoyaltyIdCodec
import com.vector.verevcodex.platform.nfc.NfcCardWriteCoordinator
import com.vector.verevcodex.platform.nfc.NfcCardWriteRequest
import com.vector.verevcodex.platform.nfc.NfcCardWriteState
import com.vector.verevcodex.platform.wallet.GoogleWalletPassFactory
import com.vector.verevcodex.platform.wallet.GoogleWalletSaveResult
import com.vector.verevcodex.platform.wallet.GoogleWalletProvisioningManager
import com.vector.verevcodex.domain.model.customer.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.customer.CustomerCredentialStatus
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerCredentialsUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerUseCase
import com.vector.verevcodex.domain.usecase.customer.UpsertCustomerCredentialUseCase
import com.vector.verevcodex.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class CustomerCredentialManagementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCustomerUseCase: ObserveCustomerUseCase,
    observeCustomerCredentialsUseCase: ObserveCustomerCredentialsUseCase,
    private val upsertCustomerCredentialUseCase: UpsertCustomerCredentialUseCase,
    private val googleWalletPassFactory: GoogleWalletPassFactory,
    private val googleWalletProvisioningManager: GoogleWalletProvisioningManager,
    private val nfcCardWriteCoordinator: NfcCardWriteCoordinator,
) : ViewModel() {
    private val customerId: String? = savedStateHandle[Screen.CustomerCredentialManagement.ARG_CUSTOMER_ID]

    private val _uiState = MutableStateFlow(CustomerCredentialManagementUiState())
    val uiState: StateFlow<CustomerCredentialManagementUiState> = _uiState.asStateFlow()

    init {
        val currentCustomerId = customerId
        if (currentCustomerId.isNullOrBlank()) {
            _uiState.value = CustomerCredentialManagementUiState(isMissingCustomer = true)
        } else {
            combine(
                observeCustomerUseCase(currentCustomerId),
                observeCustomerCredentialsUseCase(currentCustomerId),
            ) { customer, credentials ->
                val passRequest = customer?.let { member ->
                    googleWalletPassFactory.createLoyaltyPass(
                        loyaltyId = member.loyaltyId,
                        customerName = member.displayName(),
                        currentPoints = member.currentPoints,
                        activationLink = LoyaltyIdCodec.activationUrl(member.loyaltyId),
                    )
                }
                CustomerCredentialManagementUiState(
                    customer = customer,
                    credentials = credentials,
                    activationLink = customer?.let { LoyaltyIdCodec.activationUrl(it.loyaltyId) }.orEmpty(),
                    walletPassRequest = passRequest,
                    barcodeValue = customer?.loyaltyId.orEmpty(),
                    walletAvailability = _uiState.value.walletAvailability,
                    walletSaveResult = _uiState.value.walletSaveResult,
                    walletIsSaving = _uiState.value.walletIsSaving,
                    nfcWritePhase = _uiState.value.nfcWritePhase,
                    nfcStatusRes = _uiState.value.nfcStatusRes,
                )
            }.onEach { _uiState.value = it }.launchIn(viewModelScope)
        }

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

    fun refreshWalletAvailability() {
        googleWalletProvisioningManager.refreshAvailability()
    }

    fun launchWalletSave(activity: Activity) {
        _uiState.value.walletPassRequest?.let { request ->
            googleWalletProvisioningManager.launchSave(activity, request)
        }
    }

    fun startNfcWrite() {
        val customer = _uiState.value.customer ?: return
        nfcCardWriteCoordinator.startWrite(
            NfcCardWriteRequest(
                loyaltyId = customer.loyaltyId,
                activationLink = LoyaltyIdCodec.activationUrl(customer.loyaltyId),
                customerName = customer.displayName(),
            )
        )
    }

    fun retryNfcWrite() {
        nfcCardWriteCoordinator.retry()
    }

    fun clearTransientState() {
        googleWalletProvisioningManager.clearTransientResult()
        nfcCardWriteCoordinator.clear()
    }

    private fun syncCredentialMethod(
        method: CustomerCredentialMethod,
        referenceValue: String?,
    ) {
        val state = _uiState.value
        val customer = state.customer ?: return
        val existing = state.credentials.firstOrNull { it.method == method }
        if (existing?.status == CustomerCredentialStatus.LINKED && existing.referenceValue == referenceValue) return
        viewModelScope.launch {
            upsertCustomerCredentialUseCase(
                customerId = customer.id,
                loyaltyId = customer.loyaltyId,
                method = method,
                status = CustomerCredentialStatus.LINKED,
                referenceValue = referenceValue,
            )
        }
    }
}
