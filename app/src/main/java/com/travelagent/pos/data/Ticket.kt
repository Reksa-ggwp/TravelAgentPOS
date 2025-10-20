package com.travelagent.pos.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "tickets",
    foreignKeys = [
        ForeignKey(
            entity = Seat::class,
            parentColumns = ["id"],
            childColumns = ["seatId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["seatId"]),
        Index(value = ["tripId"]),
        Index(value = ["customerId"]),
        Index(value = ["status"]),
        Index(value = ["createdDate"])
    ]
)
data class Ticket(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val seatId: Int,
    val tripId: Int,
    val customerId: Int,
    val ongkos: Double,
    val status: String, // "pending", "partial", "paid", "refunded"
    val totalPaid: Double = 0.0,
    val isStamped: Boolean = false,
    val createdDate: Long = System.currentTimeMillis()
)