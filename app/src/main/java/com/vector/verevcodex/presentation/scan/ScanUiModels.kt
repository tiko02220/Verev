package com.vector.verevcodex.presentation.scan

import androidx.annotation.StringRes
import com.vector.verevcodex.domain.model.Customer
import com.vector.verevcodex.domain.model.RewardProgramScanAction
import com.vector.verevcodex.domain.model.ScanMethod
import com.vector.verevcodex.domain.model.ScanPreferences

data class ScanUiState(
    val selectedStoreId: String? = null,
    val selectedStoreName: String = "",
    val scanPreferences: ScanPreferences = ScanPreferences(),
    val availableActions: List<RewardProgramScanAction> = emptyList(),
    val activeScanMethod: ScanMethod? = null,
    val scannedLoyaltyId: String? = null,
    val customer: Customer? = null,
    val selectedAction: RewardProgramScanAction? = null,
    val isSearching: Boolean = false,
    val isSubmitting: Boolean = false,
    @StringRes val errorRes: Int? = null,
    @StringRes val messageRes: Int? = null,
)
