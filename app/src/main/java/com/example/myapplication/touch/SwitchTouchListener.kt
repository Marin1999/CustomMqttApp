package com.example.myapplication.touch

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Switch
import com.example.myapplication.fragments.HomeFragment

class SwitchTouchListener(
    private val switch: Switch,
    private val trashBin: ImageView,
    private val switchContainer: FrameLayout,
    private val onSwitchRemoved: (Switch) -> Unit

    ) : OnTouchListener {

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
                    val newX = v.x + deltaX
                    val newY = v.y + deltaY

                    v.x = newX
                    v.y = newY
                }

                if (isViewOverlapping(switch, trashBin)) {
                    switchContainer.removeView(switch)
                    onSwitchRemoved(switch)
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

    private fun isViewOverlapping(view1: View, view2: View): Boolean{
        val rect1 = Rect()
        val rect2 = Rect()
        view1.getGlobalVisibleRect(rect1)
        view2.getGlobalVisibleRect(rect2)

        val centerX1 = rect1.centerX()
        val centerY1 = rect1.centerY()
        return rect2.contains(centerX1,centerY1)

    }
}