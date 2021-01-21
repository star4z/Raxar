package com.example.raxar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NoteDao {
    @Query("SELECT * FROM  notes")
    abstract fun getNotes(): Flow<List<Note>>

    @Query("SELECT * FROM note_commits WHERE noteId IN (:noteIds)")
    abstract fun getCurrentNoteCommits(noteIds: List<Long>): Flow<List<NoteCommit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveNote(note: Note): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun saveNoteCommit(noteCommit: NoteCommit): Long

    @Delete
    abstract fun deleteNote(note: Note)

    @Query("DELETE FROM note_commits WHERE noteId=:noteId")
    abstract suspend fun deleteNoteCommitsByNoteId(noteId: Long)

    @Transaction
    @Query("SELECT * FROM notes")
    abstract fun getNotesWithCommits(): Flow<List<NoteWithCommits>>

    @Transaction
    @Query("SELECT * FROM notes WHERE noteId=:id LIMIT 1")
    abstract fun getNotesWithCommits(id: Long): Flow<NoteWithCommits>

    @Transaction
    @Query("SELECT * FROM notes WHERE parentNoteId=:parentNoteId")
    abstract fun getNotesWithCommitsForParentId(parentNoteId: Long): Flow<List<NoteWithCommits>>

    @Transaction
    @Query("SELECT * FROM notes WHERE parentNoteId IS NULL")
    abstract fun getNotesWithCommitsForRootNode(): Flow<List<NoteWithCommits>>

    @Transaction
    open fun saveNoteAndCurrentCommit(note: Note, currentNoteCommit: NoteCommit) {
        saveNote(note)
        saveNoteCommit(currentNoteCommit)
    }
}