package com.example.note.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.note.dao.NoteDao
import com.example.note.dao.Notes

@Database(entities = [Notes::class],version = 1,exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {
    companion object{
        var notesDatabase:NotesDatabase?=null

        //        @Synchronized
        fun getDataBase(context: Context):NotesDatabase{
            notesDatabase?: synchronized(this){
                notesDatabase= Room.databaseBuilder(context,
                    NotesDatabase::class.java,"notes")
                    .build()
            }
            return notesDatabase!!
        }
    }
    abstract fun noteDao(): NoteDao
}