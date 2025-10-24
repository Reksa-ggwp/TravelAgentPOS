package com.travelagent.pos.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.ActivityCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ThermalPrinterManager(private val context: Context) {

    companion object {
        const val PAPER_WIDTH_58MM = 32 // 58mm = 32 characters
        const val PAPER_WIDTH_80MM = 48 // 80mm = 48 characters
        const val PREF_PAPER_WIDTH = "printer_paper_width"
        const val PREF_PRINTER_ADDRESS = "printer_bluetooth_address"
    }

    private val prefs = context.getSharedPreferences("printer_settings", Context.MODE_PRIVATE)

    fun getPaperWidth(): Int {
        return prefs.getInt(PREF_PAPER_WIDTH, PAPER_WIDTH_80MM)
    }

    fun setPaperWidth(width: Int) {
        prefs.edit().putInt(PREF_PAPER_WIDTH, width).apply()
    }

    fun getSavedPrinterAddress(): String? {
        return prefs.getString(PREF_PRINTER_ADDRESS, null)
    }

    fun savePrinterAddress(address: String) {
        prefs.edit().putString(PREF_PRINTER_ADDRESS, address).apply()
    }

    /**
     * Get list of bonded Bluetooth printers
     */
    fun getPairedBluetoothPrinters(): List<BluetoothDevice> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return emptyList()
            }
        }

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
            bluetoothAdapter.bondedDevices.filter { device ->
                device.name?.contains("printer", ignoreCase = true) == true ||
                        device.name?.contains("POS", ignoreCase = true) == true ||
                        device.name?.contains("RPP", ignoreCase = true) == true ||
                        device.name?.contains("MTP", ignoreCase = true) == true
            }
        } else {
            emptyList()
        }
    }

    /**
     * Print ticket using ESC/POS printer
     */
    suspend fun printTicket(
        ticketText: String,
        printerAddress: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val address = printerAddress ?: getSavedPrinterAddress()
            ?: return@withContext Result.failure(Exception("No printer configured"))

            val printer = getConnectedPrinter(address)
                ?: return@withContext Result.failure(Exception("Failed to connect to printer"))

            val paperWidth = getPaperWidth()

            // Load ticket logo if exists
            val logo = loadTicketLogo()

            val escPosPrinter = EscPosPrinter(
                printer,
                203, // DPI
                if (paperWidth == PAPER_WIDTH_58MM) 48f else 80f, // Paper width in mm
                paperWidth // Characters per line
            )

            // Build print content
            val printContent = buildString {
                // Print logo if available
                if (logo != null) {
                    append("[C]<img>${PrinterTextParserImg.bitmapToHexadecimalString(escPosPrinter, logo)}</img>\n")
                }

                // Print ticket text
                append(ticketText)
            }

            escPosPrinter.printFormattedText(printContent)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Load ticket logo from drawable
     * Logo file should be: res/drawable/ticket_logo.png
     */
    private fun loadTicketLogo(): Bitmap? {
        return try {
            val resourceId = context.resources.getIdentifier(
                "ticket_logo",
                "drawable",
                context.packageName
            )
            if (resourceId != 0) {
                val originalBitmap = BitmapFactory.decodeResource(context.resources, resourceId)
                // Resize logo to fit paper width
                val maxWidth = if (getPaperWidth() == PAPER_WIDTH_58MM) 200 else 300
                val ratio = maxWidth.toFloat() / originalBitmap.width
                val newHeight = (originalBitmap.height * ratio).toInt()
                Bitmap.createScaledBitmap(originalBitmap, maxWidth, newHeight, true)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get connected ESC/POS printer
     */
    private fun getConnectedPrinter(address: String): BluetoothConnection? {
        return try {
            val bluetoothConnection = BluetoothPrintersConnections.selectFirstPaired()
            if (bluetoothConnection != null && bluetoothConnection.device.address == address) {
                bluetoothConnection
            } else {
                // Find specific printer by address
                val devices = getPairedBluetoothPrinters()
                val device = devices.find { it.address == address }
                device?.let { BluetoothConnection(it) }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Test print to verify connection
     */
    suspend fun testPrint(printerAddress: String): Result<Unit> {
        val testText = buildString {
            val paperWidth = getPaperWidth()
            val separator = "=".repeat(paperWidth)

            appendLine("[C]<b>TEST PRINT</b>")
            appendLine(separator)
            appendLine("[C]Paper Size: ${if (paperWidth == PAPER_WIDTH_58MM) "58mm" else "80mm"}")
            appendLine("[C]Characters per line: $paperWidth")
            appendLine(separator)
            appendLine("[C]Date: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id")).format(Date())}")
            appendLine(separator)
            appendLine("[C]<b>Test Successful!</b>")
            appendLine("")
            appendLine("")
            appendLine("")
        }

        return printTicket(testText, printerAddress)
    }

    /**
     * Format ticket text for thermal printing
     */
    fun formatTicketForThermal(
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
        isStamped: Boolean,
        payments: List<String> = emptyList()
    ): String {
        val paperWidth = getPaperWidth()
        val separator = "=".repeat(paperWidth)
        val dashSeparator = "-".repeat(paperWidth)

        return buildString {
            // Header
            appendLine("[C]<b>TIKET PERJALANAN</b>")
            appendLine(separator)
            appendLine("")

            // Ticket info
            appendLine("[L]No. Tiket  : $ticketId")
            appendLine("[L]Tanggal    : ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id")).format(Date())}")
            if (isStamped) {
                appendLine("[C]<b>*** SUDAH DI-STAMP ***</b>")
            }
            appendLine("")

            // Trip details
            appendLine("[L]<b>DETAIL PERJALANAN:</b>")
            appendLine("[L]Asal       : $origin")
            appendLine("[L]Tujuan     : $destination")
            appendLine("[L]Tanggal    : $date")
            appendLine("[L]Nopol      : $plateNumber")
            appendLine("[L]Sopir      : $driverName")
            appendLine("[L]No. Kursi  : $seatNumber")
            appendLine("")

            // Passenger info
            appendLine("[L]<b>PENUMPANG:</b>")
            appendLine("[L]Nama       : $customerName")
            appendLine("[L]No. Telp   : $phone")
            appendLine("[L]Alamat     : $address")
            appendLine("")

            // Payment info
            appendLine("[L]<b>PEMBAYARAN:</b>")
            appendLine("[L]Harga      : Rp ${String.format("%,d", price.toLong()).replace(',', '.')}")
            appendLine("[L]Dibayar    : Rp ${String.format("%,d", totalPaid.toLong()).replace(',', '.')}")

            val remaining = price - totalPaid
            if (remaining > 0) {
                appendLine("[L]Sisa       : Rp ${String.format("%,d", remaining.toLong()).replace(',', '.')}")
                appendLine("[L]Status     : ${status.uppercase()}")
            } else {
                appendLine("[L]<b>Status     : LUNAS</b>")
            }

            // Payment history
            if (payments.isNotEmpty()) {
                appendLine("")
                appendLine("[L]<b>RIWAYAT PEMBAYARAN:</b>")
                payments.forEachIndexed { index, payment ->
                    appendLine("[L]${index + 1}. $payment")
                }
            }

            appendLine("")
            appendLine(separator)

            // Footer
            appendLine("[C]Terima kasih atas kepercayaan")
            appendLine("[C]Anda menggunakan layanan kami!")
            appendLine(separator)
            appendLine("[C]<font size='small'>Simpan tiket ini sebagai")
            appendLine("[C]bukti pembayaran yang sah</font>")
            appendLine("")
            appendLine("")
            appendLine("")
        }
    }
}