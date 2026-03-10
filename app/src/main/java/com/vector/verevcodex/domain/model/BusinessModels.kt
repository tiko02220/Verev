package com.vector.verevcodex.domain.model

data class BusinessOwner(
    override val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
) : Identifiable

data class BusinessLocation(
    override val id: String,
    val storeId: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
) : Identifiable

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

data class StaffMember(
    override val id: String,
    val storeId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val role: StaffRole,
    val active: Boolean,
    val permissionsSummary: String,
) : Identifiable
