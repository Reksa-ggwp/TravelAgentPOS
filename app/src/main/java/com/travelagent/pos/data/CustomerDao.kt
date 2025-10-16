package com.travelagent.pos.data

import androidx.room.*

@Dao
interface CustomerDao {
    @Insert
    suspend fun insert(customer: Customer): Long

    @Update
    suspend fun update(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)

    @Query("SELECT * FROM customers ORDER BY namaLengkap ASC")
    suspend fun getAllCustomers(): List<Customer>

    @Query("SELECT * FROM customers WHERE namaLengkap LIKE '%' || :query || '%' OR nomorTelepon LIKE '%' || :query || '%'")
    suspend fun searchCustomers(query: String): List<Customer>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Int): Customer?
}