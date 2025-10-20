package com.travelagent.pos.ui
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.databinding.ActivityCustomerListBinding
import com.travelagent.pos.repository.CustomerRepository
import com.travelagent.pos.viewmodel.CustomerViewModel
import com.travelagent.pos.viewmodel.CustomerViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
class CustomerListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomerListBinding
    private lateinit var adapter: CustomerAdapter
    private var searchJob: Job? = null
    private val viewModel: CustomerViewModel by viewModels {
        val db = AppDatabase.getDatabase(this)
        CustomerViewModelFactory(
            CustomerRepository(db.customerDao(), db.customerStatsDao())
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        setupSearchBox()
        observeViewModel()
        binding.btnAddCustomer.setOnClickListener {
            startActivity(Intent(this, AddCustomerActivity::class.java))
        }
        binding.btnBack.setOnClickListener { finish() }
        viewModel.loadCustomers()
    }
    private fun setupRecyclerView() {
        adapter = CustomerAdapter(mutableListOf()) { customer ->
            val intent = Intent(this, AddCustomerActivity::class.java)
            intent.putExtra("customerId", customer.id)
            startActivity(intent)
        }
        binding.rvCustomers.layoutManager = LinearLayoutManager(this)
        binding.rvCustomers.adapter = adapter
    }
    private fun setupSearchBox() {
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300) // Debounce
                    viewModel.searchCustomers(s.toString())
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun observeViewModel() {
        viewModel.customers.observe(this) { customers ->
            adapter.updateList(customers.toMutableList())
        }
        viewModel.loading.observe(this) { isLoading ->
            // You can add a ProgressBar to your layout and show/hide it here
            // binding.pr // binding.progr ogressBar essBar.visibility = if (isLoading) V .visibility = if (isLoading) Viewiew.VISIBLE else V .VISIBLE else Viewiew.GONE .GONE
        }
        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.loadCustomers()
    }
}
