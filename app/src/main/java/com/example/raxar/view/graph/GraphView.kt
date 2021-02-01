package com.example.raxar.view.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.raxar.data.NoteDto

class GraphView : View {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val graph: Graph

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

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            graph.update(width, height)

            for (node in graph) {
                canvas.drawLine(
                    graph.origin.xPos.toFloat(), graph.origin.yPos.toFloat(),
                    node.xPos.toFloat(), node.yPos.toFloat(),
                    paint
                )
            }
        }
    }

}