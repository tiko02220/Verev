package com.vector.verevcodex.domain.model.common

data class StaffPermissions(
    val viewAnalytics: Boolean,
    val managePrograms: Boolean,
    val processTransactions: Boolean,
    val manageCustomers: Boolean,
    val manageStaff: Boolean,
    val viewSettings: Boolean,
)

fun StaffRole.defaultPermissions(): StaffPermissions = when (this) {
    StaffRole.OWNER -> StaffPermissions(true, true, true, true, true, true)
    StaffRole.STORE_MANAGER -> StaffPermissions(true, true, true, true, false, false)
    StaffRole.CASHIER -> StaffPermissions(false, false, true, false, false, false)
    StaffRole.STAFF -> StaffPermissions(false, false, true, false, false, false)
}

fun StaffPermissions.summary(): String = buildList {
    if (viewAnalytics) add("Analytics")
    if (managePrograms) add("Programs")
    if (processTransactions) add("Transactions")
    if (manageCustomers) add("Customers")
    if (manageStaff) add("Staff")
    if (viewSettings) add("Settings")
}.joinToString(", ")
