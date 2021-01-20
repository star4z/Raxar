package com.example.raxar.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = NoteCommit::class,
            parentColumns = ["noteCommitId"],
            childColumns = ["currentNoteCommitId"],
            deferred = true
        )
    ]
)
data class Note(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "noteId") val noteId: Long = 0,
    @ColumnInfo(name = "parentId") val parentNoteId: Long,
    @ColumnInfo(name = "currentNoteCommitId") val currentNoteCommitId: Long
)