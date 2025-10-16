package com.travelagent.pos.data

import androidx.room.*

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val namaLengkap: String,
    val nomorTelepon: String,
    val alamat: String,
    val createdDate: Long = System.currentTimeMillis()
)