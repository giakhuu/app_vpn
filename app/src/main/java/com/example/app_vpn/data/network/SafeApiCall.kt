package com.example.app_vpn.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

interface SafeApiCall {
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is HttpException -> {
                        Resource.Failure(false, throwable.code(), throwable.message)
                    }
                    is IOException -> {
                        // Xử lý khi có lỗi IO
                        Resource.Failure(true, null, "IO Exception: " + throwable.message)
                    }
                    else -> {
                        // Xử lý mặc định cho các loại ngoại lệ khác
                        Resource.Failure(true, null, "Lỗi gì đéo biết" + throwable.message)
                    }
                }
            }
        }
    }

}