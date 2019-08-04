package com.stylingandroid.mandelbrot

internal class RectD {
    var left: Double = 0.0
        private set
    var right: Double = 0.0
        private set
    var top: Double = 0.0
        private set
    var bottom: Double = 0.0
        private set

    constructor() : this(0.0, 0.0, 0.0, 0.0)

    constructor(l: Double, t: Double, r: Double, b: Double) {
        set(l, t, r, b)
    }

    fun set(l: Double, t: Double, r: Double, b: Double) {
        left = l
        top = t
        right = r
        bottom = b
    }

    val width: Double
        get() = right - left

    val height: Double
        get() = bottom - top
}
