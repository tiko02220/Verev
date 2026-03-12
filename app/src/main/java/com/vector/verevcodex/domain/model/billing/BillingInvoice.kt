package com.vector.verevcodex.domain.model.billing

import java.time.LocalDate

data class BillingInvoice(
    val id: String,
    val ownerId: String,
    val title: String,
    val periodLabel: String,
    val amount: Double,
    val currencyCode: String,
    val status: InvoiceStatus,
    val issuedDate: LocalDate,
)
