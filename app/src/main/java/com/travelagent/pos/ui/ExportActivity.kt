package com.travelagent.pos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import com.travelagent.pos.R
import com.travelagent.pos.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class ExportActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)

        db = AppDatabase.getDatabase(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnExportCustomers).setOnClickListener { exportCustomers() }
        findViewById<Button>(R.id.btnExportTrips).setOnClickListener { exportTrips() }
        findViewById<Button>(R.id.btnExportBookings).setOnClickListener { exportBookings() }
    }

    private fun exportCustomers() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val customers = db.customerDao().getAllCustomers()

                if (customers.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ExportActivity, "Tidak ada data pelanggan", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val csv = StringBuilder()
                // Header
                csv.append("No,Nama Lengkap,Nomor Telepon,Alamat\n")

                // Data
                customers.forEachIndexed { index, customer ->
                    csv.append("${index + 1},")
                    csv.append("\"${customer.namaLengkap}\",")
                    csv.append("\"${customer.nomorTelepon}\",")
                    csv.append("\"${customer.alamat}\"\n")
                }

                saveCSV(csv.toString(), "DataPelanggan")

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ExportActivity,
                        "❌ Export gagal: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                e.printStackTrace()
            }
        }
    }

    private fun exportTrips() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val trips = db.tripDao().getAllTrips()

                if (trips.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ExportActivity, "Tidak ada data perjalanan", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val csv = StringBuilder()
                // Header
                csv.append("No,Nopol,Asal,Tujuan,Tanggal,Sopir,No. Telp Sopir,Ongkos\n")

                // Data
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                trips.forEachIndexed { index, trip ->
                    csv.append("${index + 1},")
                    csv.append("\"${trip.nomorPolisi}\",")
                    csv.append("\"${trip.asal}\",")
                    csv.append("\"${trip.tujuan}\",")
                    csv.append("\"${sdf.format(Date(trip.tanggal))}\",")
                    csv.append("\"${trip.namaSopir}\",")
                    csv.append("\"${trip.nomorTeleponSopir}\",")
                    csv.append("${trip.ongkos}\n")
                }

                saveCSV(csv.toString(), "DataPerjalanan")

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ExportActivity,
                        "❌ Export gagal: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                e.printStackTrace()
            }
        }
    }

    private fun exportBookings() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val tickets = db.ticketDao().getAllTickets()

                if (tickets.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ExportActivity, "Tidak ada data booking", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val csv = StringBuilder()
                // Header
                csv.append("No,Pelanggan,No. Telp,Rute,Tanggal,Kursi,Ongkos,Status\n")

                // Data
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                tickets.forEachIndexed { index, ticket ->
                    val customer = db.customerDao().getCustomerById(ticket.customerId)
                    val trip = db.tripDao().getTripById(ticket.tripId)
                    val seat = db.seatDao().getSeatById(ticket.seatId)

                    if (customer != null && trip != null && seat != null) {
                        csv.append("${index + 1},")
                        csv.append("\"${customer.namaLengkap}\",")
                        csv.append("\"${customer.nomorTelepon}\",")
                        csv.append("\"${trip.asal} → ${trip.tujuan}\",")
                        csv.append("\"${sdf.format(Date(trip.tanggal))}\",")
                        csv.append("${seat.nomorKursi},")
                        csv.append("${ticket.ongkos},")
                        csv.append("\"${ticket.status}\"\n")
                    }
                }

                saveCSV(csv.toString(), "DataBooking")

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ExportActivity,
                        "❌ Export gagal: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                e.printStackTrace()
            }
        }
    }

    private suspend fun saveCSV(content: String, fileName: String) {
        try {
            val exportDir = File(getExternalFilesDir(null), "TravelAgentExports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val timestamp = sdf.format(Date())
            val file = File(exportDir, "${fileName}_$timestamp.csv")

            FileWriter(file).use { writer ->
                writer.write(content)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@ExportActivity,
                    "✓ Export berhasil!\n\nFile: ${file.name}\n\nLokasi: ${exportDir.absolutePath}\n\nBuka dengan Excel atau Google Sheets",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@ExportActivity,
                    "❌ Gagal menyimpan file: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
            e.printStackTrace()
        }
    }
}