package com.travelagent.pos.ui
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.widget.ArrayAdapter
import com.travelagent.pos.utils.ErrorHandler
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.databinding.ActivityReportsBinding
import com.travelagent.pos.utils.ReportManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
class ReportsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportsBinding
    private lateinit var reportManager: ReportManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val db = AppDatabase.getDatabase(this)
        reportManager = ReportManager(db)
        setupSpinners()
        binding.btnGenerateDaily.setOnClickListener { generateDailyReport() }
        binding.btnGenerateMonthly.setOnClickListener { generateMonthlyReport() }
        binding.btnBack.setOnClickListener { finish() }
    }
    private fun setupSpinners() {
        // Month spinner
        val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        binding.spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        // Year spinner
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear).map { it.toString() }.toTypedArray()
        binding.spinnerYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        binding.spinnerYear.setSelection(years.size - 1)
    }
    private fun generateDailyReport() {
        lifecycleScope.launch {
            try {
                val report = withContext(Dispatchers.IO) {
                    reportManager.generateDailyReport(System.currentTimeMillis())
                }
                val reportText = """
 LAPORAN HARIAN
 ═══════════════════════════════
 Total Booking: ${report.totalBookings}
 - Paid: ${report.paidBookings}
 - Pending: ${report.pendingBookings}
 - Partial: ${report.partialBookings}

 Total Perjalanan: ${report.tripCount}
 Total Pendapatan: Rp ${String.format("%,d", report.totalRevenue.toLong())}
 """.trimIndent()
                binding.tvReportResult.text = reportText
            } catch (e: Exception) {
                ErrorHandler.showError(this@ReportsActivity, "Error: ${e.message}")
            }
        }
    }
    private fun generateMonthlyReport() {
        val month = binding.spinnerMonth.selectedItemPosition + 1
        val year = binding.spinnerYear.selectedItem.toString().toInt()
        lifecycleScope.launch {
            try {
                val report = withContext(Dispatchers.IO) {
                    reportManager.generateMonthlyReport(month, year)
                }
                val reportText = """
 LAPORAN BULANAN
 ═══════════════════════════════
 Bulan: $month/$year

 Total Booking: ${report.totalBookings}
 Total Perjalanan: ${report.totalTrips}
 Total Pendapatan: Rp ${String.format("%,d", report.totalRevenue.toLong())}

 Rata-rata Per Hari: Rp ${String.format("%,d", report.averageDailyRevenue.toLong())}
CustomerStatsActivity.kt (NEW)
 Rute Terpopuler: ${report.topRoute}
 Occupancy Rate: ${String.format("%.1f", report.occupancyRate)}%
 """.trimIndent()
                binding.tvReportResult.text = reportText
            } catch (e: Exception) {
                ErrorHandler.showError(this@ReportsActivity, "Error: ${e.message}")
            }
        }
    }
}
