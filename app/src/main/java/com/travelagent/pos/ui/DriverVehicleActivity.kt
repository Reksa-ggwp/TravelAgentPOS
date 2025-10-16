package com.travelagent.pos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.*
import com.travelagent.pos.R
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Driver
import com.travelagent.pos.data.Vehicle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DriverVehicleActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var driverAdapter: DriverAdapter
    private lateinit var vehicleAdapter: VehicleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_vehicle)

        db = AppDatabase.getDatabase(this)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnAddDriver = findViewById<Button>(R.id.btnAddDriver)
        val btnAddVehicle = findViewById<Button>(R.id.btnAddVehicle)
        val rvDrivers = findViewById<RecyclerView>(R.id.rvDrivers)
        val rvVehicles = findViewById<RecyclerView>(R.id.rvVehicles)

        // Setup Driver RecyclerView
        driverAdapter = DriverAdapter(mutableListOf()) { driver ->
            showEditDriverDialog(driver)
        }
        rvDrivers.layoutManager = LinearLayoutManager(this)
        rvDrivers.adapter = driverAdapter

        // Setup Vehicle RecyclerView
        vehicleAdapter = VehicleAdapter(mutableListOf()) { vehicle ->
            showEditVehicleDialog(vehicle)
        }
        rvVehicles.layoutManager = LinearLayoutManager(this)
        rvVehicles.adapter = vehicleAdapter

        btnBack.setOnClickListener { finish() }
        btnAddDriver.setOnClickListener { showAddDriverDialog() }
        btnAddVehicle.setOnClickListener { showAddVehicleDialog() }

        loadData()
    }

    private fun loadData() {
        GlobalScope.launch(Dispatchers.Main) {
            val drivers = db.driverDao().getAllDrivers()
            val vehicles = db.vehicleDao().getAllVehicles()

            driverAdapter.updateList(drivers.toMutableList())
            vehicleAdapter.updateList(vehicles.toMutableList())
        }
    }

    private fun showAddDriverDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_driver, null)
        val etName = dialogView.findViewById<EditText>(R.id.etDriverName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etDriverPhone)

        AlertDialog.Builder(this)
            .setTitle("Tambah Sopir")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    GlobalScope.launch(Dispatchers.Main) {
                        db.driverDao().insert(Driver(namaSopir = name, nomorTelepon = phone))
                        Toast.makeText(this@DriverVehicleActivity, "Sopir ditambahkan", Toast.LENGTH_SHORT).show()
                        loadData()
                    }
                } else {
                    Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
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
                        val etName = dialogView.findViewById<EditText>(R.id.etDriverName)
                        val etPhone = dialogView.findViewById<EditText>(R.id.etDriverPhone)
                        etName.setText(driver.namaSopir)
                        etPhone.setText(driver.nomorTelepon)

                        AlertDialog.Builder(this)
                            .setTitle("Edit Sopir")
                            .setView(dialogView)
                            .setPositiveButton("Simpan") { _, _ ->
                                val name = etName.text.toString().trim()
                                val phone = etPhone.text.toString().trim()
                                if (name.isNotEmpty() && phone.isNotEmpty()) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        db.driverDao().update(driver.copy(
                                            namaSopir = name,
                                            nomorTelepon = phone
                                        ))
                                        Toast.makeText(this@DriverVehicleActivity, "Sopir diupdate", Toast.LENGTH_SHORT).show()
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
                                GlobalScope.launch(Dispatchers.Main) {
                                    db.driverDao().delete(driver)
                                    Toast.makeText(this@DriverVehicleActivity, "Sopir dihapus", Toast.LENGTH_SHORT).show()
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
        val etPlate = dialogView.findViewById<EditText>(R.id.etVehiclePlate)

        AlertDialog.Builder(this)
            .setTitle("Tambah Kendaraan")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val plate = etPlate.text.toString().trim()
                if (plate.isNotEmpty()) {
                    GlobalScope.launch(Dispatchers.Main) {
                        db.vehicleDao().insert(Vehicle(nomorPolisi = plate))
                        Toast.makeText(this@DriverVehicleActivity, "Kendaraan ditambahkan", Toast.LENGTH_SHORT).show()
                        loadData()
                    }
                } else {
                    Toast.makeText(this, "Isi nomor polisi", Toast.LENGTH_SHORT).show()
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
                        val etPlate = dialogView.findViewById<EditText>(R.id.etVehiclePlate)
                        etPlate.setText(vehicle.nomorPolisi)

                        AlertDialog.Builder(this)
                            .setTitle("Edit Kendaraan")
                            .setView(dialogView)
                            .setPositiveButton("Simpan") { _, _ ->
                                val plate = etPlate.text.toString().trim()
                                if (plate.isNotEmpty()) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        db.vehicleDao().update(vehicle.copy(nomorPolisi = plate))
                                        Toast.makeText(this@DriverVehicleActivity, "Kendaraan diupdate", Toast.LENGTH_SHORT).show()
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
                                GlobalScope.launch(Dispatchers.Main) {
                                    db.vehicleDao().delete(vehicle)
                                    Toast.makeText(this@DriverVehicleActivity, "Kendaraan dihapus", Toast.LENGTH_SHORT).show()
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