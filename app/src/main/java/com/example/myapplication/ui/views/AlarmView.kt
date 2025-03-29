package com.example.myapplication.ui.views

import android.content.Context
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.example.myapplication.R

class AlarmView(context: Context) : AppCompatTextView(context) {
    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.softPink))
        setTextColor(ContextCompat.getColor(context, R.color.white))
        text = "Alarm"
        gravity = Gravity.CENTER
        textSize = 16f
        setPadding(20, 20, 20, 20)
    }
}