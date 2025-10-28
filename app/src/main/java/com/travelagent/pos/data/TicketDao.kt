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

    // Lightweight helpers
    @Query("SELECT COUNT(*) FROM tickets WHERE tripId = :tripId")
    suspend fun countTicketsByTrip(tripId: Int): Int

    @Query("SELECT SUM(amount) FROM payments WHERE ticketId = :ticketId")
    suspend fun getTotalPaymentsForTicket(ticketId: Int): Double?

    @Query("SELECT COUNT(*) FROM tickets WHERE tripId = :tripId AND status = :status")
    suspend fun countByStatusForTrip(tripId: Int, status: String): Int

    // Transactional helpers
    @Transaction
    suspend fun bookSeatAndCreateTicket(db: AppDatabase, seatDao: SeatDao, seat: Seat, customer: Customer, trip: Trip): Int {
        // Ensure seat is available
        if (seat.status != com.travelagent.pos.utils.Constants.SEAT_STATUS_AVAILABLE) {
            throw IllegalStateException("Kursi tidak tersedia")
        }
        // Update seat
        seatDao.update(seat.copy(customerId = customer.id, status = com.travelagent.pos.utils.Constants.SEAT_STATUS_BOOKED))
        // Create ticket
        val ticketId = insert(
            Ticket(
                seatId = seat.id,
                tripId = trip.id,
                customerId = customer.id,
                ongkos = trip.ongkos,
                status = com.travelagent.pos.utils.Constants.TICKET_STATUS_PENDING
            )
        )
        return ticketId.toInt()
    }
}