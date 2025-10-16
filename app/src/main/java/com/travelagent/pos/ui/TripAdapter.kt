package com.travelagent.pos.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.travelagent.pos.R
import com.travelagent.pos.data.Trip
import java.text.SimpleDateFormat
import java.util.*

class TripAdapter(
    private var trips: MutableList<Trip>,
    private val onItemClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvNo: TextView = itemView.findViewById(R.id.tvNo)
        val tvRoute: TextView = itemView.findViewById(R.id.tvRoute)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvDriver: TextView = itemView.findViewById(R.id.tvDriver)

        fun bind(trip: Trip, position: Int) {
            tvNo.text = (position + 1).toString()
            tvRoute.text = "${trip.asal} → ${trip.tujuan}"
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            tvDate.text = sdf.format(Date(trip.tanggal))
            tvDriver.text = trip.namaSopir
            itemView.setOnClickListener { onItemClick(trip) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
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