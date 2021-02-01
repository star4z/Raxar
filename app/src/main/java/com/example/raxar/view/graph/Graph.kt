package com.example.raxar.view.graph

import com.example.raxar.data.NoteDto
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

open class Graph(notes: List<NoteDto>) : Iterable<Node> {
    private val nodes = notes.map { note -> Node(note) }

    var nodeDistance = 100.0
    var widthRatio = 1.0 / 2
    var heightRatio = 1.0 / 3
    var distanceRatio = 1.0 / 2

    var origin = Node()

    init {
        arrange()
    }

    private fun arrange() {
        nodes.forEachIndexed { index, noteNode ->
            val theta = 2 * PI * index / nodes.size
            noteNode.xPos = nodeDistance * sin(theta) + origin.xPos
            noteNode.yPos = nodeDistance * cos(theta) + origin.yPos
        }
    }

    override fun iterator(): Iterator<Node> {
        return nodes.iterator()
    }

    fun update(width: Int, height: Int) {
        origin = Node(width * widthRatio, height * heightRatio)
        nodeDistance = distanceRatio * height
        arrange()
    }
}