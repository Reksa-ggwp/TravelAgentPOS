package com.travelagent.pos.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TicketExportManager(private val context: Context) {

    private val exportDir = File(context.getExternalFilesDir(null), "TicketExports")

    init {
        exportDir.mkdirs()
    }

    /**
     * Export ticket to PDF format
     */
    suspend fun exportTicketToPDF(
        ticketId: Int,
        customerName: String,
        phone: String,
        address: String,
        origin: String,
        destination: String,
        date: String,
        plateNumber: String,
        driverName: String,
        seatNumber: Int,
        price: Double,
        totalPaid: Double,
        status: String,
        isStamped: Boolean
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Ticket_${ticketId}_$timestamp.pdf"
            val pdfFile = File(exportDir, fileName)

            val pdfWriter = PdfWriter(pdfFile)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Header
            val header = Paragraph("TIKET PERJALANAN")
                .setFontSize(20f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
            document.add(header)

            val companyName = Paragraph("TRAVEL AGENT POS")
                .setFontSize(14f)
                .setTextAlignment(TextAlignment.CENTER)
            document.add(companyName)

            document.add(Paragraph("\n"))

            // Ticket Info
            val ticketInfoTable = Table(2)
            ticketInfoTable.addCell("No. Tiket:")
            ticketInfoTable.addCell(ticketId.toString())
            ticketInfoTable.addCell("Tanggal Cetak:")
            ticketInfoTable.addCell(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id")).format(Date()))

            if (isStamped) {
                ticketInfoTable.addCell("Status:")
                ticketInfoTable.addCell("SUDAH DI-STAMP").setBackgroundColor(ColorConstants.LIGHT_GRAY)
            }

            document.add(ticketInfoTable)
            document.add(Paragraph("\n"))

            // Trip Details
            document.add(Paragraph("DETAIL PERJALANAN").setBold().setFontSize(12f))
            val tripTable = Table(2)
            tripTable.addCell("Asal:")
            tripTable.addCell(origin)
            tripTable.addCell("Tujuan:")
            tripTable.addCell(destination)
            tripTable.addCell("Tanggal:")
            tripTable.addCell(date)
            tripTable.addCell("Nomor Polisi:")
            tripTable.addCell(plateNumber)
            tripTable.addCell("Sopir:")
            tripTable.addCell(driverName)
            tripTable.addCell("Nomor Kursi:")
            tripTable.addCell(seatNumber.toString())
            document.add(tripTable)

            document.add(Paragraph("\n"))

            // Passenger Info
            document.add(Paragraph("PENUMPANG").setBold().setFontSize(12f))
            val passengerTable = Table(2)
            passengerTable.addCell("Nama:")
            passengerTable.addCell(customerName)
            passengerTable.addCell("No. Telepon:")
            passengerTable.addCell(phone)
            passengerTable.addCell("Alamat:")
            passengerTable.addCell(address)
            document.add(passengerTable)

            document.add(Paragraph("\n"))

            // Payment Info
            document.add(Paragraph("PEMBAYARAN").setBold().setFontSize(12f))
            val paymentTable = Table(2)
            paymentTable.addCell("Harga:")
            paymentTable.addCell(formatCurrency(price))
            paymentTable.addCell("Dibayar:")
            paymentTable.addCell(formatCurrency(totalPaid))

            val remaining = price - totalPaid
            if (remaining > 0) {
                paymentTable.addCell("Sisa:")
                paymentTable.addCell(formatCurrency(remaining))
                paymentTable.addCell("Status:")
                paymentTable.addCell(status.uppercase())
            } else {
                paymentTable.addCell("Status:")
                paymentTable.addCell("LUNAS").setBackgroundColor(ColorConstants.GREEN).setBold()
            }
            document.add(paymentTable)

            document.add(Paragraph("\n\n"))

            // Footer
            val footer = Paragraph("Terima kasih atas kepercayaan Anda menggunakan layanan kami!")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10f)
                .setItalic()
            document.add(footer)

            val footer2 = Paragraph("Simpan tiket ini sebagai bukti pembayaran yang sah")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(8f)
            document.add(footer2)

            document.close()

            Result.success(pdfFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Share exported file
     */
    fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = when (file.extension) {
                "pdf" -> "application/pdf"
                "txt" -> "text/plain"
                else -> "*/*"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Ticket"))
    }

    /**
     * Share ticket as text
     */
    fun shareText(text: String, filename: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, filename)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Ticket"))
    }

    /**
     * Get all exported tickets
     */
    fun getAllExports(): List<File> {
        return exportDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * Delete old exports (keep last 50)
     */
    fun cleanupOldExports() {
        val files = getAllExports()
        files.drop(50).forEach { it.delete() }
    }

    private fun formatCurrency(amount: Double): String {
        return "Rp ${String.format("%,d", amount.toLong()).replace(',', '.')}"
    }
}