package com.vector.verevcodex.domain.model.business

import com.vector.verevcodex.domain.model.common.Identifiable

data class BusinessLocation(
    override val id: String,
    val storeId: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
) : Identifiable
