package com.travelagent.pos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import com.travelagent.pos.databinding.ActivityBackupRestoreBinding
import com.travelagent.pos.utils.BackupManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BackupRestoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBackupRestoreBinding
    private lateinit var backupManager: BackupManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupRestoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        backupManager = BackupManager(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnBackup.setOnClickListener { createBackup() }
        binding.btnRestore.setOnClickListener { showRestoreDialog() }
        binding.btnViewBackups.setOnClickListener { viewBackups() }
    }

    private fun createBackup() {
        lifecycleScope.launch {
            try {
                val result = backupManager.createBackup(isAuto = false)
                result.fold(
                    onSuccess = { file ->
                        Toast.makeText(
                            this@BackupRestoreActivity,
                            "✓ Backup berhasil!\n${file.name}",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            this@BackupRestoreActivity,
                            "❌ Backup gagal: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this@BackupRestoreActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showRestoreDialog() {
        val backups = backupManager.getAllBackups()

        if (backups.isEmpty()) {
            Toast.makeText(this, "Tidak ada backup tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fileNames = backups.map {
            "${sdf.format(it.date)} (${it.sizeKB} KB) ${if (it.isAuto) "[Auto]" else ""}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Pilih Backup untuk Restore")
            .setItems(fileNames) { _, which ->
                confirmRestore(backups[which])
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmRestore(backupInfo: com.travelagent.pos.utils.BackupInfo) {
        AlertDialog.Builder(this)
            .setTitle("Restore Database?")
            .setMessage("PERHATIAN: Semua data saat ini akan diganti dengan data dari backup.\n\nBackup: ${backupInfo.name}")
            .setPositiveButton("Restore") { _, _ ->
                restoreBackup(backupInfo)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun restoreBackup(backupInfo: com.travelagent.pos.utils.BackupInfo) {
        lifecycleScope.launch {
            try {
                val result = backupManager.restoreBackup(backupInfo.file)
                result.fold(
                    onSuccess = {
                        Toast.makeText(
                            this@BackupRestoreActivity,
                            "✓ Restore berhasil!\nSilakan restart aplikasi.",
                            Toast.LENGTH_LONG
                        ).show()
                        finishAffinity()
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            this@BackupRestoreActivity,
                            "❌ Restore gagal: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this@BackupRestoreActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun viewBackups() {
        val backups = backupManager.getAllBackups()

        if (backups.isEmpty()) {
            Toast.makeText(this, "Tidak ada backup tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val message = backups.joinToString("\n\n") {
            "📁 ${it.name}\n" +
                    "📅 ${sdf.format(it.date)}\n" +
                    "💾 ${it.sizeKB} KB" +
                    if (it.isAuto) " [Auto]" else ""
        }

        AlertDialog.Builder(this)
            .setTitle("Daftar Backup (${backups.size})")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}