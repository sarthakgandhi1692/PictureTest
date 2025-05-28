package com.example.test.ui.faceDetailActivity

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.domain.AddUpdateFaceNameUseCase
import com.example.test.domain.GetProcessedImageByUriUseCase
import com.example.test.model.local.FaceInfo
import com.example.test.model.local.ProcessedImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FaceDetailActivityViewModel
@Inject constructor(
    private val addUpdateFaceNameUseCase: AddUpdateFaceNameUseCase,
    private val getProcessedImageByUriUseCase: GetProcessedImageByUriUseCase
) : ViewModel() {

    // Mutable state for the processed image
    private val _imageState = mutableStateOf<ProcessedImage?>(null)
    val imageState: State<ProcessedImage?>
        get() = _imageState

    private val _uiEventsLiveData = MutableLiveData<UIEvents>()
    val uiEventsLiveData: LiveData<UIEvents>
        get() = _uiEventsLiveData

    /**
     * Fetches the processed image by its URI.
     * @param uri The URI of the image to fetch.
     */
    fun getProcessedImage(uri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val image = getProcessedImageByUriUseCase(uri = uri)
            _imageState.value = image
        }
    }

    /**
     * Saves the name for a given face.
     * @param faceInfo The FaceInfo object representing the face to update.
     * @param name The new name for the face.
     */
    fun saveName(faceInfo: FaceInfo, name: String) {
        // If the image state is null, return early
        if (imageState.value == null) return

        // Launch a coroutine in the IO dispatcher to perform the update
        viewModelScope.launch(Dispatchers.IO) {
            // Flag to track if an update is needed
            var sendUpdate = false
            // Map over the faces in the current image state
            val updatedFaces = imageState.value!!.faces.map { face ->
                if (face.boundingBox == faceInfo.boundingBox && face.name != name) {
                    sendUpdate = true
                    face.copy(name = name)
                } else {
                    face
                }
            }

            // If no update is needed, return early
            if (sendUpdate.not()) return@launch

            // Create an updated image with the new face information
            val updatedImage = imageState.value!!.copy(
                faces = updatedFaces
            )

            // Add or update the face name using the use case
            val result = addUpdateFaceNameUseCase(
                processedImage = updatedImage
            )

            // Update the image state with the updated image
            if(result) {
                _imageState.value = updatedImage
                _uiEventsLiveData.postValue(UIEvents.SHOW_SUCCESS_TOAST)
            } else {
                _uiEventsLiveData.postValue(UIEvents.SHOW_ERROR_TOAST)
            }


        }
    }

    sealed class UIEvents() {
        object SHOW_SUCCESS_TOAST : UIEvents()
        object SHOW_ERROR_TOAST : UIEvents()
    }
}