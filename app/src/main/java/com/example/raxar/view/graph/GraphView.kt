package com.example.raxar.view.graph

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.raxar.collection.CircularMaskedList
import timber.log.Timber
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow

class GraphView : View {

  private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val graph: Graph
  private val textBounds = Rect()
  private val nodeTextSize = 40f
  private val snapping = false
  var adapter: GraphViewAdapter<*>? = null
    set(value) {
      field = value
      value?.callback = { updateState() }
    }
  private var visibleNodes = listOf<Node>()

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
    paint.color = Color.RED
    paint.textAlign = Paint.Align.CENTER

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
    val index: Int,
    val angle: Double,
    val node: Node,
  )

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    for (node in visibleNodes.withIndex()) {
      drawNodeWithLine(canvas, node.value)
      adapter?.let {
        drawTitle(it.getItem(node.index), canvas, node.value)
      }
    }
  }

  private fun updateState() {
    val oldVisibleNodes = getOldVisibleNodes()
    Timber.d("oldVisibleNodes=%s", oldVisibleNodes.map { it.index })

    graph.update(width, height)

    val newVisibleNodes = getOldVisibleNodes()
    Timber.d("newVisibleNodes=%s", newVisibleNodes.map { it.index })

    val oldFirstIndex = oldVisibleNodes.firstOrNull()?.index ?: 0
    val oldLastIndex = oldVisibleNodes.lastOrNull()?.index ?: 0
    val newFirstIndex = newVisibleNodes.firstOrNull()?.index ?: 0
    val newLastIndex = newVisibleNodes.lastOrNull()?.index ?: 0
    val leftShift = newFirstIndex - oldFirstIndex
    val rightShift = newLastIndex - oldLastIndex

    adapter?.changeMaskValues(leftShift, rightShift)

    visibleNodes = newVisibleNodes.map { it.node }

    invalidate()
  }

  private fun getOldVisibleNodes() = graph.nodes.withIndex()
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
    paint.color = Color.RED
    canvas.drawLine(
      graph.origin.x.toFloat(), graph.origin.y.toFloat(), node.x.toFloat(), node.y.toFloat(), paint
    )

    canvas.drawCircle(
      node.x.toFloat(), node.y.toFloat(), graph.geometricRadius.toFloat(), paint
    )
  }

  private fun drawTitle(
    title: String,
    canvas: Canvas,
    node: Node,
  ) {
    paint.color = Color.WHITE
    paint.textSize = nodeTextSize
    paint.textAlign = Paint.Align.CENTER
    paint.getTextBounds(title, 0, title.length, textBounds)
    canvas.drawText(
      title, node.x.toFloat(), node.y.toFloat() - textBounds.exactCenterY(), paint
    )
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    Timber.d("action=${event.action}")
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

  private fun inCircle(
    x: Float,
    y: Float,
    circleCenterX: Double,
    circleCenterY: Double,
    circleRadius: Double,
  ): Boolean {
    val dx = (x - circleCenterX).pow(2.0)
    val dy = (y - circleCenterY).pow(2.0)
    return dx + dy < circleRadius.pow(2.0)
  }
}