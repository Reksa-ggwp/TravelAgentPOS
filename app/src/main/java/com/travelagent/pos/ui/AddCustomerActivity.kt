package com.travelagent.pos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Customer
import com.travelagent.pos.databinding.ActivityAddCustomerBinding
import com.travelagent.pos.repository.CustomerRepository
import com.travelagent.pos.repository.RepositoryResult
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

        if (name.isEmpty()) {
            binding.etName.error = "Nama tidak boleh kosong"
            isValid = false
        } else if (name.length < 3) {
            binding.etName.error = "Nama terlalu pendek (minimal 3 karakter)"
            isValid = false
        }

        if (phone.isEmpty()) {
            binding.etPhone.error = "Nomor telepon tidak boleh kosong"
            isValid = false
        } else if (!phone.matches(Regex("^[0-9]{10,13}$"))) {
            binding.etPhone.error = "Format nomor telepon salah (10-13 digit)"
            isValid = false
        }

        if (address.isEmpty()) {
            binding.etAddress.error = "Alamat tidak boleh kosong"
            isValid = false
        } else if (address.length < 5) {
            binding.etAddress.error = "Alamat terlalu pendek (minimal 5 karakter)"
            isValid = false
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

        // Disable button to prevent double submission
        binding.btnSave.isEnabled = false

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
                        val message = if (customerId != null) {
                            "✅ Pelanggan berhasil diperbarui"
                        } else {
                            "✅ Pelanggan berhasil ditambahkan"
                        }

                        Snackbar.make(
                            binding.root,
                            message,
                            Snackbar.LENGTH_SHORT
                        ).show()

                        finish()
                    }
                    is RepositoryResult.Failure -> {
                        binding.btnSave.isEnabled = true
                        showErrorSnackbar(result.exception.message ?: "Terjadi kesalahan")
                    }
                }
            } catch (e: Exception) {
                binding.btnSave.isEnabled = true
                showErrorSnackbar(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(
            binding.root,
            "❌ $message",
            Snackbar.LENGTH_LONG
        ).setAction("TUTUP") {
            // Dismiss
        }.show()
    }
}