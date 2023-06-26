package com.example.note.repo

import com.example.note.dao.NoteDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {


}