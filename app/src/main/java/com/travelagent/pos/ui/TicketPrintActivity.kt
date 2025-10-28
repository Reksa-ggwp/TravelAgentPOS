package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import com.travelagent.pos.utils.ErrorHandler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.travelagent.pos.R
import com.travelagent.pos.data.*
import com.travelagent.pos.databinding.ActivityTicketPrintBinding
import com.travelagent.pos.repository.CustomerRepository
import com.travelagent.pos.repository.PaymentRepository
import com.travelagent.pos.repository.RepositoryResult
import com.travelagent.pos.utils.ThermalPrinterManager
import com.travelagent.pos.utils.TicketExportManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TicketPrintActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTicketPrintBinding
    private lateinit var db: AppDatabase
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var thermalPrinter: ThermalPrinterManager
    private lateinit var exportManager: TicketExportManager

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
        val customerRepo = CustomerRepository(db.customerDao(), db.customerStatsDao())
        paymentRepository = PaymentRepository(db.paymentDao(), db.ticketDao(), customerRepo)
        thermalPrinter = ThermalPrinterManager(this)
        exportManager = TicketExportManager(this)

        ticketId = intent.getIntExtra("ticketId", 0)

        if (ticketId == 0) {
            ErrorHandler.showError(this, "Ticket ID tidak valid")
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
        binding.btnExport.setOnClickListener { exportTicket() }
        binding.btnPrinterSettings.setOnClickListener {
            startActivity(Intent(this, PrinterSettingsActivity::class.java))
        }
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
                ErrorHandler.handleOperationError(this@TicketPrintActivity, "memuat tiket", e)
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
            ErrorHandler.showWarning(this, getString(R.string.invalid_ticket_data))
            return
        }

        // Check if printer is configured
        if (thermalPrinter.getSavedPrinterAddress() == null) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Printer Not Configured")
                .setMessage("Please configure your thermal printer first.")
                .setPositiveButton("Open Settings") { _, _ ->
                    startActivity(Intent(this, PrinterSettingsActivity::class.java))
                }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }

        binding.btnPrint.isEnabled = false
        // FIX: Explicitly use the Activity context
        binding.btnPrint.text = this@TicketPrintActivity.getString(R.string.printing)

        lifecycleScope.launch {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id"))
                val paymentHistory = payments.map {
                    "${formatCurrency(it.amount)} - ${it.paymentMethod} - ${sdf.format(Date(it.timestamp))}"
                }

                val formattedTicket = thermalPrinter.formatTicketForThermal(
                    ticketId = ticket.id,
                    customerName = customer.namaLengkap,
                    phone = customer.nomorTelepon,
                    address = customer.alamat,
                    origin = trip.asal,
                    destination = trip.tujuan,
                    date = sdf.format(Date(trip.tanggal)),
                    plateNumber = trip.nomorPolisi,
                    driverName = trip.namaSopir,
                    seatNumber = seat.nomorKursi,
                    price = ticket.ongkos,
                    totalPaid = ticket.totalPaid,
                    status = ticket.status,
                    isStamped = ticket.isStamped,
                    payments = paymentHistory
                )

                val result = thermalPrinter.printTicket(formattedTicket)

                binding.btnPrint.isEnabled = true
                // FIX: Explicitly use the Activity context
                binding.btnPrint.text = this@TicketPrintActivity.getString(R.string.print_tiket)

                result.fold(
                    onSuccess = {
                        ErrorHandler.showSuccess(this@TicketPrintActivity, "Tiket berhasil dicetak")
                    },
                    onFailure = { error ->
                        ErrorHandler.handleOperationError(this@TicketPrintActivity, "mencetak tiket", error)
                        MaterialAlertDialogBuilder(this@TicketPrintActivity)
                            .setTitle("Print Failed")
                            .setMessage("Error: ${error.message}\n\nTroubleshooting:\n• Check printer is ON\n• Check paper loaded\n• Check Bluetooth connection\n• Try printer settings")
                            .setPositiveButton("Printer Settings") { _, _ ->
                                startActivity(Intent(this@TicketPrintActivity, PrinterSettingsActivity::class.java))
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                )
            } catch (e: Exception) {
                binding.btnPrint.isEnabled = true
                // FIX: Explicitly use the Activity context here as well for consistency
                binding.btnPrint.text = this@TicketPrintActivity.getString(R.string.print_tiket)
                ErrorHandler.showError(this@TicketPrintActivity, "Error: ${e.message}")
            }
        }
    }

    private fun exportTicket() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Export Ticket")
            .setMessage("Choose export format:")
            .setPositiveButton("PDF") { _, _ ->
                exportToPDF()
            }
            .setNeutralButton("Text") { _, _ ->
                exportToText()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportToPDF() {
        binding.btnExport.isEnabled = false
        binding.btnExport.text = "Exporting..."

        lifecycleScope.launch {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id"))
                val result = exportManager.exportTicketToPDF(
                    ticketId = ticket.id,
                    customerName = customer.namaLengkap,
                    phone = customer.nomorTelepon,
                    address = customer.alamat,
                    origin = trip.asal,
                    destination = trip.tujuan,
                    date = sdf.format(Date(trip.tanggal)),
                    plateNumber = trip.nomorPolisi,
                    driverName = trip.namaSopir,
                    seatNumber = seat.nomorKursi,
                    price = ticket.ongkos,
                    totalPaid = ticket.totalPaid,
                    status = ticket.status,
                    isStamped = ticket.isStamped
                )

                binding.btnExport.isEnabled = true
                binding.btnExport.text = "📤 Export Tiket"

                result.fold(
                    onSuccess = { file ->
                        ErrorHandler.showSuccess(this@TicketPrintActivity, String.format(getString(R.string.export_success), file.name))
                        exportManager.shareFile(file)
                    },
                    onFailure = { error ->
                        ErrorHandler.handleOperationError(this@TicketPrintActivity, "export tiket", error)
                    }
                )
            } catch (e: Exception) {
                binding.btnExport.isEnabled = true
                binding.btnExport.text = "📤 Export Tiket"
                ErrorHandler.handleOperationError(this@TicketPrintActivity, "export tiket", e)
            }
        }
    }

    private fun exportToText() {
        val receipt = generateReceiptText()
        exportManager.shareText(receipt, "Ticket_${ticket.id}")
    }

    private fun stampTicket() {
        if (ticket.isStamped) {
            ErrorHandler.showWarning(this, getString(R.string.stamped))
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

                        ErrorHandler.showSuccess(this@TicketPrintActivity, getString(R.string.stamp_success))
                        updateUI()
                    } catch (e: Exception) {
                        ErrorHandler.showError(this@TicketPrintActivity, "Error: ${e.message}")
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showPaymentDialog() {
        if (!::ticket.isInitialized) {
            ErrorHandler.showWarning(this, getString(R.string.invalid_ticket_data))
            return
        }

        val remaining = ticket.ongkos - ticket.totalPaid

        if (remaining <= 0) {
            ErrorHandler.showInfo(this, getString(R.string.ticket_paid))
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_payment, null)
        val etAmount = dialogView.findViewById<android.widget.EditText>(R.id.etPaymentAmount)
        val spinnerMethod = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerPaymentMethod)
        val etNotes = dialogView.findViewById<android.widget.EditText>(R.id.etPaymentNotes)

        etAmount.setText(remaining.toInt().toString())

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
            ErrorHandler.showWarning(this, getString(R.string.amount_must_be_positive))
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
                        ErrorHandler.showSuccess(this@TicketPrintActivity, String.format(getString(R.string.payment_success), formatCurrency(amount)))

                        // Reload ticket details
                        loadTicketDetails()
                    }
                    is RepositoryResult.Failure -> {
                        ErrorHandler.showError(this@TicketPrintActivity, "Error: ${result.exception.message}")
                    }
                }
            } catch (e: Exception) {
                ErrorHandler.showError(this@TicketPrintActivity, "Error: ${e.message}")
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