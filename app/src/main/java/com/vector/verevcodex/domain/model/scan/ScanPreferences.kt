package com.vector.verevcodex.domain.model.scan

data class ScanPreferences(
    val preferredMethod: ScanMethod? = null,
    val skipMethodSelection: Boolean = false,
)
