package com.travelagent.pos.utils

import android.content.Context
import com.travelagent.pos.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ExportManager(private val context: Context, private val database: AppDatabase) {

    suspend fun exportAllData(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.getExternalFilesDir(null), "TravelAgentExports")
            exportDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                .format(Date())
            val zipFile = File(exportDir, "export_complete_$timestamp.zip")

            ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
                addCsvToZip(zip, "customers.csv", exportCustomers())
                addCsvToZip(zip, "trips.csv", exportTrips())
                addCsvToZip(zip, "bookings.csv", exportBookings())
                addCsvToZip(zip, "payments.csv", exportPayments())
                addCsvToZip(zip, "customer_stats.csv", exportCustomerStats())
                addCsvToZip(zip, "summary.txt", generateSummary())
            }

            Result.success(zipFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun exportCustomers(): String {
        val customers = database.customerDao().getAllCustomers()
        return buildString {
            appendLine("No,Nama Lengkap,Nomor Telepon,Alamat,Tanggal Dibuat")
            customers.forEachIndexed { index, customer ->
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date(customer.createdDate))
                appendLine("${index + 1},\"${customer.namaLengkap}\",\"${customer.nomorTelepon}\",\"${customer.alamat}\",$date")
            }
        }
    }

    private suspend fun exportTrips(): String {
        val trips = database.tripDao().getAllTrips()
        return buildString {
            appendLine("No,Nopol,Asal,Tujuan,Tanggal,Sopir,No. Telp Sopir,Ongkos")
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            trips.forEachIndexed { index, trip ->
                appendLine("${index + 1},\"${trip.nomorPolisi}\",\"${trip.asal}\",\"${trip.tujuan}\",\"${sdf.format(Date(trip.tanggal))}\",\"${trip.namaSopir}\",\"${trip.nomorTeleponSopir}\",${trip.ongkos}")
            }
        }
    }

    private suspend fun exportBookings(): String {
        val tickets = database.ticketDao().getAllTickets()
        return buildString {
            appendLine("No,Pelanggan,No. Telp,Rute,Tanggal,Kursi,Ongkos,Total Dibayar,Status")
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            tickets.forEachIndexed { index, ticket ->
                val customer = database.customerDao().getCustomerById(ticket.customerId)
                val trip = database.tripDao().getTripById(ticket.tripId)
                val seat = database.seatDao().getSeatById(ticket.seatId)

                if (customer != null && trip != null && seat != null) {
                    appendLine("${index + 1},\"${customer.namaLengkap}\",\"${customer.nomorTelepon}\",\"${trip.asal} → ${trip.tujuan}\",\"${sdf.format(Date(trip.tanggal))}\",${seat.nomorKursi},${ticket.ongkos},${ticket.totalPaid},\"${ticket.status}\"")
                }
            }
        }
    }

    private suspend fun exportPayments(): String {
        val payments = database.paymentDao().getRecentPayments()
        return buildString {
            appendLine("No,ID Tiket,Jumlah,Metode,No. Kwitansi,Catatan,Tanggal")
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            payments.forEachIndexed { index, payment ->
                appendLine("${index + 1},${payment.ticketId},${payment.amount},\"${payment.paymentMethod}\",\"${payment.receiptNumber ?: ""}\",\"${payment.notes ?: ""}\",\"${sdf.format(Date(payment.timestamp))}\"")
            }
        }
    }

    private suspend fun exportCustomerStats(): String {
        val stats = database.customerStatsDao().getAllStats()
        return buildString {
            appendLine("ID Pelanggan,Nama,Total Trip,Total Belanja,Poin,Tier,Terakhir Trip")
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            stats.forEach { stat ->
                val customer = database.customerDao().getCustomerById(stat.customerId)
                val lastTrip = stat.lastTripDate?.let { sdf.format(Date(it)) } ?: "-"
                appendLine("${stat.customerId},\"${customer?.namaLengkap ?: "Unknown"}\",${stat.totalTrips},${stat.totalSpent},${stat.loyaltyPoints},\"${stat.tier}\",\"$lastTrip\"")
            }
        }
    }

    private suspend fun generateSummary(): String {
        val totalCustomers = database.customerDao().getAllCustomers().size
        val totalTrips = database.tripDao().getAllTrips().size
        val totalTickets = database.ticketDao().getAllTickets().size
        val totalRevenue = database.ticketDao().getAllTickets()
            .filter { it.status == "paid" }
            .sumOf { it.ongkos }

        return """
            TRAVEL AGENT POS - EXPORT SUMMARY
            ═══════════════════════════════════
            Generated: ${SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale("id")).format(Date())}
            
            STATISTICS:
            • Total Customers: $totalCustomers
            • Total Trips: $totalTrips
            • Total Bookings: $totalTickets
            • Total Revenue: Rp ${String.format("%,d", totalRevenue.toLong())}
            
            FILES INCLUDED:
            • customers.csv - Customer database
            • trips.csv - Trip records
            • bookings.csv - Booking & ticket records
            • payments.csv - Payment history
            • customer_stats.csv - Customer statistics & loyalty tiers
            • summary.txt - This file
        """.trimIndent()
    }

    private fun addCsvToZip(zip: ZipOutputStream, filename: String, content: String) {
        zip.putNextEntry(ZipEntry(filename))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }
}