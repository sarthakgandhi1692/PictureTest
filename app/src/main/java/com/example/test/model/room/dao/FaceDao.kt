package com.example.test.model.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.test.model.entity.ImageWithFacesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FaceDao {

    @Query("SELECT * FROM faceImages ORDER BY timestamp ASC")
    fun getAllFacesFlow(): Flow<List<ImageWithFacesEntity>>

    @Query("SELECT COUNT(*) FROM faceImages")
    fun getFacesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFace(face: ImageWithFacesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImageWithfaces(faces: List<ImageWithFacesEntity>)

    @Query("SELECT * FROM faceImages WHERE imageUri = :imageUri")
    suspend fun getFacesForImage(imageUri: String): ImageWithFacesEntity

    @Query("DELETE FROM faceImages WHERE imageUri = :imageUri")
    suspend fun deleteFacesForImage(imageUri: String)

    @Query("DELETE FROM faceImages")
    suspend fun deleteAllFaces()

    @Query("SELECT EXISTS(SELECT 1 FROM faceImages WHERE imageUri = :uri LIMIT 1)")
    suspend fun isImageProcessed(uri: String): Boolean
}