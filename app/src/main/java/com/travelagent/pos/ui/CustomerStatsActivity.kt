package com.travelagent.pos.ui
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.databinding.ActivityCustomerStatsBinding
import com.travelagent.pos.repository.CustomerRepository
import com.travelagent.pos.viewmodel.CustomerViewModel
import com.travelagent.pos.viewmodel.CustomerViewModelFactory
class CustomerStatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomerStatsBinding
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
        binding.rvCustomerStats.layoutManager = LinearLayoutManager(this)
        // You'll need to create CustomerStatsAdapter
    }
    private fun observeViewModel() {
        viewModel.customerStats.observe(this) { stats ->
            // Update adapter with stats
            // Show tier breakdown
            val bronze = stats.count { it.tier == "Bronze" }
            val silver = stats.count { it.tier == "Silver" }
            val gold = stats.count { it.tier == "Gold" }
            val platinum = stats.count { it.tier == "Platinum" }
            binding.tvTierBreakdown.text = """
             Tier Breakdown:
             🥉 Bronze: $bronze
             🥈 Silver: $silver
             🥇 Gold: $gold
             💎 Platinum: $platinum
             """.trimIndent()
        }
    }
}
