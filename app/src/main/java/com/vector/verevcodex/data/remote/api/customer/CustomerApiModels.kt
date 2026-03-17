package com.vector.verevcodex.data.remote.api.customer

import com.google.gson.annotations.SerializedName

data class CustomerViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("birthDate") val birthDate: String? = null,
    @SerializedName("loyaltyId") val loyaltyId: String? = null,
    @SerializedName("enrolledDate") val enrolledDate: String? = null,
)

data class CustomerOrganizationProfileViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("homeStoreId") val homeStoreId: String? = null,
    @SerializedName("totalVisits") val totalVisits: Int? = null,
    @SerializedName("totalSpent") val totalSpent: Double? = null,
    @SerializedName("currentPoints") val currentPoints: Int? = null,
    @SerializedName("lifetimePointsEarned") val lifetimePointsEarned: Int? = null,
    @SerializedName("lifetimePointsRedeemed") val lifetimePointsRedeemed: Int? = null,
    @SerializedName("loyaltyTier") val loyaltyTier: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("version") val version: Long? = null,
)

data class CustomerMembershipViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("joinedAt") val joinedAt: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("tags") val tags: List<String>? = null,
)

data class CustomerCredentialViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("loyaltyId") val loyaltyId: String? = null,
    @SerializedName("method") val method: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("referenceValue") val referenceValue: String? = null,
    @SerializedName("issuedAt") val issuedAt: String? = null,
    @SerializedName("revokedAt") val revokedAt: String? = null,
)

data class CustomerBonusActionViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("details") val details: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
)

data class PointsLedgerEntryDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("transactionId") val transactionId: String? = null,
    @SerializedName("pointsDelta") val pointsDelta: Int? = null,
    @SerializedName("reasonCode") val reasonCode: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
)

data class CustomerDetailResponseDto(
    @SerializedName("customer") val customer: CustomerViewDto? = null,
    @SerializedName("profile") val profile: CustomerOrganizationProfileViewDto? = null,
    @SerializedName("memberships") val memberships: List<CustomerMembershipViewDto>? = null,
    @SerializedName("credentials") val credentials: List<CustomerCredentialViewDto>? = null,
)

data class CreateCustomerRequestDto(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("email") val email: String?,
    @SerializedName("birthDate") val birthDate: String?,
    @SerializedName("homeStoreId") val homeStoreId: String,
    @SerializedName("tags") val tags: List<String>?,
    @SerializedName("notes") val notes: String?,
)

data class QuickRegisterCustomerRequestDto(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("email") val email: String?,
    @SerializedName("birthDate") val birthDate: String?,
    @SerializedName("homeStoreId") val homeStoreId: String,
)

data class UpdateCustomerRequestDto(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("email") val email: String?,
    @SerializedName("birthDate") val birthDate: String?,
    @SerializedName("homeStoreId") val homeStoreId: String?,
    @SerializedName("version") val version: Long,
)

data class UpdateMembershipRequestDto(
    @SerializedName("notes") val notes: String,
    @SerializedName("tags") val tags: List<String>,
)

data class CreateCustomerCredentialRequestDto(
    @SerializedName("method") val method: String,
    @SerializedName("referenceValue") val referenceValue: String?,
)

data class PatchCustomerCredentialRequestDto(
    @SerializedName("status") val status: String,
    @SerializedName("referenceValue") val referenceValue: String?,
)

data class CreateCustomerBonusActionRequestDto(
    @SerializedName("storeId") val storeId: String?,
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("details") val details: String,
)

data class ManualPointsAdjustmentRequestDto(
    @SerializedName("storeId") val storeId: String,
    @SerializedName("delta") val delta: Int,
    @SerializedName("reason") val reason: String,
)

data class ManualVisitAdjustmentRequestDto(
    @SerializedName("storeId") val storeId: String,
    @SerializedName("delta") val delta: Int,
    @SerializedName("reason") val reason: String,
)
