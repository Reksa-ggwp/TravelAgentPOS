package com.travelagent.pos.utils

import com.travelagent.pos.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class ReportManager(private val database: AppDatabase) {

    suspend fun generateDailyReport(date: Long): DailyReport = withContext(Dispatchers.IO) {
        val dayStart = getStartOfDay(date)
        val dayEnd = getEndOfDay(date)

        val tickets = database.ticketDao().getTicketsBetween(dayStart, dayEnd)
        val paidTickets = tickets.filter { it.status == "paid" }
        val trips = database.tripDao().getTripsBetween(dayStart, dayEnd)

        DailyReport(
            date = date,
            totalBookings = tickets.size,
            paidBookings = paidTickets.size,
            pendingBookings = tickets.count { it.status == "pending" },
            partialBookings = tickets.count { it.status == "partial" },
            totalRevenue = paidTickets.sumOf { it.ongkos },
            tripCount = trips.size
        )
    }

    suspend fun generateMonthlyReport(month: Int, year: Int): MonthlyReport = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val monthStart = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val monthEnd = cal.timeInMillis

        val tickets = database.ticketDao().getTicketsBetween(monthStart, monthEnd)
        val paidTickets = tickets.filter { it.status == "paid" }
        val trips = database.tripDao().getTripsBetween(monthStart, monthEnd)

        val daysInMonth = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)

        MonthlyReport(
            month = month,
            year = year,
            totalBookings = tickets.size,
            totalRevenue = paidTickets.sumOf { it.ongkos },
            averageDailyRevenue = paidTickets.sumOf { it.ongkos } / daysInMonth,
            topRoute = findTopRoute(trips),
            totalTrips = trips.size,
            occupancyRate = calculateOccupancyRate(trips)
        )
    }

    private fun getStartOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private suspend fun findTopRoute(trips: List<com.travelagent.pos.data.Trip>): String {
        val routeCounts = mutableMapOf<String, Int>()

        trips.forEach { trip ->
            val route = "${trip.asal} → ${trip.tujuan}"
            routeCounts[route] = (routeCounts[route] ?: 0) + 1
        }

        return routeCounts.maxByOrNull { it.value }?.key ?: "N/A"
    }

    private suspend fun calculateOccupancyRate(trips: List<com.travelagent.pos.data.Trip>): Double {
        if (trips.isEmpty()) return 0.0

        var totalSeats = 0
        var occupiedSeats = 0

        trips.forEach { trip ->
            val seats = database.seatDao().getSeatsByTrip(trip.id)
            totalSeats += seats.size
            occupiedSeats += seats.count { it.status in listOf("booked", "paid") }
        }

        return if (totalSeats > 0) (occupiedSeats.toDouble() / totalSeats) * 100 else 0.0
    }
}

data class DailyReport(
    val date: Long,
    val totalBookings: Int,
    val paidBookings: Int,
    val pendingBookings: Int,
    val partialBookings: Int,
    val totalRevenue: Double,
    val tripCount: Int
)

data class MonthlyReport(
    val month: Int,
    val year: Int,
    val totalBookings: Int,
    val totalRevenue: Double,
    val averageDailyRevenue: Double,
    val topRoute: String,
    val totalTrips: Int,
    val occupancyRate: Double
)