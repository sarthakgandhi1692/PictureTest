package com.example.test.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.test.model.local.FaceInfo

/**
 * Represents an image with its associated faces.
 * @property imageUri The URI of the image.
 * @property timestamp The timestamp when the image was added.
 * @property faces The list of faces associated with the image.
 * @see FaceInfo
 */
@Entity(tableName = "faceImages")
data class ImageWithFacesEntity(
    @PrimaryKey
    @ColumnInfo(name = "image_uri")
    val imageUri: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    @ColumnInfo(name = "faces")
    val faces: List<FaceInfo>
)