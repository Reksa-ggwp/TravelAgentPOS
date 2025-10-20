package com.travelagent.pos.data

import androidx.room.*

@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: Payment): Long

    @Update
    suspend fun update(payment: Payment)

    @Delete
    suspend fun delete(payment: Payment)

    @Query("SELECT * FROM payments WHERE ticketId = :ticketId ORDER BY timestamp DESC")
    suspend fun getPaymentsByTicket(ticketId: Int): List<Payment>

    @Query("SELECT SUM(amount) FROM payments WHERE ticketId = :ticketId")
    suspend fun getTotalPaid(ticketId: Int): Double?

    @Query("SELECT * FROM payments ORDER BY timestamp DESC LIMIT 100")
    suspend fun getRecentPayments(): List<Payment>

    @Query("SELECT * FROM payments WHERE timestamp >= :startDate AND timestamp < :endDate")
    suspend fun getPaymentsBetween(startDate: Long, endDate: Long): List<Payment>
}