package com.example.app_vpn.data.network

import android.content.Context
import android.util.Log
import com.example.app_vpn.BuildConfig
import com.example.app_vpn.data.network.api.TokenRefreshApi
import com.example.app_vpn.util.BASE_URL
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class RemoteDataSource @Inject constructor() {

    fun <Api> buildApi(
        api: Class<Api>,
        context: Context
    ): Api {
        val authenticator = TokenAuthenticator(context, buildTokenApi())
        Log.d("mytag_buildapi", api.name + authenticator.toString())
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getRetrofitClient(authenticator))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)
    }

    private fun buildTokenApi(): TokenRefreshApi {
        Log.d("mytag_buildTokenApi", "buildtokenapi")
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getRetrofitClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TokenRefreshApi::class.java)
    }

    private fun getRetrofitClient(authenticator: Authenticator? = null): OkHttpClient {
        Log.d("mytag_getRetrofitClient", authenticator.toString())
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().also {
                    it.addHeader("Accept", "application/json")
                }.build())
            }.also { client ->
                authenticator?.let { client.authenticator(it) }
                if (BuildConfig.DEBUG) {
                    val logging = HttpLoggingInterceptor()
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                    client.addInterceptor(logging)
                }
            }.build()
    }
}