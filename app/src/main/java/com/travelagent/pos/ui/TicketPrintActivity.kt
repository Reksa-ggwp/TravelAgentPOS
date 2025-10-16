package com.travelagent.pos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.widget.*
import com.travelagent.pos.R
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Ticket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TicketPrintActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private var ticketId: Int = 0
    private var isStamped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_print)

        db = AppDatabase.getDatabase(this)
        ticketId = intent.getIntExtra("ticketId", 0)

        val btnPrint = findViewById<Button>(R.id.btnPrint)
        val btnStamp = findViewById<Button>(R.id.btnStamp)
        val btnMarkPaid = findViewById<Button>(R.id.btnMarkPaid)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnPrint.setOnClickListener { printTicket() }
        btnStamp.setOnClickListener {
            isStamped = !isStamped
            btnStamp.text = if (isStamped) "✓ STAMPED" else "Stamp Tiket"
        }
        btnMarkPaid.setOnClickListener { markAsPaid() }
        btnBack.setOnClickListener { finish() }

        loadTicketDetails()
    }

    private fun loadTicketDetails() {
        GlobalScope.launch(Dispatchers.Main) {
            val ticket = db.ticketDao().getAllTickets().find { it.id == ticketId } ?: return@launch
            val trip = db.tripDao().getTripById(ticket.tripId)!!
            val customer = db.customerDao().getCustomerById(ticket.customerId)!!
            val seat = db.seatDao().getSeatById(ticket.seatId)!!

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            val ticketPreview = """
═══════════════════════════════
    TIKET PERJALANAN
═══════════════════════════════
Asal      : ${trip.asal}
Tujuan    : ${trip.tujuan}
Tanggal   : ${sdf.format(Date(trip.tanggal))}
Kursi     : ${seat.nomorKursi}
Polisi    : ${trip.nomorPolisi}
Harga     : Rp ${String.format("%,.0f", ticket.ongkos)}
───────────────────────────────
Nama      : ${customer.namaLengkap}
No. Tlp   : ${customer.nomorTelepon}
Alamat    : ${customer.alamat}
═══════════════════════════════
Status    : ${ticket.status}
            """.trimIndent()

            findViewById<TextView>(R.id.tvTicketPreview).text = ticketPreview
        }
    }

    private fun printTicket() {
        Toast.makeText(this, "Fitur print akan diintegrasikan dengan thermal printer", Toast.LENGTH_SHORT).show()
    }

    private fun markAsPaid() {
        GlobalScope.launch(Dispatchers.Main) {
            val ticket = db.ticketDao().getAllTickets().find { it.id == ticketId } ?: return@launch
            db.ticketDao().update(ticket.copy(status = "paid", isStamped = isStamped))
            db.seatDao().update(db.seatDao().getSeatById(ticket.seatId)!!.copy(status = "paid"))

            Toast.makeText(this@TicketPrintActivity, "Tiket ditandai sebagai PAID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}