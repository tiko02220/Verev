package com.vector.verevcodex.domain.model.business

import com.vector.verevcodex.domain.model.common.Identifiable

data class BusinessOwner(
    override val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
) : Identifiable
