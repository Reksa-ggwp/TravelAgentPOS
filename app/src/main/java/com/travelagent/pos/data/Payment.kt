package com.travelagent.pos.data

import androidx.room.*

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Ticket::class,
            parentColumns = ["id"],
            childColumns = ["ticketId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["ticketId"]),
        Index(value = ["timestamp"])
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ticketId: Int,
    val amount: Double,
    val paymentMethod: String, // "cash", "transfer", "e-wallet"
    val receiptNumber: String? = null,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)