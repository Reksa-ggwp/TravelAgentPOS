package com.travelagent.pos.data

import androidx.room.*

@Dao
interface TripDao {
    @Insert
    suspend fun insert(trip: Trip): Long

    @Update
    suspend fun update(trip: Trip)

    @Delete
    suspend fun delete(trip: Trip)

    @Query("SELECT * FROM trips ORDER BY tanggal DESC")
    suspend fun getAllTrips(): List<Trip>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripById(id: Int): Trip?

    @Query("SELECT * FROM trips WHERE tanggal >= :startDate AND tanggal < :endDate")
    suspend fun getTripsBetween(startDate: Long, endDate: Long): List<Trip>

    // Transactional helper to delete by id, relying on FK cascade for seats/tickets/payments
    @Transaction
    suspend fun deleteByIdTransactional(db: AppDatabase, tripId: Int) {
        getTripById(tripId)?.let { delete(it) }
    }
}