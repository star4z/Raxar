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
import com.example.raxar.collection.CircularMaskedList
import timber.log.Timber
import kotlin.math.PI
import kotlin.math.atan2

class GraphView : View {

  private val nodeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val nodeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
  private val graph: Graph
  private val nodeTextSize = 40f
  private val snapping = false
  var adapter: GraphViewAdapter<*>? = null
    set(value) {
      field = value
      value?.callback = { updateState() }
    }
  private var visibleNodes = listOf<AngledNode>()

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
    nodeTextPaint.color = Color.WHITE
    nodeTextPaint.textSize = nodeTextSize
    nodeTextPaint.textAlign = Paint.Align.CENTER
    graph = Graph()
  }

  abstract class GraphViewAdapter<T> {
    private var values = CircularMaskedList<T>()
    var callback: (() -> Unit)? = null

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
    val angle: Double,
    val node: Node,
  )

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    for (node in visibleNodes.withIndex()) {
      drawNodeWithLine(canvas, node.value.node)
      adapter?.let {
        drawTitle(it.getItem(node.index), canvas, node.value.node)
      }
    }
  }

  private fun updateState() {
    val oldVisibleNodes = getVisibleNodes()

    graph.update(width, height)

    val newVisibleNodes = getVisibleNodes()

    if (oldVisibleNodes.isEmpty()) {
      Timber.d("newVisibleNodes=%s", newVisibleNodes.map { it.graphIndex })
      adapter?.setMaskSize(newVisibleNodes.size)
    } else {

      val newFirstIndex = newVisibleNodes.firstOrNull()?.graphIndex ?: 0
      val newLastIndex = newVisibleNodes.lastOrNull()?.graphIndex ?: 0
      val oldFirstIndex = oldVisibleNodes.firstOrNull()?.graphIndex ?: newFirstIndex
      val oldLastIndex = oldVisibleNodes.lastOrNull()?.graphIndex ?: newLastIndex
      val leftShift = newFirstIndex - oldFirstIndex
      val rightShift = newLastIndex - oldLastIndex

      if (leftShift != 0 || rightShift != 0) {
        Timber.d(
          "oldVisibleNodes=%s, newVisibleNodes=%s", oldVisibleNodes.map { it.graphIndex },
          newVisibleNodes.map { it.graphIndex })
        adapter?.changeMaskValues(leftShift, rightShift)
      }
    }

    visibleNodes = newVisibleNodes

    invalidate()
  }

  private fun getVisibleNodes() = graph.nodes.withIndex()
    .map { AngledNode(it.index, getAngle(it.value), it.value) }
    .filter {
      val outerAngle = PI / 2
      val b = it.angle in (-outerAngle + PI / 2)..(outerAngle + PI / 2)
      b
    }
    .sortedBy { it.angle }
    .reversed()

  private fun drawNodeWithLine(
    canvas: Canvas,
    node: Node,
  ) {
    canvas.drawLine(
      graph.origin.x.toFloat(), graph.origin.y.toFloat(), node.x.toFloat(), node.y.toFloat(),
      nodeBgPaint
    )

    canvas.drawCircle(
      node.x.toFloat(), node.y.toFloat(), graph.geometricRadius.toFloat(), nodeBgPaint
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
    canvas.save()
    canvas.translate(node.x.toFloat(), node.y.toFloat() - staticLayout.height / 2.0f)
    staticLayout.draw(canvas)
    canvas.restore()
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
        updateState()
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

  private fun getAngle(node: Node): Double {
    val height = node.y - graph.origin.y
    val width = node.x - graph.origin.x
    return atan2(height, width)
  }

  private fun getAngle(
    x: Float,
    y: Float,
  ): Double {
    val height = y - graph.origin.y
    val width = x - graph.origin.x
    return atan2(height, width)
  }
}