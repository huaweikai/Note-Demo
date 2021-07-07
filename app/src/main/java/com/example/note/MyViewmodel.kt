package com.example.note

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.note.dao.Notes
import com.example.note.database.NotesDatabase
import kotlinx.coroutines.launch

class MyViewmodel(application: Application) : AndroidViewModel(application) {
    val database=NotesDatabase.getDataBase(application)
    val dao=database.noteDao()
    private val _homedatalist=MutableLiveData<List<Notes>>(arrayListOf())
    val homedatalist:LiveData<List<Notes>> = _homedatalist

    private val _searchNotedatalist=MutableLiveData<List<Notes>>(arrayListOf())
    val searchNotedatalist:LiveData<List<Notes>> = _searchNotedatalist

    fun updateHomedatalist(){
        viewModelScope.launch {
            _homedatalist.value=dao.getAllNotes()
        }
    }
    fun searchNotedatalist(searchnote:String){
        viewModelScope.launch {
            _homedatalist.value=dao.getSearchNote(searchnote)
        }
    }
}