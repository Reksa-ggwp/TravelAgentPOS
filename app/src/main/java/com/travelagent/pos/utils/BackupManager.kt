package com.travelagent.pos.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class BackupManager(private val context: Context) {
    private val backupDir = File(context.getExternalFilesDir(null), "TravelAgentBackups")
    private val autoBackupDir = File(backupDir, "auto")
    private val manualBackupDir = File(backupDir, "manual")

    init {
        autoBackupDir.mkdirs()
        manualBackupDir.mkdirs()
    }

    suspend fun createBackup(isAuto: Boolean = false): Result<File> = withContext(Dispatchers.IO) {
        try {
            val dbPath = context.getDatabasePath("travel_agent_db").absolutePath
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                .format(Date())

            val targetDir = if (isAuto) autoBackupDir else manualBackupDir
            val backupFile = File(targetDir, "backup_$timestamp.db")

            FileInputStream(dbPath).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (isAuto) cleanOldBackups(autoBackupDir, keepCount = 10)

            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(backupFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbPath = context.getDatabasePath("travel_agent_db")

            FileInputStream(backupFile).use { input ->
                FileOutputStream(dbPath).use { output ->
                    input.copyTo(output)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllBackups(): List<BackupInfo> {
        val manualFiles = manualBackupDir.listFiles()?.toList() ?: emptyList()
        val autoFiles = autoBackupDir.listFiles()?.toList() ?: emptyList()

        return (manualFiles + autoFiles)
            .sortedByDescending { it.lastModified() }
            .map { file ->
                BackupInfo(
                    file = file,
                    name = file.name,
                    date = Date(file.lastModified()),
                    sizeKB = file.length() / 1024,
                    isAuto = file.parentFile?.name == "auto"
                )
            }
    }

    private fun cleanOldBackups(dir: File, keepCount: Int) {
        dir.listFiles()
            ?.sortedByDescending { it.lastModified() }
            ?.drop(keepCount)
            ?.forEach { it.delete() }
    }
}

data class BackupInfo(
    val file: File,
    val name: String,
    val date: Date,
    val sizeKB: Long,
    val isAuto: Boolean
)