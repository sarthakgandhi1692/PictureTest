package com.example.test.ui.faceDetailActivity

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.domain.AddFaceNameUseCase
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
    private val addFaceNameUseCase: AddFaceNameUseCase,
    private val getProcessedImageByUriUseCase: GetProcessedImageByUriUseCase
) : ViewModel() {

    val imageState = mutableStateOf<ProcessedImage?>(null)

    fun getProcessedImage(uri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val image = getProcessedImageByUriUseCase.getProcessedImageByUri(uri = uri)
            imageState.value = image
        }
    }

    fun saveName(faceInfo: FaceInfo, name: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val updatedFaces = imageState.value!!.faces.map { face ->
                if (face.boundingBox == faceInfo.boundingBox) face.copy(name = name) else face
            }

            val updatedImage = imageState.value!!.copy(
                faces = updatedFaces
            )

            addFaceNameUseCase.addFaceName(
                updatedImage
            )

            imageState.value = updatedImage
        }

    }
}