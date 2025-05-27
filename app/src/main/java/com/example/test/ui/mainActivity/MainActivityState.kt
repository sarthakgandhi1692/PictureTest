package com.example.test.ui.mainActivity

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class MainActivityState {


    private val _textState = mutableStateOf("Default text")
    val textState: State<String>
        get() = _textState


    fun updateTextState(text: String) {
        _textState.value = text
    }
}