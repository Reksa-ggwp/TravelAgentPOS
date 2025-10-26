package com.travelagent.pos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.travelagent.pos.data.Customer
import com.travelagent.pos.repository.CustomerRepository
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _customers = MutableLiveData<List<Customer>>()
    val customers: LiveData<List<Customer>> = _customers

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

    fun clearError() {
        _error.value = null
    }
}

class CustomerViewModelFactory(
    private val repository: CustomerRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomerViewModel::class.java)) {
            return CustomerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}