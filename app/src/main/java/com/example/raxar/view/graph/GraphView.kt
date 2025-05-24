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
import kotlin.math.atan2
import kotlin.math.pow

class GraphView : View {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val graph: Graph
    private val textBounds = Rect()
    private val nodeTextSize = 40f
    private val snapping = false
    private var adapter: GraphViewAdapter<String>? = null

    constructor(context: Context?) : this(context, null, 0, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        paint.color = Color.RED
        paint.textAlign = Paint.Align.CENTER

        graph = Graph()
    }

    inner class GraphViewAdapter<T>(values: List<T>) {
        val values = CircularMaskedList(values, graph.size)

        public fun onValuesChanged(leftShift: Int, rightShift: Int) {
            val addedOrRemovedLeftBoundValues = values.shiftLeftMaskBound(leftShift)
            val addedOrRemovedRightBoundValues = values.shiftRightMaskBound(rightShift)
        }

        public fun bindValue(startIndex: Int, size: Int): List<String> {
            return values.getMaskedValues().stream().map { it.toString() }.toList()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val oldVisibleNodes = graph.nodes.withIndex().filter { getAngle(it.value) > 0 }.toList()

        graph.update(width, height)

        val newVisibleNodes = graph.nodes.withIndex().filter { getAngle(it.value) > 0 }.toList()

        val addedNodes = newVisibleNodes.filter { !oldVisibleNodes.contains(it) }
        val removedNodes = oldVisibleNodes.filter { !newVisibleNodes.contains(it) }

        for (node in graph.nodes.withIndex()) {
            val angle = getAngle(node.value.x.toFloat(), node.value.y.toFloat())
            if (angle > 0) {
                drawNodeWithLine(canvas, node.value, node.index)
            }
        }
    }

    private fun drawNodeWithLine(
        canvas: Canvas,
        node: Node,
        i: Int
    ) {
        paint.color = Color.RED
        canvas.drawLine(
            graph.origin.x.toFloat(), graph.origin.y.toFloat(),
            node.x.toFloat(), node.y.toFloat(),
            paint
        )

        canvas.drawCircle(
            node.x.toFloat(), node.y.toFloat(),
            graph.geometricRadius.toFloat(), paint
        )

        drawTitle(i.toString(), canvas, node)
    }

    private fun drawTitle(
        title: String,
        canvas: Canvas,
        node: Node
    ) {
        paint.color = Color.WHITE
        paint.textSize = nodeTextSize
        paint.textAlign = Paint.Align.CENTER
        paint.getTextBounds(title, 0, title.length, textBounds)
        canvas.drawText(
            title,
            node.x.toFloat(),
            node.y.toFloat() - textBounds.exactCenterY(),
            paint
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
                invalidate()
                true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
                if (snapping) {
                    graph.snapToNearest()
                }
                invalidate()
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

    private fun getAngle(x: Float, y: Float): Double {
        val height = y - graph.origin.y
        val width = x - graph.origin.x
        return atan2(height, width)
    }

    private fun inCircle(
        x: Float,
        y: Float,
        circleCenterX: Double,
        circleCenterY: Double,
        circleRadius: Double
    ): Boolean {
        val dx = (x - circleCenterX).pow(2.0)
        val dy = (y - circleCenterY).pow(2.0)
        return dx + dy < circleRadius.pow(2.0)
    }
}