package com.example.note.vm

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.note.LCE
import com.example.note.bean.Note
import com.example.note.db.NoteDao
import com.example.note.util.dateString
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

    private fun getNoteDetail() {
        viewModelScope.launch {
            val value = if (noteId == -1) {
                Note()
            } else {
                noteDao.getSpecificNote(noteId)
            }
            _noteStateFlow.emit(value)
        }
    }

    private val _saveNoteChannel = Channel<Unit>()
    val saveNoteChannel = _saveNoteChannel.receiveAsFlow()

    private val _screenStatusChannel = Channel<LCE<Unit>>()
    val screenStatusChannel = _screenStatusChannel.receiveAsFlow()

    fun saveNote() {
        val note = _noteStateFlow.value
        if (note.title.isBlank() || note.subTitle.isBlank() || note.noteText.isBlank()) {
            viewModelScope.launch {
                _screenStatusChannel.send(LCE.Error("Please fill all the fields"))
            }
            return
        }
        viewModelScope.launch {
            runCatching {
                val date = System.currentTimeMillis()
                noteDao.insertNote(note.copy(dateTime = date.dateString, updateTime = date))
            }.onSuccess {
                _screenStatusChannel.send(LCE.Success("保存成功", Unit))
                _saveNoteChannel.send(Unit)
            }.onFailure {
                _screenStatusChannel.send(LCE.Error(it.message ?: "Error"))
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            runCatching {
                noteDao.deleteNote(noteId)
            }.onSuccess {
                _saveNoteChannel.send(Unit)
                _screenStatusChannel.send(LCE.Success("删除成功", Unit))
            }.onFailure {
                _screenStatusChannel.send(LCE.Error(it.message ?: "Error"))
            }
        }
    }

    fun sendChangeNoteEvent(event: ChangeNoteEvent) {
        viewModelScope.launch {
            runCatching {
                disposeChangeNoteEvent(event)
            }.onFailure {
                _screenStatusChannel.send(LCE.Error(it.message ?: "Error"))
            }.onSuccess { value ->
                _noteStateFlow.emit(value)
            }
        }
    }

    private fun disposeChangeNoteEvent(event: ChangeNoteEvent): Note {
        return when (event) {
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

            is ChangeNoteEvent.ChangeNoteLink -> {
                val isLink = Patterns.WEB_URL.matcher(event.link ?: "").matches()
                if (!isLink) {
                    throw IllegalArgumentException("Link is not valid")
                }
                _noteStateFlow.value.copy(webLink = event.link)
            }
        }

    }


}

sealed class ChangeNoteEvent {

    data class ChangeNoteColor(val color: String) : ChangeNoteEvent()

    data class ChangeNoteImage(val imagePath: String?) : ChangeNoteEvent()

    data class ChangeNoteLink(val link: String?) : ChangeNoteEvent()

    data class ChangeNoteContent(
        val title: String,
        val subTitle: String,
        val content: String
    ) : ChangeNoteEvent()

}