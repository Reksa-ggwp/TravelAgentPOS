package com.travelagent.pos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.databinding.ActivityPaymentHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentHistoryBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: PaymentHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        setupToolbar()
        setupRecyclerView()
        loadPayments()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = PaymentHistoryAdapter()
        binding.rvPayments.apply {
            layoutManager = LinearLayoutManager(this@PaymentHistoryActivity)
            adapter = this@PaymentHistoryActivity.adapter
        }
    }

    private fun loadPayments() {
        lifecycleScope.launch {
            val payments = withContext(Dispatchers.IO) {
                val allPayments = db.paymentDao().getRecentPayments()

                allPayments.map { payment ->
                    val ticket = db.ticketDao().getTicketById(payment.ticketId)
                    val customer = ticket?.let { db.customerDao().getCustomerById(it.customerId) }
                    val trip = ticket?.let { db.tripDao().getTripById(it.tripId) }

                    PaymentHistoryItem(
                        payment = payment,
                        customerName = customer?.namaLengkap ?: "Unknown",
                        route = trip?.let { "${it.asal} → ${it.tujuan}" } ?: "Unknown"
                    )
                }
            }

            adapter.submitList(payments)

            // Update summary
            val totalAmount = payments.sumOf { it.payment.amount }
            binding.tvTotalPayments.text = "Total: ${payments.size} pembayaran"
            binding.tvTotalAmount.text = formatCurrency(totalAmount)
        }
    }

    private fun formatCurrency(amount: Double): String {
        return "Rp ${String.format("%,d", amount.toLong()).replace(',', '.')}"
    }
}

data class PaymentHistoryItem(
    val payment: com.travelagent.pos.data.Payment,
    val customerName: String,
    val route: String
)