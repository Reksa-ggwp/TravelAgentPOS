// TripDetailsActivity.kt - COMPLETE VERSION
// Location: app/src/main/java/com/travelagent/pos/ui/TripDetailsActivity.kt
// REPLACE ENTIRE FILE

package com.travelagent.pos.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
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
    private val seatButtons = mutableMapOf<Int, Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details)

        db = AppDatabase.getDatabase(this)
        tripId = intent.getIntExtra("tripId", 0)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnInformDriver = findViewById<Button>(R.id.btnInformDriver)

        btnBack.setOnClickListener { finish() }
        btnInformDriver.setOnClickListener { sendToDriver() }

        loadTripDetails()
    }

    private fun loadTripDetails() {
        GlobalScope.launch(Dispatchers.Main) {
            trip = db.tripDao().getTripById(tripId)!!
            seats = db.seatDao().getSeatsByTrip(tripId).toMutableList()

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            val tvTripInfo = findViewById<TextView>(R.id.tvTripInfo)
            tvTripInfo.text = """
                Asal & Tujuan: ${trip.asal} → ${trip.tujuan}
                Tanggal Keberangkatan: ${sdf.format(Date(trip.tanggal))}
                Nama Sopir: ${trip.namaSopir}
                Nomor Polisi Kendaraan: ${trip.nomorPolisi}
            """.trimIndent()

            // Setup seats with image orientation
            setupSeatsLayout()
        }
    }

    private fun setupSeatsLayout() {
        // Clear existing views
        seatButtons.clear()

        // Get layouts for each row
        val row1 = findViewById<LinearLayout>(R.id.rowSeat1)
        val row2 = findViewById<LinearLayout>(R.id.rowSeat2Pintu)
        val row3 = findViewById<LinearLayout>(R.id.rowSeat3)
        val row4 = findViewById<LinearLayout>(R.id.rowSeat4)

        row1.removeAllViews()
        row2.removeAllViews()
        row3.removeAllViews()
        row4.removeAllViews()

        // Row 1: Seat 1, Empty space, SUPIR icon
        addSeatButton(row1, 1)
        addSpacerView(row1) // Empty space
        addDriverIcon(row1)

        // Row 2: PINTU label, Seat 4, 3, 2
        addLabel(row2, "PINTU")
        addSeatButton(row2, 4)
        addSeatButton(row2, 3)
        addSeatButton(row2, 2)

        // Row 3: JALAN label, Seat 7, 6, 5
        addLabel(row3, "JALAN")
        addSeatButton(row3, 7)
        addSeatButton(row3, 6)
        addSeatButton(row3, 5)

        // Row 4: BAGASI label, Seat 10, 9, 8
        addLabel(row4, "BAGASI")
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
            textSize = 18f
            updateSeatButtonColor(this, seat)
            setOnClickListener { showSeatOptions(seat) }
        }

        seatButtons[seatNumber] = btn
        row.addView(btn)
    }

    private fun addSpacerView(row: LinearLayout) {
        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 150).apply {
                weight = 1f
            }
        }
        row.addView(spacer)
    }

    private fun addDriverIcon(row: LinearLayout) {
        val driverIcon = TextView(this).apply {
            text = "SUPIR"
            layoutParams = LinearLayout.LayoutParams(0, 150).apply {
                weight = 1f
                setMargins(4, 4, 4, 4)
            }
            gravity = android.view.Gravity.CENTER
            textSize = 14f
            setBackgroundColor(android.graphics.Color.LTGRAY)
            setTextColor(android.graphics.Color.BLACK)
        }
        row.addView(driverIcon)
    }

    private fun addLabel(row: LinearLayout, labelText: String) {
        val label = TextView(this).apply {
            text = labelText
            layoutParams = LinearLayout.LayoutParams(120, 150).apply {
                setMargins(4, 4, 4, 4)
            }
            gravity = android.view.Gravity.CENTER
            textSize = 12f
            setBackgroundColor(android.graphics.Color.DKGRAY)
            setTextColor(android.graphics.Color.WHITE)
            rotation = 0f
        }
        row.addView(label)
    }

    private fun updateSeatButtonColor(btn: Button, seat: Seat) {
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
                            "Kursi ${seat.nomorKursi} ditempati ${selectedCustomer.namaLengkap}",
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
            .setTitle("Pilih Status Kursi ${seat.nomorKursi}")
            .setItems(statusLabels) { _, statusIdx ->
                GlobalScope.launch(Dispatchers.Main) {
                    db.seatDao().update(seat.copy(status = statusOptions[statusIdx]))
                    Toast.makeText(
                        this@TripDetailsActivity,
                        "Status kursi ${seat.nomorKursi} diubah menjadi ${statusLabels[statusIdx]}",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadTripDetails()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun clearSeat(seat: Seat) {
        AlertDialog.Builder(this)
            .setTitle("Kosongkan Kursi ${seat.nomorKursi}?")
            .setMessage("Yakin ingin mengosongkan kursi ini?")
            .setPositiveButton("Ya") { _, _ ->
                GlobalScope.launch(Dispatchers.Main) {
                    db.seatDao().update(seat.copy(customerId = null, status = "available"))
                    Toast.makeText(
                        this@TripDetailsActivity,
                        "Kursi ${seat.nomorKursi} telah dikosongkan",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadTripDetails()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
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

            val message = buildDriverMessage(bookedSeats)

            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("https://wa.me/${trip.nomorTeleponSopir.replace("+", "").replace("-", "").replace(" ", "")}?text=${Uri.encode(message)}")
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(
                    this@TripDetailsActivity,
                    "WhatsApp tidak terinstall",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private suspend fun buildDriverMessage(bookedSeats: List<Seat>): String {
        val passengers = mutableListOf<String>()

        bookedSeats.forEach { seat ->
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
Total Penumpang: ${bookedSeats.size}
        """.trimIndent()
    }

    override fun onResume() {
        super.onResume()
        loadTripDetails()
    }
}