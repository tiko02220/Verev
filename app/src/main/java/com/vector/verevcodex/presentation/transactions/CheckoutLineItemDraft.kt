package com.vector.verevcodex.presentation.transactions

data class CheckoutLineItemDraft(
    val id: String,
    val name: String = "",
    val quantity: String = "1",
    val unitPrice: String = "",
)
