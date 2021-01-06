package com.example.raxar

import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatTextView

class MyTextView(context: Context) : AppCompatTextView(context) {
    val children = mutableListOf<View>()
}