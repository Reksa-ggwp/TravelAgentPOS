package com.travelagent.pos.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Trip
import com.travelagent.pos.databinding.ItemTripCardBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TripAdapter(
    private var trips: MutableList<Trip>,
    private val scope: LifecycleCoroutineScope,
    private val onItemClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripAdapter.ViewHolder>() {

    private lateinit var db: AppDatabase

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
                        available <= 3 -> com.travelagent.pos.R.color.warning
                        else -> com.travelagent.pos.R.color.status_available
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

        private fun showDeleteDialog(trip: Trip) {
            AlertDialog.Builder(itemView.context)
                .setTitle("Hapus Perjalanan?")
                .setMessage("Yakin ingin menghapus perjalanan ${trip.asal} → ${trip.tujuan}?\n\nSemua data kursi dan booking akan ikut terhapus.")
                .setPositiveButton("Hapus") { _, _ ->
                    scope.launch {
                        db.tripDao().delete(trip)
                        val position = trips.indexOf(trip)
                        trips.remove(trip)
                        notifyItemRemoved(position)

                        android.widget.Toast.makeText(
                            itemView.context,
                            "✓ Perjalanan dihapus",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Batal", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        db = AppDatabase.getDatabase(parent.context)
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
        trips = newList
        notifyDataSetChanged()
    }
}

// Extension function for date formatting
fun Long.toFormattedDate(pattern: String = "dd/MM/yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale("id", "ID"))
    return sdf.format(Date(this))
}