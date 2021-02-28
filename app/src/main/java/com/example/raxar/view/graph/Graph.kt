package com.example.raxar.view.graph

import timber.log.Timber
import kotlin.math.*

open class Graph : AbstractList<Node>() {
    private var angleBetweenNodes: Double = 0.0
    var middleRowMaxNodesBetweenEdgesOfScreen: Int = 0
    var middleRowMaxNodes: Int = 0
    private var maxAngleFromVertical: Double = 0.0
    private var nodes = listOf<Node>()

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
                in 0.0..2 * PI -> {
                    value
                }
                else -> {
                    ((value % (2 * PI)) + 2 * PI) % (2 * PI)
                }
            }
            println(field.toString())
        }
    var rotating = false

    private var nodeRadiusWithPadding = nodeRadius + padding
    private var width = 1.0 // default val is unused
    private var height = 1.0 // default is unused

    var origin = Node()
    var nearestPos = 0

    init {
        arrange()
    }

    private fun arrange() {
        val nodeStacks = Array<ArrayDeque<Node>>(rows) { ArrayDeque() }
        nodes.forEachIndexed { index, node ->
            nodeStacks[index % rows].addLast(node)
        }

        val startingOffsetForAllRows =
            (-middleRowMaxNodesBetweenEdgesOfScreen + 1) / 2 * angleBetweenNodes
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

    override operator fun get(index: Int): Node {
        return nodes[index]
    }

    fun update(width: Int, height: Int) {
        this.width = width.toDouble()
        this.height = height.toDouble()
        origin = Node(width * widthToOriginXRatio, height * heightToOriginYRatio)

        nodeDistanceFromOrigin = distanceFromOriginYToHeightRatio * height
        maxAngleFromVertical =
            asin((width * distanceFromOriginXToWidthRatio - nodeRadiusWithPadding) / (height * distanceFromOriginYToHeightRatio))
        val newNumberOfNodes =
            (PI / asin(nodeRadiusWithPadding / (distanceFromOriginYToHeightRatio * height))).toInt()
        nodes = (0 until newNumberOfNodes * rows).map { Node() }.toList()

        middleRowMaxNodes = newNumberOfNodes
        middleRowMaxNodesBetweenEdgesOfScreen =
            (middleRowMaxNodes * maxAngleFromVertical / PI).toInt()
        angleBetweenNodes = 2 * PI / middleRowMaxNodes
        Timber.d("$middleRowMaxNodesBetweenEdgesOfScreen")

        nearestPos = (rotation / angleBetweenNodes).roundToInt()

        arrange()
    }

    fun snapToNearest() {
        nearestPos = (rotation / angleBetweenNodes).roundToInt()
        rotation = nearestPos * angleBetweenNodes
    }

    override val size: Int
        get() = nodes.size
}