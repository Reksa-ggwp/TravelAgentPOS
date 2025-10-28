package com.travelagent.pos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.travelagent.pos.utils.ErrorHandler
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Customer
import com.travelagent.pos.databinding.ActivityAddCustomerBinding
import com.travelagent.pos.repository.CustomerRepository
import com.travelagent.pos.repository.RepositoryResult
import com.travelagent.pos.utils.InputValidator
import com.travelagent.pos.utils.ValidationResult
import kotlinx.coroutines.launch

class AddCustomerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCustomerBinding
    private lateinit var repository: CustomerRepository
    private var customerId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getDatabase(this)
        repository = CustomerRepository(db.customerDao(), db.customerStatsDao())

        customerId = intent.getIntExtra("customerId", -1).takeIf { it != -1 }

        setupToolbar()
        setupButtons()

        if (customerId != null) {
            loadCustomer()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = if (customerId != null) "Edit Pelanggan" else "Tambah Pelanggan"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                saveCustomer()
            }
        }
    }

    private fun validateInput(): Boolean {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()

        var isValid = true

        // Validate name
        when (val nameResult = InputValidator.validateName(name, "Nama")) {
            is ValidationResult.Error -> {
                binding.etName.error = nameResult.message
                isValid = false
            }
            ValidationResult.Success -> binding.etName.error = null
        }

        // Validate phone
        when (val phoneResult = InputValidator.validatePhoneNumber(phone)) {
            is ValidationResult.Error -> {
                binding.etPhone.error = phoneResult.message
                isValid = false
            }
            ValidationResult.Success -> binding.etPhone.error = null
        }

        // Validate address
        when (val addressResult = InputValidator.validateAddress(address)) {
            is ValidationResult.Error -> {
                binding.etAddress.error = addressResult.message
                isValid = false
            }
            ValidationResult.Success -> binding.etAddress.error = null
        }

        return isValid
    }

    private fun loadCustomer() {
        lifecycleScope.launch {
            val id = customerId ?: return@launch
            val customer = repository.getCustomerById(id)
            customer?.let {
                binding.etName.setText(it.namaLengkap)
                binding.etPhone.setText(it.nomorTelepon)
                binding.etAddress.setText(it.alamat)
            }
        }
    }

    private fun saveCustomer() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()

        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Menyimpan..."

        lifecycleScope.launch {
            try {
                val customer = Customer(
                    id = customerId ?: 0,
                    namaLengkap = name,
                    nomorTelepon = phone,
                    alamat = address
                )

                val result = if (customerId != null) {
                    repository.updateCustomer(customer)
                } else {
                    repository.insertCustomer(customer)
                }

                when (result) {
                    is RepositoryResult.Success -> {
                        ErrorHandler.showSuccess(this@AddCustomerActivity, "Berhasil disimpan")
                        finish()
                    }
                    is RepositoryResult.Failure -> {
                        binding.btnSave.isEnabled = true
                        binding.btnSave.text = "Simpan"
                        ErrorHandler.showError(this@AddCustomerActivity, "Terjadi Kesalahan")
                    }
                }
            } catch (e: Exception) {
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Simpan"
                ErrorHandler.handleOperationError(this@AddCustomerActivity, "menyimpan pelanggan", e)
            }
        }
    }

    }
