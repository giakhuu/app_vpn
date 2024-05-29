package com.example.app_vpn.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_vpn.data.entities.Country
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.repository.CountryRepository
import com.example.app_vpn.data.repsonses.DataResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CountryViewModel @Inject constructor(
    private val countryRepository: CountryRepository
) : ViewModel() {
    private val _allCountry = MutableLiveData<Resource<DataResponse<List<Country>>>>()

    val allCountry : MutableLiveData<Resource<DataResponse<List<Country>>>>
        get() = _allCountry

    fun getAllCountry() = viewModelScope.launch {
        _allCountry.value = Resource.Loading
        _allCountry.value = countryRepository.getAllCountry()
    }
}