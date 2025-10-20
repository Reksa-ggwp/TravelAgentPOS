package com.travelagent.pos.data

import androidx.room.*

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["namaLengkap"]),
        Index(value = ["nomorTelepon"]),
        Index(value = ["createdDate"])
    ]
)
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val namaLengkap: String,
    val nomorTelepon: String,
    val alamat: String,
    val createdDate: Long = System.currentTimeMillis()
)