package com.travelagent.pos.data

import androidx.room.*

@Dao
interface CustomerStatsDao {
    @Query("SELECT * FROM customer_stats WHERE customerId = :customerId")
    suspend fun getStats(customerId: Int): CustomerStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStats(stats: CustomerStats)

    @Query("SELECT * FROM customer_stats ORDER BY totalTrips DESC")
    suspend fun getAllStats(): List<CustomerStats>

    @Query("SELECT * FROM customer_stats WHERE tier = :tier ORDER BY totalTrips DESC")
    suspend fun getStatsByTier(tier: String): List<CustomerStats>

    @Query("SELECT COUNT(*) FROM customer_stats WHERE tier = :tier")
    suspend fun getCountByTier(tier: String): Int
}