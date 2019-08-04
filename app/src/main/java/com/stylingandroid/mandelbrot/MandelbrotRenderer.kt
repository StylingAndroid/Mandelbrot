package com.stylingandroid.mandelbrot

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript

class MandelbrotRenderer(
    context: Context,
    private val renderScript: RenderScript = RenderScript.create(context),
    private val script: ScriptC_Mandelbrot = ScriptC_Mandelbrot(renderScript)
) {

    private var allocation: Allocation? = null
    private var bitmap: Bitmap? = null
    private var imageRatio: Double = 1.0

    fun setSize(width: Int, height: Int): Bitmap {
        imageRatio = width.toDouble() / height.toDouble()
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
            bitmap?.recycle()
            bitmap = it
            allocation?.destroy()
            allocation = Allocation.createFromBitmap(
                renderScript,
                bitmap,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT
            )
        }
    }

    fun render(zoom: Double = 1.0, offsetX: Double = 0.0, offsetY: Double = 0.0): Bitmap? =
        bitmap?.run {
            val start = System.currentTimeMillis()
            script.invoke_mandelbrot(
                script,
                allocation,
                ITERATIONS,
                imageRatio,
                zoom,
                offsetX,
                offsetY
            )
            allocation?.copyTo(this)
            println("Generation complete in ${System.currentTimeMillis() - start}ms")
            bitmap
        }

    fun destroy() {
        allocation?.destroy()
        script.destroy()
        renderScript.destroy()
    }
}

private const val ITERATIONS = 180
