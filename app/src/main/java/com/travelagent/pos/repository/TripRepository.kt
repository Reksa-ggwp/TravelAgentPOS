package com.travelagent.pos.repository

import com.travelagent.pos.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TripRepository(
    private val tripDao: TripDao,
    private val seatDao: SeatDao,
    private val ticketDao: TicketDao
) {
    private var cachedTrips: List<Trip>? = null
    private var cacheTime: Long = 0
    private val CACHE_DURATION = 5 * 60 * 1000

    suspend fun getAllTrips(forceRefresh: Boolean = false): List<Trip> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        if (!forceRefresh && cachedTrips != null && (now - cacheTime) < CACHE_DURATION) {
            return@withContext cachedTrips!!
        }

        val trips = tripDao.getAllTrips()
        cachedTrips = trips
        cacheTime = now
        trips
    }

    suspend fun getTripById(id: Int): Trip? = withContext(Dispatchers.IO) {
        tripDao.getTripById(id)
    }

    suspend fun createTripWithSeats(trip: Trip): RepositoryResult<Long> = withContext(Dispatchers.IO) {
        try {
            val tripId = tripDao.insert(trip).toInt()

            repeat(10) { i ->
                seatDao.insert(Seat(
                    tripId = tripId,
                    nomorKursi = i + 1,
                    customerId = null,
                    status = "available"
                ))
            }

            invalidateCache()
            RepositoryResult.Success(tripId.toLong())
        } catch (e: Exception) {
            RepositoryResult.Failure(e)
        }
    }

    suspend fun updateTrip(trip: Trip): RepositoryResult<Unit> = withContext(Dispatchers.IO) {
        try {
            tripDao.update(trip)
            invalidateCache()
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Failure(e)
        }
    }

    suspend fun deleteTrip(trip: Trip): RepositoryResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val tickets = ticketDao.getTicketsByTrip(trip.id)
            val hasPaidTickets = tickets.any { it.status == "paid" }

            if (hasPaidTickets) {
                RepositoryResult.Failure(Exception("Tidak dapat menghapus trip dengan tiket yang sudah dibayar"))
            } else {
                tripDao.delete(trip)
                invalidateCache()
                RepositoryResult.Success(Unit)
            }
        } catch (e: Exception) {
            RepositoryResult.Failure(e)
        }
    }

    suspend fun getTripWithDetails(tripId: Int): TripDetails? = withContext(Dispatchers.IO) {
        val trip = tripDao.getTripById(tripId) ?: return@withContext null
        val seats = seatDao.getSeatsByTrip(tripId)
        val tickets = ticketDao.getTicketsByTrip(tripId)

        TripDetails(
            trip = trip,
            seats = seats,
            tickets = tickets,
            availableSeats = seats.count { it.status == "available" },
            bookedSeats = seats.count { it.status == "booked" },
            paidSeats = seats.count { it.status == "paid" }
        )
    }

    fun invalidateCache() {
        cachedTrips = null
        cacheTime = 0
    }
}

data class TripDetails(
    val trip: Trip,
    val seats: List<Seat>,
    val tickets: List<Ticket>,
    val availableSeats: Int,
    val bookedSeats: Int,
    val paidSeats: Int
)