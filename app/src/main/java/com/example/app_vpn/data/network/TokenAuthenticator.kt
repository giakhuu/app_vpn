package com.example.app_vpn.data.network

import android.content.Context
import android.util.Log
import com.example.app_vpn.data.entities.Token
import com.example.app_vpn.data.preferences.UserPreference
import com.example.app_vpn.data.network.api.TokenRefreshApi
import com.example.app_vpn.data.repository.BaseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor (
    context: Context,
    private val tokenApi: TokenRefreshApi
): Authenticator, BaseRepository(tokenApi) {

    private val appContext = context.applicationContext
    private val userPreference = UserPreference(appContext)

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("mytag_authenticate", "authenticate")
        return runBlocking {
            when (val tokenResponse = getUpdatedToken()) {
                is Resource.Success -> {
                    userPreference.saveAccessTokens(
                        tokenResponse.value.accessToken!!,
                        tokenResponse.value.refreshToken!!
                    )
                    Log.d("mytag_authenticate", "Bearer ${tokenResponse.value.accessToken}")
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${tokenResponse.value.accessToken}")
                        .build()
                }
                else -> null
            }
        }
    }

    private suspend fun getUpdatedToken(): Resource<Token> {
        val refreshToken = userPreference.refreshToken.first()
        return safeApiCall { tokenApi.refreshAccessToken(refreshToken) }
    }

}