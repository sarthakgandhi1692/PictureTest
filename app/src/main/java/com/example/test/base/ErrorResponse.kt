package com.example.test.base

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
@Parcelize
data class ErrorResponse(
    @Json(name = "image_url")
    val imageUrl: String?,
    @Json(name = "subtitle")
    val subtitle: String?,
    @Json(name = "title")
    val title: String?,
    @Json(name = "type")
    val type: String?
) : Parcelable, Exception()
