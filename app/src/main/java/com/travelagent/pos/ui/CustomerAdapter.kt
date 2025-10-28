package com.travelagent.pos.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.travelagent.pos.data.Customer
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.databinding.ItemCustomerBinding
import com.travelagent.pos.utils.ErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerAdapter(
    private var customers: MutableList<Customer>,
    private val onItemClick: (Customer) -> Unit,
    private val onDeleteSuccess: () -> Unit // Callback to refresh list after delete
) : RecyclerView.Adapter<CustomerAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemCustomerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(customer: Customer, @Suppress("UNUSED_PARAMETER") position: Int) {
            binding.apply {
                // Set customer data
                tvCustomerName.text = customer.namaLengkap
                tvCustomerPhone.text = customer.nomorTelepon
                tvCustomerAddress.text = customer.alamat

                // Click listener
                root.setOnClickListener {
                    onItemClick(customer)
                }

                // Long click listener for delete
                root.setOnLongClickListener {
                    showDeleteCustomerDialog(customer)
                    true
                }

                // Add ripple effect on click
                root.isClickable = true
                root.isFocusable = true
            }
        }

        private fun showDeleteCustomerDialog(customer: Customer) {
            val context = itemView.context
            val db = AppDatabase.getDatabase(context)
            val scope = CoroutineScope(Dispatchers.IO)

            // Check if customer has any bookings
            scope.launch {
                val tickets = db.ticketDao().getAllTickets().filter { it.customerId == customer.id }
                val hasBookings = tickets.isNotEmpty()
                val paidCount = tickets.count { it.status == "paid" }

                withContext(Dispatchers.Main) {
                    // Cannot delete if customer has paid tickets
                    if (paidCount > 0) {
                        ErrorHandler.handleValidationError(
                            context,
                            "Pelanggan ${customer.namaLengkap} memiliki $paidCount tiket yang sudah dibayar.\n\nUntuk menjaga integritas data, pelanggan dengan riwayat pembayaran tidak dapat dihapus."
                        )
                        return@withContext
                    }

                    // Show appropriate warning message
                    val message = if (hasBookings) {
                        "Yakin ingin menghapus ${customer.namaLengkap}?\n\n" +
                                "⚠️ PERHATIAN:\n" +
                                "• Pelanggan ini memiliki ${tickets.size} booking\n" +
                                "• Semua booking akan dihapus\n" +
                                "• Data tidak dapat dikembalikan"
                    } else {
                        "Yakin ingin menghapus ${customer.namaLengkap}?\n\n" +
                                "Data tidak dapat dikembalikan."
                    }

                    ErrorHandler.showConfirmationDialog(
                        context,
                        "Hapus Pelanggan?",
                        message
                    ) {
                        performDelete(customer, tickets, db)
                    }
                }
            }
        }

        private fun performDelete(
            customer: Customer,
            tickets: List<com.travelagent.pos.data.Ticket>,
            db: AppDatabase
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Delete all associated tickets first (cascade delete)
                    tickets.forEach { ticket ->
                        db.ticketDao().delete(ticket)
                    }

                    // Delete the customer
                    db.customerDao().delete(customer)

                    withContext(Dispatchers.Main) {
                        // Remove from local list
                        val position = customers.indexOf(customer)
                        if (position != -1) {
                            customers.removeAt(position)
                            notifyItemRemoved(position)
                        }
                        ErrorHandler.showSuccess(itemView.context, "Pelanggan dihapus")
                        // Callback to refresh if needed
                        onDeleteSuccess()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        ErrorHandler.handleOperationError(itemView.context, "menghapus pelanggan", e)
                    }
                }
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
        // Use DiffUtil to compute minimal updates for smoother UI
        val diffCallback = CustomerDiffCallback(customers.toList(), newList)
        val diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(diffCallback)

        customers.clear()
        customers.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
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