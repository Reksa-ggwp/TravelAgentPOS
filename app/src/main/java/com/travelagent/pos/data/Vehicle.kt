package com.travelagent.pos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nomorPolisi: String,
    val createdDate: Long = System.currentTimeMillis()
)