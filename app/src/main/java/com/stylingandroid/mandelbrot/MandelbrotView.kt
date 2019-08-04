package com.stylingandroid.mandelbrot

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView

class MandelbrotView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultThemeAttr: Int = -1
) : AppCompatImageView(context, attrs, defaultThemeAttr) {

    var delegate: TouchHandler? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        delegate?.onTouchEvent(event)
        return true
    }
}
