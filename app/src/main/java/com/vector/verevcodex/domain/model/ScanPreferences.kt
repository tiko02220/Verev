package com.vector.verevcodex.domain.model

data class ScanPreferences(
    val preferredMethod: ScanMethod? = null,
    val skipMethodSelection: Boolean = false,
)
