package com.example.taskn21.data.remote.common

import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeoutException

sealed class HandleErrorStates(val errorCode: ErrorCode) {
    data class ClientError(val code: Int, val errorBody: String = "") : HandleErrorStates(ErrorCode.CLIENT_ERROR)
    object ServerError : HandleErrorStates(ErrorCode.SERVER_ERROR)
    data class HttpError(val httpCode: Int, val errorBody: String = "") : HandleErrorStates(
        ErrorCode.HTTP_ERROR
    )
    object NetworkError : HandleErrorStates(ErrorCode.NETWORK_ERROR)
    object TimeoutError : HandleErrorStates(ErrorCode.TIMEOUT_ERROR)
    object UnknownError : HandleErrorStates(ErrorCode.UNKNOWN_ERROR)

    enum class ErrorCode {
        CLIENT_ERROR, SERVER_ERROR, HTTP_ERROR, NETWORK_ERROR, TIMEOUT_ERROR, UNKNOWN_ERROR
    }

    companion object {
        fun handleException(throwable: Throwable): HandleErrorStates {
            return when (throwable) {
                is IOException -> NetworkError
                is HttpException -> when (throwable.code()) {
                    in 400..499 -> ClientError(throwable.code(), throwable.response()?.errorBody()?.string() ?: "")
                    in 500..599 -> ServerError
                    else -> HttpError(throwable.code(), throwable.response()?.errorBody()?.string() ?: "")
                }
                is TimeoutException -> TimeoutError
                else -> UnknownError
            }
        }
    }
}

