package com.vector.verevcodex.domain.model.billing

data class PaymentMethodDraft(
    val brand: String,
    val last4: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val isDefault: Boolean,
)
