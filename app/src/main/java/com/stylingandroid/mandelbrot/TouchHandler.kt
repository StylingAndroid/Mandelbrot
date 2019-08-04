package com.stylingandroid.mandelbrot

import android.view.MotionEvent

interface TouchHandler {
    fun onTouchEvent(event: MotionEvent)
}
