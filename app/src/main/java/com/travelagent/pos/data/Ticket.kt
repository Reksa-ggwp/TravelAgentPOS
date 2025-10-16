package com.travelagent.pos.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    tableName = "tickets",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = Seat::class,
            parentColumns = ["id"],
            childColumns = ["seatId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        ),
        androidx.room.ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        ),
        androidx.room.ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class Ticket(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val seatId: Int,
    val tripId: Int,
    val customerId: Int,
    val ongkos: Double,
    val status: String, // "pending", "paid"
    val isStamped: Boolean = false,
    val createdDate: Long = System.currentTimeMillis()
)