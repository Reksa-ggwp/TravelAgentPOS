package com.travelagent.pos.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.TextView
import com.travelagent.pos.R

class LoadingDialog(private val context: Context) {
    private var dialog: Dialog? = null

    fun show(message: String = "Memproses...") {
        if (dialog == null) {
            dialog = Dialog(context).apply {
                val view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null, false)
                setContentView(view)
                setCancelable(false)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                view.findViewById<TextView>(R.id.tvLoadingMessage)?.text = message
            }
        }

        dialog?.show()
    }

    fun updateMessage(message: String) {
        dialog?.findViewById<TextView>(R.id.tvLoadingMessage)?.text = message
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }

    fun isShowing(): Boolean = dialog?.isShowing == true
}