package com.example.raxar.data

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.time.ZonedDateTime

@Entity(
    tableName = "note_commits",
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["noteId"],
            childColumns = ["noteId"],
            onDelete = CASCADE,
            deferred = true
        )],
    indices = [
        Index("noteId"),
    ]
)
data class NoteCommit(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "noteCommitId") val noteCommitId: Long = 0,
    @ColumnInfo(name = "noteId") val noteId: Long = 0,
    @ColumnInfo(name = "parentNoteCommitId") val parentNoteCommitId: Long = 0,
    @ColumnInfo(name = "time") val time: ZonedDateTime = ZonedDateTime.now(),
    @ColumnInfo(name = "color") val color: String = "",
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "body") val body: String = ""
)