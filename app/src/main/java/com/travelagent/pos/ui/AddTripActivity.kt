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
    private var tripId: Int? = null // For edit mode
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_trip)

        db = AppDatabase.getDatabase(this)

        // Check if this is edit mode
        tripId = intent.getIntExtra("tripId", -1).takeIf { it != -1 }
        isEditMode = tripId != null

        val spinnerAsal = findViewById<Spinner>(R.id.spinnerAsal)
        val spinnerTujuan = findViewById<Spinner>(R.id.spinnerTujuan)
        val etDate = findViewById<EditText>(R.id.etDate)
        val spinnerDriver = findViewById<Spinner>(R.id.spinnerDriver)
        val etDriverPhone = findViewById<EditText>(R.id.etDriverPhone)
        val spinnerPlate = findViewById<Spinner>(R.id.spinnerPlate)
        val etPrice = findViewById<EditText>(R.id.etPrice)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // Update title if edit mode
        if (isEditMode) {
            supportActionBar?.title = "Edit Perjalanan"
            btnSave.text = "💾 Update Perjalanan"
        }

        // Setup City Spinners
        val cities = arrayOf("Sibolga", "Medan")
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAsal.adapter = cityAdapter
        spinnerTujuan.adapter = cityAdapter

        // Load Drivers and Vehicles
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

            // Load existing trip data if edit mode
            if (isEditMode) {
                loadTripData(spinnerAsal, spinnerTujuan, etDate, etPrice, drivers, vehicles,
                    spinnerDriver, spinnerPlate)
            }
        }

        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            if (selectedDate > 0) cal.timeInMillis = selectedDate

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

            if (isEditMode) {
                updateTrip(asal, tujuan, price)
            } else {
                createTrip(asal, tujuan, price)
            }
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun loadTripData(
        spinnerAsal: Spinner,
        spinnerTujuan: Spinner,
        etDate: EditText,
        etPrice: EditText,
        drivers: List<Driver>,
        vehicles: List<Vehicle>,
        spinnerDriver: Spinner,
        spinnerPlate: Spinner
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val trip = db.tripDao().getTripById(tripId!!) ?: return@launch

            selectedDate = trip.tanggal

            // Set asal
            val cities = arrayOf("Sibolga", "Medan")
            spinnerAsal.setSelection(cities.indexOf(trip.asal))
            spinnerTujuan.setSelection(cities.indexOf(trip.tujuan))

            // Set date
            val cal = Calendar.getInstance()
            cal.timeInMillis = trip.tanggal
            etDate.setText("${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}")

            // Set driver
            val driverIndex = drivers.indexOfFirst { it.namaSopir == trip.namaSopir }
            if (driverIndex >= 0) {
                spinnerDriver.setSelection(driverIndex)
            }

            // Set vehicle
            val vehicleIndex = vehicles.indexOfFirst { it.nomorPolisi == trip.nomorPolisi }
            if (vehicleIndex >= 0) {
                spinnerPlate.setSelection(vehicleIndex)
            }

            // Set price
            etPrice.setText(trip.ongkos.toInt().toString())
        }
    }

    private fun createTrip(asal: String, tujuan: String, price: Double) {
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
            val newTripId = db.tripDao().insert(trip).toInt()

            // Create 10 seats automatically
            repeat(10) { i ->
                db.seatDao().insert(
                    Seat(
                        tripId = newTripId,
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

    private fun updateTrip(asal: String, tujuan: String, price: Double) {
        GlobalScope.launch(Dispatchers.Main) {
            val trip = Trip(
                id = tripId!!,
                asal = asal,
                tujuan = tujuan,
                tanggal = selectedDate,
                namaSopir = selectedDriver!!.namaSopir,
                nomorTeleponSopir = selectedDriver!!.nomorTelepon,
                nomorPolisi = selectedVehicle!!.nomorPolisi,
                ongkos = price
            )
            db.tripDao().update(trip)

            Toast.makeText(
                this@AddTripActivity,
                "✓ Perjalanan berhasil diupdate",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}