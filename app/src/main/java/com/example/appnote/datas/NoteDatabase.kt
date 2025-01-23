package com.example.appnote.datas

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.appnote.models.Note

@Database(entities = [Note::class], version = 1)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDAO
}