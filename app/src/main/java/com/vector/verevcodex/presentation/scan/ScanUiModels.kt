package com.vector.verevcodex.presentation.scan

import androidx.annotation.StringRes
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.model.scan.ScanMethod
import com.vector.verevcodex.domain.model.scan.ScanPreferences

internal const val SCAN_FIELD_AMOUNT = "scan_amount"
internal const val SCAN_FIELD_POINTS = "scan_points"

enum class ScanContentMode {
    ACTIVE_SCAN,
    LOOKUP,
    CUSTOMER,
}

enum class BarcodeScanFailureReason {
    GENERIC,
    PERMISSION_DENIED,
    CAMERA_UNAVAILABLE,
    UNSUPPORTED_CODE,
    CANCELLED,
}

data class ScanUiState(
    val selectedStoreId: String? = null,
    val selectedStoreName: String = "",
    val scanPreferences: ScanPreferences = ScanPreferences(),
    val contentMode: ScanContentMode = ScanContentMode.ACTIVE_SCAN,
    val scanSessionToken: Int = 0,
    val activePrograms: List<RewardProgram> = emptyList(),
    val availableActions: List<RewardProgramScanAction> = emptyList(),
    val activeScanMethod: ScanMethod? = null,
    val scannedLoyaltyId: String? = null,
    val visitCountedForCurrentScan: Boolean = false,
    val customer: Customer? = null,
    val selectedAction: RewardProgramScanAction? = null,
    val isSearching: Boolean = false,
    val isSubmitting: Boolean = false,
    val fieldErrors: Map<String, Int> = emptyMap(),
    @StringRes val errorRes: Int? = null,
    @StringRes val messageRes: Int? = null,
)
