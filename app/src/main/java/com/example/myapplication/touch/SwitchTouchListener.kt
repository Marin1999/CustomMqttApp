package com.example.myapplication.touch

import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Switch

class SwitchTouchListener(private val switch: Switch) : OnTouchListener {
    private var lastX = 0f
    private var lastY = 0f
    private var touchDownTime: Long = 0L
    private val longPressDuration = 150

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val x = event.rawX
        val y = event.rawY

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
                touchDownTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - lastX
                val deltaY = y - lastY

                if (System.currentTimeMillis() - touchDownTime > longPressDuration) {
                    // Long press detected; initiate dragging
                    val newX = v.x + deltaX
                    val newY = v.y + deltaY

                    v.x = newX
                    v.y = newY
                }

                lastX = x
                lastY = y
            }
            MotionEvent.ACTION_UP -> {
                // If the touch duration is short, it's considered a tap (toggle the switch)
                if (System.currentTimeMillis() - touchDownTime <= longPressDuration) {
                    switch.isChecked = !switch.isChecked
                }
            }
        }

        return true
    }
}