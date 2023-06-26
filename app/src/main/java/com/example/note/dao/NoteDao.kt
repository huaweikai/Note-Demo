package com.example.note.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id =:id")
    suspend fun getSpecificNote(id:Int) : Note

    @Query("SELECT * FROM notes WHERE title LIKE :title ORDER BY ID DESC")
    fun getSearchNote(title:String):List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(note:Note)

    @Delete
    suspend fun deleteNote(note:Note)

    @Query("DELETE FROM notes WHERE id =:id")
    suspend fun deleteSpecificNote(id:Int)

    @Update
    suspend fun updateNote(note:Note)
}