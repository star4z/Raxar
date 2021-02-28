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
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.pow

class GraphView : View {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val graph: Graph
    private val textBounds = Rect()

    private var lastX = 0f
    private var lastY = 0f
    private var lastAngle = 0.0

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

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            graph.update(width, height)

            for (node in graph) {
                if (node.state.visible) {
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

                    node.title?.let { title ->
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
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        Timber.d("action=${event.action}")
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                graph.rotating = true
                lastAngle = getAngle(event.x, event.y)
                lastX = event.x
                lastY = event.y
                true
            }
            MotionEvent.ACTION_MOVE -> {
                val angle = getAngle(event.x, event.y)
                var angleDifference = angle - lastAngle
                if (graph.rotating) {
                    if (abs(angleDifference) > PI / 2) {
                        angleDifference -= PI
                    }
                    graph.rotation += angleDifference
                }
                lastAngle = angle
                lastX = event.x
                lastY = event.y
                invalidate()
                true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
                graph.rotating = false
                lastAngle = getAngle(event.x, event.y)
                lastX = event.x
                lastY = event.y
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