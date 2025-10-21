package com.travelagent.pos.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.travelagent.pos.R
import com.travelagent.pos.data.Trip
import com.travelagent.pos.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TripAdapter(
    private var trips: MutableList<Trip>,
    private val scope: CoroutineScope,
    private val onItemClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripAdapter.ViewHolder>() {

    private lateinit var db: AppDatabase

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvNo: TextView = itemView.findViewById(R.id.tvNo)
        val tvPlate: TextView = itemView.findViewById(R.id.tvPlate)
        val tvRoute: TextView = itemView.findViewById(R.id.tvRoute)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvDriver: TextView = itemView.findViewById(R.id.tvDriver)
        val tvAvailable: TextView = itemView.findViewById(R.id.tvAvailable)

        fun bind(trip: Trip, position: Int) {
            tvNo.text = (position + 1).toString()
            tvPlate.text = trip.nomorPolisi
            tvRoute.text = "${trip.asal} → ${trip.tujuan}"
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            tvDate.text = sdf.format(Date(trip.tanggal))
            tvDriver.text = trip.namaSopir

            // Calculate available seats (use provided scope to respect lifecycle)
            scope.launch {
                val seats = db.seatDao().getSeatsByTrip(trip.id)
                val available = seats.count { it.status == "available" }
                tvAvailable.text = available.toString()
            }

            // Regular click - open details
            itemView.setOnClickListener { onItemClick(trip) }

            // Long click - show edit/delete options
            itemView.setOnLongClickListener {
                showTripOptions(trip)
                true
            }
        }

        private fun showTripOptions(trip: Trip) {
            val options = arrayOf("Edit", "Hapus")
            AlertDialog.Builder(itemView.context)
                .setTitle("${trip.asal} → ${trip.tujuan}")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> editTrip(trip)
                        1 -> deleteTrip(trip)
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        private fun editTrip(trip: Trip) {
            val intent = Intent(itemView.context, AddTripActivity::class.java)
            intent.putExtra("tripId", trip.id)
            itemView.context.startActivity(intent)
        }

        private fun deleteTrip(trip: Trip) {
            AlertDialog.Builder(itemView.context)
                .setTitle("Hapus Perjalanan?")
                .setMessage("Yakin ingin menghapus perjalanan ${trip.asal} \u2192 ${trip.tujuan}?\n\nSemua data kursi dan booking akan ikut terhapus.")
                .setPositiveButton("Hapus") { _, _ ->
                    scope.launch {
                        db.tripDao().delete(trip)
                        trips.remove(trip)
                        notifyDataSetChanged()
                        android.widget.Toast.makeText(
                            itemView.context,
                            "\u2713 Perjalanan dihapus",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        db = AppDatabase.getDatabase(parent.context)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip, parent, false)
        return ViewHolder(view)
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