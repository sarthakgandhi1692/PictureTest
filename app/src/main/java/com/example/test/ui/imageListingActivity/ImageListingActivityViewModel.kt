package com.example.test.ui.imageListingActivity

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.domain.ImageProcessingUseCase
import com.example.test.model.local.ProcessedImage
import com.example.test.model.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Image Listing Activity.
 *
 * This ViewModel is responsible for managing the data related to processed images and user permissions.
 * It interacts with the [ImageRepository] to fetch and observe processed images,
 * and uses the [ImageProcessingUseCase] to trigger the image processing logic.
 */
@HiltViewModel
class ImageListingActivityViewModel
@Inject constructor(
    private val imageRepository: ImageRepository,
    private val imageProcessingUseCase: ImageProcessingUseCase
) : ViewModel() {

    private val _allFacesFlow = MutableStateFlow<List<ProcessedImage>>(emptyList())
    val allFacesFlow: StateFlow<List<ProcessedImage>> = _allFacesFlow

    private val _hasPermission = mutableStateOf(false)
    val hasPermission: State<Boolean> = _hasPermission

    /**
     * Updates the permission state.
     *
     * @param isGranted True if the permission is granted, false otherwise.
     */
    fun updatePermissionState(isGranted: Boolean) {
        _hasPermission.value = isGranted
    }

    /**
     * Starts observing changes in the processed faces from the repository.
     *
     * This function launches a coroutine in the IO dispatcher to observe the flow of processed images.
     * It uses `onEach` to log new face detections and `runningFold` to accumulate the images,
     * updating existing images if they are re-processed or adding new ones.
     * The accumulated list is then emitted to `_allFacesFlow`.
     */
    fun startObservingFaces() {
        viewModelScope.launch(Dispatchers.IO) {
            imageRepository.observeAllProcessedFaces()
                .onEach { Log.d("FaceDao", "New face detected: ${it.uri}") }
                .runningFold(emptyList<ProcessedImage>()) { acc, newImage ->
                    val existingImageIndex = acc.indexOfFirst { it.uri == newImage.uri }
                    if (existingImageIndex != -1) {
                        // If the image already exists, update it
                        acc.toMutableList().apply { this[existingImageIndex] = newImage }
                    } else {
                        // If the image is new, add it to the list
                        acc + newImage
                    }
                }
                .collect {
                    // Update the StateFlow with the latest list of images
                    _allFacesFlow.value = it
                }
        }
    }

    /**
     * Initiates the image processing operation.
     *
     * This function launches a coroutine in the IO dispatcher.
     * It first starts observing faces and then triggers the image processing use case.
     */
    fun processImages() {
        viewModelScope.launch(Dispatchers.IO) {
            // Ensure we are observing for new faces before processing
            startObservingFaces()
            // Start the image processing
            imageProcessingUseCase()
        }
    }
}