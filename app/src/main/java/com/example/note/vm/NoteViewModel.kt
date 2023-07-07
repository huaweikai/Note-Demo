package com.example.note.vm

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.note.bean.Note
import com.example.note.db.NoteDao
import com.example.note.util.writeToJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val dao: NoteDao,
    application: Application
) : AndroidViewModel(application) {
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

    fun backupToFile(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.getAllNotes().collectLatest {
                val interPath = File(getApplication<Application>().filesDir, "Backup")
                if (!interPath.exists()) {
                    interPath.mkdir()
                }
                it.writeToJson("note.json", interPath.absolutePath)
                if (path.startsWith("content://")) {
                    val tree = DocumentFile.fromTreeUri(getApplication(), Uri.parse(path))!!
                    tree.findFile("note.json")?.delete()
                    val fileUri = tree.createFile("application/json", "note.json")?.uri ?: return@collectLatest
                    getApplication<Application>().contentResolver.openOutputStream(fileUri).use { ops ->
                        FileInputStream(File(interPath, "note.json")).use { ins ->
                            ins.copyTo(ops!!)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(getApplication(), "备份成功", Toast.LENGTH_SHORT).show()
                    }
                }
             }
        }
    }

}