package com.vector.verevcodex.data.db.entity.customer

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "customer_business_relations")
data class CustomerBusinessRelationEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val storeId: String,
    val joinedAt: String,
    val notes: String,
    val tags: String = "",
)

@Entity(
    tableName = "customer_bonus_actions",
    indices = [Index("customerId")],
)
data class CustomerBonusActionEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val storeId: String?,
    val type: String,
    val title: String,
    val details: String,
    val createdAt: String,
)
