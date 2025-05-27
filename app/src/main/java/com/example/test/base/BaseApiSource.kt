package com.example.test.base

import android.util.Log
import com.squareup.moshi.Moshi
import retrofit2.Response


abstract class BaseApiSource {

    companion object {
        private const val TAG = "BaseApiSource"
    }

    protected suspend fun <T> getResult(call: suspend () -> Response<T>): Result<T> {
        val moshiAdapter = Moshi
            .Builder()
            .build()
        try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) return Result.Success(body)
            }

            val errorBody = response.errorBody()
                ?: return getDefaultError(response.code())

            return try {
                val errorBytes = String(errorBody.bytes())
                val moshi = moshiAdapter.adapter(ErrorResponse::class.java)
                val value = moshi.fromJson(errorBytes)
                if (value != null) {
                    error(ApiException(response.code(), value))
                } else {
                    getDefaultError(response.code())
                }
            } catch (e: Exception) {
                getDefaultError(response.code())
            }
        } catch (e: Exception) {
            Log.e("BaseApiSource", e.toString())
            return error(e)
        }
    }

    private fun <T> getDefaultError(responseCode: Int): Result<T> {
        return error(ApiException(responseCode, getDefaultErrorResponse()))
    }

    private fun <T> error(error: Exception): Result<T> {
        return Result.Error(error)
    }

    private fun getDefaultErrorResponse(): ErrorResponse {
        return ErrorResponse(
            type = "ShowToastError",
            title = "Something went wrong",
            subtitle = "Something went wrong",
            imageUrl = ""
        )
    }
}

class ApiException(
    val responseCode: Int,
    val errorResponse: ErrorResponse?
) : Exception()
