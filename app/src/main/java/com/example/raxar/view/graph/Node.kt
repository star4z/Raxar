package com.example.raxar.view.graph

import com.example.raxar.data.NoteDto

data class Node(
    var xPos: Double = 0.0,
    var yPos: Double = 0.0,
    var title: String? = null
) {
    constructor(noteDto: NoteDto) : this(title = noteDto.title)
}