package com.travelagent.pos.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Driver
import com.travelagent.pos.data.Seat
import com.travelagent.pos.data.Trip
import com.travelagent.pos.data.Vehicle
import com.travelagent.pos.databinding.ActivityAddTripBinding
import com.travelagent.pos.utils.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddTripActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTripBinding
    private lateinit var db: AppDatabase

    private var selectedDate: Long = 0
    private var selectedDriver: Driver? = null
    private var selectedVehicle: Vehicle? = null
    private var tripId: Int? = null
    private var isEditMode = false

    private val cities = arrayOf("Sibolga", "Medan", "Padang", "Pekanbaru", "Jambi")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        // Check if edit mode
        tripId = intent.getIntExtra("tripId", -1).takeIf { it != -1 }
        isEditMode = tripId != null

        setupToolbar()
        setupCityInputs()
        setupDatePicker()
        setupDriverAndVehicleInputs()
        setupSaveButton()

        if (isEditMode) {
            loadTripData()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = if (isEditMode) "Edit Perjalanan" else "Tambah Perjalanan"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupCityInputs() {
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)

        binding.actvOrigin.setAdapter(cityAdapter)
        binding.actvDestination.setAdapter(cityAdapter)

        // Set threshold to show dropdown immediately
        binding.actvOrigin.threshold = 1
        binding.actvDestination.threshold = 1
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        binding.etDate.isFocusable = false
        binding.etDate.isClickable = true
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        if (selectedDate > 0) {
            calendar.timeInMillis = selectedDate
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate = calendar.timeInMillis

                val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                binding.etDate.setText(sdf.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
            show()
        }
    }

    private fun setupDriverAndVehicleInputs() {
        lifecycleScope.launch {
            val drivers = db.driverDao().getAllDrivers()
            val vehicles = db.vehicleDao().getAllVehicles()

            if (drivers.isEmpty() || vehicles.isEmpty()) {
                showInfoDialog()
            }

            setupDriverDropdown(drivers)
            setupVehicleDropdown(vehicles)
        }
    }

    private fun setupDriverDropdown(drivers: List<Driver>) {
        val driverNames = drivers.map { it.namaSopir }
        val driverAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            driverNames
        )

        binding.actvDriver.setAdapter(driverAdapter)
        binding.actvDriver.threshold = 1

        binding.actvDriver.setOnItemClickListener { _, _, position, _ ->
            if (drivers.isNotEmpty()) {
                selectedDriver = drivers[position]
                binding.etDriverPhone.setText(selectedDriver?.nomorTelepon)
            }
        }
    }

    private fun setupVehicleDropdown(vehicles: List<Vehicle>) {
        val plateNumbers = vehicles.map { it.nomorPolisi }
        val vehicleAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            plateNumbers
        )

        binding.actvVehicle.setAdapter(vehicleAdapter)
        binding.actvVehicle.threshold = 1

        binding.actvVehicle.setOnItemClickListener { _, _, position, _ ->
            if (vehicles.isNotEmpty()) {
                selectedVehicle = vehicles[position]
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                if (isEditMode) {
                    updateTrip()
                } else {
                    createTrip()
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        val origin = binding.actvOrigin.text.toString().trim()
        val destination = binding.actvDestination.text.toString().trim()
        val price = binding.etPrice.text.toString().toDoubleOrNull()

        when {
            origin.isEmpty() -> {
                binding.actvOrigin.error = "Pilih kota asal"
                return false
            }
            destination.isEmpty() -> {
                binding.actvDestination.error = "Pilih kota tujuan"
                return false
            }
            origin == destination -> {
                binding.actvDestination.error = "Kota tujuan harus berbeda"
                return false
            }
            selectedDate == 0L -> {
                Toast.makeText(this, "Pilih tanggal keberangkatan", Toast.LENGTH_SHORT).show()
                return false
            }
            selectedDriver == null -> {
                Toast.makeText(this, "Pilih sopir", Toast.LENGTH_SHORT).show()
                return false
            }
            selectedVehicle == null -> {
                Toast.makeText(this, "Pilih kendaraan", Toast.LENGTH_SHORT).show()
                return false
            }
            price == null || price <= 0 -> {
                binding.etPrice.error = "Masukkan harga yang valid"
                return false
            }
        }

        return true
    }

    private fun createTrip() {
        val origin = binding.actvOrigin.text.toString().trim()
        val destination = binding.actvDestination.text.toString().trim()
        val price = binding.etPrice.text.toString().toDouble()

        lifecycleScope.launch {
            try {
                val driver = selectedDriver!!
                val vehicle = selectedVehicle!!

                val trip = Trip(
                    asal = origin,
                    tujuan = destination,
                    tanggal = selectedDate,
                    namaSopir = driver.namaSopir,
                    nomorTeleponSopir = driver.nomorTelepon,
                    nomorPolisi = vehicle.nomorPolisi,
                    ongkos = price
                )

                val newTripId = db.tripDao().insert(trip).toInt()

                // Create seats automatically
                repeat(Constants.DEFAULT_SEAT_COUNT) { i ->
                    db.seatDao().insert(
                        Seat(
                            tripId = newTripId,
                            nomorKursi = i + 1,
                            customerId = null,
                            status = Constants.SEAT_STATUS_AVAILABLE
                        )
                    )
                }

                showSuccessMessage("Perjalanan berhasil ditambahkan")
                finish()

            } catch (e: Exception) {
                Toast.makeText(
                    this@AddTripActivity,
                    "❌ Gagal: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateTrip() {
        val origin = binding.actvOrigin.text.toString().trim()
        val destination = binding.actvDestination.text.toString().trim()
        val price = binding.etPrice.text.toString().toDouble()

        lifecycleScope.launch {
            try {
                val existingTripId = tripId ?: return@launch
                val driver = selectedDriver!!
                val vehicle = selectedVehicle!!

                val trip = Trip(
                    id = existingTripId,
                    asal = origin,
                    tujuan = destination,
                    tanggal = selectedDate,
                    namaSopir = driver.namaSopir,
                    nomorTeleponSopir = driver.nomorTelepon,
                    nomorPolisi = vehicle.nomorPolisi,
                    ongkos = price
                )

                db.tripDao().update(trip)

                showSuccessMessage("Perjalanan berhasil diupdate")
                finish()

            } catch (e: Exception) {
                Toast.makeText(
                    this@AddTripActivity,
                    "❌ Gagal: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadTripData() {
        lifecycleScope.launch {
            val existingTripId = tripId ?: return@launch
            val trip = db.tripDao().getTripById(existingTripId) ?: return@launch

            // Set date
            selectedDate = trip.tanggal
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            binding.etDate.setText(sdf.format(Date(trip.tanggal)))

            // Set origin and destination
            binding.actvOrigin.setText(trip.asal, false)
            binding.actvDestination.setText(trip.tujuan, false)

            // Set price
            binding.etPrice.setText(trip.ongkos.toInt().toString())

            // Load and set driver
            val drivers = db.driverDao().getAllDrivers()
            val driver = drivers.find { it.namaSopir == trip.namaSopir }
            if (driver != null) {
                selectedDriver = driver
                binding.actvDriver.setText(driver.namaSopir, false)
                binding.etDriverPhone.setText(driver.nomorTelepon)
            }

            // Load and set vehicle
            val vehicles = db.vehicleDao().getAllVehicles()
            val vehicle = vehicles.find { it.nomorPolisi == trip.nomorPolisi }
            if (vehicle != null) {
                selectedVehicle = vehicle
                binding.actvVehicle.setText(vehicle.nomorPolisi, false)
            }
        }
    }

    private fun showSuccessMessage(message: String) {
        Toast.makeText(
            this,
            "✅ $message",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showInfoDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("⚠️ Perhatian")
            .setMessage("Pastikan Anda sudah menambahkan data Sopir dan Kendaraan terlebih dahulu di menu 'Data Sopir & Kendaraan'.")
            .setPositiveButton("OK", null)
            .show()
    }
}