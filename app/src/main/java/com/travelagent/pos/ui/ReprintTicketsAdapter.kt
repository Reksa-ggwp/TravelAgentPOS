package com.travelagent.pos.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.travelagent.pos.databinding.ItemReprintTicketBinding
import java.text.SimpleDateFormat
import java.util.*

class ReprintTicketsAdapter(
    private val onItemClick: (ReprintTicketItem) -> Unit
) : ListAdapter<ReprintTicketItem, ReprintTicketsAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemReprintTicketBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReprintTicketItem) {
            binding.apply {
                tvTicketId.text = "Ticket #${item.ticket.id}"
                tvCustomerName.text = item.customerName
                tvRoute.text = item.route
                tvSeatNumber.text = "Seat ${item.seatNumber}"
                tvPrice.text = formatCurrency(item.ticket.ongkos)
                tvDate.text = formatDate(item.date)

                // Status badge
                val statusText = when (item.ticket.status) {
                    "paid" -> "✅ LUNAS"
                    "partial" -> "💰 PARTIAL"
                    "pending" -> "⏳ PENDING"
                    else -> item.ticket.status.uppercase()
                }
                tvStatus.text = statusText

                // Stamp indicator
                if (item.ticket.isStamped) {
                    chipStamped.visibility = android.view.View.VISIBLE
                } else {
                    chipStamped.visibility = android.view.View.GONE
                }

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }

        private fun formatCurrency(amount: Double): String {
            return "Rp ${String.format("%,d", amount.toLong()).replace(',', '.')}"
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id"))
            return sdf.format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReprintTicketBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ReprintTicketItem>() {
        override fun areItemsTheSame(oldItem: ReprintTicketItem, newItem: ReprintTicketItem): Boolean {
            return oldItem.ticket.id == newItem.ticket.id
        }

        override fun areContentsTheSame(oldItem: ReprintTicketItem, newItem: ReprintTicketItem): Boolean {
            return oldItem == newItem
        }
    }
}