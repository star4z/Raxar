package com.example.raxar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NoteDao {
    @Query("SELECT * FROM  notes")
    abstract fun getNotes(): Flow<List<Note>>

    @Query("SELECT * FROM note_commits WHERE noteId IN (:noteIds)")
    abstract fun getCurrentNoteCommits(noteIds: List<Long>): Flow<List<NoteCommit>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertNote(note: Note)

    @Update
    abstract fun updateNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertNoteCommit(noteCommit: NoteCommit)

    @Delete
    abstract fun deleteNote(note: Note)

    @Query("DELETE FROM note_commits WHERE noteId=:noteId")
    abstract suspend fun deleteNoteCommitsByNoteId(noteId: Long)

    @Transaction
    @Query("SELECT * FROM notes")
    abstract fun getNotesWithCommits(): Flow<List<NoteWithCommits>>

    @Transaction
    @Query("SELECT * FROM notes WHERE noteId=:id LIMIT 1")
    abstract fun getNotesWithCommits(id: Long): Flow<NoteWithCommits?>

    @Transaction
    @Query("SELECT * FROM notes WHERE parentNoteId=:parentNoteId")
    abstract fun getNotesWithCommitsForParentId(parentNoteId: Long): Flow<List<NoteWithCommits>>

    @Transaction
    @Query("SELECT * FROM notes WHERE parentNoteId IS NULL")
    abstract fun getNotesWithCommitsForRootNode(): Flow<List<NoteWithCommits>>

    @Transaction
    open fun insertNoteAndNoteCommit(note: Note, currentNoteCommit: NoteCommit) {
        insertNote(note)
        insertNoteCommit(currentNoteCommit)
    }

    @Transaction
    open fun updateNoteAndInsertNoteCommit(note: Note, currentNoteCommit: NoteCommit) {
        updateNote(note)
        insertNoteCommit(currentNoteCommit)
    }
}