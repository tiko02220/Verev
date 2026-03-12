package com.vector.verevcodex.platform.wallet

import android.app.Activity
import androidx.activity.result.ActivityResult
import com.google.android.gms.pay.Pay
import com.google.android.gms.pay.PayApiAvailabilityStatus
import com.google.android.gms.pay.PayClient
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class GoogleWalletAvailability {
    NOT_CONFIGURED,
    CHECKING,
    AVAILABLE,
    UNAVAILABLE,
}

enum class GoogleWalletSaveResult {
    NONE,
    SAVED,
    CANCELED,
    ERROR,
}

data class GoogleWalletProvisioningState(
    val availability: GoogleWalletAvailability = GoogleWalletAvailability.NOT_CONFIGURED,
    val saveResult: GoogleWalletSaveResult = GoogleWalletSaveResult.NONE,
    val isSaving: Boolean = false,
)

@Singleton
class GoogleWalletProvisioningManager @Inject constructor(
    @ApplicationContext context: Context,
    private val configProvider: GoogleWalletConfigProvider,
) {
    private val payClient: PayClient = Pay.getClient(context)
    private val _state = MutableStateFlow(GoogleWalletProvisioningState())
    val state: StateFlow<GoogleWalletProvisioningState> = _state.asStateFlow()

    fun refreshAvailability() {
        val config = configProvider.get()
        if (!config.isConfigured) {
            _state.value = GoogleWalletProvisioningState(
                availability = GoogleWalletAvailability.NOT_CONFIGURED,
                saveResult = GoogleWalletSaveResult.NONE,
                isSaving = false,
            )
            return
        }

        _state.value = _state.value.copy(
            availability = GoogleWalletAvailability.CHECKING,
            saveResult = GoogleWalletSaveResult.NONE,
            isSaving = false,
        )
        payClient.getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
            .addOnSuccessListener { status ->
                _state.value = _state.value.copy(
                    availability = if (status == PayApiAvailabilityStatus.AVAILABLE) {
                        GoogleWalletAvailability.AVAILABLE
                    } else {
                        GoogleWalletAvailability.UNAVAILABLE
                    },
                    isSaving = false,
                )
            }
            .addOnFailureListener {
                _state.value = _state.value.copy(
                    availability = GoogleWalletAvailability.UNAVAILABLE,
                    isSaving = false,
                )
            }
    }

    fun launchSave(activity: Activity, request: GoogleWalletPassRequest) {
        if (_state.value.availability != GoogleWalletAvailability.AVAILABLE) return
        _state.value = _state.value.copy(isSaving = true, saveResult = GoogleWalletSaveResult.NONE)
        payClient.savePasses(request.passJson, activity, SAVE_PASS_REQUEST_CODE)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int): Boolean {
        if (requestCode != SAVE_PASS_REQUEST_CODE) return false
        _state.value = _state.value.copy(
            isSaving = false,
            saveResult = when (resultCode) {
                Activity.RESULT_OK -> GoogleWalletSaveResult.SAVED
                Activity.RESULT_CANCELED -> GoogleWalletSaveResult.CANCELED
                else -> GoogleWalletSaveResult.ERROR
            },
        )
        return true
    }

    fun clearTransientResult() {
        if (_state.value.saveResult == GoogleWalletSaveResult.NONE) return
        _state.value = _state.value.copy(saveResult = GoogleWalletSaveResult.NONE)
    }

    private companion object {
        const val SAVE_PASS_REQUEST_CODE = 2127
    }
}
