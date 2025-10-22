package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
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

        setupToolbar()
        setupRecyclerView()
        setupSearchBox()
        setupFab()
        observeViewModel()

        viewModel.loadCustomers()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = CustomerAdapter(mutableListOf()) { customer ->
            val intent = Intent(this, AddCustomerActivity::class.java)
            intent.putExtra("customerId", customer.id)
            startActivity(intent)
        }

        binding.rvCustomers.apply {
            layoutManager = LinearLayoutManager(this@CustomerListActivity)
            adapter = this@CustomerListActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearchBox() {
        binding.etSearch.addTextChangedListener { text ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(300) // Debounce for 300ms
                viewModel.searchCustomers(text.toString())
            }
        }
    }

    private fun setupFab() {
        binding.btnAddCustomer.setOnClickListener {
            startActivity(Intent(this, AddCustomerActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.customers.observe(this) { customers ->
            if (customers.isEmpty() && binding.etSearch.text.isNullOrEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                adapter.updateList(customers.toMutableList())
            }
        }

        viewModel.loading.observe(this) { _ ->
            // You can show/hide a progress bar here
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                showErrorSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    private fun showEmptyState() {
        // You can add an empty state view to your layout
        Snackbar.make(
            binding.root,
            "Belum ada data pelanggan",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun hideEmptyState() {
        // Hide empty state view if you have one
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        ).setAction("TUTUP") {
            // Dismiss
        }.show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadCustomers()
    }
}