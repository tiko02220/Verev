package com.vector.verevcodex.domain.model.business

data class StoreDraft(
    val name: String,
    val address: String,
    val contactInfo: String,
    val category: String,
    val workingHours: String,
    val primaryColor: String,
    val secondaryColor: String,
)
