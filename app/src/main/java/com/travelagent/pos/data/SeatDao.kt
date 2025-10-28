package com.travelagent.pos.data

import androidx.room.*

@Dao
interface SeatDao {
    @Insert
    suspend fun insert(seat: Seat): Long

    @Update
    suspend fun update(seat: Seat)

    @Delete
    suspend fun delete(seat: Seat)

    @Query("SELECT * FROM seats WHERE tripId = :tripId ORDER BY nomorKursi ASC")
    suspend fun getSeatsByTrip(tripId: Int): List<Seat>

    @Query("SELECT * FROM seats WHERE id = :id")
    suspend fun getSeatById(id: Int): Seat?

    // Helper: fast count of available seats for a trip
    @Query("SELECT COUNT(*) FROM seats WHERE tripId = :tripId AND status = :status")
    suspend fun countSeatsByStatus(tripId: Int, status: String): Int
}