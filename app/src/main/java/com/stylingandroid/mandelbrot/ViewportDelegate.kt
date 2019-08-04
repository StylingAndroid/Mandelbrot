package com.stylingandroid.mandelbrot

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import kotlin.math.max
import kotlin.math.min

class ViewportDelegate(
    context: Context,
    private val imageView: MandelbrotView,
    private val renderer: MandelbrotRenderer
) : ScaleGestureDetector.OnScaleGestureListener,
    GestureDetector.SimpleOnGestureListener(),
    TouchHandler {

    private var scaleDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)
    private var gestureDetector: GestureDetector = GestureDetector(context, this)

    private val viewPort = RectD()

    init {
        imageView.apply {
            delegate = this@ViewportDelegate
            addOnLayoutChangeListener { _, l, t, r, b, lOld, tOld, rOld, bOld ->
                if (r - l != rOld - lOld || b - t != bOld - tOld) {
                    viewPort.set(l.toDouble(), t.toDouble(), r.toDouble(), b.toDouble())
                    renderer.setSize(r - l, b - t)
                    renderImage()
                }
            }
        }
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean = true

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        scale(
            detector.focusX.toDouble(),
            detector.focusY.toDouble(),
            detector.scaleFactor.toDouble()
        )
        return true
    }

    private fun scale(focusX: Double, focusY: Double, factor: Double) {
        val newViewportWidth = viewPort.width / factor
        val newViewportHeight = viewPort.height / factor
        val xFraction = focusX / imageView.width.toDouble()
        val yFraction = focusY / imageView.height.toDouble()
        val newLeft =
            viewPort.left + viewPort.width * xFraction - newViewportWidth * xFraction
        val newTop =
            viewPort.top + viewPort.height * yFraction - newViewportHeight * yFraction
        updateViewport(newLeft, newTop, newViewportWidth, newViewportHeight)
        renderImage()
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) { /* NO-OP */
    }

    override fun onTouchEvent(event: MotionEvent) {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        scale(e.x.toDouble(), e.y.toDouble(), 2.0)
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
        updateViewport(0.0, 0.0, imageView.width.toDouble(), imageView.height.toDouble())
        renderImage()
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        updateViewport(
            viewPort.left + distanceX.toDouble() * viewPort.width / imageView.width.toDouble(),
            viewPort.top + distanceY.toDouble() * viewPort.height / imageView.height.toDouble(),
            viewPort.width,
            viewPort.height
        )
        renderImage()
        return true
    }

    private fun updateViewport(left: Double, top: Double, width: Double, height: Double) {
        val newLeft = min(max(left, 0.0), imageView.width.toDouble() - width)
        val newTop = min(max(top, 0.0), imageView.height.toDouble() - height)
        viewPort.set(newLeft, newTop, newLeft + width, newTop + height)
    }

    private fun renderImage() {
        renderer.render(
            viewPort.width / imageView.width.toDouble(),
            viewPort.left / imageView.width.toDouble(),
            viewPort.top / imageView.height.toDouble()
        )
    }
}
