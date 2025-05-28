package com.example.test.model.local

import android.graphics.Rect
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

/**
 * Represents a face's information.
 * @property imageUri The URI of the image containing the face.
 * @property boundingBox The bounding box around the face.
 * @property name The name of the person associated with the face, if any.
 */
data class FaceInfo(
    @SerializedName("imageUri")
    val imageUri: String,
    @SerializedName("boundingBox")
    val boundingBox: Rect,
    @SerializedName("name")
    val name: String? = null
)

/**
 * Type converter for Room database.
 */
class Converters {

    @TypeConverter
    fun fromFaceInfoList(faceInfoList: List<FaceInfo>?): String? {
        if (faceInfoList == null) {
            return null
        }
        val gson = Gson()
        return gson.toJson(faceInfoList)
    }

    @TypeConverter
    fun toFaceInfoList(faceInfoListString: String?): List<FaceInfo>? {
        if (faceInfoListString == null) {
            return null
        }
        val gson = Gson()
        // This is likely where your FaceInfo.kt:37 points to, or a similar line
        val listType = object : TypeToken<List<FaceInfo>>() {}.type
        return gson.fromJson(faceInfoListString, listType)
    }
}