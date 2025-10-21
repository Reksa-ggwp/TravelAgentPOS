package com.travelagent.pos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import android.widget.EditText
import android.widget.Toast
import com.travelagent.pos.data.*
import com.travelagent.pos.databinding.ActivityTicketPrintBinding
import com.travelagent.pos.repository.PaymentRepository
import com.travelagent.pos.repository.CustomerRepository
import com.travelagent.pos.repository.RepositoryResult
import com.travelagent.pos.utils.ReceiptPrinter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TicketPrintActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTicketPrintBinding
    private lateinit var db: AppDatabase
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var receiptPrinter: ReceiptPrinter
    private var ticketId: Int = 0
    private lateinit var ticket: Ticket
    private lateinit var trip: Trip
    private lateinit var customer: Customer
    private lateinit var seat: Seat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketPrintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)
        val customerRepo = CustomerRepository(
            db.customerDao(),
            db.customerStatsDao()
        )
        paymentRepository = PaymentRepository(
            db.paymentDao(),
            db.ticketDao(),
            customerRepo
        )
        receiptPrinter = ReceiptPrinter(this)

        ticketId = intent.getIntExtra("ticketId", 0)

        binding.btnPrint.setOnClickListener { printTicket() }
        binding.btnMarkPaid.setOnClickListener { showPaymentDialog() }
        binding.btnBack.setOnClickListener { finish() }

        loadTicketDetails()
    }

    private fun loadTicketDetails() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val loadedTicket = db.ticketDao().getTicketById(ticketId)
                    if (loadedTicket == null) throw Exception("Tiket tidak ditemukan")
                    ticket = loadedTicket

                    val loadedTrip = db.tripDao().getTripById(ticket.tripId)
                    if (loadedTrip == null) throw Exception("Perjalanan tidak ditemukan")
                    trip = loadedTrip

                    val loadedCustomer = db.customerDao().getCustomerById(ticket.customerId)
                    if (loadedCustomer == null) throw Exception("Pelanggan tidak ditemukan")
                    customer = loadedCustomer

                    val loadedSeat = db.seatDao().getSeatById(ticket.seatId)
                    if (loadedSeat == null) throw Exception("Kursi tidak ditemukan")
                    seat = loadedSeat
                }

                val receipt = receiptPrinter.generateReceipt(ticket, trip, customer, seat)
                binding.tvTicketPreview.text = receipt
            } catch (e: Exception) {
                Toast.makeText(
                    this@TicketPrintActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun printTicket() {
        if (!::ticket.isInitialized || !::trip.isInitialized || !::customer.isInitialized || !::seat.isInitialized) {
            Toast.makeText(this, "Data tiket belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        val receipt = receiptPrinter.generateReceipt(ticket, trip, customer, seat)
        receiptPrinter.shareReceipt(receipt)
    }

    private fun showPaymentDialog() {
        if (!::ticket.isInitialized) {
            Toast.makeText(this, "Data tiket belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val remaining = ticket.ongkos - ticket.totalPaid

        if (remaining <= 0) {
            Toast.makeText(this, "Tiket sudah lunas", Toast.LENGTH_SHORT).show()
            return
        }

        val etAmount = EditText(this).apply {
            hint = "Jumlah pembayaran"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(remaining.toString())
        }

        AlertDialog.Builder(this)
            .setTitle("Pembayaran (Sisa: Rp ${String.format("%,d", remaining.toLong())})")
            .setView(etAmount)
            .setPositiveButton("Bayar") { _, _ ->
                val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
                processPayment(amount)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun processPayment(amount: Double) {
        lifecycleScope.launch {
            try {
                val result = paymentRepository.addPayment(
                    ticket = ticket,
                    amount = amount,
                    method = "cash"
                )

                when (result) {
                    is RepositoryResult.Success -> {
                        Toast.makeText(
                            this@TicketPrintActivity,
                            "✓ Pembayaran berhasil",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadTicketDetails()
                    }
                    is RepositoryResult.Failure -> {
                        Toast.makeText(
                            this@TicketPrintActivity,
                            "Error: ${result.exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@TicketPrintActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}