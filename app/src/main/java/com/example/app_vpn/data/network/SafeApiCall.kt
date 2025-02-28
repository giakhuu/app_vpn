package com.example.app_vpn.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

suspend fun <T> safeCall(
    apiCall: suspend () -> Response<T>
): Resource<T> {
    return withContext(Dispatchers.IO) {
        try {
            val response = apiCall.invoke()
            if (response.isSuccessful) {
                Resource.Success(response.body())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Resource.Error(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (throwable: Throwable) {
            Resource.Error(
                when (throwable) {
                    is HttpException -> Exception("Network error: ${throwable.message()}")
                    is IOException -> Exception("IO error: ${throwable.message}")
                    else -> Exception("Unexpected error: ${throwable.message}")
                }
            )
        }
    }
}