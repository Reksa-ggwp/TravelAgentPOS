package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import android.widget.*
import com.travelagent.pos.R
import com.travelagent.pos.data.*
import com.travelagent.pos.databinding.ActivityBookingBinding
import com.travelagent.pos.repository.CustomerRepository
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
            db.customerStatsDao() // ✅ Fixed
        )

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSelectCustomer.setOnClickListener { selectCustomer() }
        binding.btnSelectTrip.setOnClickListener { selectTrip() }
        binding.btnCreateBooking.setOnClickListener { createBooking() }

        disableSeatsSelection()
    }

    private fun selectCustomer() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_search_customer, null)
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearchCustomer)
        val lvCustomers = dialogView.findViewById<ListView>(R.id.lvCustomers)

        lifecycleScope.launch {
            val customers = withContext(Dispatchers.IO) {
                customerRepository.getAllCustomers()
            }

            if (customers.isEmpty()) {
                Toast.makeText(this@BookingActivity, "Belum ada pelanggan. Tambahkan terlebih dahulu.", Toast.LENGTH_LONG).show()
                return@launch
            }

            val customerNames = customers.map { it.namaLengkap }.toMutableList()
            val adapter = ArrayAdapter(
                this@BookingActivity,
                android.R.layout.simple_list_item_1,
                customerNames
            )
            lvCustomers.adapter = adapter

            val dialog = AlertDialog.Builder(this@BookingActivity)
                .setTitle("Pilih Pelanggan")
                .setView(dialogView)
                .setNegativeButton("Batal", null)
                .create()

            etSearch.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {
                    val filtered = customers.filter {
                        it.namaLengkap.contains(s.toString(), ignoreCase = true) ||
                                it.nomorTelepon.contains(s.toString())
                    }
                    adapter.clear()
                    adapter.addAll(filtered.map { it.namaLengkap })
                    adapter.notifyDataSetChanged()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            lvCustomers.setOnItemClickListener { _, _, position, _ ->
                val displayedName = adapter.getItem(position)
                selectedCustomer = customers.find { it.namaLengkap == displayedName }
                binding.btnSelectCustomer.text = "✓ ${selectedCustomer?.namaLengkap}"
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun selectTrip() {
        lifecycleScope.launch {
            val trips = withContext(Dispatchers.IO) {
                db.tripDao().getAllTrips()
            }

            if (trips.isEmpty()) {
                Toast.makeText(this@BookingActivity, "Belum ada perjalanan tersedia", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val tripDisplay = trips.map { "${it.nomorPolisi} | ${it.asal} → ${it.tujuan}" }.toTypedArray()

            AlertDialog.Builder(this@BookingActivity)
                .setTitle("Pilih Perjalanan")
                .setItems(tripDisplay) { _, which ->
                    selectedTrip = trips[which]
                    selectedSeats.clear()
                    binding.btnSelectTrip.text = "✓ ${tripDisplay[which]}"
                    loadSeats()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun loadSeats() {
        if (selectedTrip == null) return

        lifecycleScope.launch {
            allSeats = withContext(Dispatchers.IO) {
                db.seatDao().getSeatsByTrip(selectedTrip!!.id)
            }
            setupSeatsLayout()
        }
    }

    private fun setupSeatsLayout() {
        binding.seatRow1.removeAllViews()
        binding.seatRow2.removeAllViews()
        binding.seatRow3.removeAllViews()
        binding.seatRow4.removeAllViews()

        addSeatButton(binding.seatRow1, 1)
        addPlaceholder(binding.seatRow1, "X")
        addPlaceholder(binding.seatRow1, "Supir")

        addSeatButton(binding.seatRow2, 4)
        addSeatButton(binding.seatRow2, 3)
        addSeatButton(binding.seatRow2, 2)

        addSeatButton(binding.seatRow3, 7)
        addSeatButton(binding.seatRow3, 6)
        addSeatButton(binding.seatRow3, 5)

        addSeatButton(binding.seatRow4, 10)
        addSeatButton(binding.seatRow4, 9)
        addSeatButton(binding.seatRow4, 8)

        updateSummary()
    }

    private fun addSeatButton(row: LinearLayout, seatNumber: Int) {
        val seat = allSeats.find { it.nomorKursi == seatNumber } ?: return

        val btn = Button(this).apply {
            text = seatNumber.toString()
            layoutParams = LinearLayout.LayoutParams(0, 150).apply {
                weight = 1f
                setMargins(4, 4, 4, 4)
            }
            textSize = 20f

            when (seat.status) {
                "available" -> {
                    setBackgroundColor(Color.GREEN)
                    setTextColor(Color.WHITE)
                    setOnClickListener {
                        if (selectedSeats.contains(seat)) {
                            selectedSeats.remove(seat)
                            setBackgroundColor(Color.GREEN)
                        } else {
                            selectedSeats.add(seat)
                            setBackgroundColor(Color.parseColor("#2196F3"))
                        }
                        updateSummary()
                    }
                }
                else -> {
                    setBackgroundColor(Color.LTGRAY)
                    setTextColor(Color.DKGRAY)
                    isEnabled = false
                }
            }
        }

        row.addView(btn)
    }

    private fun addPlaceholder(row: LinearLayout, label: String) {
        val placeholder = TextView(this).apply {
            text = label
            layoutParams = LinearLayout.LayoutParams(0, 150).apply {
                weight = 1f
                setMargins(4, 4, 4, 4)
            }
            gravity = android.view.Gravity.CENTER
            textSize = 18f
            setBackgroundColor(Color.LTGRAY)
            setTextColor(Color.BLACK)
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
        if (selectedSeats.isEmpty()) {
            binding.btnCreateBooking.isEnabled = false
        } else {
            binding.btnCreateBooking.isEnabled = true
            val seats = selectedSeats.map { it.nomorKursi }.sorted().joinToString(", ")
            val total = selectedSeats.size * (selectedTrip?.ongkos ?: 0.0)
            // Update UI with selected seats info (add TextViews to your layout)
        }
    }

    private fun createBooking() {
        if (selectedCustomer == null) {
            Toast.makeText(this, "⚠️ Pilih pelanggan terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedTrip == null) {
            Toast.makeText(this, "⚠️ Pilih perjalanan terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedSeats.isEmpty()) {
            Toast.makeText(this, "⚠️ Pilih kursi terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    selectedSeats.forEach { seat ->
                        db.seatDao().update(seat.copy(
                            customerId = selectedCustomer!!.id,
                            status = "booked"
                        ))

                        db.ticketDao().insert(Ticket(
                            seatId = seat.id,
                            tripId = selectedTrip!!.id,
                            customerId = selectedCustomer!!.id,
                            ongkos = selectedTrip!!.ongkos,
                            status = "pending"
                        ))
                    }
                }

                Toast.makeText(
                    this@BookingActivity,
                    "✓ ${selectedSeats.size} booking berhasil dibuat!",
                    Toast.LENGTH_LONG
                ).show()
                finish()

            } catch (e: Exception) {
                Toast.makeText(
                    this@BookingActivity,
                    "Gagal: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}