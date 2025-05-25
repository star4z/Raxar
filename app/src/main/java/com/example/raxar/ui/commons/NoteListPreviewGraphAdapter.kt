package com.example.raxar.ui.commons

import com.example.raxar.data.NoteDto
import com.example.raxar.view.graph.GraphView

class NoteListPreviewGraphAdapter: GraphView.GraphViewAdapter<NoteDto>() {
    override fun bindValue(value: NoteDto): String {
        return value.title
    }
}