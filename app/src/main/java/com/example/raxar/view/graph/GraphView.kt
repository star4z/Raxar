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
import timber.log.Timber
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.pow

class GraphView : View {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val graph: Graph
    private val textBounds = Rect()

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

    private val nodeTextSize = 40f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        graph.update(width, height)

        for (iNode in graph.withIndex()) {
            drawNodeWithLine(canvas, iNode.value, iNode.index)
        }
    }

    private fun drawNodeWithLine(
        canvas: Canvas,
        node: Node,
        i: Int
    ) {
        paint.color = Color.RED
        canvas.drawLine(
            graph.origin.xPos.toFloat(), graph.origin.yPos.toFloat(),
            node.xPos.toFloat(), node.yPos.toFloat(),
            paint
        )

        canvas.drawCircle(
            node.xPos.toFloat(), node.yPos.toFloat(),
            graph.nodeRadius.toFloat(), paint
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
            node.xPos.toFloat(),
            node.yPos.toFloat() - textBounds.exactCenterY(),
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
                val angleDifference = (newAngle - oldAngle) % (PI / 2)
                graph.rotation += angleDifference
                invalidate()
                true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
                graph.snapToNearest()
                invalidate()
                true
            }

            else -> super.onTouchEvent(event)
        }
    }

    private fun getAngle(x: Float, y: Float): Double {
        val height = y - graph.origin.yPos
        val width = x - graph.origin.xPos
        return atan(width / height)
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