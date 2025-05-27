package com.example.test.model.local

import android.graphics.Rect
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class FaceInfo(
    val imageUri: String,
    val boundingBox: Rect,
    val name: String? = null
)

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromFaceInfoList(faces: List<FaceInfo>): String {
        return gson.toJson(faces)
    }

    @TypeConverter
    fun toFaceInfoList(data: String): List<FaceInfo> {
        val listType = object : TypeToken<List<FaceInfo>>() {}.type
        return gson.fromJson(data, listType)
    }
}