package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import androidx.core.content.FileProvider
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.databinding.ActivityExportBinding
import com.travelagent.pos.utils.ExportManager
import kotlinx.coroutines.launch

class ExportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExportBinding
    private lateinit var exportManager: ExportManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getDatabase(this)
        exportManager = ExportManager(this, db)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnExportCustomers.setOnClickListener { exportAllData() }
        binding.btnExportTrips.setOnClickListener { exportAllData() }
        binding.btnExportBookings.setOnClickListener { exportAllData() }
    }

    private fun exportAllData() {
        lifecycleScope.launch {
            try {
                val result = exportManager.exportAllData()
                result.fold(
                    onSuccess = { file ->
                        Toast.makeText(
                            this@ExportActivity,
                            "✓ Export berhasil!\n${file.name}",
                            Toast.LENGTH_LONG
                        ).show()

                        // Share file
                        val uri = FileProvider.getUriForFile(
                            this@ExportActivity,
                            "${packageName}.fileprovider",
                            file
                        )

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/zip"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        startActivity(Intent.createChooser(shareIntent, "Bagikan Export"))
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            this@ExportActivity,
                            "❌ Export gagal: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this@ExportActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}