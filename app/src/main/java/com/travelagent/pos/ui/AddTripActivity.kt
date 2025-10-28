package com.travelagent.pos.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.travelagent.pos.utils.ErrorHandler
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Driver
import com.travelagent.pos.data.Seat
import com.travelagent.pos.data.Trip
import com.travelagent.pos.data.Vehicle
import com.travelagent.pos.databinding.ActivityAddTripBinding
import com.travelagent.pos.utils.Constants
import com.travelagent.pos.utils.InputValidator
import com.travelagent.pos.utils.ValidationResult
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

    // FIXED: Only 2 cities allowed
    private val cities = arrayOf("Sibolga", "Medan")

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
        setupVehicleAutoFormat()
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
        // FIXED: Cities are dropdown only, not editable
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)

        binding.actvOrigin.setAdapter(cityAdapter)
        binding.actvDestination.setAdapter(cityAdapter)

        // Make them non-editable (dropdown only)
        binding.actvOrigin.inputType = 0
        binding.actvDestination.inputType = 0

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

    private fun setupVehicleAutoFormat() {
        binding.actvVehicle.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val original = s.toString()
                val formatted = InputValidator.formatPlateNumber(original)
                if (formatted != original) {
                    binding.actvVehicle.setText(formatted)
                    binding.actvVehicle.setSelection(formatted.length)
                }
                isFormatting = false
            }
        })
    }

    private fun setupDriverDropdown(drivers: List<Driver>) {
        // FIXED: Driver names as dropdown
        val driverNames = drivers.map { it.namaSopir }
        val driverAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            driverNames
        )

        binding.actvDriver.setAdapter(driverAdapter)

        // FIXED: Make non-editable (dropdown only)
        binding.actvDriver.inputType = 0
        binding.actvDriver.threshold = 1

        // FIXED: Auto-fill phone number when driver selected
        binding.actvDriver.setOnItemClickListener { _, _, position, _ ->
            if (position < drivers.size) {
                selectedDriver = drivers[position]
                binding.etDriverPhone.setText(selectedDriver?.nomorTelepon)
            }
        }
    }

    private fun setupVehicleDropdown(vehicles: List<Vehicle>) {
        // FIXED: Vehicle plates as dropdown
        val plateNumbers = vehicles.map { it.nomorPolisi }
        val vehicleAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            plateNumbers
        )

        binding.actvVehicle.setAdapter(vehicleAdapter)

        // FIXED: Make non-editable (dropdown only)
        binding.actvVehicle.inputType = 0
        binding.actvVehicle.threshold = 1

        binding.actvVehicle.setOnItemClickListener { _, _, position, _ ->
            if (position < vehicles.size) {
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
        val price = binding.etPrice.text.toString()
        val vehicle = binding.actvVehicle.text.toString().trim()
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
                ErrorHandler.handleValidationError(this, "Pilih tanggal keberangkatan")
                return false
            }
            selectedDriver == null -> {
                ErrorHandler.handleValidationError(this, "Pilih sopir")
                return false
            }
            vehicle.isEmpty() -> {
                ErrorHandler.handleValidationError(this, "Pilih kendaraan")
                return false
            }
        }
        // Validate plate number format
        when (val plateResult = InputValidator.validatePlateNumber(vehicle)) {
            is ValidationResult.Error -> {
                ErrorHandler.handleValidationError(
                    this,
                    "${plateResult.message}\n\nContoh format yang benar:\n• B 1234 ABC\n• L 5678 CD\n\nSemua huruf harus KAPITAL."
                )
                return false
            }
            ValidationResult.Success -> { /* OK */
            }
        }
        // Validate price
        when (val priceResult = InputValidator.validatePrice(price)) {
            is ValidationResult.Error -> {
                binding.etPrice.error = priceResult.message
                binding.etPrice.requestFocus()
                return false
            }
            ValidationResult.Success -> binding.etPrice.error = null
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

                ErrorHandler.showSuccess(this@AddTripActivity, "Perjalanan berhasil ditambahkan")
                finish()

            } catch (e: Exception) {
                ErrorHandler.handleOperationError(this@AddTripActivity, "menambah perjalanan", e)
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

                ErrorHandler.showSuccess(this@AddTripActivity, "Perjalanan berhasil diupdate")
                finish()

            } catch (e: Exception) {
                ErrorHandler.handleOperationError(this@AddTripActivity, "mengupdate perjalanan", e)
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

    private fun showInfoDialog() {
        ErrorHandler.showInfo(
            this,
            "Pastikan Anda sudah menambahkan data Sopir dan Kendaraan terlebih dahulu di menu 'Data Sopir & Kendaraan'."
        )
    }
}
