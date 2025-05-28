package com.example.test.model.datasource

import androidx.collection.LruCache
import com.example.test.model.local.ProcessedImage
import javax.inject.Inject

interface ImageCacheDataSource {
    fun getImageFromCache(uri: String): ProcessedImage?
    fun deleteImageFromCache(uri: String)
    fun addImageToCache(image: ProcessedImage)
}

class ImageCacheDataSourceImpl @Inject constructor() : ImageCacheDataSource {

    companion object {
        private const val TAG = "ImageCacheDataSource"
        private const val CACHE_SIZE = 50
    }

    private val processedCache = LruCache<String, ProcessedImage>(CACHE_SIZE)

    override fun getImageFromCache(uri: String): ProcessedImage? {
        return processedCache[uri]
    }

    override fun deleteImageFromCache(uri: String) {
        processedCache.remove(uri)
    }

    override fun addImageToCache(image: ProcessedImage) {
        processedCache.put(image.uri, image)
    }


}