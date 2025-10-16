package com.travelagent.pos.data
import androidx.room.*

@Dao
interface TicketDao {
    @Insert
    suspend fun insert(ticket: Ticket): Long

    @Update
    suspend fun update(ticket: Ticket)

    @Query("SELECT * FROM tickets ORDER BY createdDate DESC")
    suspend fun getAllTickets(): List<Ticket>

    @Query("SELECT * FROM tickets WHERE tripId = :tripId")
    suspend fun getTicketsByTrip(tripId: Int): List<Ticket>
}