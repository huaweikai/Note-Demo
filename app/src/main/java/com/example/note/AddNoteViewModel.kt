package com.example.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.note.bean.Note
import com.example.note.db.NoteDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val noteDao: NoteDao
) : ViewModel() {
    var noteId = -1
        set(value) {
            field = value
            getNoteDetail()
        }

    private val _noteStateFlow = MutableStateFlow(Note())
    val noteStateFlow = _noteStateFlow.asStateFlow()

    fun getNoteDetail() {
        viewModelScope.launch {
            val value = if (noteId == -1) {
                Note()
            } else {
                noteDao.getSpecificNote(noteId)
            }
            _noteStateFlow.emit(value)
        }
    }

    private val _saveNoteChannel = Channel<LCE<Unit>>()
    val saveNoteChannel = _saveNoteChannel.receiveAsFlow()

    fun saveNote(
        title: String,
        subTitle: String,
        content: String,
        imagePath: String?,
        webLink: String?
    ) {
        if (title.isBlank() || subTitle.isBlank() || content.isBlank()) {
            viewModelScope.launch {
                _saveNoteChannel.send(LCE.Error("Please fill all the fields"))
            }
            return
        }
        val note = _noteStateFlow.value
        val saveNote = note.copy(
            title = title,
            subTitle = subTitle,
            noteText = content,
            imgPath = imagePath,
            webLink = webLink
        )
        viewModelScope.launch {
            runCatching {
                noteDao.insertNotes(saveNote)
            }.onSuccess {
                _saveNoteChannel.send(LCE.Success("保存成功", Unit))
            }.onFailure {
                _saveNoteChannel.send(LCE.Error(it.message ?: "Error"))
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            runCatching {
                noteDao.deleteNote(noteId)
            }.onSuccess {
                _saveNoteChannel.send(LCE.Success("删除成功", Unit))
            }.onFailure {
                _saveNoteChannel.send(LCE.Error(it.message ?: "Error"))
            }
        }
    }

    fun sendChangeNoteEvent(event: ChangeNoteEvent) {
        val value = when (event) {
            is ChangeNoteEvent.ChangeNoteColor -> {
                _noteStateFlow.value.copy(color = event.color)
            }
            is ChangeNoteEvent.ChangeNoteImage -> {
                _noteStateFlow.value.copy(imgPath = event.imagePath)
            }
            is ChangeNoteEvent.ChangeNoteContent -> {
                _noteStateFlow.value.copy(
                    title = event.title,
                    subTitle = event.subTitle,
                    noteText = event.content
                )
            }
        }
        viewModelScope.launch {
            _noteStateFlow.emit(value)
        }
    }


}

sealed class ChangeNoteEvent {

    data class ChangeNoteColor(val color: String) : ChangeNoteEvent()

    data class ChangeNoteImage(val imagePath: String?) : ChangeNoteEvent()

    data class ChangeNoteContent(
        val title: String,
        val subTitle: String,
        val content: String
    ): ChangeNoteEvent()

}