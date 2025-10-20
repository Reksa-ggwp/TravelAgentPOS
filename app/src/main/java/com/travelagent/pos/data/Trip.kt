package com.travelagent.pos.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "trips",
    indices = [
        Index(value = ["tanggal"]),
        Index(value = ["asal", "tujuan"]),
        Index(value = ["namaSopir"]),
        Index(value = ["nomorPolisi"])
    ]
)
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val asal: String,
    val tujuan: String,
    val tanggal: Long,
    val namaSopir: String,
    val nomorTeleponSopir: String,
    val nomorPolisi: String,
    val ongkos: Double,
    val createdDate: Long = System.currentTimeMillis()
)