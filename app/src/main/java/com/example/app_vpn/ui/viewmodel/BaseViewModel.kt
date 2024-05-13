package com.example.app_vpn.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.app_vpn.data.repository.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseViewModel (
    private val repository: BaseRepository
) : ViewModel() {

    suspend fun logout() = withContext(Dispatchers.IO) {
        repository.logout()
    }
}