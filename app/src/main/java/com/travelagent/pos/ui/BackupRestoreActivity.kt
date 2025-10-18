package com.travelagent.pos.ui

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.widget.*
import com.travelagent.pos.R
import com.travelagent.pos.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class BackupRestoreActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup_restore)

        db = AppDatabase.getDatabase(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnBackup).setOnClickListener { createBackup() }
        findViewById<Button>(R.id.btnRestore).setOnClickListener { showRestoreDialog() }
        findViewById<Button>(R.id.btnViewBackups).setOnClickListener { viewBackups() }
    }

    private fun createBackup() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                // Get database path
                val dbPath = getDatabasePath("travel_agent_db").absolutePath

                // Create backup directory
                val backupDir = File(getExternalFilesDir(null), "TravelAgentBackups")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }

                // Create backup file with timestamp
                val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                val timestamp = sdf.format(Date())
                val backupFile = File(backupDir, "backup_$timestamp.db")

                // Copy database
                FileInputStream(dbPath).use { input ->
                    FileOutputStream(backupFile).use { output ->
                        input.copyTo(output)
                    }
                }

                Toast.makeText(
                    this@BackupRestoreActivity,
                    "✓ Backup berhasil!\nLokasi: ${backupFile.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    this@BackupRestoreActivity,
                    "❌ Backup gagal: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }

    private fun showRestoreDialog() {
        val backupDir = File(getExternalFilesDir(null), "TravelAgentBackups")

        if (!backupDir.exists() || backupDir.listFiles()?.isEmpty() == true) {
            Toast.makeText(this, "Tidak ada backup tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val backupFiles = backupDir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
        val fileNames = backupFiles.map {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            "${sdf.format(Date(it.lastModified()))} (${it.length() / 1024} KB)"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Pilih Backup untuk Restore")
            .setItems(fileNames) { _, which ->
                confirmRestore(backupFiles[which])
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmRestore(backupFile: File) {
        AlertDialog.Builder(this)
            .setTitle("Restore Database?")
            .setMessage("PERHATIAN: Semua data saat ini akan diganti dengan data dari backup. Proses ini tidak dapat dibatalkan!\n\nBackup: ${backupFile.name}")
            .setPositiveButton("Restore") { _, _ ->
                restoreBackup(backupFile)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun restoreBackup(backupFile: File) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                // Close database
                AppDatabase.getDatabase(this@BackupRestoreActivity).close()

                // Get current database path
                val dbPath = getDatabasePath("travel_agent_db")

                // Copy backup to database
                FileInputStream(backupFile).use { input ->
                    FileOutputStream(dbPath).use { output ->
                        input.copyTo(output)
                    }
                }

                Toast.makeText(
                    this@BackupRestoreActivity,
                    "✓ Database berhasil direstore!\nSilakan restart aplikasi.",
                    Toast.LENGTH_LONG
                ).show()

                // Exit app so user can restart
                finishAffinity()

            } catch (e: Exception) {
                Toast.makeText(
                    this@BackupRestoreActivity,
                    "❌ Restore gagal: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }

    private fun viewBackups() {
        val backupDir = File(getExternalFilesDir(null), "TravelAgentBackups")

        if (!backupDir.exists() || backupDir.listFiles()?.isEmpty() == true) {
            Toast.makeText(this, "Tidak ada backup tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val backupFiles = backupDir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        val message = backupFiles.joinToString("\n\n") {
            "📁 ${it.name}\n" +
                    "📅 ${sdf.format(Date(it.lastModified()))}\n" +
                    "💾 ${it.length() / 1024} KB"
        }

        AlertDialog.Builder(this)
            .setTitle("Daftar Backup (${backupFiles.size})")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}