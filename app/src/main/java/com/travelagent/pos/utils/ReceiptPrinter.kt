package com.travelagent.pos.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.travelagent.pos.data.Customer
import com.travelagent.pos.data.Seat
import com.travelagent.pos.data.Ticket
import com.travelagent.pos.data.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ReceiptPrinter(private val context: Context) {

    fun generateReceipt(
        ticket: Ticket,
        trip: Trip,
        customer: Customer,
        seat: Seat
    ): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id"))

        return buildString {
            appendLine("================================")
            appendLine("     TRAVEL AGENT POS")
            appendLine("================================")
            appendLine()
            appendLine("TIKET PERJALANAN")
            appendLine("--------------------------------")
            appendLine("No. Tiket  : ${ticket.id}")
            appendLine("Tanggal    : ${sdf.format(Date())}")
            appendLine()
            appendLine("DETAIL PERJALANAN:")
            appendLine("Asal       : ${trip.asal}")
            appendLine("Tujuan     : ${trip.tujuan}")
            appendLine("Tanggal    : ${sdf.format(Date(trip.tanggal))}")
            appendLine("Nopol      : ${trip.nomorPolisi}")
            appendLine("Sopir      : ${trip.namaSopir}")
            appendLine("No. Kursi  : ${seat.nomorKursi}")
            appendLine()
            appendLine("PENUMPANG:")
            appendLine("Nama       : ${customer.namaLengkap}")
            appendLine("No. Telp   : ${customer.nomorTelepon}")
            appendLine()
            appendLine("PEMBAYARAN:")
            appendLine("Harga      : Rp ${String.format("%,d", ticket.ongkos.toLong())}")
            appendLine("Dibayar    : Rp ${String.format("%,d", ticket.totalPaid.toLong())}")
            if (ticket.totalPaid < ticket.ongkos) {
                appendLine("Sisa       : Rp ${String.format("%,d", (ticket.ongkos - ticket.totalPaid).toLong())}")
            }
            appendLine("Status     : ${ticket.status.uppercase()}")
            appendLine()
            appendLine("================================")
            appendLine("  Terima kasih atas kepercayaan")
            appendLine("       Anda menggunakan")
            appendLine("         layanan kami!")
            appendLine("================================")
            appendLine()
            appendLine()
            appendLine()
        }
    }

    fun shareReceipt(receipt: String) {
        val file = File(context.getExternalFilesDir(null), "receipt_${System.currentTimeMillis()}.txt")
        file.writeText(receipt)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, receipt)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Tiket"))
    }
}