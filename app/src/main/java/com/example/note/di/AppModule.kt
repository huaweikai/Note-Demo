package com.example.note.di

import android.content.Context
import androidx.room.Room
import com.example.note.dao.NoteDao
import com.example.note.database.NotesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideDataBase(
        @ApplicationContext context: Context
    ): NotesDatabase {
        return Room.databaseBuilder(
            context,
            NotesDatabase::class.java, "notes"
        ).build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: NotesDatabase): NoteDao {
        return database.noteDao
    }

}