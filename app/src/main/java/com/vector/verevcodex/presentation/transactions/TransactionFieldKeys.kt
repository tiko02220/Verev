package com.vector.verevcodex.presentation.transactions

internal const val TRANSACTION_FIELD_CUSTOMER = "customer"
internal const val TRANSACTION_FIELD_LINE_ITEMS = "line_items"
internal const val TRANSACTION_FIELD_REDEEM = "redeem_points"

internal fun transactionItemNameFieldKey(lineItemId: String): String = "${lineItemId}_name"
internal fun transactionItemQuantityFieldKey(lineItemId: String): String = "${lineItemId}_quantity"
internal fun transactionItemPriceFieldKey(lineItemId: String): String = "${lineItemId}_price"
