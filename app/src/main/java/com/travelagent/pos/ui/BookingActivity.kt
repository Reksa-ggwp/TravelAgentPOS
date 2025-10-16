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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        db = AppDatabase.getDatabase(this)

        val btnSelectCustomer = findViewById<Button>(R.id.btnSelectCustomer)
        val btnSelectTrip = findViewById<Button>(R.id.btnSelectTrip)
        val btnSelectSeat = findViewById<Button>(R.id.btnSelectSeat)
        val btnBooking = findViewById<Button>(R.id.btnBooking)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnSelectCustomer.setOnClickListener { selectCustomerWithSearch() }
        btnSelectTrip.setOnClickListener { selectTripImproved() }
        btnSelectSeat.setOnClickListener { selectSeatImproved() }
        btnBooking.setOnClickListener { createBooking() }
        btnBack.setOnClickListener { finish() }
    }

    private fun selectCustomerWithSearch() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_search_customer, null)
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearchCustomer)
        val lvCustomers = dialogView.findViewById<ListView>(R.id.lvCustomers)

        GlobalScope.launch(Dispatchers.Main) {
            val customers = db.customerDao().getAllCustomers().toMutableList()
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
                val filteredCustomers = customers.filter { customer ->
                    customerNames.contains(customer.namaLengkap)
                }
                selectedCustomer = filteredCustomers.getOrNull(position)
                findViewById<Button>(R.id.btnSelectCustomer).text = "✓ ${selectedCustomer?.namaLengkap}"
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun selectTripImproved() {
        GlobalScope.launch(Dispatchers.Main) {
            val trips = db.tripDao().getAllTrips()

            if (trips.isEmpty()) {
                Toast.makeText(this@BookingActivity, "Belum ada perjalanan", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val tripDisplay = mutableListOf<String>()
            trips.forEach { trip ->
                tripDisplay.add("${trip.nomorPolisi} | ${trip.asal} → ${trip.tujuan}")
            }

            AlertDialog.Builder(this@BookingActivity)
                .setTitle("Pilih Perjalanan")
                .setItems(tripDisplay.toTypedArray()) { _, which ->
                    selectedTrip = trips[which]
                    selectedSeat = null // Reset seat selection
                    findViewById<Button>(R.id.btnSelectTrip).text = "✓ ${tripDisplay[which]}"
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun selectSeatImproved() {
        if (selectedTrip == null) {
            Toast.makeText(this, "Pilih perjalanan terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        GlobalScope.launch(Dispatchers.Main) {
            val seats = db.seatDao().getSeatsByTrip(selectedTrip!!.id)
            val availableSeats = seats.filter { it.status == "available" }

            if (availableSeats.isEmpty()) {
                Toast.makeText(this@BookingActivity, "Tidak ada kursi tersedia", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Create visual seat selector
            showVisualSeatSelector(availableSeats, seats)
        }
    }

    private fun showVisualSeatSelector(availableSeats: List<Seat>, allSeats: List<Seat>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_seat_selector, null)

        val row1 = dialogView.findViewById<LinearLayout>(R.id.selectorRow1)
        val row2 = dialogView.findViewById<LinearLayout>(R.id.selectorRow2)
        val row3 = dialogView.findViewById<LinearLayout>(R.id.selectorRow3)
        val row4 = dialogView.findViewById<LinearLayout>(R.id.selectorRow4)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Pilih Kursi - ${selectedTrip!!.nomorPolisi}")
            .setView(dialogView)
            .setNegativeButton("Batal", null)
            .create()

        // Add seats to rows matching the layout
        addSeatToRow(row1, allSeats.find { it.nomorKursi == 1 }!!, availableSeats, dialog)

        listOf(4, 3, 2).forEach { num ->
            addSeatToRow(row2, allSeats.find { it.nomorKursi == num }!!, availableSeats, dialog)
        }

        listOf(7, 6, 5).forEach { num ->
            addSeatToRow(row3, allSeats.find { it.nomorKursi == num }!!, availableSeats, dialog)
        }

        listOf(10, 9, 8).forEach { num ->
            addSeatToRow(row4, allSeats.find { it.nomorKursi == num }!!, availableSeats, dialog)
        }

        dialog.show()
    }

    private fun addSeatToRow(row: LinearLayout, seat: Seat, availableSeats: List<Seat>, dialog: AlertDialog) {
        val btn = Button(this).apply {
            text = seat.nomorKursi.toString()
            layoutParams = LinearLayout.LayoutParams(0, 150).apply {
                weight = 1f
                setMargins(4, 4, 4, 4)
            }
            textSize = 18f

            if (availableSeats.contains(seat)) {
                setBackgroundColor(android.graphics.Color.GREEN)
                setTextColor(android.graphics.Color.WHITE)
                setOnClickListener {
                    selectedSeat = seat
                    findViewById<Button>(R.id.btnSelectSeat).text = "✓ Kursi ${seat.nomorKursi}"
                    dialog.dismiss()
                }
            } else {
                setBackgroundColor(android.graphics.Color.LTGRAY)
                setTextColor(android.graphics.Color.DKGRAY)
                isEnabled = false
            }
        }
        row.addView(btn)
    }

    private fun createBooking() {
        if (selectedCustomer == null || selectedTrip == null || selectedSeat == null) {
            Toast.makeText(this, "Pilih pelanggan, perjalanan, dan kursi", Toast.LENGTH_SHORT).show()
            return
        }

        GlobalScope.launch(Dispatchers.Main) {
            db.seatDao().update(selectedSeat!!.copy(customerId = selectedCustomer!!.id, status = "booked"))

            val ticket = Ticket(
                seatId = selectedSeat!!.id,
                tripId = selectedTrip!!.id,
                customerId = selectedCustomer!!.id,
                ongkos = selectedTrip!!.ongkos,
                status = "pending"
            )
            val ticketId = db.ticketDao().insert(ticket).toInt()

            val intent = Intent(this@BookingActivity, TicketPrintActivity::class.java)
            intent.putExtra("ticketId", ticketId)
            startActivity(intent)
            finish()
        }
    }
}