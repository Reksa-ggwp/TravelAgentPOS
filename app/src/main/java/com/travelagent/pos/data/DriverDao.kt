package com.travelagent.pos.data

import androidx.room.*

@Dao
interface DriverDao {
    @Insert
    suspend fun insert(driver: Driver): Long

    @Update
    suspend fun update(driver: Driver)

    @Delete
    suspend fun delete(driver: Driver)

    @Query("SELECT * FROM drivers ORDER BY namaSopir ASC")
    suspend fun getAllDrivers(): List<Driver>

    @Query("SELECT * FROM drivers WHERE id = :id")
    suspend fun getDriverById(id: Int): Driver?
}