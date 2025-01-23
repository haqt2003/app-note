package com.example.appnote.datas

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.appnote.models.Note

@Dao
interface NoteDAO {
    @Query("SELECT * FROM note")
    suspend fun getAll(): List<Note>

    @Query("SELECT * FROM note WHERE id = :id")
    suspend fun getNote(id: Int): Note

    @Insert
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

}