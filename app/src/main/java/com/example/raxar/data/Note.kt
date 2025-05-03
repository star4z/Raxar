package com.example.raxar.data

import androidx.room.*
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = NoteCommit::class,
            parentColumns = ["noteCommitId"],
            childColumns = ["currentNoteCommitId"],
            deferred = true
        ),
        ForeignKey(
            entity = Note::class,
            parentColumns = ["noteId"],
            childColumns = ["parentNoteId"],
            onDelete = CASCADE,
            deferred = true
        )
    ],
    indices = [
        Index("currentNoteCommitId"),
        Index("parentNoteId")
    ]
)
data class Note(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "noteId") val noteId: Long = 0,
    @ColumnInfo(name = "parentNoteId") val parentNoteId: Long? = null,
    @ColumnInfo(name = "currentNoteCommitId") val currentNoteCommitId: Long = 0
)