package com.travelagent.pos.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.R
import com.travelagent.pos.data.Driver
import com.travelagent.pos.data.Vehicle
import com.travelagent.pos.databinding.ActivityDriverVehicleBinding
import androidx.lifecycle.lifecycleScope
import com.travelagent.pos.utils.ErrorHandler
import com.travelagent.pos.utils.InputValidator
import com.travelagent.pos.utils.ValidationResult
import kotlinx.coroutines.launch

class DriverVehicleActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var driverAdapter: DriverAdapter
    private lateinit var vehicleAdapter: VehicleAdapter
    private lateinit var binding: ActivityDriverVehicleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverVehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        // Setup Driver RecyclerView
        driverAdapter = DriverAdapter(mutableListOf()) { driver ->
            showEditDriverDialog(driver)
        }
        binding.rvDrivers.layoutManager = LinearLayoutManager(this)
        binding.rvDrivers.adapter = driverAdapter

        // Setup Vehicle RecyclerView
        vehicleAdapter = VehicleAdapter(mutableListOf()) { vehicle ->
            showEditVehicleDialog(vehicle)
        }
        binding.rvVehicles.layoutManager = LinearLayoutManager(this)
        binding.rvVehicles.adapter = vehicleAdapter

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddDriver.setOnClickListener { showAddDriverDialog() }
        binding.btnAddVehicle.setOnClickListener { showAddVehicleDialog() }

        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            val drivers = db.driverDao().getAllDrivers()
            val vehicles = db.vehicleDao().getAllVehicles()

            driverAdapter.updateList(drivers.toMutableList())
            vehicleAdapter.updateList(vehicles.toMutableList())
        }
    }

    private fun showAddDriverDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_driver, null)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etDriverName)
        val etPhone = dialogView.findViewById<android.widget.EditText>(R.id.etDriverPhone)

        AlertDialog.Builder(this)
            .setTitle("Tambah Sopir")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    lifecycleScope.launch {
                        db.driverDao().insert(Driver(namaSopir = name, nomorTelepon = phone))
                        ErrorHandler.showSuccess(this@DriverVehicleActivity, "Sopir ditambahkan")
                        loadData()
                    }
                } else {
                    ErrorHandler.showError(this, "Isi semua field")
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditDriverDialog(driver: Driver) {
        val options = arrayOf("Edit", "Hapus")
        AlertDialog.Builder(this)
            .setTitle(driver.namaSopir)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val dialogView = layoutInflater.inflate(R.layout.dialog_add_driver, null)
                        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etDriverName)
                        val etPhone = dialogView.findViewById<android.widget.EditText>(R.id.etDriverPhone)
                        etName.setText(driver.namaSopir)
                        etPhone.setText(driver.nomorTelepon)

                        AlertDialog.Builder(this)
                            .setTitle("Edit Sopir")
                            .setView(dialogView)
                            .setPositiveButton("Simpan") { _, _ ->
                                val name = etName.text.toString().trim()
                                val phone = etPhone.text.toString().trim()
                                if (name.isNotEmpty() && phone.isNotEmpty()) {
                                    lifecycleScope.launch {
                                        db.driverDao().update(driver.copy(
                                            namaSopir = name,
                                            nomorTelepon = phone
                                        ))
                                        ErrorHandler.showSuccess(this@DriverVehicleActivity, "Sopir diupdate")
                                        loadData()
                                    }
                                }
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    }
                    1 -> {
                        AlertDialog.Builder(this)
                            .setTitle("Hapus Sopir?")
                            .setMessage("Yakin ingin menghapus ${driver.namaSopir}?")
                            .setPositiveButton("Hapus") { _, _ ->
                                lifecycleScope.launch {
                                    db.driverDao().delete(driver)
                                    ErrorHandler.showSuccess(this@DriverVehicleActivity, "Sopir dihapus")
                                    loadData()
                                }
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    }
                }
            }
            .show()
    }

    private fun showAddVehicleDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_vehicle, null)
        val etPlate = dialogView.findViewById<android.widget.EditText>(R.id.etVehiclePlate)

        // Add auto-formatting
        etPlate.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                isFormatting = true
                val original = s.toString()
                val formatted = InputValidator.formatPlateNumber(original)

                if (formatted != original) {
                    etPlate.setText(formatted)
                    etPlate.setSelection(formatted.length)
                }
                isFormatting = false
            }
        })

        AlertDialog.Builder(this)
            .setTitle("Tambah Kendaraan")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val plate = etPlate.text.toString().trim()

                // Validate before saving
                when (val result = InputValidator.validatePlateNumber(plate)) {
                    is ValidationResult.Error -> {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("⚠️ Nomor Polisi Tidak Valid")
                            .setMessage("${result.message}\n\nContoh: B 1234 ABC (semua huruf KAPITAL)")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                    ValidationResult.Success -> {
                        lifecycleScope.launch {
                            val formatted = InputValidator.formatPlateNumber(plate)
                            db.vehicleDao().insert(Vehicle(nomorPolisi = formatted))
                            ErrorHandler.showSuccess(this@DriverVehicleActivity, "Kendaraan ditambahkan")
                            loadData()
                        }
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditVehicleDialog(vehicle: Vehicle) {
        val options = arrayOf("Edit", "Hapus")
        AlertDialog.Builder(this)
            .setTitle(vehicle.nomorPolisi)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val dialogView = layoutInflater.inflate(R.layout.dialog_add_vehicle, null)
                        val etPlate = dialogView.findViewById<android.widget.EditText>(R.id.etVehiclePlate)
                        etPlate.setText(vehicle.nomorPolisi)

                        AlertDialog.Builder(this)
                            .setTitle("Edit Kendaraan")
                            .setView(dialogView)
                            .setPositiveButton("Simpan") { _, _ ->
                                val plate = etPlate.text.toString().trim()
                                if (plate.isNotEmpty()) {
                                    lifecycleScope.launch {
                                        db.vehicleDao().update(vehicle.copy(nomorPolisi = plate))
                                        ErrorHandler.showSuccess(this@DriverVehicleActivity, "Kendaraan diupdate")
                                        loadData()
                                    }
                                }
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    }
                    1 -> {
                        AlertDialog.Builder(this)
                            .setTitle("Hapus Kendaraan?")
                            .setMessage("Yakin ingin menghapus ${vehicle.nomorPolisi}?")
                            .setPositiveButton("Hapus") { _, _ ->
                                lifecycleScope.launch {
                                    db.vehicleDao().delete(vehicle)
                                    ErrorHandler.showSuccess(this@DriverVehicleActivity, "Kendaraan dihapus")
                                    loadData()
                                }
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    }
                }
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}
