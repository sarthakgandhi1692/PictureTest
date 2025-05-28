package com.example.test.model.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.test.model.entity.ImageWithFacesEntity
import com.example.test.model.local.Converters
import com.example.test.model.room.dao.FaceDao

/**
 * Database for images with faces.
 * Provides access to the FaceDao for database operations.
 */
@Database(
    entities = [ImageWithFacesEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ImagesDatabase : RoomDatabase() {
    abstract fun faceDao(): FaceDao
}