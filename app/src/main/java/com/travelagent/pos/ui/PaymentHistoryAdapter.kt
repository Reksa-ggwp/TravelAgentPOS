package com.travelagent.pos.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.travelagent.pos.databinding.ItemPaymentHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class PaymentHistoryAdapter(
    private val onItemClick: (PaymentHistoryItem) -> Unit = {}
) : ListAdapter<PaymentHistoryItem, PaymentHistoryAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemPaymentHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PaymentHistoryItem) {
            binding.apply {
                tvCustomerName.text = item.customerName
                tvRoute.text = item.route
                tvAmount.text = formatCurrency(item.payment.amount)
                tvPaymentMethod.text = getMethodText(item.payment.paymentMethod)
                tvDate.text = formatDate(item.payment.timestamp)

                if (!item.payment.notes.isNullOrEmpty()) {
                    tvNotes.text = "Catatan: ${item.payment.notes}"
                    tvNotes.visibility = android.view.View.VISIBLE
                } else {
                    tvNotes.visibility = android.view.View.GONE
                }

                // NEW: Click handler to reprint ticket
                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }

        private fun formatCurrency(amount: Double): String {
            return "Rp ${String.format("%,d", amount.toLong()).replace(',', '.')}"
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id"))
            return sdf.format(Date(timestamp))
        }

        private fun getMethodText(method: String): String {
            return when (method.lowercase()) {
                "cash" -> "💵 Cash"
                "transfer" -> "🏦 Transfer"
                "e-wallet" -> "📱 E-Wallet"
                else -> method
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPaymentHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<PaymentHistoryItem>() {
        override fun areItemsTheSame(oldItem: PaymentHistoryItem, newItem: PaymentHistoryItem): Boolean {
            return oldItem.payment.id == newItem.payment.id
        }

        override fun areContentsTheSame(oldItem: PaymentHistoryItem, newItem: PaymentHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}