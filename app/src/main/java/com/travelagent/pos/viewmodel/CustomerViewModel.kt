package com.travelagent.pos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelagent.pos.data.Customer
import com.travelagent.pos.data.CustomerStats
import com.travelagent.pos.repository.CustomerRepository
import com.travelagent.pos.repository.RepositoryResult
import kotlinx.coroutines.launch

class CustomerViewModel(private val repository: CustomerRepository) : ViewModel() {

    private val _customers = MutableLiveData<List<Customer>>()
    val customers: LiveData<List<Customer>> = _customers

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _customerStats = MutableLiveData<List<CustomerStats>>()
    val customerStats: LiveData<List<CustomerStats>> = _customerStats

    fun loadCustomers() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val customerList = repository.getAllCustomers()
                _customers.value = customerList
            } catch (e: Exception) {
                _error.value = "Gagal memuat data: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun searchCustomers(query: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val results = if (query.isBlank()) {
                    repository.getAllCustomers()
                } else {
                    repository.searchCustomers(query)
                }
                _customers.value = results
            } catch (e: Exception) {
                _error.value = "Gagal mencari: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadCustomerStats() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val stats = repository.getAllCustomerStats()
                _customerStats.value = stats
            } catch (e: Exception) {
                _error.value = "Gagal memuat statistik: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteCustomer(customer: Customer, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repository.deleteCustomer(customer)) {
                is RepositoryResult.Success -> {
                    loadCustomers()
                    onSuccess()
                }
                is RepositoryResult.Failure -> {
                    _error.value = result.exception.message
                }
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}