package com.travelagent.pos.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.travelagent.pos.R
import com.travelagent.pos.databinding.ActivityPrinterSettingsBinding
import com.travelagent.pos.utils.ErrorHandler
import com.travelagent.pos.utils.ThermalPrinterManager
import kotlinx.coroutines.launch

class PrinterSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrinterSettingsBinding
    private lateinit var printerManager: ThermalPrinterManager

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            loadPrinters()
        } else {
            ErrorHandler.showError(this, "Bluetooth permission required")
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            requestBluetoothPermissions()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrinterSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        printerManager = ThermalPrinterManager(this)

        setupToolbar()
        setupPaperSizeSelection()
        setupButtons()
        loadCurrentSettings()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupPaperSizeSelection() {
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val width = when (checkedId) {
                R.id.radio58mm -> ThermalPrinterManager.PAPER_WIDTH_58MM
                R.id.radio80mm -> ThermalPrinterManager.PAPER_WIDTH_80MM
                else -> ThermalPrinterManager.PAPER_WIDTH_80MM
            }
            printerManager.setPaperWidth(width)
            ErrorHandler.showSuccess(this, "Paper size saved: ${if (width == 32) "58mm" else "80mm"}")
        }
    }

    private fun setupButtons() {
        binding.btnSelectPrinter.setOnClickListener {
            checkBluetoothAndLoadPrinters()
        }

        binding.btnTestPrint.setOnClickListener {
            testPrint()
        }
    }

    private fun loadCurrentSettings() {
        // Load paper size
        val currentWidth = printerManager.getPaperWidth()
        if (currentWidth == ThermalPrinterManager.PAPER_WIDTH_58MM) {
            binding.radio58mm.isChecked = true
        } else {
            binding.radio80mm.isChecked = true
        }

        // Load saved printer
        val savedAddress = printerManager.getSavedPrinterAddress()
        if (savedAddress != null) {
            binding.tvSelectedPrinter.text = "Connected: $savedAddress"
        }
    }

    private fun checkBluetoothAndLoadPrinters() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            ErrorHandler.showError(this, "Bluetooth not supported on this device")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
            return
        }

        requestBluetoothPermissions()
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )

            val allGranted = permissions.all {
                ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }

            if (!allGranted) {
                bluetoothPermissionLauncher.launch(permissions)
            } else {
                loadPrinters()
            }
        } else {
            loadPrinters()
        }
    }

    private fun loadPrinters() {
        val printers = printerManager.getPairedBluetoothPrinters()

        if (printers.isEmpty()) {
            MaterialAlertDialogBuilder(this)
                .setTitle("No Printers Found")
                .setMessage("Please pair your Bluetooth printer in Android Settings first.")
                .setPositiveButton("Open Settings") { _, _ ->
                    startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
                }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }

        val printerNames = printers.map { "${it.name} (${it.address})" }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Printer")
            .setItems(printerNames) { _, which ->
                val selectedPrinter = printers[which]
                printerManager.savePrinterAddress(selectedPrinter.address)
                binding.tvSelectedPrinter.text = "Connected: ${selectedPrinter.name}"
                ErrorHandler.showSuccess(this, "Printer saved: ${selectedPrinter.name}")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun testPrint() {
        val printerAddress = printerManager.getSavedPrinterAddress()
        if (printerAddress == null) {
            ErrorHandler.showWarning(this, "Please select a printer first")
            return
        }

    binding.btnTestPrint.isEnabled = false
    binding.btnTestPrint.text = getString(R.string.printing)

        lifecycleScope.launch {
            val result = printerManager.testPrint(printerAddress)

                binding.btnTestPrint.isEnabled = true
                binding.btnTestPrint.text = getString(R.string.test_print)

            result.fold(
                onSuccess = {
                    ErrorHandler.showSuccess(this@PrinterSettingsActivity, "Test print successful")
                },
                onFailure = { error ->
                    MaterialAlertDialogBuilder(this@PrinterSettingsActivity)
                        .setTitle("Print Failed")
                        .setMessage("Error: ${error.message}\n\nMake sure:\n• Printer is turned on\n• Bluetooth is enabled\n• Printer is paired\n• Paper is loaded")
                        .setPositiveButton("OK", null)
                        .show()
                }
            )
        }
    }
}