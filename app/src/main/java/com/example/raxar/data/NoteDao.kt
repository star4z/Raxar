package com.example.raxar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM  notes")
    fun getNotes(): Flow<List<Note>>

    @Query("SELECT * FROM note_commits WHERE noteId IN (:noteIds)")
    fun getCurrentNoteCommits(noteIds: List<Long>): Flow<List<NoteCommit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNote(note: Note): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun saveNoteCommit(noteCommit: NoteCommit): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun saveNoteCommits(noteCommits: List<NoteCommit>): List<Long>

    @Delete
    fun deleteNote(note: Note)

    @Transaction
    @Query("SELECT * FROM notes")
    fun getNotesWithCommits(): Flow<List<NoteWithCommits>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id=:id LIMIT 1")
    fun getNotesWithCommits(id: Long): Flow<NoteWithCommits>
}