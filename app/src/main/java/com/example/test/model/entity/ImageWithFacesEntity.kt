package com.example.test.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.test.model.local.FaceInfo

@Entity(tableName = "faceImages")
data class ImageWithFacesEntity(
    @PrimaryKey
    val imageUri: String,
    val timestamp: Long,
    val faces: List<FaceInfo>
)