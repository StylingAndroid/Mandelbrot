package com.stylingandroid.mandelbrot

import android.content.Context
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MandelbrotLifecycleObserver(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    private val imageView: ImageView
) : CoroutineScope, LifecycleObserver {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val renderer: MandelbrotRenderer by lazy(LazyThreadSafetyMode.NONE) {
        MandelbrotRenderer(context)
    }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
        imageView.addOnLayoutChangeListener { _, l, t, r, b, lOld, tOld, rOld, bOld ->
            if (r - l != rOld - lOld || b - t != bOld - tOld) {
                renderer.setSize(r - l, b - t)
                generateImage()
            }
        }
    }

    private fun generateImage() = launch {
        imageView.setImageBitmap(withContext(Dispatchers.Default) {
            renderer.render()
        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @Suppress("Unused")
    fun onDestroy() {
        job.cancel()
        renderer.destroy()
    }
}
