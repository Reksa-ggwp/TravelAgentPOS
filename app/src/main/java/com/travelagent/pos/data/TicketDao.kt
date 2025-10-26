package com.travelagent.pos.data

import androidx.room.*

@Dao
interface TicketDao {
    @Insert
    suspend fun insert(ticket: Ticket): Long

    @Update
    suspend fun update(ticket: Ticket)

    @Delete
    suspend fun delete(ticket: Ticket)

    @Query("SELECT * FROM tickets ORDER BY createdDate DESC")
    suspend fun getAllTickets(): List<Ticket>

    @Query("SELECT * FROM tickets WHERE tripId = :tripId")
    suspend fun getTicketsByTrip(tripId: Int): List<Ticket>

    @Query("SELECT * FROM tickets WHERE createdDate >= :startDate AND createdDate < :endDate")
    suspend fun getTicketsBetween(startDate: Long, endDate: Long): List<Ticket>

    @Query("SELECT * FROM tickets WHERE id = :id")
    suspend fun getTicketById(id: Int): Ticket?

    @Query("SELECT * FROM tickets WHERE customerId = :customerId")
    suspend fun getTicketsByCustomer(customerId: Int): List<Ticket>
}