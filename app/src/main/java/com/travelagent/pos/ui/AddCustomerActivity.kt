package com.travelagent.pos.ui
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Customer
import com.travelagent.pos.databinding.ActivityAddCustomerBinding
import com.travelagent.pos.repository.CustomerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        if (customerId != null) {
            loadCustomer()
        }
        binding.btnSave.setOnClickListener {
            saveCustomer()
        }
        binding.btnBack.setOnClickListener { finish() }
    }
    private fun loadCustomer() {
        lifecycleScope.launch {
            val customer = withContext(Dispatchers.IO) {
                repository.getCustomerById(customerId!!)
            }
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
                result.fold(
                    onSuccess = {
                        Toast.makeText(
                            this@AddCustomerActivity,
                            if (customerId != null) "Pelanggan diperbarui" else "Pelanggan ditambahkan",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            this@AddCustomerActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@AddCustomerActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}