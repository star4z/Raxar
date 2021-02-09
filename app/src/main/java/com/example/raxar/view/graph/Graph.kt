package com.example.raxar.view.graph

import com.example.raxar.data.NoteDto
import kotlin.math.*

open class Graph(notes: List<NoteDto>) : Iterable<Node> {
    private var angleBetweenNodes: Double = 0.0
    private var numberOfNodesToDisplay: Int = 0
    private var numberOfPossibleNodesForCircle: Double = 0.0
    private var maxAngleFromVertical: Double = 0.0
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
    var rotation = 0.0
    var rotating = false

    private var nodeRadiusWithPadding = nodeRadius + padding
    private var width = 1.0 // default val is unused
    private var height = 1.0 // default is unused

    var origin = Node()

    init {
        arrange()
    }

    private fun arrange() {
        nodes.forEachIndexed { index, noteNode ->
            noteNode.state.visible = true
            when (index) {
                in 0..numberOfNodesToDisplay -> {
                    val theta = angleBetweenNodes * index - maxAngleFromVertical + rotation
                    noteNode.xPos = nodeDistanceFromOrigin * sin(theta) + origin.xPos
                    noteNode.yPos = nodeDistanceFromOrigin * cos(theta) + origin.yPos
                }
                in numberOfNodesToDisplay + 1..numberOfNodesToDisplay * 2 -> {
                    val theta =
                        angleBetweenNodes * (index - (numberOfNodesToDisplay + 1)) - maxAngleFromVertical + 0.5 * angleBetweenNodes + rotation
                    noteNode.xPos =
                        (nodeDistanceFromOrigin - 2 * nodeRadiusWithPadding) * sin(theta) + origin.xPos
                    noteNode.yPos =
                        (nodeDistanceFromOrigin - 2 * nodeRadiusWithPadding) * cos(theta) + origin.yPos
                }
                in numberOfNodesToDisplay * 2 + 1..numberOfNodesToDisplay * 3 -> {
                    val theta =
                        angleBetweenNodes * (index - (numberOfNodesToDisplay * 2 + 1)) - maxAngleFromVertical + 0.5 * angleBetweenNodes + rotation
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
        maxAngleFromVertical =
            asin((width * distanceFromOriginXToWidthRatio - nodeRadiusWithPadding) / (height * distanceFromOriginYToHeightRatio))
        numberOfPossibleNodesForCircle =
            PI / asin(nodeRadiusWithPadding / (distanceFromOriginYToHeightRatio * height))
        numberOfNodesToDisplay =
            (numberOfPossibleNodesForCircle * maxAngleFromVertical / PI).toInt()
        angleBetweenNodes = maxAngleFromVertical * 2 / numberOfNodesToDisplay

        arrange()
    }

    fun snapToNearest() {
        rotation = (rotation / angleBetweenNodes).roundToInt() * angleBetweenNodes
    }
}