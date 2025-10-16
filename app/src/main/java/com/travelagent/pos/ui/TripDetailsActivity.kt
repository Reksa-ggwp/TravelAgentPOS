package com.travelagent.pos.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.widget.*
import com.travelagent.pos.R
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Seat
import com.travelagent.pos.data.Trip
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

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnInformDriver).setOnClickListener { sendToDriver() }

        loadTripDetails()
    }

    private fun loadTripDetails() {
        GlobalScope.launch(Dispatchers.Main) {
            trip = db.tripDao().getTripById(tripId)!!
            seats = db.seatDao().getSeatsByTrip(tripId).toMutableList()

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            findViewById<TextView>(R.id.tvTripInfo).text = """
                Asal & Tujuan: ${trip.asal} → ${trip.tujuan}
                Tanggal Keberangkatan: ${sdf.format(Date(trip.tanggal))}
                Nama Sopir: ${trip.namaSopir}
                Nomor Polisi Kendaraan: ${trip.nomorPolisi}
            """.trimIndent()

            setupSeatsLayout()
        }
    }

    private fun setupSeatsLayout() {
        val row1 = findViewById<LinearLayout>(R.id.rowSeat1)
        val row2 = findViewById<LinearLayout>(R.id.rowSeat2)
        val row3 = findViewById<LinearLayout>(R.id.rowSeat3)
        val row4 = findViewById<LinearLayout>(R.id.rowSeat4)

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
        val seat = seats.find { it.nomorKursi == seatNumber } ?: return

        val btn = Button(this).apply {
            text = seatNumber.toString()
            layoutParams = LinearLayout.LayoutParams(0, 150).apply {
                weight = 1f
                setMargins(4, 4, 4, 4)
            }
            textSize = 20f
            typeface = android.graphics.Typeface.DEFAULT_BOLD

            when (seat.status) {
                "available" -> {
                    setBackgroundColor(android.graphics.Color.GREEN)
                    setTextColor(android.graphics.Color.WHITE)
                }
                "booked" -> {
                    setBackgroundColor(android.graphics.Color.parseColor("#FFA500"))
                    setTextColor(android.graphics.Color.WHITE)
                }
                "paid" -> {
                    setBackgroundColor(android.graphics.Color.RED)
                    setTextColor(android.graphics.Color.WHITE)
                }
            }

            setOnClickListener { showSeatOptions(seat) }
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

    private fun showSeatOptions(seat: Seat) {
        GlobalScope.launch(Dispatchers.Main) {
            val customer = seat.customerId?.let { db.customerDao().getCustomerById(it) }
            val customerName = customer?.namaLengkap ?: "Kosong"

            val options = if (seat.customerId != null) {
                arrayOf("Ganti Penumpang", "Ubah Status", "Kosongkan Kursi")
            } else {
                arrayOf("Isi Penumpang", "Ubah Status")
            }

            AlertDialog.Builder(this@TripDetailsActivity)
                .setTitle("Kursi ${seat.nomorKursi} - $customerName")
                .setItems(options) { _, which ->
                    when {
                        options[which] == "Isi Penumpang" || options[which] == "Ganti Penumpang" ->
                            showCustomerSelectionDialog(seat)
                        options[which] == "Ubah Status" -> showStatusDialog(seat)
                        options[which] == "Kosongkan Kursi" -> clearSeat(seat)
                    }
                }
                .show()
        }
    }

    private fun showCustomerSelectionDialog(seat: Seat) {
        GlobalScope.launch(Dispatchers.Main) {
            val customers = db.customerDao().getAllCustomers()
            if (customers.isEmpty()) {
                Toast.makeText(this@TripDetailsActivity, "Belum ada pelanggan", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val customerNames = customers.map { it.namaLengkap }.toTypedArray()
            AlertDialog.Builder(this@TripDetailsActivity)
                .setTitle("Pilih Penumpang untuk Kursi ${seat.nomorKursi}")
                .setItems(customerNames) { _, which ->
                    GlobalScope.launch(Dispatchers.Main) {
                        val selectedCustomer = customers[which]
                        db.seatDao().update(seat.copy(
                            customerId = selectedCustomer.id,
                            status = "booked"
                        ))
                        Toast.makeText(
                            this@TripDetailsActivity,
                            "Kursi ${seat.nomorKursi}: ${selectedCustomer.namaLengkap}",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadTripDetails()
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun showStatusDialog(seat: Seat) {
        val statusOptions = arrayOf("available", "booked", "paid")
        val statusLabels = arrayOf("Kosong", "Booked", "Paid")

        AlertDialog.Builder(this)
            .setTitle("Status Kursi ${seat.nomorKursi}")
            .setItems(statusLabels) { _, idx ->
                GlobalScope.launch(Dispatchers.Main) {
                    db.seatDao().update(seat.copy(status = statusOptions[idx]))
                    loadTripDetails()
                }
            }
            .show()
    }

    private fun clearSeat(seat: Seat) {
        GlobalScope.launch(Dispatchers.Main) {
            db.seatDao().update(seat.copy(customerId = null, status = "available"))
            Toast.makeText(this@TripDetailsActivity, "Kursi ${seat.nomorKursi} dikosongkan", Toast.LENGTH_SHORT).show()
            loadTripDetails()
        }
    }

    private fun sendToDriver() {
        GlobalScope.launch(Dispatchers.Main) {
            val bookedSeats = seats.filter {
                it.status in listOf("booked", "paid") && it.customerId != null
            }.sortedBy { it.nomorKursi }

            if (bookedSeats.isEmpty()) {
                Toast.makeText(this@TripDetailsActivity, "Tidak ada penumpang", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val passengers = mutableListOf<String>()
            bookedSeats.forEach { seat ->
                val customer = seat.customerId?.let { db.customerDao().getCustomerById(it) }
                if (customer != null) {
                    passengers.add("Kursi ${seat.nomorKursi}: ${customer.namaLengkap} - ${customer.nomorTelepon} - ${customer.alamat}")
                }
            }

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            val message = """
Sopir ${trip.namaSopir},

Data penumpang ${trip.asal} → ${trip.tujuan} (${sdf.format(Date(trip.tanggal))}):

${passengers.joinToString("\n")}

Nomor Polisi: ${trip.nomorPolisi}
Total: ${bookedSeats.size} penumpang
            """.trimIndent()

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(
                "https://wa.me/${trip.nomorTeleponSopir.replace(Regex("[^0-9]"), "")}?text=${Uri.encode(message)}"
            ))
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadTripDetails()
    }
}