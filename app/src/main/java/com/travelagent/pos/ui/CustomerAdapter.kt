package com.travelagent.pos.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.travelagent.pos.data.Customer
import com.travelagent.pos.databinding.ItemCustomerBinding

class CustomerAdapter(
    private var customers: MutableList<Customer>,
    private val onItemClick: (Customer) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemCustomerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(customer: Customer, position: Int) {
            binding.apply {
                // Set initial (first letter of name)
                val initial = customer.namaLengkap.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                tvInitial.text = initial

                // Set customer data
                tvCustomerName.text = customer.namaLengkap
                tvCustomerPhone.text = customer.nomorTelepon
                tvCustomerAddress.text = customer.alamat

                // Click listener
                root.setOnClickListener {
                    onItemClick(customer)
                }

                // Add ripple effect on click
                root.isClickable = true
                root.isFocusable = true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCustomerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(customers[position], position)
    }

    override fun getItemCount() = customers.size

    fun updateList(newList: MutableList<Customer>) {
        customers = newList
        notifyDataSetChanged()
    }

    // Optional: Add DiffUtil for better performance
    fun updateListWithDiff(newList: MutableList<Customer>) {
        val diffCallback = CustomerDiffCallback(customers, newList)
        val diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(diffCallback)

        customers.clear()
        customers.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}

// DiffUtil Callback for efficient updates
class CustomerDiffCallback(
    private val oldList: List<Customer>,
    private val newList: List<Customer>
) : androidx.recyclerview.widget.DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}