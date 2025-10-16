package com.travelagent.pos.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.travelagent.pos.R
import com.travelagent.pos.data.Customer

class CustomerAdapter(
    private var customers: MutableList<Customer>,
    private val onItemClick: (Customer) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvNo: TextView = itemView.findViewById(R.id.tvNo)
        val tvName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvPhone: TextView = itemView.findViewById(R.id.tvCustomerPhone)
        val tvAddress: TextView = itemView.findViewById(R.id.tvCustomerAddress)

        fun bind(customer: Customer, position: Int) {
            tvNo.text = (position + 1).toString()
            tvName.text = customer.namaLengkap
            tvPhone.text = customer.nomorTelepon
            tvAddress.text = customer.alamat
            itemView.setOnClickListener { onItemClick(customer) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(customers[position], position)
    }

    override fun getItemCount() = customers.size

    fun updateList(newList: MutableList<Customer>) {
        customers = newList
        notifyDataSetChanged()
    }
}