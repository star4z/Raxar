package com.example.raxar.view.graph

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

open class Graph : AbstractList<Node>() {
  private var angleBetweenNodes: Double = 0.0 // default val is unused

  // Number of nodes in the middle row that will fit between the vertical and a specified angle
  // Used to calculate the positions of the first nodes in each row
  // "Displayed" meaning between the two view bounds. Currently, this is not used for visibility
  // calculations and instead is only for indexing purposes.
  private var maxDisplayedNodesMiddleRow: Int =
    0 // default val is unused

  private var maxNodesMiddleRow: Int = 0 // default val is unused

  // Max angle that can fit in the space between the vertical and a gap 1 node
  // width + padding from the edge of the view
  var maxAngleFromVertical: Double = 0.0 // default val is unused
  var nodes = listOf<Node>()

  var rows = 1

  // Distance of origin to the left edge of the screen as a proportion of the view width
  var widthToOriginXRatio = 1.0 / 2.0

  // Distance of origin to the top edge of the screen as a proportion of the view height
  var heightToOriginYRatio = 1.0 / 5.0

  // Distance of the origin from the top edge of the view as a proportion of the view height
  var distanceFromOriginYToHeightRatio = 11.0 / 20.0

  // Distance of the origin from the left edge of the view as a proportion of the view width
  var distanceFromOriginXToWidthRatio = 1.0 / 2.0

  // Distance from node to origin
  var orbitalRadius = 1.0 // default val is unused

  // Size of the node
  val geometricRadius = 100.0

  // Padding for each node. Enforced for nodes in the same row.
  private val padding = 100.0
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
    val startingOffsetForAllRows = getStartingOffsetForAllRows()

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

  private fun getStartingOffsetForAllRows() =
    (-maxDisplayedNodesMiddleRow + 1) / 2 * angleBetweenNodes

  override fun iterator(): Iterator<Node> {
    return nodes.iterator()
  }

  fun update(
    width: Int,
    height: Int,
  ) {
    this.width = width.toDouble()
    this.height = height.toDouble()
    origin = Node(width * widthToOriginXRatio, height * heightToOriginYRatio)

    orbitalRadius = distanceFromOriginYToHeightRatio * height

    // t = asin(opp/hyp). Max angle that can fit in the space between the vertical and a gap 1 node
    // width + padding from the edge of the view
    // This is an arbitrary calculation
    maxAngleFromVertical =
      asin(
        (width * distanceFromOriginXToWidthRatio - geometricRadiusWithPadding()) / (height * distanceFromOriginYToHeightRatio)
      )

    // Number of nodes that will fit in the circumference of the middle row, including padding
    maxNodesMiddleRow =
      (PI / asin(
        geometricRadiusWithPadding() / (distanceFromOriginYToHeightRatio * height)
      )).toInt()

    // Number of nodes in the middle row that will fit between the vertical and the specified angle
    maxDisplayedNodesMiddleRow =
      (maxNodesMiddleRow * maxAngleFromVertical / PI).toInt()

    nodes = List(maxNodesMiddleRow * rows) { Node() }

    // Angle between adjacent nodes in the middle row (currently used for all rows)
    // All adjacent nodes are equidistant
    angleBetweenNodes = 2 * PI / maxNodesMiddleRow
    arrange()
  }

  fun snapToNearest() {
    rotation = (rotation / angleBetweenNodes).roundToInt() * angleBetweenNodes
  }
}