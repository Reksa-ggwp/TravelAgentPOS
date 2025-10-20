package com.travelagent.pos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelagent.pos.data.Trip
import com.travelagent.pos.repository.TripDetails
import com.travelagent.pos.repository.TripRepository
import com.travelagent.pos.repository.RepositoryResult
import kotlinx.coroutines.launch

class TripViewModel(private val repository: TripRepository) : ViewModel() {

    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> = _trips

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _tripDetails = MutableLiveData<TripDetails?>()
    val tripDetails: LiveData<TripDetails?> = _tripDetails

    fun loadTrips(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val tripList = repository.getAllTrips(forceRefresh)
                _trips.value = tripList
            } catch (e: Exception) {
                _error.value = "Gagal memuat data: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadTripDetails(tripId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val details = repository.getTripWithDetails(tripId)
                _tripDetails.value = details
            } catch (e: Exception) {
                _error.value = "Gagal memuat detail: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteTrip(trip: Trip, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            when (val result = repository.deleteTrip(trip)) {
                is RepositoryResult.Success -> {
                    loadTrips(forceRefresh = true)
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