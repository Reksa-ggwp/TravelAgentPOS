package com.travelagent.pos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drivers")
data class Driver(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val namaSopir: String,
    val nomorTelepon: String,
    val createdDate: Long = System.currentTimeMillis()
)

