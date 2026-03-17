package com.vector.verevcodex.data.remote.api.billing

import com.google.gson.annotations.SerializedName

data class PlanCatalogViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("billingCycle") val billingCycle: String? = null,
    @SerializedName("basePrice") val basePrice: Double? = null,
    @SerializedName("currencyCode") val currencyCode: String? = null,
    @SerializedName("active") val active: Boolean? = null,
)

data class OrganizationSubscriptionViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("planCatalogId") val planCatalogId: String? = null,
    @SerializedName("planCode") val planCode: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("monthlyPrice") val monthlyPrice: Double? = null,
    @SerializedName("currencyCode") val currencyCode: String? = null,
    @SerializedName("billingCycle") val billingCycle: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("renewalDate") val renewalDate: String? = null,
    @SerializedName("active") val active: Boolean? = null,
)

data class PaymentMethodViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("last4") val last4: String? = null,
    @SerializedName("expiryMonth") val expiryMonth: Int? = null,
    @SerializedName("expiryYear") val expiryYear: Int? = null,
    @SerializedName("isDefault") val isDefault: Boolean? = null,
)

data class BillingInvoiceViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("periodLabel") val periodLabel: String? = null,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("currencyCode") val currencyCode: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("issuedDate") val issuedDate: String? = null,
)

data class BillingOverviewResponseDto(
    @SerializedName("currentSubscription") val currentSubscription: OrganizationSubscriptionViewDto? = null,
    @SerializedName("plans") val plans: List<PlanCatalogViewDto>? = null,
    @SerializedName("paymentMethods") val paymentMethods: List<PaymentMethodViewDto>? = null,
    @SerializedName("invoices") val invoices: List<BillingInvoiceViewDto>? = null,
)

data class CreatePaymentMethodRequestDto(
    @SerializedName("brand") val brand: String,
    @SerializedName("last4") val last4: String,
    @SerializedName("expiryMonth") val expiryMonth: Int,
    @SerializedName("expiryYear") val expiryYear: Int,
    @SerializedName("setDefault") val setDefault: Boolean,
)

data class RemovePaymentMethodResponseDto(
    @SerializedName("removedPaymentMethodId") val removedPaymentMethodId: String? = null,
    @SerializedName("reassignedDefaultPaymentMethodId") val reassignedDefaultPaymentMethodId: String? = null,
)

data class UpdateCurrentSubscriptionRequestDto(
    @SerializedName("planCode") val planCode: String,
    @SerializedName("autoRenew") val autoRenew: Boolean,
)
