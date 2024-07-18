package com.example.myapplication.views

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView

class AlarmView(context: Context) : AppCompatTextView(context){
    init {
        setBackgroundColor(Color.RED)
        setTextColor(Color.WHITE)
        text = "Alarm"
        gravity = Gravity.CENTER
        textSize = 16f
        setPadding(20, 20, 20, 20)
    }
}