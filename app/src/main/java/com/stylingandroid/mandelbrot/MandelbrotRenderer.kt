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

    fun setSize(width: Int, height: Int): Bitmap =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
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

    fun render(): Bitmap? =
        bitmap?.run {
            val start = System.currentTimeMillis()
            script.invoke_mandelbrot(
                script,
                allocation,
                ITERATIONS
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
