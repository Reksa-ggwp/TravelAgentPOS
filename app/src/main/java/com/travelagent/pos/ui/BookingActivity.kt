package com.travelagent.pos.ui

import android.app.DatePickerDialog
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

        btnSelectCustomer.setOnClickListener { selectCustomer() }
        btnSelectTrip.setOnClickListener { selectTrip() }
        btnSelectSeat.setOnClickListener { selectSeat() }
        btnBooking.setOnClickListener { createBooking() }
        btnBack.setOnClickListener { finish() }
    }

    private fun selectCustomer() {
        GlobalScope.launch(Dispatchers.Main) {
            val customers = db.customerDao().getAllCustomers()
            val names = customers.map { it.namaLengkap }.toTypedArray()

            AlertDialog.Builder(this@BookingActivity)
                .setTitle("Pilih Pelanggan")
                .setItems(names) { _, which ->
                    selectedCustomer = customers[which]
                    findViewById<Button>(R.id.btnSelectCustomer).text = "✓ ${selectedCustomer?.namaLengkap}"
                }
                .setNegativeButton("Batal") { d, _ -> d.dismiss() }
                .show()
        }
    }

    private fun selectTrip() {
        GlobalScope.launch(Dispatchers.Main) {
            val trips = db.tripDao().getAllTrips()
            val routes = trips.map { "${it.asal} → ${it.tujuan}" }.toTypedArray()

            AlertDialog.Builder(this@BookingActivity)
                .setTitle("Pilih Perjalanan")
                .setItems(routes) { _, which ->
                    selectedTrip = trips[which]
                    selectedSeat = null
                    findViewById<Button>(R.id.btnSelectTrip).text = "✓ ${routes[which]}"
                }
                .setNegativeButton("Batal") { d, _ -> d.dismiss() }
                .show()
        }
    }

    private fun selectSeat() {
        if (selectedTrip == null) {
            Toast.makeText(this, "Pilih perjalanan terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        GlobalScope.launch(Dispatchers.Main) {
            val seats = db.seatDao().getSeatsByTrip(selectedTrip!!.id)
            val availableSeats = seats.filter { it.status == "available" }
            val seatNumbers = availableSeats.map { "Kursi ${it.nomorKursi}" }.toTypedArray()

            AlertDialog.Builder(this@BookingActivity)
                .setTitle("Pilih Kursi")
                .setItems(seatNumbers) { _, which ->
                    selectedSeat = availableSeats[which]
                    findViewById<Button>(R.id.btnSelectSeat).text = "✓ Kursi ${selectedSeat?.nomorKursi}"
                }
                .setNegativeButton("Batal") { d, _ -> d.dismiss() }
                .show()
        }
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