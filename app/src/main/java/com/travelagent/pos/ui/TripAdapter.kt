package com.travelagent.pos.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.travelagent.pos.R
import com.travelagent.pos.data.Trip
import com.travelagent.pos.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TripAdapter(
    private var trips: MutableList<Trip>,
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

            // Calculate available seats
            GlobalScope.launch(Dispatchers.Main) {
                val seats = db.seatDao().getSeatsByTrip(trip.id)
                val available = seats.count { it.status == "available" }
                tvAvailable.text = available.toString()
            }

            itemView.setOnClickListener { onItemClick(trip) }
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