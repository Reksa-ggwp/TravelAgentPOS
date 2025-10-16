// AddCustomerActivity.kt
package com.travelagent.pos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.travelagent.pos.R
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddCustomerActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private var customerId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_customer)

        db = AppDatabase.getDatabase(this)

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        customerId = intent.getIntExtra("customerId", -1).takeIf { it != -1 }

        if (customerId != null) {
            GlobalScope.launch {
                val customer = db.customerDao().getCustomerById(customerId!!)
                customer?.let {
                    runOnUiThread {
                        etName.setText(it.namaLengkap)
                        etPhone.setText(it.nomorTelepon)
                        etAddress.setText(it.alamat)
                    }
                }
            }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            GlobalScope.launch(Dispatchers.Main) {
                if (customerId != null) {
                    db.customerDao().update(
                        Customer(customerId!!, name, phone, address)
                    )
                    Toast.makeText(this@AddCustomerActivity, "Pelanggan diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    db.customerDao().insert(Customer(0, name, phone, address))
                    Toast.makeText(this@AddCustomerActivity, "Pelanggan ditambahkan", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        }

        btnBack.setOnClickListener { finish() }
    }
}