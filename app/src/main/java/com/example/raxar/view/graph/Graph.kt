package com.example.raxar.view.graph

import com.example.raxar.data.NoteDto
import kotlin.math.*

open class Graph(notes: List<NoteDto>) : Iterable<Node> {
    private val nodes = notes.map { note -> Node(note) }

    var widthToOriginXRatio = 1.0 / 2.0
    var heightToOriginYRatio = 1.0 / 5.0
    var distanceFromOriginYToHeightRatio = 11.0 / 20.0
    var distanceFromOriginXToWidthRatio = 1.0 / 2.0
    var nodeDistanceFromOrigin = 1.0 // default val is unused
    var nodeRadius = 100.0
    var width = 1.0 // default val is unused
    var height = 1.0 // default is unused

    var origin = Node()

    init {
        arrange()
    }

    private fun arrange() {
        val maxAngleFromVertical =
            asin((width * distanceFromOriginXToWidthRatio - nodeRadius) / (height * distanceFromOriginYToHeightRatio))
        val numberOfPossibleNodesForCircle =
            PI / asin(nodeRadius / (distanceFromOriginYToHeightRatio * height))
        val numberOfNodesToDisplay =
            floor(numberOfPossibleNodesForCircle * maxAngleFromVertical / PI)

        nodes.forEachIndexed { index, noteNode ->
            // this needs to be calculated exactly
            val theta = maxAngleFromVertical * (2 / numberOfNodesToDisplay * index - 1)
            noteNode.xPos = nodeDistanceFromOrigin * sin(theta) + origin.xPos
            noteNode.yPos = nodeDistanceFromOrigin * cos(theta) + origin.yPos
            noteNode.state.visible = index <= numberOfNodesToDisplay
        }
    }

    override fun iterator(): Iterator<Node> {
        return nodes.iterator()
    }

    fun update(width: Int, height: Int) {
        this.width = width.toDouble()
        this.height = height.toDouble()
        origin = Node(width * widthToOriginXRatio, height * heightToOriginYRatio)
        nodeDistanceFromOrigin = distanceFromOriginYToHeightRatio * height
        arrange()
    }
}