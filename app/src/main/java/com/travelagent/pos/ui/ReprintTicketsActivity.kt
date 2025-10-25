package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Ticket
import com.travelagent.pos.databinding.ActivityReprintTicketsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ReprintTicketsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReprintTicketsBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: ReprintTicketsAdapter

    private var startDate: Long = 0
    private var endDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReprintTicketsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        setupToolbar()
        setupRecyclerView()
        setupButtons()
        loadAllTickets()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = ReprintTicketsAdapter { reprintTicketItem -> // Renamed for clarity
            // Access the 'id' through the nested 'ticket' property
            openTicketPrint(reprintTicketItem.ticket.id)
        }

        binding.rvTickets.apply {
            layoutManager = LinearLayoutManager(this@ReprintTicketsActivity)
            adapter = this@ReprintTicketsActivity.adapter
        }
    }

    private fun setupButtons() {
        binding.btnFilterDate.setOnClickListener {
            showDateRangePicker()
        }

        binding.btnClearFilter.setOnClickListener {
            startDate = 0
            endDate = 0
            binding.tvDateRange.text = "Showing all tickets"
            loadAllTickets()
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

            loadTicketsByDateRange(startDate, endDate)
        }

        dateRangePicker.show(supportFragmentManager, "date_range_picker")
    }

    private fun loadAllTickets() {
        lifecycleScope.launch {
            val tickets = withContext(Dispatchers.IO) {
                db.ticketDao().getAllTickets()
            }

            val ticketItems = tickets.map { ticket ->
                val customer = withContext(Dispatchers.IO) {
                    db.customerDao().getCustomerById(ticket.customerId)
                }
                val trip = withContext(Dispatchers.IO) {
                    db.tripDao().getTripById(ticket.tripId)
                }
                val seat = withContext(Dispatchers.IO) {
                    db.seatDao().getSeatById(ticket.seatId)
                }

                ReprintTicketItem(
                    ticket = ticket,
                    customerName = customer?.namaLengkap ?: "Unknown",
                    route = trip?.let { "${it.asal} → ${it.tujuan}" } ?: "Unknown",
                    seatNumber = seat?.nomorKursi ?: 0,
                    date = trip?.tanggal ?: 0
                )
            }

            adapter.submitList(ticketItems)
            binding.tvTotalTickets.text = "Total: ${ticketItems.size} tickets"
        }
    }

    private fun loadTicketsByDateRange(start: Long, end: Long) {
        lifecycleScope.launch {
            val tickets = withContext(Dispatchers.IO) {
                db.ticketDao().getTicketsBetween(start, end)
            }

            val ticketItems = tickets.map { ticket ->
                val customer = withContext(Dispatchers.IO) {
                    db.customerDao().getCustomerById(ticket.customerId)
                }
                val trip = withContext(Dispatchers.IO) {
                    db.tripDao().getTripById(ticket.tripId)
                }
                val seat = withContext(Dispatchers.IO) {
                    db.seatDao().getSeatById(ticket.seatId)
                }

                ReprintTicketItem(
                    ticket = ticket,
                    customerName = customer?.namaLengkap ?: "Unknown",
                    route = trip?.let { "${it.asal} → ${it.tujuan}" } ?: "Unknown",
                    seatNumber = seat?.nomorKursi ?: 0,
                    date = trip?.tanggal ?: 0
                )
            }

            adapter.submitList(ticketItems)
            binding.tvTotalTickets.text = "Total: ${ticketItems.size} tickets"
        }
    }

    private fun openTicketPrint(ticketId: Int) {
        val intent = Intent(this, TicketPrintActivity::class.java)
        intent.putExtra("ticketId", ticketId)
        startActivity(intent)
    }
}

data class ReprintTicketItem(
    val ticket: Ticket,
    val customerName: String,
    val route: String,
    val seatNumber: Int,
    val date: Long
)