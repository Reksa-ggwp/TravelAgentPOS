package com.travelagent.pos.data
import androidx.room.*

@Entity(
    tableName = "seats",
    foreignKeys = [
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
            onDelete = androidx.room.ForeignKey.SET_NULL
        )
    ]
)
data class Seat(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tripId: Int,
    val nomorKursi: Int, // 1-10
    val customerId: Int?,
    val status: String, // "available", "booked", "paid"
    val createdDate: Long = System.currentTimeMillis()
)