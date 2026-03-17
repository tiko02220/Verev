package com.vector.verevcodex.data.remote.api.store

import com.google.gson.annotations.SerializedName

data class StoreViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("contactInfo") val contactInfo: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("workingHours") val workingHours: String? = null,
    @SerializedName("logoUrl") val logoUrl: String? = null,
    @SerializedName("primaryColor") val primaryColor: String? = null,
    @SerializedName("secondaryColor") val secondaryColor: String? = null,
    @SerializedName("active") val active: Boolean? = null,
)

data class CreateStoreRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("contactInfo") val contactInfo: String,
    @SerializedName("category") val category: String,
    @SerializedName("workingHours") val workingHours: String,
    @SerializedName("logoUrl") val logoUrl: String?,
    @SerializedName("primaryColor") val primaryColor: String,
    @SerializedName("secondaryColor") val secondaryColor: String,
)

data class UpdateStoreRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("contactInfo") val contactInfo: String,
    @SerializedName("category") val category: String,
    @SerializedName("workingHours") val workingHours: String,
    @SerializedName("logoUrl") val logoUrl: String?,
    @SerializedName("primaryColor") val primaryColor: String,
    @SerializedName("secondaryColor") val secondaryColor: String,
)

data class BrandingSettingsViewDto(
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("selectedPaletteId") val selectedPaletteId: String? = null,
    @SerializedName("themeMode") val themeMode: String? = null,
    @SerializedName("accentColor") val accentColor: String? = null,
)

data class UpdateBrandingRequestDto(
    @SerializedName("selectedPaletteId") val selectedPaletteId: String,
    @SerializedName("themeMode") val themeMode: String,
    @SerializedName("accentColor") val accentColor: String,
)

data class BranchConfigurationViewDto(
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("customerSelfEnrollmentEnabled") val customerSelfEnrollmentEnabled: Boolean? = null,
    @SerializedName("nfcCardProvisioningEnabled") val nfcCardProvisioningEnabled: Boolean? = null,
    @SerializedName("barcodeScannerEnabled") val barcodeScannerEnabled: Boolean? = null,
    @SerializedName("managerApprovalRequiredForRedemption") val managerApprovalRequiredForRedemption: Boolean? = null,
    @SerializedName("managerApprovalRequiredForPointsAdjustment") val managerApprovalRequiredForPointsAdjustment: Boolean? = null,
    @SerializedName("receiptFooter") val receiptFooter: String? = null,
)

data class UpdateBranchConfigurationRequestDto(
    @SerializedName("customerSelfEnrollmentEnabled") val customerSelfEnrollmentEnabled: Boolean,
    @SerializedName("nfcCardProvisioningEnabled") val nfcCardProvisioningEnabled: Boolean,
    @SerializedName("barcodeScannerEnabled") val barcodeScannerEnabled: Boolean,
    @SerializedName("managerApprovalRequiredForRedemption") val managerApprovalRequiredForRedemption: Boolean,
    @SerializedName("managerApprovalRequiredForPointsAdjustment") val managerApprovalRequiredForPointsAdjustment: Boolean,
    @SerializedName("receiptFooter") val receiptFooter: String,
)
