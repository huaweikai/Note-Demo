package com.example.note.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.note.dao.NoteDao
import com.example.note.dao.Note

@Database(entities = [Note::class],version = 1,exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao
}