package com.vector.verevcodex.domain.model.settings

data class BranchConfiguration(
    val storeId: String,
    val customerSelfEnrollmentEnabled: Boolean,
    val nfcCardProvisioningEnabled: Boolean,
    val barcodeScannerEnabled: Boolean,
    val managerApprovalRequiredForRedemption: Boolean,
    val managerApprovalRequiredForPointsAdjustment: Boolean,
    val receiptFooter: String,
)
