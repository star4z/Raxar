package com.example.raxar.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.raxar.R

abstract class SwipeCallback constructor(context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {


    private val iconRight = ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_24)!!
    private val iconLeft = ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_24)!!

    private val background = ColorDrawable()
    private val backgroundColorRight = ContextCompat.getColor(context, R.color.delete_background)
    private val backgroundColorLeft = ContextCompat.getColor(context, R.color.delete_background)


    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }


    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top

        lateinit var icon: Drawable

        when {
            dX < 0 -> {
                // Draw the red delete background
                background.color = backgroundColorRight

                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background.draw(c)

                icon = iconRight

                val inWidth = icon.intrinsicWidth
                val inHeight = icon.intrinsicHeight

                // Calculate position of delete icon
                val iconTop = itemView.top + (itemHeight - inHeight) / 2
                val iconMargin = (itemHeight - inHeight) / 2
                val iconLeft = itemView.right - iconMargin - inWidth
                val iconRight = itemView.right - iconMargin
                val iconBottom = iconTop + inHeight

                // Draw the delete icon
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                icon.draw(c)
            }
            dX > 0 -> {
                background.color = backgroundColorLeft

                background.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.left + dX.toInt(),
                    itemView.bottom
                )
                background.draw(c)

                icon = iconLeft

                val inWidth = icon.intrinsicWidth
                val inHeight = icon.intrinsicHeight

                // Calculate position of delete icon
                val iconTop = itemView.top + (itemHeight - inHeight) / 2
                val iconMargin = (itemHeight - inHeight) / 2
                val iconLeft = itemView.left + iconMargin
                val iconRight = itemView.left + iconMargin + inWidth
                val iconBottom = iconTop + inHeight

                // Draw the delete icon
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                icon.draw(c)
            }

        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}