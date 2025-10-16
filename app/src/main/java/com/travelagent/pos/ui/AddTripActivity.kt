package com.travelagent.pos.ui

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import com.travelagent.pos.R
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Trip
import com.travelagent.pos.data.Driver
import com.travelagent.pos.data.Vehicle
import com.travelagent.pos.data.Seat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class AddTripActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private var selectedDate: Long = 0
    private var selectedDriver: Driver? = null
    private var selectedVehicle: Vehicle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_trip)

        db = AppDatabase.getDatabase(this)

        val spinnerAsal = findViewById<Spinner>(R.id.spinnerAsal)
        val spinnerTujuan = findViewById<Spinner>(R.id.spinnerTujuan)
        val etDate = findViewById<EditText>(R.id.etDate)
        val spinnerDriver = findViewById<Spinner>(R.id.spinnerDriver)
        val etDriverPhone = findViewById<EditText>(R.id.etDriverPhone)
        val spinnerPlate = findViewById<Spinner>(R.id.spinnerPlate)
        val etPrice = findViewById<EditText>(R.id.etPrice)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // Setup City Spinners (Asal & Tujuan)
        val cities = arrayOf("Sibolga", "Medan")
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAsal.adapter = cityAdapter
        spinnerTujuan.adapter = cityAdapter

        // Load Drivers and Vehicles from database
        GlobalScope.launch(Dispatchers.Main) {
            val drivers = db.driverDao().getAllDrivers()
            val vehicles = db.vehicleDao().getAllVehicles()

            if (drivers.isEmpty()) {
                Toast.makeText(
                    this@AddTripActivity,
                    "Tambahkan sopir terlebih dahulu di menu Data Sopir & Kendaraan",
                    Toast.LENGTH_LONG
                ).show()
            }

            if (vehicles.isEmpty()) {
                Toast.makeText(
                    this@AddTripActivity,
                    "Tambahkan kendaraan terlebih dahulu di menu Data Sopir & Kendaraan",
                    Toast.LENGTH_LONG
                ).show()
            }

            // Setup Driver Spinner
            val driverNames = drivers.map { it.namaSopir }
            val driverAdapter = ArrayAdapter(
                this@AddTripActivity,
                android.R.layout.simple_spinner_item,
                driverNames
            )
            driverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDriver.adapter = driverAdapter

            spinnerDriver.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    if (drivers.isNotEmpty()) {
                        selectedDriver = drivers[position]
                        etDriverPhone.setText(selectedDriver?.nomorTelepon)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            // Setup Vehicle Spinner
            val plateNumbers = vehicles.map { it.nomorPolisi }
            val vehicleAdapter = ArrayAdapter(
                this@AddTripActivity,
                android.R.layout.simple_spinner_item,
                plateNumbers
            )
            vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPlate.adapter = vehicleAdapter

            spinnerPlate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    if (vehicles.isNotEmpty()) {
                        selectedVehicle = vehicles[position]
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        // Date Picker
        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    cal.set(year, month, day)
                    selectedDate = cal.timeInMillis
                    etDate.setText("$day/${month + 1}/$year")
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Save Button
        btnSave.setOnClickListener {
            val asal = spinnerAsal.selectedItem.toString()
            val tujuan = spinnerTujuan.selectedItem.toString()
            val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0

            if (selectedDriver == null || selectedVehicle == null || selectedDate == 0L) {
                Toast.makeText(this, "Lengkapi semua field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (price <= 0) {
                Toast.makeText(this, "Masukkan harga yang valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            GlobalScope.launch(Dispatchers.Main) {
                val trip = Trip(
                    asal = asal,
                    tujuan = tujuan,
                    tanggal = selectedDate,
                    namaSopir = selectedDriver!!.namaSopir,
                    nomorTeleponSopir = selectedDriver!!.nomorTelepon,
                    nomorPolisi = selectedVehicle!!.nomorPolisi,
                    ongkos = price
                )
                val tripId = db.tripDao().insert(trip).toInt()

                // Create 10 seats automatically
                repeat(10) { i ->
                    db.seatDao().insert(
                        Seat(
                            tripId = tripId,
                            nomorKursi = i + 1,
                            customerId = null,
                            status = "available"
                        )
                    )
                }

                Toast.makeText(
                    this@AddTripActivity,
                    "Perjalanan berhasil ditambahkan",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

        btnBack.setOnClickListener { finish() }
    }
}