package com.travelagent.pos.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.travelagent.pos.R

class EmptyStateView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val imageView: ImageView
    private val titleText: TextView
    private val messageText: TextView
    private val actionButton: MaterialButton

    init {
        // Inflate the layout resource and attach it to this view
        LayoutInflater.from(context).inflate(R.layout.view_empty_state, this, true)

        // Find the views by their correct IDs from the XML
        imageView = findViewById(R.id.ivEmptyIcon)
        titleText = findViewById(R.id.tvEmptyTitle)
        messageText = findViewById(R.id.tvEmptyMessage)
        actionButton = findViewById(R.id.btnEmptyAction)
    }

    fun show(
        icon: Int,
        title: String,
        message: String,
        actionText: String? = null,
        onActionClick: (() -> Unit)? = null
    ) {
        visibility = VISIBLE
        imageView.setImageResource(icon)
        titleText.text = title
        messageText.text = message

        if (actionText != null && onActionClick != null) {
            actionButton.visibility = VISIBLE
            actionButton.text = actionText
            actionButton.setOnClickListener { onActionClick() }
        } else {
            actionButton.visibility = GONE
        }
    }

    fun hide() {
        visibility = GONE
    }
}
