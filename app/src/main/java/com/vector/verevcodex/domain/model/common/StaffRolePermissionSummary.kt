package com.vector.verevcodex.domain.model.common

fun StaffRole.defaultPermissionsSummary(): String = when (this) {
    StaffRole.OWNER -> "Full access across stores, analytics, campaigns, and loyalty settings"
    StaffRole.STORE_MANAGER -> "Manage customers, rewards, staff coordination, and store analytics"
    StaffRole.CASHIER -> "Process scans, transactions, and reward redemptions"
    StaffRole.STAFF -> "Limited operational access for assisted checkout and customer handling"
}
