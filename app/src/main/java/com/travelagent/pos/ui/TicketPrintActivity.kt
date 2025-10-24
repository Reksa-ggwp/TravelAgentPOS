package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.travelagent.pos.R
import com.travelagent.pos.data.*
import com.travelagent.pos.databinding.ActivityTicketPrintBinding
import com.travelagent.pos.repository.CustomerRepository
import com.travelagent.pos.repository.PaymentRepository
import com.travelagent.pos.repository.RepositoryResult
import com.travelagent.pos.utils.ReceiptPrinter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

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
    private var payments: List<Payment> = emptyList()

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

        if (ticketId == 0) {
            Toast.makeText(this, "Error: Ticket ID tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
        loadTicketDetails()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnPrint.setOnClickListener { printTicket() }
        binding.btnStamp.setOnClickListener { stampTicket() }
        binding.btnMarkPaid.setOnClickListener { showPaymentDialog() }
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

                    payments = db.paymentDao().getPaymentsByTicket(ticketId)
                }

                updateUI()
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

    private fun updateUI() {
        val receipt = generateReceiptText()
        binding.tvTicketPreview.text = receipt

        // Update buttons based on ticket status
        when (ticket.status) {
            "paid" -> {
                binding.btnMarkPaid.isEnabled = false
                binding.btnMarkPaid.text = "✅ SUDAH LUNAS"
            }
            "partial" -> {
                binding.btnMarkPaid.isEnabled = true
                binding.btnMarkPaid.text = "💰 BAYAR SISA (${formatCurrency(ticket.ongkos - ticket.totalPaid)})"
            }
            else -> {
                binding.btnMarkPaid.isEnabled = true
                binding.btnMarkPaid.text = "💰 TANDAI SEBAGAI PAID"
            }
        }

        // Update stamp button
        if (ticket.isStamped) {
            binding.btnStamp.isEnabled = false
            binding.btnStamp.text = "✅ SUDAH DI-STAMP"
        }
    }

    private fun generateReceiptText(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id"))
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("id"))

        return buildString {
            appendLine("================================")
            appendLine("     TRAVEL AGENT POS")
            appendLine("================================")
            appendLine()
            appendLine("TIKET PERJALANAN")
            appendLine("--------------------------------")
            appendLine("No. Tiket  : ${ticket.id}")
            appendLine("Tanggal    : ${sdf.format(Date())}")
            if (ticket.isStamped) {
                appendLine("Status     : ✓ SUDAH DI-STAMP")
            }
            appendLine()
            appendLine("DETAIL PERJALANAN:")
            appendLine("Asal       : ${trip.asal}")
            appendLine("Tujuan     : ${trip.tujuan}")
            appendLine("Tanggal    : ${dateFormat.format(Date(trip.tanggal))}")
            appendLine("Nopol      : ${trip.nomorPolisi}")
            appendLine("Sopir      : ${trip.namaSopir}")
            appendLine("No. Kursi  : ${seat.nomorKursi}")
            appendLine()
            appendLine("PENUMPANG:")
            appendLine("Nama       : ${customer.namaLengkap}")
            appendLine("No. Telp   : ${customer.nomorTelepon}")
            appendLine("Alamat     : ${customer.alamat}")
            appendLine()
            appendLine("PEMBAYARAN:")
            appendLine("Harga      : ${formatCurrency(ticket.ongkos)}")
            appendLine("Dibayar    : ${formatCurrency(ticket.totalPaid)}")

            val remaining = ticket.ongkos - ticket.totalPaid
            if (remaining > 0) {
                appendLine("Sisa       : ${formatCurrency(remaining)}")
                appendLine("Status     : ${getStatusText(ticket.status)}")
            } else {
                appendLine("Status     : ✓ LUNAS")
            }

            if (payments.isNotEmpty()) {
                appendLine()
                appendLine("RIWAYAT PEMBAYARAN:")
                payments.forEachIndexed { index, payment ->
                    appendLine("${index + 1}. ${formatCurrency(payment.amount)} - ${payment.paymentMethod}")
                    appendLine("   ${sdf.format(Date(payment.timestamp))}")
                }
            }

            appendLine()
            appendLine("================================")
            appendLine("  Terima kasih atas kepercayaan")
            appendLine("       Anda menggunakan")
            appendLine("         layanan kami!")
            appendLine("================================")
            appendLine()
            appendLine("Simpan tiket ini sebagai bukti")
            appendLine("pembayaran yang sah")
            appendLine()
        }
    }

    private fun printTicket() {
        if (!::ticket.isInitialized) {
            Toast.makeText(this, "Data tiket belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val receipt = generateReceiptText()
        receiptPrinter.shareReceipt(receipt)
    }

    private fun stampTicket() {
        if (ticket.isStamped) {
            Toast.makeText(this, "Tiket sudah di-stamp", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Stamp Tiket?")
            .setMessage("Apakah Anda yakin ingin meng-stamp tiket ini?\n\nTiket yang sudah di-stamp menandakan penumpang sudah naik.")
            .setPositiveButton("Ya, Stamp") { _, _ ->
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            db.ticketDao().update(ticket.copy(isStamped = true))
                            ticket = ticket.copy(isStamped = true)
                        }

                        Toast.makeText(
                            this@TicketPrintActivity,
                            "✓ Tiket berhasil di-stamp",
                            Toast.LENGTH_SHORT
                        ).show()

                        updateUI()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@TicketPrintActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
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

        val dialogView = layoutInflater.inflate(R.layout.dialog_payment, null)
        val etAmount = dialogView.findViewById<android.widget.EditText>(R.id.etPaymentAmount)
        val spinnerMethod = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerPaymentMethod)
        val etNotes = dialogView.findViewById<android.widget.EditText>(R.id.etPaymentNotes)

        // Set default amount to remaining
        etAmount.setText(remaining.toInt().toString())

        // Setup payment method spinner
        val methods = arrayOf("Cash", "Transfer Bank", "E-Wallet")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, methods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMethod.adapter = adapter

        MaterialAlertDialogBuilder(this)
            .setTitle("Pembayaran")
            .setMessage("Sisa pembayaran: ${formatCurrency(remaining)}")
            .setView(dialogView)
            .setPositiveButton("Bayar") { _, _ ->
                val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
                val method = spinnerMethod.selectedItem.toString()
                val notes = etNotes.text.toString().trim()

                processPayment(amount, method, notes)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun processPayment(amount: Double, method: String, notes: String) {
        if (amount <= 0) {
            Toast.makeText(this, "Jumlah pembayaran harus lebih dari 0", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val result = paymentRepository.addPayment(
                    ticket = ticket,
                    amount = amount,
                    method = method.lowercase(),
                    receiptNumber = null,
                    notes = notes.ifEmpty { null }
                )

                when (result) {
                    is RepositoryResult.Success -> {
                        Toast.makeText(
                            this@TicketPrintActivity,
                            "✓ Pembayaran berhasil: ${formatCurrency(amount)}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Reload ticket details
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

    private fun formatCurrency(amount: Double): String {
        return "Rp ${String.format("%,d", amount.toLong()).replace(',', '.')}"
    }

    private fun getStatusText(status: String): String {
        return when (status) {
            "paid" -> "LUNAS"
            "partial" -> "DIBAYAR SEBAGIAN"
            "pending" -> "BELUM BAYAR"
            else -> status.uppercase()
        }
    }
}