package com.example.app_vpn.data.network

import android.content.Context
import android.util.Log
import com.example.app_vpn.BuildConfig
import com.example.app_vpn.util.API_KEY
import com.example.app_vpn.util.BASE_URL
import com.example.app_vpn.util.PAYPAL_URL
import com.example.app_vpn.util.SECRET_KEY
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Inject

class RemoteDataSource @Inject constructor() {

    fun <Api> buildApi(
        api: Class<Api>,
    ): Api {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getRetrofitClient())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)
    }


    private fun getRetrofitClient(authenticator: Authenticator? = null): OkHttpClient {
        Log.d("mytag_getRetrofitClient", authenticator.toString())
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().also {
                    it.addHeader("Accept", "application/json")
                    it.addHeader("apikey", API_KEY)
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

    fun <Api> buildPaypalApi(
        api: Class<Api>
    ) : Api {

        return Retrofit.Builder()
            .baseUrl(PAYPAL_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)
    }

    fun <Api> buildApiSecret(
        api: Class<Api>,
    ): Api {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getRetrofitClientForSecret())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)
    }


    private fun getRetrofitClientForSecret(authenticator: Authenticator? = null): OkHttpClient {
        Log.d("mytag_getRetrofitClient", authenticator.toString())
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().also {
                    it.addHeader("Accept", "application/json")
                    it.addHeader("apikey", SECRET_KEY)
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