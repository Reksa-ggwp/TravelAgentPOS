package com.travelagent.pos.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.travelagent.pos.R

object  ErrorHandler {
    
    fun showError(context: Context, message: String) {
        Toast.makeText(context, "❌ $message", Toast.LENGTH_LONG).show()
    }
    
    fun showSuccess(context: Context, message: String) {
        Toast.makeText(context, "✅ $message", Toast.LENGTH_SHORT).show()
    }
    
    fun showWarning(context: Context, message: String) {
        Toast.makeText(context, "⚠️ $message", Toast.LENGTH_SHORT).show()
    }
    
    fun showInfo(context: Context, message: String) {
        Toast.makeText(context, "ℹ️ $message", Toast.LENGTH_SHORT).show()
    }
    
    fun showConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ya") { _, _ -> onConfirm() }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    fun showErrorDialog(
        context: Context,
        title: String = "Error",
        message: String
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    fun handleValidationError(context: Context, message: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle("⚠️ Perhatian")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    fun handleOperationError(context: Context, operation: String, error: Exception) {
        val message = "Gagal $operation:\n${getUserFriendlyMessage(error)}"
        MaterialAlertDialogBuilder(context)
            .setTitle("❌ Operasi Gagal")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun getUserFriendlyMessage(error: Exception): String {
        return when {
            error.message?.contains("timeout") == true -> "Operasi memakan waktu terlalu lama"
            error.message?.contains("permission") == true -> "Tidak memiliki izin yang diperlukan"
            else -> error.message?.take(100) ?: "Kesalahan tidak diketahui"
        }
    }
    // Common error messages
    object Messages {
        const val NETWORK_ERROR = "Tidak ada koneksi internet"
        const val DATABASE_ERROR = "Terjadi kesalahan pada database"
        const val VALIDATION_ERROR = "Data yang dimasukkan tidak valid"
        const val PERMISSION_ERROR = "Aplikasi memerlukan izin untuk melanjutkan"
        const val FILE_ERROR = "Terjadi kesalahan saat mengakses file"
        const val UNKNOWN_ERROR = "Terjadi kesalahan yang tidak diketahui"
    }
}
