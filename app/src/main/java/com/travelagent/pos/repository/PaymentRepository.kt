package com.travelagent.pos.repository

import com.travelagent.pos.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val ticketDao: TicketDao,
    private val customerRepository: CustomerRepository
) {
    suspend fun addPayment(
        ticket: Ticket,
        amount: Double,
        method: String,
        receiptNumber: String? = null,
        notes: String? = null
    ): RepositoryResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val totalPaid = paymentDao.getTotalPaid(ticket.id) ?: 0.0
            val remaining = ticket.ongkos - totalPaid

            if (amount <= 0) {
                return@withContext RepositoryResult.Failure(Exception("Jumlah pembayaran harus lebih dari 0"))
            }

            if (amount > remaining) {
                return@withContext RepositoryResult.Failure(Exception("Jumlah pembayaran melebihi sisa tagihan"))
            }

            paymentDao.insert(Payment(
                ticketId = ticket.id,
                amount = amount,
                paymentMethod = method,
                receiptNumber = receiptNumber,
                notes = notes
            ))

            val newTotalPaid = totalPaid + amount
            val newStatus = when {
                newTotalPaid >= ticket.ongkos -> "paid"
                newTotalPaid > 0 -> "partial"
                else -> "pending"
            }

            ticketDao.update(ticket.copy(
                totalPaid = newTotalPaid,
                status = newStatus
            ))

            if (newStatus == "paid") {
                customerRepository.updateCustomerStats(ticket.customerId, ticket.ongkos)
            }

            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Failure(e)
        }
    }

    suspend fun getPaymentSummary(ticketId: Int): PaymentSummary? = withContext(Dispatchers.IO) {
        val tickets = ticketDao.getAllTickets()
        val ticket = tickets.find { it.id == ticketId } ?: return@withContext null
        val payments = paymentDao.getPaymentsByTicket(ticketId)
        val totalPaid = payments.sumOf { it.amount }

        PaymentSummary(
            ticketId = ticketId,
            totalAmount = ticket.ongkos,
            totalPaid = totalPaid,
            remaining = ticket.ongkos - totalPaid,
            payments = payments,
            status = ticket.status
        )
    }

    suspend fun getPaymentsByTicket(ticketId: Int): List<Payment> = withContext(Dispatchers.IO) {
        paymentDao.getPaymentsByTicket(ticketId)
    }
}

data class PaymentSummary(
    val ticketId: Int,
    val totalAmount: Double,
    val totalPaid: Double,
    val remaining: Double,
    val payments: List<Payment>,
    val status: String
)