package com.example.raxar.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

@Entity(
    tableName = "note_commits",
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = CASCADE
        )]
)
class NoteCommit(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val noteCommitId: Long = 0,
    val noteId: Long = 0,
    val parentNoteCommitId: Long = 0,
    val time: ZonedDateTime = ZonedDateTime.now(),
    val color: String = "",
    val title: String = "",
    val body: String = ""
)