package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.domain.model.customer.CustomerActivity
import com.vector.verevcodex.domain.model.customer.CustomerActivityType
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.model.loyalty.PointsLedger
import com.vector.verevcodex.domain.model.transactions.Transaction

internal object CustomerActivityTimelineBuilder {
    fun build(
        relation: CustomerBusinessRelation?,
        transactions: List<Transaction>,
        ledgerEntries: List<PointsLedger>,
    ): List<CustomerActivity> {
        val items = buildList {
            relation?.let { addRelationActivities(it) }
            transactions.forEach { transaction ->
                add(
                    CustomerActivity(
                        id = "transaction-${transaction.id}",
                        type = CustomerActivityType.TRANSACTION,
                        title = CustomerActivityText.transactionRecordedTitle,
                        description = transaction.metadata.ifBlank { CustomerActivityText.storeTransactionDescription },
                        timestamp = transaction.timestamp,
                        amount = transaction.amount,
                        pointsDelta = transaction.pointsEarned - transaction.pointsRedeemed,
                        transactionId = transaction.id,
                    )
                )
            }
            ledgerEntries.filter { it.transactionId == null }.forEach { entry ->
                add(
                    CustomerActivity(
                        id = "ledger-${entry.id}",
                        type = CustomerActivityType.POINTS_ADJUSTMENT,
                        title = if (entry.delta >= 0) {
                            CustomerActivityText.manualPointsAddedTitle
                        } else {
                            CustomerActivityText.pointsRemovedTitle
                        },
                        description = entry.reason,
                        timestamp = entry.createdAt,
                        pointsDelta = entry.delta,
                    )
                )
            }
        }
        return items.sortedByDescending { it.timestamp }
    }

    private fun MutableList<CustomerActivity>.addRelationActivities(relation: CustomerBusinessRelation) {
        add(
            CustomerActivity(
                id = "joined-${relation.id}",
                type = CustomerActivityType.JOINED,
                title = CustomerActivityText.joinedTitle,
                description = CustomerActivityText.joinedDescription,
                timestamp = relation.joinedAt,
            )
        )
        if (relation.notes.isNotBlank()) {
            add(
                CustomerActivity(
                    id = "notes-${relation.id}",
                    type = CustomerActivityType.NOTE_UPDATED,
                    title = CustomerActivityText.notesSavedTitle,
                    description = relation.notes,
                    timestamp = relation.joinedAt,
                )
            )
        }
        if (relation.tags.isNotEmpty()) {
            add(
                CustomerActivity(
                    id = "tags-${relation.id}",
                    type = CustomerActivityType.TAGS_UPDATED,
                    title = CustomerActivityText.tagsAssignedTitle,
                    description = relation.tags.joinToString(", "),
                    timestamp = relation.joinedAt,
                )
            )
        }
    }
}
