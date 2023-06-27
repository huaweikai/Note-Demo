package com.example.note.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteActivityViewModel @Inject constructor(
    application: Application
): AndroidViewModel(application) {

    private val _noteEventSharedFlow = MutableSharedFlow<BottomNoteEvent>()
    val noteEventSharedFlow = _noteEventSharedFlow.asSharedFlow()


    fun sendEvent(event: BottomNoteEvent) {
        viewModelScope.launch {
            _noteEventSharedFlow.emit(event)
        }
    }

}

sealed class BottomNoteEvent {
    object DeleteNoteEvent: BottomNoteEvent()

    object ChooseImageEvent: BottomNoteEvent()

    object ChooseWebLinkEvent: BottomNoteEvent()

    data class ChooseColorEvent(val color: String): BottomNoteEvent()

}