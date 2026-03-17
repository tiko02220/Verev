package com.vector.verevcodex.data.remote.api.staff

import com.google.gson.annotations.SerializedName

data class StaffPermissionsDto(
    @SerializedName("viewAnalytics") val viewAnalytics: Boolean? = null,
    @SerializedName("managePrograms") val managePrograms: Boolean? = null,
    @SerializedName("processTransactions") val processTransactions: Boolean? = null,
    @SerializedName("manageCustomers") val manageCustomers: Boolean? = null,
    @SerializedName("manageStaff") val manageStaff: Boolean? = null,
    @SerializedName("viewSettings") val viewSettings: Boolean? = null,
)

data class StaffViewDto(
    @SerializedName("staffId") val staffId: String? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("primaryStoreId") val primaryStoreId: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("active") val active: Boolean? = null,
    @SerializedName("permissionsSummary") val permissionsSummary: String? = null,
    @SerializedName("permissions") val permissions: StaffPermissionsDto? = null,
)

data class StaffOnboardingMemberRequestDto(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String,
    @SerializedName("permissionsSummary") val permissionsSummary: String,
    @SerializedName("permissions") val permissions: StaffPermissionsDto,
)

data class BulkCreateStaffRequestDto(
    @SerializedName("primaryStoreId") val primaryStoreId: String,
    @SerializedName("storeIds") val storeIds: List<String>,
    @SerializedName("members") val members: List<StaffOnboardingMemberRequestDto>,
)

data class UpdateStaffRequestDto(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("role") val role: String,
    @SerializedName("permissions") val permissions: StaffPermissionsDto,
    @SerializedName("primaryStoreId") val primaryStoreId: String,
)
