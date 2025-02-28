package com.example.app_vpn.ui.viewmodel

import android.content.Context
import android.credentials.GetCredentialException
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.repository.repoImpl.AuthRepository
import com.example.app_vpn.ui.auth.login.TAG
import com.example.app_vpn.util.REDIRECT_URL
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.AccessTokenProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _registerResponse = MutableStateFlow<Resource<UserInfo>>(Resource.Loading)
    val registerResponse: StateFlow<Resource<UserInfo>> = _registerResponse.asStateFlow()

    private val _loginResponse = MutableStateFlow<Resource<UserInfo>>(Resource.Loading)
    val loginResponse: StateFlow<Resource<UserInfo>> = _loginResponse.asStateFlow()

    private val _resetPasswordResponse = MutableStateFlow<Resource<Nothing>>(Resource.Loading)
    val resetPasswordResponse: StateFlow<Resource<Nothing>> = _resetPasswordResponse.asStateFlow()

    private val _updatePasswordResponse = MutableStateFlow<Resource<Nothing>>(Resource.Loading)
    val updatePasswordResponse: StateFlow<Resource<Nothing>> = _updatePasswordResponse.asStateFlow()


    fun loginWithGoogle(context: Context) = viewModelScope.launch {
        val result = authRepository.loginWithGoogle(context)
        _loginResponse.value = result
    }


    fun registerWithEmailPassword(email: String, password: String) = viewModelScope.launch {
        _registerResponse.value = authRepository.registerWithEmailPassword(email, password)
    }

    fun loginWithEmail(email: String, password: String) = viewModelScope.launch {
        _loginResponse.value = authRepository.loginWithEmail(email, password)
    }

    fun resetPassword(email: String) = viewModelScope.launch {
        _resetPasswordResponse.value = authRepository.resetPassword(email)
    }

    fun updatePassword(nEmail: String?, newPassword: String, accessToken: String) = viewModelScope.launch {
        _updatePasswordResponse.value = authRepository.updatePassword(nEmail, newPassword, accessToken)
    }
}
