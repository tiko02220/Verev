package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.model.loyalty.TierProgramRule

internal fun mapCustomerListCards(
    customers: List<Customer>,
    relations: List<CustomerBusinessRelation>,
    tierRule: TierProgramRule?,
): List<CustomerListCardUi> {
    val relationByCustomerId = relations.associateBy { it.customerId }
    return customers.map { customer ->
        val relation = relationByCustomerId[customer.id]
        val displayCustomer = customer.resolveDisplayedTier(tierRule)
        CustomerListCardUi(
            customer = displayCustomer,
            showsTierBadge = displayCustomer.loyaltyTierLabel.isNotBlank(),
            notesPreview = relation?.notes?.takeIf { it.isNotBlank() },
            tagsPreview = relation?.tags.orEmpty(),
        )
    }
}
