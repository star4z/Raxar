package com.example.raxar.view.graph

import com.example.raxar.data.NoteDto
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

open class Graph(notes: List<NoteDto>) : Iterable<Node> {
    private val nodes = notes.map { note -> Node(note) }

    var widthToOriginXRatio = 1.0 / 2.0
    var heightToOriginYRatio = 1.0 / 5.0
    var distanceFromOriginYToHeightRatio = 11.0 / 20.0
    var distanceFromOriginXToWidthRatio = 1.0 / 2.0
    var nodeDistanceFromOrigin = 1.0 // default val is unused
    var nodeRadius = 100.0
        set(value) {
            nodeRadiusWithPadding = value + padding
            field = value
        }
    var padding = 20.0
        set(value) {
            nodeRadiusWithPadding = nodeRadius + value
            field = value
        }
    var totalRings = 3
        set(value) {
            if (value in 0..3) {
                field = value
            }
        }

    private var nodeRadiusWithPadding = nodeRadius + padding
    private var width = 1.0 // default val is unused
    private var height = 1.0 // default is unused

    var origin = Node()

    init {
        arrange()
    }

    private fun arrange() {
        val maxAngleFromVertical =
            asin((width * distanceFromOriginXToWidthRatio - nodeRadiusWithPadding) / (height * distanceFromOriginYToHeightRatio))
        val numberOfPossibleNodesForCircle =
            PI / asin(nodeRadiusWithPadding / (distanceFromOriginYToHeightRatio * height))
        val numberOfNodesToDisplay =
            (numberOfPossibleNodesForCircle * maxAngleFromVertical / PI).toInt()
        val angleBetweenNodes = maxAngleFromVertical * 2 / numberOfNodesToDisplay

//        val nodeDeque = ArrayDeque(nodes)
//
//        for (ringN in 0 until totalRings) {
//            val numberOfNodesToPop =
//                if (ringN == 0) numberOfNodesToDisplay else numberOfNodesToDisplay - 1
//            val startingAngle =
//                if (ringN == 0) -maxAngleFromVertical else -maxAngleFromVertical + 0.5 * angleBetweenNodes
//            for (index in 0 until numberOfNodesToPop) {
//                nodeDeque.removeLastOrNull()?.let { noteNode ->
//                    noteNode.state.visible = true
//                    val theta = angleBetweenNodes * index - startingAngle
//                    noteNode.xPos =
//                        (nodeDistanceFromOrigin - nodeRadiusWithPadding) * sin(theta) + origin.xPos
//                    noteNode.yPos =
//                        (nodeDistanceFromOrigin - nodeRadiusWithPadding) * cos(theta) + origin.yPos
//                }
//            }
//        }
//
//        // Set all remaining nodes as invisible
//        while (!nodeDeque.isEmpty()) {
//            val noteNode = nodeDeque.removeLast()
//            noteNode.state.visible = false
//        }

        nodes.forEachIndexed { index, noteNode ->
            noteNode.state.visible = true
            when (index) {
                in 0..numberOfNodesToDisplay -> {
                    val theta = angleBetweenNodes * index - maxAngleFromVertical
                    noteNode.xPos = nodeDistanceFromOrigin * sin(theta) + origin.xPos
                    noteNode.yPos = nodeDistanceFromOrigin * cos(theta) + origin.yPos
                }
                in numberOfNodesToDisplay + 1..numberOfNodesToDisplay * 2 -> {
                    val theta =
                        angleBetweenNodes * (index - numberOfNodesToDisplay - 1) - maxAngleFromVertical + 0.5 * angleBetweenNodes
                    noteNode.xPos =
                        (nodeDistanceFromOrigin - 2 * nodeRadiusWithPadding) * sin(theta) + origin.xPos
                    noteNode.yPos =
                        (nodeDistanceFromOrigin - 2 * nodeRadiusWithPadding) * cos(theta) + origin.yPos
                }
                in numberOfNodesToDisplay * 2 + 1..numberOfNodesToDisplay * 3 -> {
                    val theta =
                        angleBetweenNodes * (index - (numberOfNodesToDisplay * 2 + 1)) - maxAngleFromVertical + 0.5 * angleBetweenNodes
                    noteNode.xPos =
                        (nodeDistanceFromOrigin + 2 * nodeRadiusWithPadding) * sin(theta) + origin.xPos
                    noteNode.yPos =
                        (nodeDistanceFromOrigin + 2 * nodeRadiusWithPadding) * cos(theta) + origin.yPos
                }
                else -> {
                    noteNode.state.visible = false
                }
            }
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