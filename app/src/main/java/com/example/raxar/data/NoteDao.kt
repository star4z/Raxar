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
    suspend fun saveNote(vararg note: Note)

    /**
     * When attempting to
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun saveNoteCommit(vararg noteCommits: NoteCommit)

    @Delete
    fun deleteNote(note: Note)

    @Transaction
    @Query("SELECT * FROM notes")
    fun getNotesWithCommits(): Flow<List<NoteWithCommits>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id=:id LIMIT 1")
    fun getNotesWithCommits(id: Long): Flow<NoteWithCommits>
}