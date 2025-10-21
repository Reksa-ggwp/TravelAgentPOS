// ========================================= //
// Sample Data Generator
// Add this as a temporary Activity to generate test data
// ========================================= //
// Location: app/src/main/java/com/travelagent/pos/ui/GenerateSampleDataActivity.kt
// CREATE NEW FILE (temporary, for testing only)

package com.travelagent.pos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import com.travelagent.pos.R
import com.travelagent.pos.data.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar

class GenerateSampleDataActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create simple layout programmatically
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        val btnGenerate = Button(this).apply {
            text = "Generate Sample Data"
            textSize = 18f
            setOnClickListener { generateSampleData() }
        }

        layout.addView(btnGenerate)
        setContentView(layout)

        db = AppDatabase.getDatabase(this)
    }

    private fun generateSampleData() {
        lifecycleScope.launch {
            try {
                // 1. Create Drivers
                val driver1 = Driver(namaSopir = "Ahmad Surya", nomorTelepon = "081234567890")
                val driver2 = Driver(namaSopir = "Budi Santoso", nomorTelepon = "081298765432")
                val driver3 = Driver(namaSopir = "Citra Dewi", nomorTelepon = "082156789012")

                val driverId1 = db.driverDao().insert(driver1).toInt()
                val driverId2 = db.driverDao().insert(driver2).toInt()
                val driverId3 = db.driverDao().insert(driver3).toInt()

                // 2. Create Vehicles
                val vehicle1 = Vehicle(nomorPolisi = "B 1234 XX")
                val vehicle2 = Vehicle(nomorPolisi = "B 5678 YY")
                val vehicle3 = Vehicle(nomorPolisi = "B 9012 ZZ")

                db.vehicleDao().insert(vehicle1)
                db.vehicleDao().insert(vehicle2)
                db.vehicleDao().insert(vehicle3)

                // 3. Create Customers
                val customers = listOf(
                    Customer(namaLengkap = "Andi Wijaya", nomorTelepon = "081111111111", alamat = "Jl. Merdeka No. 10, Sibolga"),
                    Customer(namaLengkap = "Siti Nurhaliza", nomorTelepon = "081222222222", alamat = "Jl. Sudirman No. 25, Medan"),
                    Customer(namaLengkap = "Budi Hartono", nomorTelepon = "081333333333", alamat = "Jl. Gatot Subroto No. 15, Sibolga"),
                    Customer(namaLengkap = "Dewi Lestari", nomorTelepon = "081444444444", alamat = "Jl. Ahmad Yani No. 8, Medan"),
                    Customer(namaLengkap = "Rudi Santoso", nomorTelepon = "081555555555", alamat = "Jl. Pahlawan No. 20, Sibolga"),
                    Customer(namaLengkap = "Maya Sari", nomorTelepon = "081666666666", alamat = "Jl. Kartini No. 12, Medan"),
                    Customer(namaLengkap = "Agus Salim", nomorTelepon = "081777777777", alamat = "Jl. Diponegoro No. 30, Sibolga"),
                    Customer(namaLengkap = "Lina Marlina", nomorTelepon = "081888888888", alamat = "Jl. Imam Bonjol No. 5, Medan"),
                    Customer(namaLengkap = "Hendra Gunawan", nomorTelepon = "081999999999", alamat = "Jl. Veteran No. 18, Sibolga"),
                    Customer(namaLengkap = "Nina Sari", nomorTelepon = "082000000000", alamat = "Jl. MT Haryono No. 22, Medan")
                )

                val customerIds = mutableListOf<Int>()
                customers.forEach { customer ->
                    customerIds.add(db.customerDao().insert(customer).toInt())
                }

                // 4. Create Trips (3 trips with different dates)
                val cal = Calendar.getInstance()

                // Trip 1: Today
                val trip1 = Trip(
                    asal = "Sibolga",
                    tujuan = "Medan",
                    tanggal = cal.timeInMillis,
                    namaSopir = "Ahmad Surya",
                    nomorTeleponSopir = "081234567890",
                    nomorPolisi = "B 1234 XX",
                    ongkos = 150000.0
                )
                val trip1Id = db.tripDao().insert(trip1).toInt()

                // Create 10 seats for trip 1
                repeat(10) { i ->
                    db.seatDao().insert(Seat(
                        tripId = trip1Id,
                        nomorKursi = i + 1,
                        customerId = null,
                        status = "available"
                    ))
                }

                // Assign some customers to trip 1
                val seats1 = db.seatDao().getSeatsByTrip(trip1Id)
                db.seatDao().update(seats1[0].copy(customerId = customerIds[0], status = "paid"))
                db.seatDao().update(seats1[1].copy(customerId = customerIds[1], status = "paid"))
                db.seatDao().update(seats1[4].copy(customerId = customerIds[2], status = "booked"))
                db.seatDao().update(seats1[6].copy(customerId = customerIds[3], status = "paid"))

                // Trip 2: Tomorrow
                cal.add(Calendar.DAY_OF_MONTH, 1)
                val trip2 = Trip(
                    asal = "Medan",
                    tujuan = "Sibolga",
                    tanggal = cal.timeInMillis,
                    namaSopir = "Budi Santoso",
                    nomorTeleponSopir = "081298765432",
                    nomorPolisi = "B 5678 YY",
                    ongkos = 150000.0
                )
                val trip2Id = db.tripDao().insert(trip2).toInt()

                repeat(10) { i ->
                    db.seatDao().insert(Seat(
                        tripId = trip2Id,
                        nomorKursi = i + 1,
                        customerId = null,
                        status = "available"
                    ))
                }

                val seats2 = db.seatDao().getSeatsByTrip(trip2Id)
                db.seatDao().update(seats2[2].copy(customerId = customerIds[4], status = "booked"))
                db.seatDao().update(seats2[5].copy(customerId = customerIds[5], status = "paid"))

                // Trip 3: Day after tomorrow
                cal.add(Calendar.DAY_OF_MONTH, 1)
                val trip3 = Trip(
                    asal = "Sibolga",
                    tujuan = "Medan",
                    tanggal = cal.timeInMillis,
                    namaSopir = "Citra Dewi",
                    nomorTeleponSopir = "082156789012",
                    nomorPolisi = "B 9012 ZZ",
                    ongkos = 175000.0
                )
                val trip3Id = db.tripDao().insert(trip3).toInt()

                repeat(10) { i ->
                    db.seatDao().insert(Seat(
                        tripId = trip3Id,
                        nomorKursi = i + 1,
                        customerId = null,
                        status = "available"
                    ))
                }

                // Create tickets for booked seats
                val ticket1 = Ticket(
                    seatId = seats1[0].id,
                    tripId = trip1Id,
                    customerId = customerIds[0],
                    ongkos = 150000.0,
                    status = "paid",
                    isStamped = true
                )
                db.ticketDao().insert(ticket1)

                Toast.makeText(
                    this@GenerateSampleDataActivity,
                    "✓ Sample data berhasil dibuat!\n" +
                            "- 3 Drivers\n" +
                            "- 3 Vehicles\n" +
                            "- 10 Customers\n" +
                            "- 3 Trips dengan beberapa booking\n\n" +
                            "Silakan backup sekarang!",
                    Toast.LENGTH_LONG
                ).show()

                finish()

            } catch (e: Exception) {
                Toast.makeText(
                    this@GenerateSampleDataActivity,
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }
}

// ========================================= //
// HOW TO USE:
// ========================================= //
/*
1. Add this activity to AndroidManifest.xml:
   <activity android:name=".ui.GenerateSampleDataActivity" android:exported="true" />

2. Run app, then use adb to open this activity:
   adb shell am start -n com.travelagent.pos/.ui.GenerateSampleDataActivity

3. Tap "Generate Sample Data" button

4. Go to Backup & Restore → Create Backup

5. You now have a backup file with realistic test data!

6. Share the backup file with yourself via email/drive

7. For future testing:
   - Install fresh app
   - Copy backup file to device
   - Use Restore function

8. DELETE GenerateSampleDataActivity.kt when done testing
*/