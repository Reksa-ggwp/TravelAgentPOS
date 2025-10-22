package com.travelagent.pos.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.travelagent.pos.R
import com.travelagent.pos.data.CustomerStats
import java.text.SimpleDateFormat
import java.util.*

class CustomerStatsAdapter(
    private var stats: List<CustomerStatsWithName>,
    private val onItemClick: (CustomerStatsWithName) -> Unit = {}
) : RecyclerView.Adapter<CustomerStatsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvTier: TextView = itemView.findViewById(R.id.tvTier)
        val tvTrips: TextView = itemView.findViewById(R.id.tvTrips)
        val tvSpent: TextView = itemView.findViewById(R.id.tvSpent)
        val tvPoints: TextView = itemView.findViewById(R.id.tvPoints)
        val tvLastTrip: TextView = itemView.findViewById(R.id.tvLastTrip)

        fun bind(item: CustomerStatsWithName, @Suppress("UNUSED_PARAMETER") position: Int) {
            tvName.text = item.customerName
            tvTier.text = getTierEmoji(item.stats.tier) + " " + item.stats.tier
            tvTrips.text = "${item.stats.totalTrips} trip"
            tvSpent.text = "Rp ${String.format("%,d", item.stats.totalSpent.toLong())}"
            tvPoints.text = "${item.stats.loyaltyPoints} poin"

            val lastTripText = if (item.stats.lastTripDate != null) {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date(item.stats.lastTripDate))
            } else {
                "Belum ada"
            }
            tvLastTrip.text = lastTripText

            itemView.setOnClickListener { onItemClick(item) }
        }

        private fun getTierEmoji(tier: String): String {
            return when (tier) {
                "Platinum" -> "💎"
                "Gold" -> "🥇"
                "Silver" -> "🥈"
                "Bronze" -> "🥉"
                else -> "⭐"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer_stats, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stats[position], position)
    }

    override fun getItemCount() = stats.size

    fun updateList(newList: List<CustomerStatsWithName>) {
        stats = newList
        notifyDataSetChanged()
    }
}

data class CustomerStatsWithName(
    val stats: CustomerStats,
    val customerName: String
)