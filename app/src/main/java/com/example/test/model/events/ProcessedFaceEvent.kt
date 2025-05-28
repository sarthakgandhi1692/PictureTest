package com.example.test.model.events

import com.example.test.model.entity.ImageWithFacesEntity

/**
 * Sealed class representing different events related to processed faces.
 */
sealed class ProcessedFaceEvent {
    data class NewFaceAdded(val entity: ImageWithFacesEntity) : ProcessedFaceEvent()
    data class FaceUpdated(val entity: ImageWithFacesEntity) : ProcessedFaceEvent()
    object InitialLoad : ProcessedFaceEvent()
}