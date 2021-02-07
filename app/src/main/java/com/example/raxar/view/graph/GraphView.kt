package com.example.raxar.view.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.example.raxar.data.NoteDto
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

        graph = Graph((1..20).map { NoteDto(title = "Title $it") })
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

                    canvas.drawCircle(node.xPos.toFloat(), node.yPos.toFloat(), 100f, paint)

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

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                for (node in graph) {
//                    if (inCircle(event.x, event.y, node.xPos, node.yPos, graph.nodeRadius)) {
//                        node.state.moving = true
//                    }
//                }
//            }
//            else -> {
//                for (node in graph) {
//                    node.state.moving = false
//                }
//            }
//        }
//
//        return true
//    }

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