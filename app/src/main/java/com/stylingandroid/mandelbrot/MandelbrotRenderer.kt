package com.stylingandroid.mandelbrot

import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.util.SparseArray
import android.widget.ImageView
import androidx.core.util.forEach
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MandelbrotRenderer(
    private val imageView: ImageView,
    override val coroutineContext: CoroutineContext,
    private val renderScript: RenderScript = RenderScript.create(imageView.context),
    private val script: ScriptC_Mandelbrot = ScriptC_Mandelbrot(renderScript)
) : CoroutineScope {

    private val allocationBitmaps = SparseArray<AllocationBitmap>()
    private var currentJob: Job? = null
    private var imageRatio = imageView.width.toDouble() / imageView.height.toDouble()

    fun setSize(width: Int, height: Int) {
        imageRatio = width.toDouble() / height.toDouble()
        var factor = LOW_FACTOR
        while (factor >= 1) {
            allocationBitmaps.put(
                factor,
                AllocationBitmap(
                    renderScript,
                    Bitmap.createBitmap(
                        imageView.width / factor,
                        imageView.height / factor,
                        Bitmap.Config.ARGB_8888
                    )
                )
            )
            factor /= 2
        }
    }

    private data class AllocationBitmap constructor(
        val allocation: Allocation,
        val bitmap: Bitmap
    ) {
        constructor(renderScript: RenderScript, bitmap: Bitmap) : this(
            Allocation.createFromBitmap(renderScript, bitmap),
            bitmap
        )
    }

    private data class RenderParameters(
        val zoom: Double,
        val offsetX: Double,
        val offsetY: Double,
        val factor: Int
    )

    private var queuedRender: RenderParameters? = null

    fun render(zoom: Double = 1.0, offsetX: Double = 0.0, offsetY: Double = 0.0) {
        if (currentJob?.isActive == true) {
            queuedRender = RenderParameters(zoom, offsetX, offsetY, LOW_FACTOR)
            currentJob?.cancel()
        }
        val renderParameters = RenderParameters(zoom, offsetX, offsetY, LOW_FACTOR)
        currentJob = launch {
            render(renderParameters)
        }.also {
            it.invokeOnCompletion { cause ->
                if (cause == null) {
                    passCompleted(renderParameters)
                }
            }
        }
    }

    private suspend fun render(renderParameters: RenderParameters) {
        allocationBitmaps[renderParameters.factor].also { allocationBitmap ->
            withContext(Dispatchers.Default) {
                val start = System.currentTimeMillis()
                script.invoke_mandelbrot(
                    script,
                    allocationBitmap.allocation,
                    ITERATIONS,
                    imageRatio,
                    renderParameters.zoom,
                    renderParameters.offsetX,
                    renderParameters.offsetY
                )
                allocationBitmap.allocation.copyTo(allocationBitmap.bitmap)
                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(allocationBitmap.bitmap)
                    val elapsed = System.currentTimeMillis() - start
                    println("Render ${renderParameters.factor} complete in ${elapsed}ms")
                }
            }
        }
    }

    private fun passCompleted(renderParameters: RenderParameters) {
        val newParams = renderParameters.copy(factor = renderParameters.factor / 2)
        if (renderParameters.factor > 1) {
            currentJob = launch {
                render(newParams)
            }.also {
                it.invokeOnCompletion { cause ->
                    if (cause == null) {
                        passCompleted(newParams)
                    }
                }
            }
        }
    }

    fun destroy() {
        allocationBitmaps.forEach { _, value ->
            value.allocation.destroy()
            value.bitmap.recycle()
        }
        script.destroy()
        renderScript.destroy()
    }
}

private const val LOW_FACTOR = 16
private const val ITERATIONS = 180
