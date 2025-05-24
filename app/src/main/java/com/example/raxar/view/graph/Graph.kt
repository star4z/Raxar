package com.example.raxar.view.graph

import timber.log.Timber
import kotlin.math.*

open class Graph : AbstractList<Node>() {
    private var angleBetweenNodes: Double = 0.0
    private var maxDisplayedNodesMiddleRow: Int =
        0 // "Displayed" meaning between the two screen bounds
    private var maxNodesMiddleRow: Int = 0
    private var maxAngleFromVertical: Double = 0.0
    var nodes = listOf<Node>()

    var rows = 3

    // Distance of origin to the left edge of the screen as a proportion of the view width
    var widthToOriginXRatio = 1.0 / 2.0

    // Distance of origin to the top edge of the screen as a proportion of the view height
    var heightToOriginYRatio = 1.0 / 5.0

    // Distance of nodes to the origin as a proportion of the view height
    var distanceFromOriginYToHeightRatio = 11.0 / 20.0

    // Distance of node to the origin as a proportion of the view width
    var distanceFromOriginXToWidthRatio = 1.0 / 2.0

    // Distance from node to origin
    var orbitalRadius = 1.0 // default val is unused

    // Size of the node
    val geometricRadius = 100.0

    // Padding for each node. Enforced for nodes in the same row.
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
            Timber.d("rotation=$field")
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
        // Offset the angle of all rows so that they're aligned near the left edge of the screen
        val startingOffsetForAllRows = (-maxDisplayedNodesMiddleRow + 1) / 2 * angleBetweenNodes

        // Pre-calculate row-specific values

        // Set distance from node to the origin.
        // Each row is 1 node diameter further from the origin than the previous node.
        val rowDistances = DoubleArray(rows) { row ->
            orbitalRadius + 2 * (row - 1) * geometricRadiusWithPadding()
        }

        // Sets an additional row-specific angle offset so the Nodes in each row don't touch.
        // This assumes that all rows have the same number of Nodes.
        val rowOffsets = DoubleArray(rows) { row ->
            0.5 * angleBetweenNodes * (row - 1)
        }

        val newNodes = buildList {
            for (i in nodes.indices) {
                val row = i % rows
                val col =
                    i / rows // Assuming nodes are filled column by column in each row effectively

                val nodeDistance = rowDistances[row]

                // Sets the angle for the node
                val theta =
                    angleBetweenNodes * col + startingOffsetForAllRows + rowOffsets[row] + rotation

                // Sets the position of the node based on the angle, distance from the origin, and
                // position of the origin.
                add(
                    Node(
                        x = nodeDistance * sin(theta) + origin.x,
                        y = nodeDistance * cos(theta) + origin.y
                    )
                )
            }
        }

        nodes = newNodes
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