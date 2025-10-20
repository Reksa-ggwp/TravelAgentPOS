package com.travelagent.pos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.databinding.ActivityCustomerStatsBinding
import com.travelagent.pos.repository.CustomerRepository
import com.travelagent.pos.viewmodel.CustomerViewModel
import com.travelagent.pos.viewmodel.CustomerViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerStatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomerStatsBinding
    private lateinit var adapter: CustomerStatsAdapter

    private val viewModel: CustomerViewModel by viewModels {
        val db = AppDatabase.getDatabase(this)
        CustomerViewModelFactory(
            CustomerRepository(db.customerDao(), db.customerStatsDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()

        binding.btnBack.setOnClickListener { finish() }

        viewModel.loadCustomerStats()
    }

    private fun setupRecyclerView() {
        adapter = CustomerStatsAdapter(emptyList())
        binding.rvCustomerStats.layoutManager = LinearLayoutManager(this)
        binding.rvCustomerStats.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.customerStats.observe(this) { stats ->
            lifecycleScope.launch {
                // Get customer names for each stat
                val db = AppDatabase.getDatabase(this@CustomerStatsActivity)
                val statsWithNames = withContext(Dispatchers.IO) {
                    stats.map { stat ->
                        val customer = db.customerDao().getCustomerById(stat.customerId)
                        CustomerStatsWithName(
                            stats = stat,
                            customerName = customer?.namaLengkap ?: "Unknown"
                        )
                    }
                }

                adapter.updateList(statsWithNames)

                // Update tier breakdown
                val bronze = stats.count { it.tier == "Bronze" }
                val silver = stats.count { it.tier == "Silver" }
                val gold = stats.count { it.tier == "Gold" }
                val platinum = stats.count { it.tier == "Platinum" }

                binding.tvTierBreakdown.text = """
                    Tier Breakdown:
                    🥉 Bronze: $bronze pelanggan
                    🥈 Silver: $silver pelanggan
                    🥇 Gold: $gold pelanggan
                    💎 Platinum: $platinum pelanggan
                    
                    Total: ${stats.size} pelanggan
                """.trimIndent()
            }
        }
    }
}