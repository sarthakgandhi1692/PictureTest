package com.example.test.ui.mainActivity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.domain.AddFaceNameUseCase
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

@HiltViewModel
class MainActivityViewModel
@Inject constructor(
    private val imageRepository: ImageRepository,
    private val imageProcessingUseCase: ImageProcessingUseCase
) : ViewModel() {

    private val _allFacesFlow = MutableStateFlow<List<ProcessedImage>>(emptyList())
    val allFacesFlow: StateFlow<List<ProcessedImage>> = _allFacesFlow


    fun startObservingFaces() {
        viewModelScope.launch {
            imageRepository.observeAllProcessedFaces()
                .onEach { Log.d("FaceDao", "New face detected: ${it.uri}") }
                .runningFold(emptyList<ProcessedImage>()) { acc, newImage ->
                    val existingImageIndex = acc.indexOfFirst { it.uri == newImage.uri }
                    if (existingImageIndex != -1) {
                        acc.toMutableList().apply { this[existingImageIndex] = newImage }
                    } else {
                        acc + newImage
                    }
                }
                .collect {
                    _allFacesFlow.value = it
                }
        }
    }

    fun processImages() {
        viewModelScope.launch(Dispatchers.IO) {
            startObservingFaces()
            imageProcessingUseCase.processImages()
        }
    }

    val state = MainActivityState()


}