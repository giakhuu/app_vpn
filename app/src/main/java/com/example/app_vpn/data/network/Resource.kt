package com.example.app_vpn.data.network

sealed class Resource<out T> {
    data class Success<out T>(val value: T) : Resource<T>()
    data class Failure(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        val errorBody: String?,
    ) : Resource<Nothing>()

    /*sealed class Failure<out T> : Resource<T>() {
        data class NetworkError(
            val errorCode: Int?,
            val errorBody: String?,
        ) : Failure<Nothing>()

        data class GenericError(
            val errorCode: Int?,
            val errorBody: String?,
        ) : Failure<Nothing>()
    }*/
    object Loading : Resource<Nothing>()
}