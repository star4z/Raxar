package com.example.raxar.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val noteId: Long = 0,
    val parentId: Long,
    val currentNoteCommitId: Long
)