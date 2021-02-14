package com.example.raxar.view.graph

import com.example.raxar.data.NoteDto
import timber.log.Timber
import kotlin.math.*

open class Graph(notes: List<NoteDto>) : Iterable<Node> {
    private var angleBetweenNodes: Double = 0.0
    private var numberOfNodesToDisplay: Int = 0
    private var numberOfPossibleNodesForCircle: Int = 0
    private var maxAngleFromVertical: Double = 0.0
    private val nodes = notes.map { note -> Node(note) }

    private val _2PI = 2 * PI

    var rows = 3
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
        set(value) {
            field = when (value) {
                in 0.0.._2PI -> {
                    value
                }
                else -> {
                    ((value % _2PI) + _2PI) % _2PI
                }
            }
            println(field.toString())
        }
    var rotating = false

    private var nodeRadiusWithPadding = nodeRadius + padding
    private var width = 1.0 // default val is unused
    private var height = 1.0 // default is unused

    var origin = Node()

    init {
        arrange()
    }

    private fun arrange() {
        val nodeStacks = Array<ArrayDeque<Node>>(rows) { ArrayDeque() }
        nodes.forEachIndexed { index, node ->
            if (index < rows * numberOfPossibleNodesForCircle) {
                nodeStacks[index % rows].addLast(node)
            } else {
                node.state.visible = false
            }
        }

        val startingOffsetForAllRows = (-numberOfNodesToDisplay + 1) / 2 * angleBetweenNodes
        nodeStacks.forEachIndexed { row, nodeStack ->
            val nodeDistance = nodeDistanceFromOrigin + 2 * (row - 1) * nodeRadiusWithPadding
            val offsetForRow = 0.5 * angleBetweenNodes * (row - 1)
            nodeStack.forEachIndexed { col, node ->
                node.state.visible = true
                val theta =
                    angleBetweenNodes * col + startingOffsetForAllRows + offsetForRow + rotation
                node.xPos = nodeDistance * sin(theta) + origin.xPos
                node.yPos = nodeDistance * cos(theta) + origin.yPos
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
            (PI / asin(nodeRadiusWithPadding / (distanceFromOriginYToHeightRatio * height))).toInt()
        numberOfNodesToDisplay =
            (numberOfPossibleNodesForCircle * maxAngleFromVertical / PI).toInt()
        angleBetweenNodes = _2PI / numberOfPossibleNodesForCircle
        Timber.d("$numberOfNodesToDisplay")
        arrange()
    }

    fun snapToNearest() {
        rotation = (rotation / angleBetweenNodes).roundToInt() * angleBetweenNodes
    }
}