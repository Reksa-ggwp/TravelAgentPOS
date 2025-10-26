package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.travelagent.pos.R
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.databinding.ActivityCustomerListBinding
import com.travelagent.pos.repository.CustomerRepository
import com.travelagent.pos.utils.LoadingDialog
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

    private lateinit var loadingDialog: LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearchBox()
        setupFab()
        setupSwipeRefresh()
        observeViewModel()

        viewModel.loadCustomers()
        loadingDialog = LoadingDialog(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = CustomerAdapter(
            customers = mutableListOf(),
            onItemClick = { customer ->
                val intent = Intent(this, AddCustomerActivity::class.java)
                intent.putExtra("customerId", customer.id)
                startActivity(intent)
            },
            onDeleteSuccess = {
                // Show success message
                Snackbar.make(
                    binding.root,
                    "Pelanggan berhasil dihapus",
                    Snackbar.LENGTH_SHORT
                ).show()

                // Reload the customer list
                viewModel.loadCustomers()
            }
        )

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

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(
            R.color.info,
            R.color.success,
            R.color.warning
        )

        binding.swipeRefresh.setOnRefreshListener {
            // Clear search when refreshing
            binding.etSearch.text?.clear()
            viewModel.loadCustomers()
        }
    }

    private fun observeViewModel() {
        viewModel.customers.observe(this) { customers ->
            // Stop refresh animation
            binding.swipeRefresh.isRefreshing = false

            if (customers.isEmpty() && binding.etSearch.text.isNullOrEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                adapter.updateList(customers.toMutableList())
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            if (isLoading) {
                loadingDialog.show("Memuat data...")
            } else {
                loadingDialog.dismiss()
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            binding.swipeRefresh.isRefreshing = false
            errorMessage?.let {
                showErrorSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    private fun showEmptyState() {
        binding.rvCustomers.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        binding.rvCustomers.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE
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
