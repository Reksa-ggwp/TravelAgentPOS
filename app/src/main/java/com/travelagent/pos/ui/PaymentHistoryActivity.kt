package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.databinding.ActivityPaymentHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class PaymentHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentHistoryBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: PaymentHistoryAdapter

    private var startDate: Long = 0
    private var endDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        setupToolbar()
        setupRecyclerView()
        setupFilterButtons()
        loadPayments()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        // UPDATED: Pass click listener to open ticket print for reprint
        adapter = PaymentHistoryAdapter { paymentItem ->
            openTicketForReprint(paymentItem.payment.ticketId)
        }
        binding.rvPayments.apply {
            layoutManager = LinearLayoutManager(this@PaymentHistoryActivity)
            adapter = this@PaymentHistoryActivity.adapter
        }
    }

    private fun setupFilterButtons() {
        binding.btnFilterDate.setOnClickListener {
            showDateRangePicker()
        }

        binding.btnClearFilter.setOnClickListener {
            startDate = 0
            endDate = 0
            binding.tvDateRange.text = "Showing all payments"
            loadPayments()
        }
    }

    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            startDate = selection.first ?: 0
            endDate = selection.second ?: 0

            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale("id"))
            binding.tvDateRange.text = "From ${sdf.format(Date(startDate))} to ${sdf.format(Date(endDate))}"

            loadPaymentsByDateRange(startDate, endDate)
        }

        dateRangePicker.show(supportFragmentManager, "date_range_picker")
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

    private fun loadPaymentsByDateRange(start: Long, end: Long) {
        lifecycleScope.launch {
            val payments = withContext(Dispatchers.IO) {
                val filteredPayments = db.paymentDao().getPaymentsBetween(start, end)

                filteredPayments.map { payment ->
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

            val totalAmount = payments.sumOf { it.payment.amount }
            binding.tvTotalPayments.text = "Total: ${payments.size} pembayaran"
            binding.tvTotalAmount.text = formatCurrency(totalAmount)
        }
    }

    // NEW: Open ticket print activity for reprinting
    private fun openTicketForReprint(ticketId: Int) {
        val intent = Intent(this, TicketPrintActivity::class.java)
        intent.putExtra("ticketId", ticketId)
        startActivity(intent)
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