// CustomerListActivity.kt
package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import com.travelagent.pos.ui.AddCustomerActivity
import com.travelagent.pos.ui.CustomerAdapter
import com.travelagent.pos.R
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CustomerListActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var adapter: CustomerAdapter
    private var allCustomers = mutableListOf<Customer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_list)

        db = AppDatabase.getDatabase(this)

        val recyclerView = findViewById<RecyclerView>(R.id.rvCustomers)
        val searchBox = findViewById<EditText>(R.id.etSearch)
        val btnAdd = findViewById<Button>(R.id.btnAddCustomer)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        adapter = CustomerAdapter(mutableListOf()) { customer ->
            val intent = Intent(this, AddCustomerActivity::class.java)
            intent.putExtra("customerId", customer.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddCustomerActivity::class.java))
        }

        btnBack.setOnClickListener { finish() }

        searchBox.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCustomers(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        loadCustomers()
    }

    private fun loadCustomers() {
        GlobalScope.launch(Dispatchers.Main) {
            allCustomers = db.customerDao().getAllCustomers().toMutableList()
            adapter.updateList(allCustomers)
        }
    }

    private fun filterCustomers(query: String) {
        GlobalScope.launch(Dispatchers.Main) {
            if (query.isEmpty()) {
                adapter.updateList(allCustomers)
            } else {
                val filtered = allCustomers.filter {
                    it.namaLengkap.contains(query, ignoreCase = true) ||
                            it.nomorTelepon.contains(query)
                }
                adapter.updateList(filtered.toMutableList())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCustomers()
    }
}