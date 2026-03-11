package com.vector.verevcodex.presentation.customers

import androidx.annotation.StringRes
import com.vector.verevcodex.R
import com.vector.verevcodex.core.nfc.NfcCardWriteError
import com.vector.verevcodex.core.nfc.NfcCardWriteState

enum class NfcWritePhase {
    IDLE,
    READY,
    WRITING,
    SUCCESS,
    ERROR,
}

internal fun NfcCardWriteState.toUiPhase(): NfcWritePhase = when (this) {
    NfcCardWriteState.Idle -> NfcWritePhase.IDLE
    is NfcCardWriteState.Ready -> NfcWritePhase.READY
    is NfcCardWriteState.Writing -> NfcWritePhase.WRITING
    is NfcCardWriteState.Success -> NfcWritePhase.SUCCESS
    is NfcCardWriteState.Error -> NfcWritePhase.ERROR
}

@StringRes
internal fun NfcCardWriteState.toStatusRes(): Int? = when (this) {
    NfcCardWriteState.Idle -> null
    is NfcCardWriteState.Ready -> R.string.merchant_add_customer_nfc_ready
    is NfcCardWriteState.Writing -> R.string.merchant_add_customer_nfc_writing
    is NfcCardWriteState.Success -> R.string.merchant_add_customer_nfc_success
    is NfcCardWriteState.Error -> when (reason) {
        NfcCardWriteError.TAG_NOT_SUPPORTED -> R.string.merchant_add_customer_nfc_error_not_supported
        NfcCardWriteError.TAG_READ_ONLY -> R.string.merchant_add_customer_nfc_error_read_only
        NfcCardWriteError.CAPACITY_TOO_SMALL -> R.string.merchant_add_customer_nfc_error_capacity
        NfcCardWriteError.FORMAT_FAILED -> R.string.merchant_add_customer_nfc_error_format
        NfcCardWriteError.WRITE_FAILED -> R.string.merchant_add_customer_nfc_error_generic
    }
}
