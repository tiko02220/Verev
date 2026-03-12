package com.vector.verevcodex.domain.model.business

import com.vector.verevcodex.domain.model.common.Identifiable

data class Store(
    override val id: String,
    val ownerId: String,
    val name: String,
    val address: String,
    val contactInfo: String,
    val category: String,
    val workingHours: String,
    val logoUrl: String,
    val primaryColor: String,
    val secondaryColor: String,
    val active: Boolean,
) : Identifiable
