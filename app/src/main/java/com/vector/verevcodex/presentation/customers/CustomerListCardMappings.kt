package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation

internal fun mapCustomerListCards(
    customers: List<Customer>,
    relations: List<CustomerBusinessRelation>,
    showTierBadge: Boolean,
): List<CustomerListCardUi> {
    val relationByCustomerId = relations.associateBy { it.customerId }
    return customers.map { customer ->
        val relation = relationByCustomerId[customer.id]
        CustomerListCardUi(
            customer = customer,
            showsTierBadge = showTierBadge,
            notesPreview = relation?.notes?.takeIf { it.isNotBlank() },
            tagsPreview = relation?.tags.orEmpty(),
        )
    }
}
