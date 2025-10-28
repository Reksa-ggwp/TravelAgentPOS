package com.travelagent.pos.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.travelagent.pos.R
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Trip
import com.travelagent.pos.databinding.ItemTripCardBinding
import com.travelagent.pos.utils.ErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TripAdapter(
    private var trips: MutableList<Trip>,
    private val scope: LifecycleCoroutineScope,
    private val onItemClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripAdapter.ViewHolder>() {

    // Lazy DB to avoid initializing on main thread repeatedly
    private val db: AppDatabase by lazy { AppDatabase.getDatabase(contextRef) }
    private lateinit var contextRef: android.content.Context

    inner class ViewHolder(private val binding: ItemTripCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(trip: Trip, @Suppress("UNUSED_PARAMETER") position: Int) {
            binding.apply {
                // Set plate number chip
                chipPlate.text = trip.nomorPolisi

                // Set route
                tvOrigin.text = trip.asal
                tvDestination.text = trip.tujuan

                // Format and set date
                tvDate.text = trip.tanggal.toFormattedDate("dd MMM yyyy")

                // Set driver
                tvDriver.text = trip.namaSopir

                // Calculate and set available seats
                scope.launch {
                    val seats = db.seatDao().getSeatsByTrip(trip.id)
                    val available = seats.count { it.status == "available" }
                    chipAvailable.text = "$available kursi"

                    // Change color based on availability
                    val color = when {
                        available == 0 -> android.R.color.darker_gray
                        available <= 3 -> R.color.warning
                        else -> R.color.status_available
                    }
                    chipAvailable.setChipBackgroundColorResource(color)
                }

                // Click listeners
                root.setOnClickListener { onItemClick(trip) }

                btnDetails.setOnClickListener {
                    val intent = Intent(itemView.context, TripDetailsActivity::class.java)
                    intent.putExtra("tripId", trip.id)
                    itemView.context.startActivity(intent)
                }

                btnEdit.setOnClickListener {
                    val intent = Intent(itemView.context, AddTripActivity::class.java)
                    intent.putExtra("tripId", trip.id)
                    itemView.context.startActivity(intent)
                }

                // Long click for delete
                root.setOnLongClickListener {
                    showDeleteDialog(trip)
                    true
                }
            }
        }

        // ============================================================
        // CRITICAL UPDATE #3: TripAdapter.kt
        // Add confirmation dialog before delete
        // ============================================================
        private fun showDeleteDialog(trip: Trip) {
            scope.launch {
                // Check for paid tickets first
                // NOTE: Assuming db.ticketDao() exists and has getTicketsByTrip method
                val tickets = db.ticketDao().getTicketsByTrip(trip.id)
                val hasPaidTickets = tickets.any { it.status == "paid" }
                val bookedCount = tickets.count { it.status in listOf("booked", "partial", "paid") }

                withContext(Dispatchers.Main) {
                    if (hasPaidTickets) {
                        MaterialAlertDialogBuilder(itemView.context)
                            .setTitle("⛔ Tidak Dapat Dihapus")
                            .setMessage(
                                "Perjalanan ini memiliki tiket yang sudah dibayar.\n\n" +
                                        "Untuk menjaga integritas data pembayaran, perjalanan dengan tiket yang sudah dibayar tidak dapat dihapus."
                            )
                            .setPositiveButton("Mengerti", null)
                            .setIcon(R.drawable.ic_warning)
                            .show()
                        return@withContext
                    }

                    val message = if (bookedCount > 0) {
                        "Yakin ingin menghapus perjalanan ${trip.asal} → ${trip.tujuan}?\n\n" +
                                "⚠️ PERHATIAN:\n" +
                                "• Ada $bookedCount kursi yang sudah di-booking\n" +
                                "• Semua data kursi akan dihapus\n" +
                                "• Semua booking akan hilang\n" +
                                "• Data tidak dapat dikembalikan"
                    } else {
                        "Yakin ingin menghapus perjalanan ${trip.asal} → ${trip.tujuan}?\n\n" +
                                "Data tidak dapat dikembalikan."
                    }

                    MaterialAlertDialogBuilder(itemView.context)
                        .setTitle("❌ Hapus Perjalanan?")
                        .setMessage(message)
                        .setPositiveButton("Hapus") { _, _ ->
                            scope.launch {
                                try {
                                    db.tripDao().delete(trip)
                                    withContext(Dispatchers.Main) {
                                        val position = trips.indexOf(trip)
                                        // Check if position is valid before removing
                                        if (position != -1) {
                                            trips.removeAt(position)
                                            notifyItemRemoved(position)
                                        }
                                        ErrorHandler.showSuccess(itemView.context, "Perjalanan dihapus")
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        ErrorHandler.handleOperationError(itemView.context, "menghapus perjalanan", e)
                                    }
                                }
                            }
                        }
                        .setNegativeButton("Batal", null)
                        .setIcon(R.drawable.ic_warning)
                        .show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Store appContext for lazy DB init and to avoid leaking Activity
        contextRef = parent.context.applicationContext
        val binding = ItemTripCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(trips[position], position)
    }

    override fun getItemCount() = trips.size

    fun updateList(newList: MutableList<Trip>) {
        // Lightweight diffing to avoid full notify
        val oldList = trips
        trips = newList
        // Fallback simple updates when sizes change a lot
        if (oldList.isEmpty() || newList.isEmpty()) {
            notifyDataSetChanged()
            return
        }
        // Notify bounds; avoids heavy DiffUtil dependency
        val minSize = minOf(oldList.size, newList.size)
        for (i in 0 until minSize) notifyItemChanged(i)
        if (newList.size > oldList.size) {
            notifyItemRangeInserted(minSize, newList.size - oldList.size)
        } else if (oldList.size > newList.size) {
            notifyItemRangeRemoved(minSize, oldList.size - newList.size)
        }
    }
}

// Extension function for date formatting
fun Long.toFormattedDate(pattern: String = "dd/MM/yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale("id", "ID"))
    return sdf.format(Date(this))
}
