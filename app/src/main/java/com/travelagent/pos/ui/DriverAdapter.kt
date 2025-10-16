package com.travelagent.pos.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.travelagent.pos.R
import com.travelagent.pos.data.Driver

class DriverAdapter(
    private var drivers: MutableList<Driver>,
    private val onItemClick: (Driver) -> Unit
) : RecyclerView.Adapter<DriverAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNo: TextView = itemView.findViewById(R.id.tvNo)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)

        fun bind(driver: Driver, position: Int) {
            tvNo.text = (position + 1).toString()
            tvName.text = driver.namaSopir
            tvPhone.text = driver.nomorTelepon
            itemView.setOnClickListener { onItemClick(driver) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_driver, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(drivers[position], position)
    }

    override fun getItemCount() = drivers.size

    fun updateList(newList: MutableList<Driver>) {
        drivers = newList
        notifyDataSetChanged()
    }
}