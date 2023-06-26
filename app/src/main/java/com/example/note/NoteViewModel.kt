package com.example.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.note.bean.Note
import com.example.note.db.NoteDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val dao: NoteDao
) : ViewModel() {
    init {
        searchDatabase("")
    }

    private val _noteDataList = MutableStateFlow<List<Note>>(emptyList())
    val noteDataList = _noteDataList.asStateFlow()

    fun searchDatabase(searchQuery: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val searchResult = dao.getSearchNote("%$searchQuery%")
            _noteDataList.value = searchResult
        }
    }

}