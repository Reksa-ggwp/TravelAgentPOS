package com.travelagent.pos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelagent.pos.data.Customer
import com.travelagent.pos.data.CustomerStats
import com.travelagent.pos.repository.CustomerRepository
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _customers = MutableLiveData<List<Customer>>()
    val customers: LiveData<List<Customer>> = _customers

    private val _customerStats = MutableLiveData<List<CustomerStats>>()
    val customerStats: LiveData<List<CustomerStats>> = _customerStats

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadCustomers(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val customerList = repository.getAllCustomers()
                _customers.value = customerList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Gagal memuat data pelanggan: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun searchCustomers(query: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val customerList = if (query.isBlank()) {
                    repository.getAllCustomers()
                } else {
                    repository.searchCustomers(query)
                }
                _customers.value = customerList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Gagal mencari pelanggan: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadCustomerStats() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val stats = repository.getAllCustomerStats()
                _customerStats.value = stats
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Gagal memuat statistik pelanggan: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}