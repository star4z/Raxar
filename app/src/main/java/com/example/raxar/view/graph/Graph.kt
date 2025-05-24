package com.example.raxar.view.graph

import timber.log.Timber
import kotlin.math.*

open class Graph : AbstractList<Node>() {
    private var angleBetweenNodes: Double = 0.0
    private var maxDisplayedNodesMiddleRow: Int =
        0 // "Displayed" meaning between the two screen bounds
    private var maxNodesMiddleRow: Int = 0
    private var maxAngleFromVertical: Double = 0.0
    private var nodes = listOf<Node>()

    var rows = 3
    var widthToOriginXRatio = 1.0 / 2.0
    var heightToOriginYRatio = 1.0 / 5.0
    var distanceFromOriginYToHeightRatio = 11.0 / 20.0
    var distanceFromOriginXToWidthRatio = 1.0 / 2.0
    var orbitalRadius = 1.0 // default val is unused
    val geometricRadius = 100.0
    private val padding = 20.0
    var rotation = 0.0
        set(value) {
            field = when (value) {
                in 0.0..2 * PI -> {
                    value
                }

                else -> {
                    // Ensures value is in [0, 2*PI) and is positive
                    ((value % (2 * PI)) + 2 * PI) % (2 * PI)
                }
            }
            println(field.toString())
        }
    private fun geometricRadiusWithPadding() = geometricRadius + padding
    private var width = 1.0 // default val is unused
    private var height = 1.0 // default is unused

    var origin = Node()

    init {
        arrange()
    }

    override val size: Int
        get() = nodes.size

    override operator fun get(index: Int) = nodes[index]

    /**
     * Updates the positions of the nodes in the graph to match the current rotation.
     */
    private fun arrange() {
        // Group nodes by row
        val nodeStacks = Array<ArrayDeque<Node>>(rows) { ArrayDeque() }
        nodes.forEachIndexed { index, node ->
            nodeStacks[index % rows].addLast(node)
        }

        // Offset the angle of all rows so that they're aligned near the left edge of the screen
        val startingOffsetForAllRows = (-maxDisplayedNodesMiddleRow + 1) / 2 * angleBetweenNodes

        nodeStacks.forEachIndexed { row, nodeStack ->
            // Set distance from node to the origin.
            // Each row is 1 node diameter further from the origin than the previous node.
            val nodeDistance = orbitalRadius + 2 * (row - 1) * geometricRadiusWithPadding()

            // Sets an additional row-specific angle offset so the Nodes in each row don't touch.
            // This assumes that all rows have the same number of Nodes.
            val offsetForRow = 0.5 * angleBetweenNodes * (row - 1)

            nodeStack.forEachIndexed { col, node ->
                // Sets the angle for the node
                val theta =
                    angleBetweenNodes * col + startingOffsetForAllRows + offsetForRow + rotation

                // Sets the position of the node based on the angle, distance from the origin, and
                // position of the origin.
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

        orbitalRadius = distanceFromOriginYToHeightRatio * height
        maxAngleFromVertical =
            asin((width * distanceFromOriginXToWidthRatio - geometricRadiusWithPadding()) / (height * distanceFromOriginYToHeightRatio))
        maxNodesMiddleRow =
            (PI / asin(geometricRadiusWithPadding() / (distanceFromOriginYToHeightRatio * height))).toInt()
        maxDisplayedNodesMiddleRow =
            (maxNodesMiddleRow * maxAngleFromVertical / PI).toInt()
        nodes = List(maxNodesMiddleRow * rows) { Node() }
        angleBetweenNodes = 2 * PI / maxNodesMiddleRow
        Timber.d("$maxDisplayedNodesMiddleRow")
        arrange()
    }

    fun snapToNearest() {
        rotation = (rotation / angleBetweenNodes).roundToInt() * angleBetweenNodes
    }

}