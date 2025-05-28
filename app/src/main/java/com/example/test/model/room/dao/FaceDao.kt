package com.example.test.model.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.test.model.entity.ImageWithFacesEntity

@Dao
interface FaceDao {

    /**
     * Retrieves all images with associated faces from the database.
     * @return A list of [ImageWithFacesEntity] objects.
     */
    @Query("SELECT * FROM faceImages ORDER BY timestamp DESC")
    suspend fun getAllImages(): List<ImageWithFacesEntity>

    /**
     * Inserts a face into the database.
     * @param face The face to insert.
     * @return The ID of the inserted face.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFace(face: ImageWithFacesEntity): Long

    /**
     * Retrieves faces associated with a specific image from the database.
     * @param imageUri The URI of the image.
     */
    @Query("SELECT * FROM faceImages WHERE image_uri = :imageUri")
    suspend fun getFacesForImage(imageUri: String): ImageWithFacesEntity?

    /**
     * Checks if an image with the given URI has been processed.
     * @param uri The URI of the image to check.
     * @return True if the image has been processed, false otherwise.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM faceImages WHERE image_uri = :uri LIMIT 1)")
    suspend fun isImageProcessed(uri: String): Boolean

    /**
     * Deletes images with the specified URIs from the database.
     * @param uris The list of URIs of images to delete.
     */
    @Query("DELETE FROM faceImages WHERE image_uri IN (:uris)")
    suspend fun deleteImagesByUris(uris: List<String>): Int

}