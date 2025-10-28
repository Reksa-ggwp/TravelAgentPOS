package com.travelagent.pos.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.travelagent.pos.utils.ErrorHandler
import com.travelagent.pos.R
import com.travelagent.pos.data.*
import com.travelagent.pos.databinding.ActivityBookingBinding
import com.travelagent.pos.repository.CustomerRepository
import com.travelagent.pos.utils.Constants
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookingBinding
    private lateinit var db: AppDatabase
    private lateinit var customerRepository: CustomerRepository

    private var selectedCustomer: Customer? = null
    private var selectedTrip: Trip? = null
    private val selectedSeats = mutableSetOf<Seat>()
    private var allSeats = listOf<Seat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)
        customerRepository = CustomerRepository(
            db.customerDao(),
            db.customerStatsDao()
        )

        setupToolbar()
        setupClickListeners()
        disableSeatsSelection()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupClickListeners() {
        binding.btnSelectCustomer.setOnClickListener { selectCustomer() }
        binding.btnSelectTrip.setOnClickListener { selectTrip() }
        binding.btnCreateBooking.setOnClickListener { createBooking() }

        binding.btnCreateBooking.isEnabled = false
    }

    private fun selectCustomer() {
        lifecycleScope.launch {
            val customers = withContext(Dispatchers.IO) {
                customerRepository.getAllCustomers()
            }

            if (customers.isEmpty()) {
                ErrorHandler.showInfo(
                    this@BookingActivity,
                    "Silakan tambahkan pelanggan terlebih dahulu dari menu Data Pelanggan."
                )
                return@launch
            }

            val customerNames = customers.map { it.namaLengkap }.toTypedArray()

            MaterialAlertDialogBuilder(this@BookingActivity)
                .setTitle("Pilih Pelanggan")
                .setItems(customerNames) { _, which ->
                    selectedCustomer = customers[which]
                    updateCustomerButton()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun updateCustomerButton() {
        selectedCustomer?.let { customer ->
            binding.btnSelectCustomer.apply {
                text = "✓ ${customer.namaLengkap}"
                setIconResource(R.drawable.ic_check)
                setIconTintResource(R.color.success)
            }
            checkIfReadyToBook()
        }
    }

    private fun selectTrip() {
        lifecycleScope.launch {
            val trips = withContext(Dispatchers.IO) {
                db.tripDao().getAllTrips()
            }

            if (trips.isEmpty()) {
                ErrorHandler.showInfo(
                    this@BookingActivity,
                    "Silakan tambahkan perjalanan terlebih dahulu."
                )
                return@launch
            }

            val tripDisplay = trips.map {
                "${it.nomorPolisi} | ${it.asal} → ${it.tujuan}"
            }.toTypedArray()

            MaterialAlertDialogBuilder(this@BookingActivity)
                .setTitle("Pilih Perjalanan")
                .setItems(tripDisplay) { _, which ->
                    selectedTrip = trips[which]
                    selectedSeats.clear()
                    updateTripButton(tripDisplay[which])
                    loadSeats()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun updateTripButton(displayText: String) {
        binding.btnSelectTrip.apply {
            text = "✓ $displayText"
            setIconResource(R.drawable.ic_check)
            setIconTintResource(R.color.success)
        }
        checkIfReadyToBook()
    }

    private fun loadSeats() {
        selectedTrip?.let { trip ->
            lifecycleScope.launch {
                allSeats = withContext(Dispatchers.IO) {
                    db.seatDao().getSeatsByTrip(trip.id)
                }
                setupSeatsLayout()
            }
        }
    }

    private fun setupSeatsLayout() {
        binding.seatRow1.removeAllViews()
        binding.seatRow2.removeAllViews()
        binding.seatRow3.removeAllViews()
        binding.seatRow4.removeAllViews()

        // Row 1: 1 | X | Driver
        addSeatButton(binding.seatRow1, 1)
        addPlaceholder(binding.seatRow1, "X")
        addPlaceholder(binding.seatRow1, "🚗 Supir")

        // Row 2: 4 | 3 | 2
        addSeatButton(binding.seatRow2, 4)
        addSeatButton(binding.seatRow2, 3)
        addSeatButton(binding.seatRow2, 2)

        // Row 3: 7 | 6 | 5
        addSeatButton(binding.seatRow3, 7)
        addSeatButton(binding.seatRow3, 6)
        addSeatButton(binding.seatRow3, 5)

        // Row 4: 10 | 9 | 8
        addSeatButton(binding.seatRow4, 10)
        addSeatButton(binding.seatRow4, 9)
        addSeatButton(binding.seatRow4, 8)

        updateSummary()
    }

    private fun addSeatButton(row: LinearLayout, seatNumber: Int) {
        val seat = allSeats.find { it.nomorKursi == seatNumber } ?: return

        val btn = Button(this).apply {
            text = seatNumber.toString()
            layoutParams = LinearLayout.LayoutParams(0, 160).apply {
                weight = 1f
                setMargins(8, 8, 8, 8)
            }
            textSize = 18f
            setTextColor(Color.WHITE)
            elevation = 4f

            when (seat.status) {
                Constants.SEAT_STATUS_AVAILABLE -> {
                    setBackgroundColor(ContextCompat.getColor(context, R.color.status_available))
                    setOnClickListener {
                        if (selectedSeats.contains(seat)) {
                            selectedSeats.remove(seat)
                            setBackgroundColor(ContextCompat.getColor(context, R.color.status_available))
                        } else {
                            selectedSeats.add(seat)
                            setBackgroundColor(ContextCompat.getColor(context, R.color.info))
                        }
                        updateSummary()
                    }
                }
                else -> {
                    setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray))
                    setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
                    isEnabled = false
                }
            }
        }

        row.addView(btn)
    }

    private fun addPlaceholder(row: LinearLayout, label: String) {
        val placeholder = TextView(this).apply {
            text = label
            layoutParams = LinearLayout.LayoutParams(0, 160).apply {
                weight = 1f
                setMargins(8, 8, 8, 8)
            }
            gravity = android.view.Gravity.CENTER
            textSize = 16f
            setBackgroundColor(ContextCompat.getColor(context, R.color.lighter_gray))
            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        }
        row.addView(placeholder)
    }

    private fun disableSeatsSelection() {
        binding.seatRow1.removeAllViews()
        binding.seatRow2.removeAllViews()
        binding.seatRow3.removeAllViews()
        binding.seatRow4.removeAllViews()
    }

    private fun updateSummary() {
        binding.btnCreateBooking.isEnabled = selectedSeats.isNotEmpty()
        checkIfReadyToBook()
    }

    private fun checkIfReadyToBook() {
        binding.btnCreateBooking.isEnabled =
            selectedCustomer != null &&
                    selectedTrip != null &&
                    selectedSeats.isNotEmpty()
    }

    private fun createBooking() {
        if (selectedCustomer == null || selectedTrip == null || selectedSeats.isEmpty()) {
            ErrorHandler.handleValidationError(this, "Lengkapi semua pilihan")
            return
        }

        lifecycleScope.launch {
            try {
                // Perform booking of all selected seats in a single database transaction
                val createdTicketIds = withContext(Dispatchers.IO) {
                    db.withTransaction {
                        val customer = selectedCustomer!!
                        val trip = selectedTrip!!
                        val ids = mutableListOf<Int>()

                        selectedSeats.forEach { seat ->
                            if (seat.status != com.travelagent.pos.utils.Constants.SEAT_STATUS_AVAILABLE) {
                                throw IllegalStateException("Kursi ${seat.nomorKursi} tidak tersedia")
                            }

                            // mark seat as booked
                            db.seatDao().update(seat.copy(customerId = customer.id, status = com.travelagent.pos.utils.Constants.SEAT_STATUS_BOOKED))

                            // create ticket
                            val ticketId = db.ticketDao().insert(
                                com.travelagent.pos.data.Ticket(
                                    seatId = seat.id,
                                    tripId = trip.id,
                                    customerId = customer.id,
                                    ongkos = trip.ongkos,
                                    status = com.travelagent.pos.utils.Constants.TICKET_STATUS_PENDING
                                )
                            )

                            ids.add(ticketId.toInt())
                        }

                        ids
                    }
                }

                showSuccessDialog(createdTicketIds)

            } catch (e: Exception) {
                ErrorHandler.handleOperationError(this@BookingActivity, "membuat booking", e)
            }
        }
    }

    private fun showSuccessDialog(ticketIds: List<Int>) {
        MaterialAlertDialogBuilder(this)
            .setTitle("✅ Booking Berhasil")
            .setMessage("${selectedSeats.size} kursi berhasil di-booking untuk ${selectedCustomer?.namaLengkap}\n\nApakah Anda ingin mencetak tiket sekarang?")
            .setPositiveButton("Ya, Cetak") { _, _ ->
                // Open ticket print for first ticket
                if (ticketIds.isNotEmpty()) {
                    val intent = Intent(this, TicketPrintActivity::class.java)
                    intent.putExtra("ticketId", ticketIds[0])
                    startActivity(intent)
                }
                finish()
            }
            .setNegativeButton("Nanti Saja") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

}

