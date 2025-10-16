package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.widget.*
import com.travelagent.pos.R
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Customer
import com.travelagent.pos.data.Seat
import com.travelagent.pos.data.Ticket
import com.travelagent.pos.data.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BookingActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private var selectedCustomer: Customer? = null
    private var selectedTrip: Trip? = null
    private var selectedSeat: Seat? = null
    private var allSeats = listOf<Seat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        db = AppDatabase.getDatabase(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSelectCustomer).setOnClickListener { selectCustomer() }
        findViewById<Button>(R.id.btnSelectTrip).setOnClickListener { selectTrip() }
        findViewById<Button>(R.id.btnCreateBooking).setOnClickListener { createBooking() }

        // Initially disable seat selection
        disableSeatsSelection()
    }

    private fun selectCustomer() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_search_customer, null)
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearchCustomer)
        val lvCustomers = dialogView.findViewById<ListView>(R.id.lvCustomers)

        GlobalScope.launch(Dispatchers.Main) {
            val customers = db.customerDao().getAllCustomers().toMutableList()

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
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val filtered = customers.filter {
                        it.namaLengkap.contains(s.toString(), ignoreCase = true) ||
                                it.nomorTelepon.contains(s.toString())
                    }
                    adapter.clear()
                    adapter.addAll(filtered.map { it.namaLengkap })
                    adapter.notifyDataSetChanged()
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })

            lvCustomers.setOnItemClickListener { _, _, position, _ ->
                val displayedName = adapter.getItem(position)
                selectedCustomer = customers.find { it.namaLengkap == displayedName }
                findViewById<Button>(R.id.btnSelectCustomer).text = "✓ ${selectedCustomer?.namaLengkap}"
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun selectTrip() {
        GlobalScope.launch(Dispatchers.Main) {
            val trips = db.tripDao().getAllTrips()

            if (trips.isEmpty()) {
                Toast.makeText(this@BookingActivity, "Belum ada perjalanan tersedia", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val tripDisplay = trips.map { "${it.nomorPolisi} | ${it.asal} → ${it.tujuan}" }.toTypedArray()

            AlertDialog.Builder(this@BookingActivity)
                .setTitle("Pilih Perjalanan")
                .setItems(tripDisplay) { _, which ->
                    selectedTrip = trips[which]
                    selectedSeat = null // Reset seat selection
                    findViewById<Button>(R.id.btnSelectTrip).text = "✓ ${tripDisplay[which]}"
                    loadSeats()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun loadSeats() {
        if (selectedTrip == null) return

        GlobalScope.launch(Dispatchers.Main) {
            allSeats = db.seatDao().getSeatsByTrip(selectedTrip!!.id)
            setupSeatsLayout()
        }
    }

    private fun setupSeatsLayout() {
        val row1 = findViewById<LinearLayout>(R.id.seatRow1)
        val row2 = findViewById<LinearLayout>(R.id.seatRow2)
        val row3 = findViewById<LinearLayout>(R.id.seatRow3)
        val row4 = findViewById<LinearLayout>(R.id.seatRow4)

        row1.removeAllViews()
        row2.removeAllViews()
        row3.removeAllViews()
        row4.removeAllViews()

        // Row 1: 1 | X | Supir
        addSeatButton(row1, 1)
        addPlaceholder(row1, "X")
        addPlaceholder(row1, "Supir")

        // Row 2: 4 | 3 | 2
        addSeatButton(row2, 4)
        addSeatButton(row2, 3)
        addSeatButton(row2, 2)

        // Row 3: 7 | 6 | 5
        addSeatButton(row3, 7)
        addSeatButton(row3, 6)
        addSeatButton(row3, 5)

        // Row 4: 10 | 9 | 8
        addSeatButton(row4, 10)
        addSeatButton(row4, 9)
        addSeatButton(row4, 8)
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

            if (seat.status == "available") {
                setBackgroundColor(android.graphics.Color.GREEN)
                setTextColor(android.graphics.Color.WHITE)
                setOnClickListener {
                    // Deselect previous seat if any
                    selectedSeat?.let { prevSeat ->
                        if (prevSeat.nomorKursi != seatNumber) {
                            setupSeatsLayout() // Refresh to show all available seats
                        }
                    }

                    selectedSeat = seat
                    setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                    Toast.makeText(this@BookingActivity, "Kursi $seatNumber dipilih", Toast.LENGTH_SHORT).show()
                }
            } else {
                setBackgroundColor(android.graphics.Color.LTGRAY)
                setTextColor(android.graphics.Color.DKGRAY)
                isEnabled = false
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
            setBackgroundColor(android.graphics.Color.LTGRAY)
            setTextColor(android.graphics.Color.BLACK)
        }
        row.addView(placeholder)
    }

    private fun disableSeatsSelection() {
        findViewById<LinearLayout>(R.id.seatRow1).removeAllViews()
        findViewById<LinearLayout>(R.id.seatRow2).removeAllViews()
        findViewById<LinearLayout>(R.id.seatRow3).removeAllViews()
        findViewById<LinearLayout>(R.id.seatRow4).removeAllViews()
    }

    private fun createBooking() {
        // Validation
        if (selectedCustomer == null) {
            Toast.makeText(this, "⚠️ Pilih pelanggan terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedTrip == null) {
            Toast.makeText(this, "⚠️ Pilih perjalanan terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedSeat == null) {
            Toast.makeText(this, "⚠️ Pilih kursi terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Create booking
        GlobalScope.launch(Dispatchers.Main) {
            db.seatDao().update(selectedSeat!!.copy(
                customerId = selectedCustomer!!.id,
                status = "booked"
            ))

            val ticket = Ticket(
                seatId = selectedSeat!!.id,
                tripId = selectedTrip!!.id,
                customerId = selectedCustomer!!.id,
                ongkos = selectedTrip!!.ongkos,
                status = "pending"
            )
            val ticketId = db.ticketDao().insert(ticket).toInt()

            Toast.makeText(
                this@BookingActivity,
                "✓ Booking berhasil dibuat!",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this@BookingActivity, TicketPrintActivity::class.java)
            intent.putExtra("ticketId", ticketId)
            startActivity(intent)
            finish()
        }
    }
}