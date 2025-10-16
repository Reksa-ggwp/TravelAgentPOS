package com.travelagent.pos.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val asal: String,
    val tujuan: String,
    val tanggal: Long, // Unix timestamp
    val namaSopir: String,
    val nomorTeleponSopir: String,
    val nomorPolisi: String,
    val ongkos: Double,
    val createdDate: Long = System.currentTimeMillis()
)