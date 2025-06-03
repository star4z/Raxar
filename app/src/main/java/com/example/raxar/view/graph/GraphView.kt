package com.example.raxar.view.graph

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils.TruncateAt.END
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withTranslation
import com.example.raxar.collection.CircularMaskedList
import com.example.raxar.util.HALF_PI
import com.example.raxar.util.PI
import timber.log.Timber
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class GraphView : View {

  private val nodeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val fadedNodePaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val nodeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
  private val graph: Graph
  private val nodeTextSize = 40f
  private val snapping = false

  // Angle of visibility from the origin along the view center.
  private val outerAngle = 0.6f * PI
  var adapter: GraphViewAdapter<*>? = null
    set(value) {
      field = value
      value?.callback = { updateState() }
    }
  private var nodes = listOf<AngledNode>()

  constructor(context: Context?) : this(context, null, 0, 0)
  constructor(
    context: Context?,
    attrs: AttributeSet?,
  ) : this(context, attrs, 0, 0)

  constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
  ) : this(
    context, attrs, defStyleAttr, 0
  )

  constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
  ) : super(context, attrs, defStyleAttr, defStyleRes) {
    nodeBgPaint.color = Color.RED
    nodeBgPaint.textAlign = Paint.Align.CENTER
    fadedNodePaint.color = Color.MAGENTA
    nodeTextPaint.color = Color.WHITE
    nodeTextPaint.textSize = nodeTextSize
    nodeTextPaint.textAlign = Paint.Align.CENTER
    graph = Graph()
  }

  abstract class GraphViewAdapter<T> {
    private var values = CircularMaskedList<T>()
    var callback: (() -> Unit)? = null

    /**
     * Updates the mask size. Only accepts positive values.
     */
    fun setMaskSize(maskSize: Int) {
      values.shiftRightMaskBound(maskSize - values.maskSize)
    }

    fun submitList(list: List<T>) {
      Timber.d("submitList(%s), values.maskSize=%s", list, values.maskSize)
      values = CircularMaskedList(list, values.maskSize)
      callback?.invoke()
    }

    fun changeMaskValues(
      leftShift: Int,
      rightShift: Int,
    ) {
      values.shiftLeftMaskBound(leftShift)
      values.shiftRightMaskBound(rightShift)
    }

    fun getItem(index: Int): String {
      return if (index in values.getMaskedValues().indices) {
        bindValue(values.getMaskedValues()[index])
      } else {
        index.toString()
      }
    }

    abstract fun bindValue(value: T): String
  }

  private data class AngledNode(
    val graphIndex: Int,
    val angle: Float,
    val node: Node,
    val visible: Boolean,
  )

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    // Draw debug lines for the outer angle.
    drawLine(canvas, Color.CYAN, outerAngle, 500f)
    drawLine(canvas, Color.CYAN, (-outerAngle), 500f)

    for (node in nodes.withIndex()) {
      if (node.value.visible) {
        drawNodeWithLine(canvas, node.value.node, nodeBgPaint)
        adapter?.let {
          drawTitle(it.getItem(node.index), canvas, node.value.node)
        }
      } else {
        drawNodeWithLine(canvas, node.value.node, fadedNodePaint)
      }
    }
  }

  private fun updateState(angleDifference: Float = 0.0f) {
    val oldVisibleNodes = getNodes().filter { it.visible }

    graph.update(width, height)

    val newNodes = getNodes()
    Timber.d("outerAngle=%s..%s", (-outerAngle + HALF_PI), (outerAngle + HALF_PI))
    Timber.d("newNodes=%s", newNodes.map { "${it.angle},${it.visible}" })
    val newVisibleNodes = newNodes.filter { it.visible }

    if (oldVisibleNodes.isEmpty()) {
      Timber.d("newVisibleNodes=%s", newVisibleNodes.map { it.graphIndex })
      adapter?.setMaskSize(newVisibleNodes.size)
    } else {
      val newFirstIndex = newVisibleNodes.firstOrNull()?.graphIndex ?: 0
      val newLastIndex = newVisibleNodes.lastOrNull()?.graphIndex ?: 0
      val oldFirstIndex = oldVisibleNodes.firstOrNull()?.graphIndex ?: newFirstIndex
      val oldLastIndex = oldVisibleNodes.lastOrNull()?.graphIndex ?: newLastIndex

      // Negative angleDifference is clockwise.
      val leftShift =
        if (oldFirstIndex > newFirstIndex && angleDifference < 0)
        // If the angle rotated left but the index decreased, we wrapped around.
        // Ex. if highest index is 10: 11 - 10 + 0 = 1. 11 - 10 + 1 = 2.
          graph.nodes.size - oldFirstIndex + newFirstIndex
        else if (oldFirstIndex < newFirstIndex && angleDifference > 0)
        // If the angle rotated right but the index increased, we wrapped around.
        // Ex. if highest index is 10: -(11 - 10 + 0) = -1
          -(graph.nodes.size - newFirstIndex + oldFirstIndex)
        else newFirstIndex - oldFirstIndex
      val rightShift =
        if (oldLastIndex > newLastIndex && angleDifference < 0)
        // Ex. 10 -> 0: 11 - 10 + 0
          graph.nodes.size - oldLastIndex + newLastIndex
        else if (oldLastIndex < newLastIndex && angleDifference > 0)
        // Ex. 0 -> 10: -(11 - 10 + 0)
          -(graph.nodes.size - newLastIndex + oldLastIndex)
        else newLastIndex - oldLastIndex

      if (leftShift != 0 || rightShift != 0) {
        Timber.d(
          "angleDifference=%s, oldVisibleNodes=%s, newVisibleNodes=%s", angleDifference,
          oldVisibleNodes.map { it.graphIndex },
          newVisibleNodes.map { it.graphIndex })
        adapter?.changeMaskValues(leftShift, rightShift)
      }
    }

    nodes = newNodes

    invalidate()
  }

  private fun getNodes() = graph.nodes.withIndex()
    .map {
      val angle = getAngle(it.value)
      AngledNode(
        it.index, angle, it.value, isNodeVisible(angle)
      )
    }
    .sortedBy { it.angle }
    .reversed()

  private fun isNodeVisible(angle: Float): Boolean {
    val lowerBound = -outerAngle + HALF_PI
    val upperBound = outerAngle + HALF_PI
    return if (lowerBound > 0) {
      angle in lowerBound..upperBound
    } else {
      (angle in PI + lowerBound..PI) || (angle in 0f..upperBound)
    }
  }

  private fun drawNodeWithLine(
    canvas: Canvas,
    node: Node,
    paint: Paint,
  ) {
    canvas.drawLine(
      graph.origin.x, graph.origin.y, node.x, node.y,
      paint
    )

    canvas.drawCircle(
      node.x, node.y, graph.geometricRadius, paint
    )
  }

  private fun drawTitle(
    title: String,
    canvas: Canvas,
    node: Node,
  ) {
    val staticLayout = StaticLayout.Builder.obtain(
      title, 0, title.length, nodeTextPaint, graph.geometricRadius.toInt() * 2
    )
      .setEllipsize(END)
      .setMaxLines(1)
      .build()
    canvas.withTranslation(node.x, node.y - staticLayout.height / 2.0f) {
      staticLayout.draw(this)
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    return when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        true
      }

      MotionEvent.ACTION_MOVE -> {
        if (event.historySize < 1) {
          return true
        }

        val oldAngle = getAngle(event.getHistoricalX(0), event.getHistoricalY(0))
        val newAngle = getAngle(event.x, event.y)
        val angleDifference = (newAngle - oldAngle)
        graph.rotation -= angleDifference
        updateState(-angleDifference)
        true
      }

      MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
        if (snapping) {
          graph.snapToNearest()
        }
        updateState()
        true
      }

      else -> super.onTouchEvent(event)
    }
  }

  private fun getAngle(node: Node): Float {
    val height = node.y - graph.origin.y
    val width = node.x - graph.origin.x
    return atan2(height, width)
  }

  private fun getAngle(
    x: Float,
    y: Float,
  ): Float {
    val height = y - graph.origin.y
    val width = x - graph.origin.x
    return atan2(height, width)
  }

  private fun drawLine(
    canvas: Canvas,
    color: Int,
    angle: Float,
    length: Float,
  ) {
    val x = length * sin(angle)
    val y = length * cos(angle)
    val originX = graph.origin.x
    val originY = graph.origin.y
    canvas.drawLine(
      originX, originY, originX + x, originY + y,
      Paint().apply {
        this.color = color
      }
    )
  }
}