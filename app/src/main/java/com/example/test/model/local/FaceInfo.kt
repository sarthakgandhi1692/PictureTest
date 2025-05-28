package com.example.test.model.local

import android.graphics.Rect
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Represents a face's information.
 * @property imageUri The URI of the image containing the face.
 * @property boundingBox The bounding box around the face.
 * @property name The name of the person associated with the face, if any.
 */
data class FaceInfo(
    val imageUri: String,
    val boundingBox: Rect,
    val name: String? = null
)

/**
 * Type converter for Room database.
 */
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