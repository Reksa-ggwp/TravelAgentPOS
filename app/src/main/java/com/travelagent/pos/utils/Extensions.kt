package com.travelagent.pos.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

// Toast extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showSuccessToast(message: String) {
    Toast.makeText(this, "✅ $message", Toast.LENGTH_SHORT).show()
}

fun Context.showErrorToast(message: String) {
    Toast.makeText(this, "❌ $message", Toast.LENGTH_LONG).show()
}

// Snackbar extensions
fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun View.showSnackbarWithAction(
    message: String,
    actionText: String,
    action: () -> Unit
) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        .setAction(actionText) { action() }
        .show()
}

// Hide keyboard
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

// Date formatting
fun Long.toFormattedDate(pattern: String = "dd/MM/yyyy"): String {
    val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale("id", "ID"))
    return sdf.format(java.util.Date(this))
}

// Currency formatting
fun Double.toRupiah(): String {
    return "Rp ${String.format("%,d", this.toLong())}"
}