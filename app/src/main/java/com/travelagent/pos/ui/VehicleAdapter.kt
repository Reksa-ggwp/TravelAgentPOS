package com.travelagent.pos.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.travelagent.pos.R
import com.travelagent.pos.data.Vehicle

class VehicleAdapter(
    private var vehicles: MutableList<Vehicle>,
    private val onItemClick: (Vehicle) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNo: TextView = itemView.findViewById(R.id.tvNo)
        val tvPlate: TextView = itemView.findViewById(R.id.tvPlate)

        fun bind(vehicle: Vehicle, position: Int) {
            tvNo.text = (position + 1).toString()
            tvPlate.text = vehicle.nomorPolisi
            itemView.setOnClickListener { onItemClick(vehicle) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vehicle, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(vehicles[position], position)
    }

    override fun getItemCount() = vehicles.size

    fun updateList(newList: MutableList<Vehicle>) {
        vehicles = newList
        notifyDataSetChanged()
    }
}