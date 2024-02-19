package com.example.taskn21.data.remote.common

sealed class Resource<T: Any> {
    data class Loading<T: Any>(val loading: Boolean) : Resource<T>()
    data class Success<T: Any>(val response: T) : Resource<T>()
    data class Error<T: Any>(val error: HandleErrorStates, val throwable: Throwable) : Resource<T>()
}