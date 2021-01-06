package com.example.raxar

import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatButton

class MyButton(context: Context): AppCompatButton(context) {
    val children = mutableListOf<View>()
}