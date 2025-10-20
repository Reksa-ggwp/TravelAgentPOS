package com.travelagent.pos.data

import androidx.room.*

@Entity(
    tableName = "seats",
    foreignKeys = [
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
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["tripId", "nomorKursi"], unique = true),
        Index(value = ["status"]),
        Index(value = ["customerId"])
    ]
)
data class Seat(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tripId: Int,
    val nomorKursi: Int,
    val customerId: Int?,
    val status: String, // "available", "booked", "paid"
    val createdDate: Long = System.currentTimeMillis()
)