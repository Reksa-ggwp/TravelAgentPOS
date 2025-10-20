package com.travelagent.pos.repository

import com.travelagent.pos.data.Customer
import com.travelagent.pos.data.CustomerDao
import com.travelagent.pos.data.CustomerStats
import com.travelagent.pos.data.CustomerStatsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CustomerRepository(
    private val customerDao: CustomerDao,
    private val customerStatsDao: CustomerStatsDao
) {
    suspend fun getAllCustomers(): List<Customer> = withContext(Dispatchers.IO) {
        customerDao.getAllCustomers()
    }

    suspend fun searchCustomers(query: String): List<Customer> = withContext(Dispatchers.IO) {
        customerDao.searchCustomers(query)
    }

    suspend fun getCustomerById(id: Int): Customer? = withContext(Dispatchers.IO) {
        customerDao.getCustomerById(id)
    }

    suspend fun insertCustomer(customer: Customer): RepositoryResult<Long> = withContext(Dispatchers.IO) {
        try {
            val validation = validateCustomer(customer)
            if (validation is ValidationResult.Error) {
                return@withContext RepositoryResult.Failure(Exception(validation.message))
            }

            val id = customerDao.insert(customer)
            RepositoryResult.Success(id)
        } catch (e: Exception) {
            RepositoryResult.Failure(e)
        }
    }

    suspend fun updateCustomer(customer: Customer): RepositoryResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val validation = validateCustomer(customer)
            if (validation is ValidationResult.Error) {
                return@withContext RepositoryResult.Failure(Exception(validation.message))
            }

            customerDao.update(customer)
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Failure(e)
        }
    }

    suspend fun deleteCustomer(customer: Customer): RepositoryResult<Unit> = withContext(Dispatchers.IO) {
        try {
            customerDao.delete(customer)
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Failure(e)
        }
    }

    suspend fun getCustomerStats(customerId: Int): CustomerStats? = withContext(Dispatchers.IO) {
        customerStatsDao.getStats(customerId)
    }

    suspend fun getAllCustomerStats(): List<CustomerStats> = withContext(Dispatchers.IO) {
        customerStatsDao.getAllStats()
    }

    suspend fun updateCustomerStats(customerId: Int, ticketPrice: Double) = withContext(Dispatchers.IO) {
        val stats = customerStatsDao.getStats(customerId) ?: CustomerStats(customerId)

        val newStats = stats.copy(
            totalTrips = stats.totalTrips + 1,
            totalSpent = stats.totalSpent + ticketPrice,
            lastTripDate = System.currentTimeMillis(),
            loyaltyPoints = stats.loyaltyPoints + (ticketPrice / 10000).toInt(),
            tier = calculateTier(stats.totalTrips + 1, stats.totalSpent + ticketPrice)
        )

        customerStatsDao.updateStats(newStats)
    }

    private fun calculateTier(totalTrips: Int, totalSpent: Double): String {
        return when {
            totalTrips >= 50 || totalSpent >= 5_000_000 -> "Platinum"
            totalTrips >= 20 || totalSpent >= 2_000_000 -> "Gold"
            totalTrips >= 10 || totalSpent >= 1_000_000 -> "Silver"
            else -> "Bronze"
        }
    }

    private fun validateCustomer(customer: Customer): ValidationResult {
        return when {
            customer.namaLengkap.isBlank() -> ValidationResult.Error("Nama tidak boleh kosong")
            customer.namaLengkap.length < 3 -> ValidationResult.Error("Nama terlalu pendek (min 3 karakter)")
            customer.nomorTelepon.isBlank() -> ValidationResult.Error("Nomor telepon tidak boleh kosong")
            !customer.nomorTelepon.matches(Regex("^[0-9]{10,13}$")) ->
                ValidationResult.Error("Format nomor telepon salah (10-13 digit)")
            customer.alamat.isBlank() -> ValidationResult.Error("Alamat tidak boleh kosong")
            customer.alamat.length < 5 -> ValidationResult.Error("Alamat terlalu pendek (min 5 karakter)")
            else -> ValidationResult.Success
        }
    }
}