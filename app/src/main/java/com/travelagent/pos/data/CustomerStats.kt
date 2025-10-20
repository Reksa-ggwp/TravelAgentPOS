package com.travelagent.pos.data

import androidx.room.*

@Entity(tableName = "customer_stats")
data class CustomerStats(
    @PrimaryKey
    val customerId: Int,
    val totalTrips: Int = 0,
    val totalSpent: Double = 0.0,
    val lastTripDate: Long? = null,
    val loyaltyPoints: Int = 0,
    val tier: String = "Bronze" // Bronze, Silver, Gold, Platinum
)