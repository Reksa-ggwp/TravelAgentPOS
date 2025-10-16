package com.travelagent.pos.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.gridlayout.widget.GridLayout
import android.widget.*
import com.travelagent.pos.R
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Seat
import com.travelagent.pos.data.Trip
import com.travelagent.pos.data.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TripDetailsActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private var tripId: Int = 0
    private lateinit var trip: Trip
    private var seats = mutableListOf<Seat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details)

        db = AppDatabase.getDatabase(this)
        tripId = intent.getIntExtra("tripId", 0)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnInformDriver = findViewById<Button>(R.id.btnInformDriver)
        val gridLayout = findViewById<GridLayout>(R.id.gridSeats)

        btnBack.setOnClickListener { finish() }
        btnInformDriver.setOnClickListener { sendToDriver() }

        loadTripDetails(gridLayout)
    }

    private fun loadTripDetails(gridLayout: GridLayout) {
        GlobalScope.launch(Dispatchers.Main) {
            trip = db.tripDao().getTripById(tripId)!!
            seats = db.seatDao().getSeatsByTrip(tripId).toMutableList()

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            findViewById<TextView>(R.id.tvTripInfo).text =
                "${trip.asal} → ${trip.tujuan}\n${sdf.format(Date(trip.tanggal))}\n${trip.namaSopir}\n${trip.nomorPolisi}"

            gridLayout.removeAllViews()
            seats.forEach { seat ->
                val btn = Button(this@TripDetailsActivity).apply {
                    text = seat.nomorKursi.toString()
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = 120
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        setMargins(8, 8, 8, 8)
                    }
                    updateSeatButton(this, seat)
                    setOnClickListener { manageSeat(seat) }
                }
                gridLayout.addView(btn)
            }
        }
    }

    private fun updateSeatButton(btn: Button, seat: Seat) {
        when (seat.status) {
            "available" -> {
                btn.setBackgroundColor(android.graphics.Color.GREEN)
                btn.setTextColor(android.graphics.Color.WHITE)
            }
            "booked" -> {
                btn.setBackgroundColor(android.graphics.Color.parseColor("#FFA500"))
                btn.setTextColor(android.graphics.Color.WHITE)
            }
            "paid" -> {
                btn.setBackgroundColor(android.graphics.Color.RED)
                btn.setTextColor(android.graphics.Color.WHITE)
            }
        }
    }

    private fun manageSeat(seat: Seat) {
        val options = arrayOf("Hapus", "Ubah Status")
        AlertDialog.Builder(this)
            .setTitle("Kursi ${seat.nomorKursi}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        GlobalScope.launch(Dispatchers.Main) {
                            db.seatDao().update(seat.copy(customerId = null, status = "available"))
                            loadTripDetails(findViewById(R.id.gridSeats))
                        }
                    }
                    1 -> {
                        val statusOptions = arrayOf("available", "booked", "paid")
                        AlertDialog.Builder(this)
                            .setTitle("Pilih Status Kursi ${seat.nomorKursi}")
                            .setItems(statusOptions) { _, statusIdx ->
                                GlobalScope.launch(Dispatchers.Main) {
                                    db.seatDao().update(seat.copy(status = statusOptions[statusIdx]))
                                    loadTripDetails(findViewById(R.id.gridSeats))
                                }
                            }.show()
                    }
                }
            }.show()
    }

    private fun sendToDriver() {
        GlobalScope.launch(Dispatchers.Main) {
            val bookedSeats = seats.filter { it.status in listOf("booked", "paid") && it.customerId != null }

            if (bookedSeats.isEmpty()) {
                Toast.makeText(this@TripDetailsActivity, "Tidak ada penumpang", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val message = buildDriverMessage(bookedSeats)

            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("https://wa.me/${trip.nomorTeleponSopir.replace("+", "").replace("-", "")}?text=${Uri.encode(message)}")
            }
            startActivity(intent)
        }
    }

    private suspend fun buildDriverMessage(bookedSeats: List<Seat>): String {
        val passengers = mutableListOf<String>()

        bookedSeats.sortedBy { it.nomorKursi }.forEach { seat ->
            val customer = seat.customerId?.let { db.customerDao().getCustomerById(it) }
            if (customer != null) {
                passengers.add("Kursi ${seat.nomorKursi}: ${customer.namaLengkap} - ${customer.nomorTelepon} - ${customer.alamat}")
            }
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        return """
Sopir ${trip.namaSopir},

Berikut data penumpang perjalanan ${trip.asal} → ${trip.tujuan} (${sdf.format(Date(trip.tanggal))}):

${passengers.joinToString("\n")}

Nomor Polisi: ${trip.nomorPolisi}
        """.trimIndent()
    }

    override fun onResume() {
        super.onResume()
        loadTripDetails(findViewById(R.id.gridSeats))
    }
}