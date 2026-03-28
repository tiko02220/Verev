package com.vector.verevcodex.domain.model.customer

data class CustomerMergeAssessment(
    val sourceOrganizationProfiles: Int,
    val overlappingOrganizationProfiles: Int,
    val sourceMemberships: Int,
    val overlappingMemberships: Int,
    val sourceCredentials: Int,
    val overlappingCredentials: Int,
    val sourceBonusActions: Int,
    val sourceTransactions: Int,
    val sourcePointsLedgerEntries: Int,
)

data class CustomerMergePreview(
    val sourceCustomer: Customer,
    val targetCustomer: Customer,
    val assessment: CustomerMergeAssessment,
    val warnings: List<String>,
    val blockingReasons: List<String>,
    val canMerge: Boolean,
)

data class CustomerMergeResult(
    val sourceCustomerId: String,
    val targetCustomer: Customer,
)

data class CustomerSplitAssessment(
    val organizationProfilesToMove: Int,
    val membershipsToMove: Int,
    val credentialsToMove: Int,
    val bonusActionsToMove: Int,
    val transactionsToMove: Int,
    val approvalRequestsToMove: Int,
    val pointsLedgerEntriesToMove: Int,
    val remainingOrganizationProfiles: Int,
    val sourceHasConsumerAccount: Boolean,
)

data class CustomerSplitPreview(
    val sourceCustomer: Customer,
    val organizationId: String,
    val assessment: CustomerSplitAssessment,
    val warnings: List<String>,
    val blockingReasons: List<String>,
    val canSplit: Boolean,
)

data class CustomerSplitResult(
    val sourceCustomerId: String,
    val targetCustomer: Customer,
)
