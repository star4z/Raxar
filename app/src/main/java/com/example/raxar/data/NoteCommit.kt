package com.example.raxar.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
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
    ),
    ForeignKey(
      entity = NoteCommit::class,
      parentColumns = ["noteCommitId"],
      childColumns = ["parentNoteCommitId"],
      onDelete = CASCADE,
      deferred = true
    )
  ],
  indices = [
    Index("noteId"),
    Index("parentNoteCommitId")
  ]
)
data class NoteCommit(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "noteCommitId") val noteCommitId: Long = 0,
  @ColumnInfo(name = "noteId") val noteId: Long = 0,
  @ColumnInfo(name = "parentNoteCommitId") val parentNoteCommitId: Long? = null,
  @ColumnInfo(name = "time") val time: ZonedDateTime = ZonedDateTime.now(),
  @ColumnInfo(name = "color") val color: String = "",
  @ColumnInfo(name = "title") val title: String = "",
  @ColumnInfo(name = "body") val body: String = "",
)